package malgnsoft.db;

import java.util.Date;
import java.util.Random;
import java.sql.ResultSet;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.servlet.jsp.JspWriter;

import malgnsoft.db.RecordSet;
import malgnsoft.db.DB;
import malgnsoft.util.Malgn;
import malgnsoft.util.Config;

public class DataObject {

	public int limit = 1000;
	public String PK = "id";
	public String dbType = "oracle";
	public String fields = "*";
	public String table = "";
	public String join = "";
	public String jndi = Config.getJndi();
	public String sql = "";
	public String id = "-1";
	public int newId = 0;
	public int seq = 0;
	public Hashtable<String, Object> record = new Hashtable<String, Object>();
	public String errMsg = null;
	private JspWriter out = null;
	private boolean debug = false;
	public String useSeq = Config.get("useSeq");

	public DataObject() {
		
	}

	public DataObject(String table) {
		this.table = table;
	}

	public void setDebug(JspWriter out) {
		debug = true;
		this.out = out;
	}

	public void setFields(String f) {
		this.fields = f;
	}

	public void setTable(String tb) {
		this.table = tb;
	}

	public void setError(String msg) throws Exception {
		this.errMsg = msg;
		if(this.debug == true && this.out != null) {
			out.print("<hr>" + msg + "<hr>");
		}
	}

