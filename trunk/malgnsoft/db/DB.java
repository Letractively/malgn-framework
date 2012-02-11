package malgnsoft.db;

import java.io.File;
import java.util.Hashtable;
import java.util.Enumeration;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import org.apache.commons.dbcp.BasicDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.servlet.jsp.JspWriter;

import malgnsoft.db.DataSet;
import malgnsoft.db.RecordSet;
import malgnsoft.util.Malgn;
import malgnsoft.util.Config;
import malgnsoft.util.SimpleParser;

public class DB {

	private static final int DEFAULT_TIMEOUT = 1000;
	private static Hashtable<String, DataSource> dsTable = new Hashtable<String, DataSource>();
	private static Hashtable<String, String> dbTypes = new Hashtable<String, String>();

	private Connection _conn = null;
	private Statement _stmt = null;
	private PreparedStatement _pstmt = null;
	private int newId = 0;

	private JspWriter out = null;
	private boolean debug = false;

	private String jndi = Config.getJndi();
	private String was = Config.getWas();

	public String errMsg = null;
	public String query = null;

	public DB() {

	}

	public DB(String jndi) {
		this.jndi = jndi;
	}

	public void setDebug(JspWriter out) {
		this.debug = true;
		this.out = out;
	}

	public void setError(String msg) {
		this.errMsg = msg;
		if(debug == true && out != null) {
			try {
				out.println("<script>try { parent.document.getElementById('sysfrm').width = '100%'; parent.document.getElementById('sysfrm').height = 700; } catch(e) {}</script><hr>" + msg + "<hr>");
			} catch(Exception e) {}
		}
	}

	public String getQuery() {
		return this.query;
	}

	public String getError() {
		return this.errMsg;
	}

	public static DataSource getDataSource(String jndi) {
		return dsTable.get(jndi);
	}

	public Connection getConnection() throws Exception {
		if(dsTable == null) {
			dsTable = new Hashtable<String, DataSource>();
		}

		Connection conn = null;
		DataSource ds = dsTable.get(jndi);

		if(ds == null) {
			String path = Config.getDocRoot() + "/WEB-INF/config.xml";
			if((new File(path)).exists()) {
				SimpleParser sp = new SimpleParser(path);
				if(debug == true) sp.setDebug(out);
				DataSet rs = sp.getDataSet("//config/database");
				while(rs.next()) {
					if(jndi.equals(rs.getString("jndi-name"))) {
						BasicDataSource bds = null;
						try {
							bds = new BasicDataSource();            
							bds.setDriverClassName(rs.getString("driver"));
							bds.setUrl(rs.getString("url"));
							bds.setUsername(rs.getString("user"));
							bds.setPassword(rs.getString("password"));
							bds.setMaxActive(rs.getInt("max-active"));
							//bds.setInitialSize(rs.getInt("max-idle"));
							bds.setMinIdle(rs.getInt("min-idle"));
							bds.setMaxWait(rs.getInt("max-wait-time"));
							bds.setPoolPreparedStatements(true);

							conn = bds.getConnection();
							dsTable.put(jndi, (DataSource)bds);

							return conn;
						} catch(Exception e) {
							if(bds != null) bds.close();
							setError(e.getMessage());
							Malgn.errorLog("{DB.getConnection} " + e.getMessage(), e);
						}
						break;
					}
				}
			}

			Context ctx = null;
			try {
				ctx = new InitialContext();
				if("resin".equals(was) || "tomcat".equals(was)) {
					ds = (DataSource)ctx.lookup("java:comp/env/" + jndi);
				} else {
					ds = (DataSource)ctx.lookup(jndi);
				}
				conn = ds.getConnection();
				dsTable.put(jndi, ds);

				return conn;
			} catch(Exception e) {
				setError(e.getMessage());
				Malgn.errorLog("{DB.getConnection} " + e.getMessage(), e);
			} finally {
				if(ctx != null) try { ctx.close(); } catch(Exception e) {}
			}
		} else {
			try {
				conn = ds.getConnection();
			} catch(Exception e) {
				setError(e.getMessage());
				Malgn.errorLog("{DB.getConnection} " + e.getMessage(), e);
			}
		}

		return conn;
	}