	public void addJoin(String tb, String type, String cond) {
		this.join += " " + type + " JOIN " + tb + " ON " + cond;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public void item(String name, Object obj) {
		if(obj == null) {
			record.put(name, "");
	    } else if(obj instanceof String) {
			record.put(name, Malgn.replace((String)obj, "`", "'"));
		} else if(obj instanceof Date) {
			record.put(name, new java.sql.Timestamp(((Date)obj).getTime()));
		} else {
			record.put(name, obj);
		}
	}

	public void item(String name, int obj) {
		record.put(name, new Integer(obj));
	}

	public void item(String name, long obj) {
		record.put(name, new Long(obj));
	}

	public void item(String name, double obj) {
		record.put(name, new Double(obj));
	}

	public void item(Hashtable obj) {
		this.item(obj, "");
	}

	public void item(Hashtable obj, String exceptions) {
		Enumeration e = obj.keys();
		String[] arr = exceptions.split(",");
		while(e.hasMoreElements()) {
			String key = ((String)e.nextElement()).trim();
			if(Malgn.inArray(key, arr)) continue;
			this.item(key, null != obj.get(key) ? obj.get(key) : "");
		}
	}

	public void clear() {
		record.clear();
	}

	public RecordSet get(int i) {
		this.id = "" + i;
		return find(this.PK + " = " + i);
	}

	public RecordSet get(String id) {
		this.id = id;
		return find(this.PK + " = '" + id + "'");
	}

	public int getOneInt(String query) {
		String str = getOne(query);
		if(str.matches("^[0-9]+$")) {
			return Integer.parseInt(str);
		}
		return 0;
	}

	public String getOne(String query) {
		DataSet info = this.selectLimit(query, 1);
		if(info.next()) {
			Enumeration e = info.getRow().keys();
			while(e.hasMoreElements()) {
				String key = (String)e.nextElement();
				if(key.length() > 0 && "_".equals(key.substring(0, 1))) continue;
				return info.getString(key);
			}
		}
		return "";
	}

	public RecordSet find(String where) {
		return find(where, this.fields, null);
	}

	public RecordSet find(String where, String fields) {
		return find(where, fields, null);
	}

	public RecordSet find(String where, String fields, int limit) {
		return find(where, fields, null, limit);
	}

	public RecordSet find(String where, String fields, String sort) {
		String sql = "SELECT " + fields + " FROM " + this.table + this.join;
		if(where != null && !"".equals(where)) sql = sql + " WHERE " + where;
		if(sort != null && !"".equals(sort)) sql = sql + " ORDER BY " + sort;
		return query(sql);
	}
	
	public RecordSet find(String where, String fields, String sort, int limit) {
		String sql = "SELECT " + fields + " FROM " + this.table + this.join;
		if(where != null && !"".equals(where)) sql = sql + " WHERE " + where;
		if(sort != null && !"".equals(sort)) sql = sql + " ORDER BY " + sort;
		return selectLimit(sql, limit);
	}

    public String getDBType() {
        DB db = null;
        try {
            db = new DB(this.jndi);
            return db.getDBType();
        } catch(Exception e) {
            this.errMsg = db.errMsg;
			Malgn.errorLog("{DataObject.getDBType} " + e.getMessage());
        }       
        return this.dbType;
    }

	public RecordSet selectLimit(String sql, int limit) {
		RecordSet rs = null;
		DB db = null;
		try {
			long stime = System.currentTimeMillis();

			db = new DB(this.jndi);
			if(debug == true) db.setDebug(out);
			rs = db.selectLimit(sql, limit);
			if(rs == null) this.errMsg = db.errMsg;
			db.close();

			long etime = System.currentTimeMillis();
			if(debug == true) {
				out.print("<hr>Execution Time : " + (etime - stime) + " (1/1000 sec)<hr>");
			}
		} catch(Exception e) {
			this.errMsg = db.errMsg;
			Malgn.errorLog("{DataObject.selectLimit} " + e.getMessage());
		} finally {
			if(db != null) db.close();
		}
		return rs;
	}
	public RecordSet selectRandom(String sql, int limit) throws Exception {
		RecordSet rs = null;
		DB db = null;
		try {
			long stime = System.currentTimeMillis();

			db = new DB(this.jndi);
			if(debug == true) db.setDebug(out);
			rs = db.selectRandom(sql, limit);
			if(rs == null) this.errMsg = db.errMsg;
			db.close();

			long etime = System.currentTimeMillis();
			if(debug == true) {
				out.print("<hr>Execution Time : " + (etime - stime) + " (1/1000 sec)<hr>");
			}
		} catch(Exception e) {
			this.errMsg = db.errMsg;
			Malgn.errorLog("{DataObject.selectRandom} " + e.getMessage());
		} finally {
			if(db != null) db.close();
		}
		return rs;
	}
	
	public int findCount(String where) {
		RecordSet rs = find(where, " COUNT(*) AS count ");
		if(rs == null || !rs.next()) {
			return 0;
		} else {
			return rs.getInt("count");
		}
	}

	public boolean insert() {
		boolean seqFlag = false;
		if("Y".equals(useSeq) && PK.equals("id") && !record.containsKey("id")) {
			item("id", getSequence());
			seqFlag = true;
		}

		int max = record.size();
		Enumeration keys = record.keys();
		StringBuffer sb = new StringBuffer();

		sb.append("INSERT INTO " + this.table + " (");
		for(int k=0; keys.hasMoreElements(); k++) {
			String key = (String)keys.nextElement();
			sb.append(key);
			if(k < (max - 1)) sb.append(",");
		}

		sb.append(") VALUES (");
		for(int i=0; i<max; i++) {
			sb.append("?");
			if(i < (max - 1)) sb.append(",");
		}
		sb.append(")");
		String sql = sb.toString();

		int ret = execute(sql, record);
		if(seqFlag) record.remove("id");
		return ret > 0 ? true : false;
	}

	public boolean update() {
		return update(this.PK + " = '" + id + "'");
	}

	public boolean update(String where) {
		int max = record.size();
		Enumeration keys = record.keys();
		StringBuffer sb = new StringBuffer();

		sb.append("UPDATE " + this.table + " SET ");
		for(int k=0; keys.hasMoreElements(); k++) {
			String key = (String)keys.nextElement();
			sb.append(key + "=?");
			if(k < (max - 1)) sb.append(",");
		}
		sb.append(" WHERE " + where);
		String sql = sb.toString();

		int ret = execute(sql, record);
		return ret > -1 ? true : false;
	}

	public boolean delete() {
		return delete(this.PK + " = '" + this.id + "'");
	}

	public boolean delete(int id) {
		return delete(this.PK + " = " + id);
	}

	public boolean delete(String where) {
		String sql = "DELETE FROM " + this.table + " WHERE " + where;

		int ret = execute(sql);
		return ret > -1 ? true : false;
	}

	public int getInsertId() {
		if(seq > 0) return seq;
		if(newId > 0) return newId;
		RecordSet rs = query("SELECT MAX("+ this.PK +") AS id FROM "+ table);
		if(rs != null && rs.next()) return rs.getInt("id");
		else return 0;
	}

	public int getSequence() {
		RecordSet rs = query("SELECT seq FROM tb_sequence WHERE id = '" + table + "'");
		if(rs != null && rs.next()) {
			execute("UPDATE tb_sequence SET seq = seq + 1 WHERE id = '" + table + "'");
			seq = rs.getInt("seq") + 1;
		} else {
			execute("INSERT INTO tb_sequence (id, seq) VALUES ('" + table + "', 1)");
			seq = 1;
		}
		return seq;
	}

	public String getSequence(String prefix, int length) {
		return prefix + Malgn.strrpad("" + getSequence(), length, "0");
	}


	public RecordSet getListData(String where, String orderby, int pageSize, int cPage) {
		orderby = orderby.toLowerCase();
		if(pageSize < 1) pageSize = 10;
		if(cPage < 1) cPage = 1;
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT * FROM (");
		sb.append(" SELECT TOP " + pageSize + " * FROM (");
		sb.append(" SELECT TOP " + (pageSize * cPage) + " " + this.fields);
		sb.append(" FROM " + this.table); 
		sb.append(" WHERE " + where); 
		sb.append(" ORDER BY " + orderby + ") AS A"); 
		sb.append(" ORDER BY " + orderby.replaceAll("asc", "DESC").replaceAll("desc", "ASC") + ") AS B");
		sb.append(" ORDER BY " + orderby);
		sql = sb.toString();

		return query(sql);
	}

	public RecordSet query(String sql) {
		RecordSet rs = null;
		DB db = null;
		try {
			long stime = System.currentTimeMillis();

			db = new DB(this.jndi);
			if(debug == true) db.setDebug(out);
			rs = db.query(sql);
			if(rs == null) this.errMsg = db.errMsg;
			db.close();

			long etime = System.currentTimeMillis();
			if(debug == true) {
				out.print("<hr>Execution Time : " + (etime - stime) + " (1/1000 sec)<hr>");
			}
		} catch(Exception e) {
			this.errMsg = db.errMsg;
			Malgn.errorLog("{DataObject.query} " + e.getMessage());
		} finally {
			if(db != null) db.close();
		}
		return rs;
	}

	public RecordSet query(String sql, int limit) {
		return this.selectLimit(sql, limit);
	}

	public int execute(String sql) {
		int ret = -1;
		DB db = null;
		try {
			long stime = System.currentTimeMillis();

			db = new DB(this.jndi);
			if(debug == true) db.setDebug(out);
			ret = db.execute(sql);
			newId = db.getInsertId();
			db.close();

			long etime = System.currentTimeMillis();
			if(debug == true) {
				out.print("<hr>Execution Time : " + (etime - stime) + " (1/1000 sec)<hr>");
			}
		} catch(Exception e) {
			this.errMsg = db.errMsg;
			Malgn.errorLog("{DataObject.execute} " + e.getMessage());
		} finally {
			if(db != null) db.close();
		}
		return ret;
	}
	
	public int execute(String sql, Hashtable record) {
		int ret = 0;
		DB db = null;
		try {
			long stime = System.currentTimeMillis();

			db = new DB(this.jndi);
			if(debug == true) db.setDebug(out);
			ret = db.execute(sql, record);
			newId = db.getInsertId();
			if(ret == -1) this.errMsg = db.errMsg;
			db.close();

			long etime = System.currentTimeMillis();
			if(debug == true) {
				out.print("<hr>Execution Time : " + (etime - stime) + " (1/1000 sec)<hr>");
			}
		} catch(Exception e) {
			this.errMsg = db.errMsg;
			Malgn.errorLog("{DataObject.execute} " + e.getMessage());
		} finally {
			if(db != null) db.close();
		}		
		return ret;
	}

	public String getErrMsg() {
		return this.errMsg;
	}

	public long getNextId() {
		return System.currentTimeMillis() * 1000 + (new Random()).nextInt(999);		
	}

	public String getNextId(String prefix) {
		return prefix + getNextId();
	}

/*
	public String addWhere(String cond) {
		if(this.where == null) {
			this.where = cond;
		} else {
			this.where = this.where + " AND " + cond;
		}
		return cond;
	}

	public String addSearch(String field, String keyword) {
		return addSearch(field, keyword, "=", 1);
	}
	
	public String addSearch(String field, String keyword, String oper) {
		int type = 1;
		if("LIKE".equals(oper.toUpperCase())) type = 2;
		return addSearch(field, keyword, oper, type);
	}
	
	public String addSearch(String field, String keyword, String oper, int type) {
		if(keyword != null && !"".equals(keyword)) {
			if(type == 1) keyword = "'" + keyword + "'";
			else if(type == 2) keyword = "'%" + keyword + "%'";
			return addWhere(field + " " + oper + " " + keyword);
		}
		return "";
	}
*/

}