	public String getDBType() throws Exception {
		String dbType = dbTypes.get(this.jndi);
		if(dbType == null) {
			Connection conn = this.getConnection();
			try {
				String connURL = conn.getMetaData().getURL();
				if(connURL.indexOf("jdbc:oracle") != -1) dbType = "oracle";
				else if(connURL.indexOf("jdbc:sqlserver") != -1) dbType = "mssql";
				else if(connURL.indexOf("jdbc:mysql") != -1) dbType = "mysql";
				else if(connURL.indexOf("jdbc:db2") != -1) dbType = "db2";
				dbTypes.put(this.jndi, dbType);
			} catch(Exception e) {
				setError(e.getMessage());
				Malgn.errorLog("{DB.getDBType} " + e.getMessage(), e);
			} finally {
				if(conn != null) try { conn.close(); } catch(Exception e) {}
			}
		}
		return dbType;
	}

	public void close() {
		if(_stmt != null) try { _stmt.close(); } catch(Exception e) {} finally { _stmt = null; }
		if(_pstmt != null) try { _pstmt.close(); } catch(Exception e) {} finally { _pstmt = null; }
		if(_conn != null) try { _conn.close(); } catch(Exception e) {} finally { _conn = null; }
	}

	public void begin() throws Exception {
		if(_conn == null) _conn = this.getConnection();
		_conn.setAutoCommit(false);
	}

	public void commit() throws SQLException {
		if(_conn != null) {
			if(this.errMsg == null) _conn.commit();
			else _conn.rollback();
			_conn.setAutoCommit(false);
			this.close();
		}
	}

	public void rollback() throws SQLException {
		if(_conn != null) _conn.rollback();
	}

	public RecordSet selectLimit(String sql, int limit) throws Exception {
		String dbType = getDBType();
		if("oracle".equals(dbType)) {
			sql = "SELECT * FROM (" + sql + ") WHERE rownum  <= " + limit;
		} else if("mssql".equals(dbType)) {
			sql = sql.replaceAll("(?i)^(SELECT)", "SELECT TOP(" + limit + ")");
		} else if("db2".equals(dbType)) {
			sql += " FETCH FIRST " + limit + " ROWS ONLY";
		} else {
			sql += " LIMIT " + limit;
		}
		return query(sql);
	}
	public RecordSet selectRandom(String sql, int limit) throws Exception {
		String dbType = getDBType();
		if("oracle".equals(dbType)) {
			sql = "SELECT * FROM (" + sql + " ORDER BY dbms_random.value) WHERE rownum  <= " + limit;
		} else if("mssql".equals(dbType)) {
			sql = sql.replaceAll("(?i)^(SELECT)", "SELECT TOP(" + limit + ")") + " ORDER BY NEWID()";
		} else if("db2".equals(dbType)) {
			sql = sql.replaceAll("(?i)^(SELECT)", "SELECT RAND() as IDXX, ") + " ORDER BY IDXX FETCH FIRST " + limit + " ROWS ONLY";
		} else {
			sql += " ORDER BY RAND() LIMIT " + limit; 
		}
		return query(sql);
	}

	public RecordSet query(String query) throws Exception {
		Connection conn = this.getConnection();
		if(conn == null) return new RecordSet(null);

		this.query = query;
		ResultSet rs = null;
		Statement stmt = null;
		RecordSet records = null;

		try {
			setError(query);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(DEFAULT_TIMEOUT);
			rs = stmt.executeQuery(query);
			records = new RecordSet(rs);
		} catch(Exception e) {
			setError(e.getMessage());
			Malgn.errorLog("{DB.query} " + query + " => " + e.getMessage(), e);
		} finally {
			if(rs != null) try { rs.close(); } catch(Exception e) {}
			if(stmt != null) try { stmt.close(); } catch(Exception e) {}
			if(conn != null) try { conn.close(); } catch(Exception e) {}
		}

		if(records == null) records = new RecordSet(null);
		return records;
	}

	public int execute(String query) throws Exception {
		Connection conn = this.getConnection();
		if(conn == null) return -1;

		this.query = query;
		Statement stmt = null;
		int ret = -1;
		try {
			setError(query);
			stmt = conn.createStatement();
			stmt.setQueryTimeout(DEFAULT_TIMEOUT);
			ret = stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			if(ret == 1 && "INSERT".equals(query.trim().substring(0, 6).toUpperCase()) && !"oracle".equals(getDBType())) {
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs != null && rs.next()) {
					try { newId = rs.getInt(1); } catch(Exception e) {} finally { rs.close(); }
				}
			}
		} catch(Exception e) {
			setError(e.getMessage());
			Malgn.errorLog("{DB.execute} " + query + " => " + e.getMessage(), e);
		} finally {
			if(stmt != null) try { stmt.close(); } catch(Exception e) {}
			if(conn != null) try { conn.close(); } catch(Exception e) {}
		}

		return ret;
	}

	public int execute(String query, Hashtable record) throws Exception {
		Connection conn = this.getConnection();
		if(conn == null) return -1;

		this.query = query;
		PreparedStatement pstmt = null;
		int ret = -1;
		try {
			setError(query);
			pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			pstmt.setQueryTimeout(DEFAULT_TIMEOUT);
			if(record != null) {
				setError(record.toString());
				Enumeration elements = record.elements();
				for(int k=1; elements.hasMoreElements(); k++) {
					pstmt.setObject(k, elements.nextElement());
				}
			}
			ret = pstmt.executeUpdate();
			if(ret == 1 && "INSERT".equals(query.trim().substring(0, 6).toUpperCase()) && !"oracle".equals(getDBType())) {
				ResultSet rs = pstmt.getGeneratedKeys();
				if (rs != null && rs.next()) {
					try { newId = rs.getInt(1); } catch(Exception e) {} finally { rs.close(); }
				}
			}
		} catch(Exception e) {
			setError(e.getMessage());
			Malgn.errorLog("{DB.execute} " + query + " => " + e.getMessage() + "\n" + record.toString(), e);
		} finally {
			if(pstmt != null) try { pstmt.close(); } catch(Exception e) {}
			if(conn != null) try { conn.close(); } catch(Exception e) {}
		}
		return ret;
	}

	public void setCommand(String cmd) throws Exception {
		if(_conn == null) _conn = this.getConnection();

		this.query = cmd;
		try {
			setError(this.query);
			_pstmt = _conn.prepareStatement(this.query);
			_pstmt.setQueryTimeout(DEFAULT_TIMEOUT);
		} catch(Exception e) {
			setError(e.getMessage());
			Malgn.errorLog("{DB.setCommand} " + cmd + " => " + e.getMessage(), e);
		}
	}

	public void setParam(int i, String param) throws Exception {
		_pstmt.setString(i, param); 
	}

	public void setParam(int i, int param) throws Exception {
		_pstmt.setInt(i, param); 
	}

	public void setParam(int i, double param) throws Exception {
		_pstmt.setDouble(i, param); 
	}

	public void setParam(int i, long param) throws Exception {
		_pstmt.setLong(i, param); 
	}

	public RecordSet query() throws Exception {
		ResultSet rs = null;
		RecordSet records = null;
		try {
			rs = _pstmt.executeQuery();
			records = new RecordSet(rs);
		} catch(Exception e) {
			setError(e.getMessage());
			Malgn.errorLog("{DB.query} " + e.getMessage(), e);
		} finally {
			try { rs.close(); } catch(Exception e) {}
			try { _pstmt.close(); _pstmt = null; } catch(Exception e) {}
			try { _conn.close(); _conn = null; } catch(Exception e) {}
		}
		if(records == null) records = new RecordSet(null);
		return records;		
	}

	public int execute() throws Exception {
		int ret = -1;
		try {
			ret = _pstmt.executeUpdate();
		} catch(Exception e) {
			setError(e.getMessage());
			Malgn.errorLog("{DB.execute} " + e.getMessage(), e);
		} finally {
			try { _pstmt.close(); _pstmt = null; } catch(Exception e) {}
			try { _conn.close(); _conn = null; } catch(Exception e) {}
		}
		return ret;
	}

	public ResultSet executeQuery(String query) throws Exception {
		return executeQuery(query, DEFAULT_TIMEOUT);
	}

	public ResultSet executeQuery(String query, int timeout) throws Exception {
		if(_conn == null) _conn = this.getConnection();

		this.query = query;
		ResultSet rs = null;
		try {
			setError(query);
			if(_stmt == null) {
				_stmt = _conn.createStatement();
				_stmt.setQueryTimeout(timeout);
			}
			rs = _stmt.executeQuery(query);
		} catch(Exception e) {
			setError(e.getMessage());
			Malgn.errorLog("{DB.executeQuery} " + query + " => " + e.getMessage(), e);
		}

		return rs;
	}

	public int executeUpdate(String query) throws Exception {
		return executeUpdate(query, DEFAULT_TIMEOUT);
	}

	public int executeUpdate(String query, int timeout) throws Exception {
		if(_conn == null) _conn = this.getConnection();

		this.query = query;
		int ret = -1;
		try {
			setError(query);
			if(_stmt == null) {
				_stmt = _conn.createStatement();
				_stmt.setQueryTimeout(timeout);
			}
			ret = _stmt.executeUpdate(query);
		} catch(Exception e) {
			setError(e.getMessage());
			Malgn.errorLog("{DB.executeUpdate} " + query + " => " + e.getMessage(), e);
		}

		return ret;
	}

	public int getInsertId() {
		return newId;
	}
}
