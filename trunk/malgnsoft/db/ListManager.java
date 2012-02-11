package malgnsoft.db;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import malgnsoft.db.DataObject;
import malgnsoft.db.RecordSet;
import malgnsoft.db.DataSet;
import malgnsoft.util.Malgn;
import malgnsoft.util.Config;
import malgnsoft.util.Pager;

public class ListManager {

	public String jndi = Config.getJndi();
	public String table = null;
	public String fields = "*";
	public String where = null;
	public String orderby = null;
	public String groupby = null;
	public boolean debug = false;
	public int totalNum = 0;
	public int listNum = 10;
	public int pageNum = 1;
	public int listMode = 1;
	public int naviNum = 10;
	public String errMsg = null;

	private String listQuery = null;
	private Hashtable<String, String[]> items = new Hashtable<String, String[]>();
	private JspWriter out = null;
	private HttpServletRequest request = null;
	private String pageVar = "page";
	private String dbType = "mysql";

	public String pagebar = "";

	public ListManager() {
	}

	public ListManager(String jndi) {
		this.jndi = jndi;
	}

	public ListManager(String jndi, HttpServletRequest request) {
		this.jndi = jndi;
		setRequest(request);
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;

		String page = request.getParameter(pageVar);
		if(page == null || "".equals(page)) pageNum = 1;
		else if(page.matches("^[0-9]+$")) pageNum = Integer.parseInt(page);
		else pageNum = 1;
	}
	
	public void setDebug(JspWriter out) {
		this.debug = true;
		this.out = out;
	}
	
	public void setPage(int pageNum) {
		if(pageNum < 1) pageNum = 1;
		this.pageNum = pageNum;
	}

	public void setListNum(int size) {
		this.listNum = size;
	}
	
	public void setNaviNum(int size) {
		this.naviNum = size;
	}

	public void setPagebar(String name) {
		this.pagebar = name;
	}

	public void setTable(String table) {
		this.table = table;
	}
	
	public void setFields(String fields) {
		this.fields = fields;
	}	

	public void setOrderBy(String orderby) {
		this.orderby = orderby.toLowerCase();
	}	
	
	public void setGroupBy(String groupby) {
		this.groupby = groupby;
	}	
	
	public void setWhere(String where) {
		this.where = where;
	}
	
	public void addWhere(String where) {
		if(where != null && !"".equals(where)) {
			if(this.where == null) {
				this.where = where;
			} else {
				this.where = this.where + " AND " + where;
			}
		}
	}

	public void addSearch(String field, String keyword) {
		addSearch(field, keyword, "=", 1);
	}
	
	public void addSearch(String field, String keyword, String oper) {
		int type = 1;
		if("LIKE".equals(oper.toUpperCase())) type = 2;
		addSearch(field, keyword, oper, type);
	}
	
	public void addSearch(String field, String keyword, String oper, int type) {
		if(keyword != null && !"".equals(keyword)) {
			if(type == 1) keyword = "'" + keyword + "'";
			else if(type == 2) keyword = "'%" + keyword + "%'";
			addWhere(field + " " + oper + " " + keyword);
		}
	}

	public void addItem(String name, String func, String field, int param) {
		String[] arr = {func, field, "" + param};
		items.put(name, arr);
	}

	public void addItem(String name, String func, String field, String param) {
		String[] arr = {func, field, param};
		items.put(name, arr);
	}

	public void setListMode(int mode) {
		this.listMode = mode;
	}

	public int getTotalNum() throws Exception {
		if(this.totalNum > 0) return this.totalNum;
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT count(*) AS count FROM " + table);
		if(where != null) sb.append(" WHERE " + where); 
		String sql = sb.toString();

		//Temporary Add
		if(groupby != null) {
			sb.append(" GROUP BY " + groupby);
			sql = sb.toString();
			sql = "SELECT COUNT(*) count FROM (" + sql + ") ZA";
		}

		DB db = null;
		RecordSet rs = null;
		try {
			long stime = System.currentTimeMillis();

			db = new DB(this.jndi);
			if(debug == true) db.setDebug(out);
			rs = db.query(sql);
			if(rs != null && rs.next()) {
				this.totalNum = rs.getInt("count");	
			} else {
				this.errMsg = db.errMsg;
			}
			db.close();

			long etime = System.currentTimeMillis();
			if(debug == true) {
				out.print("<hr>Execution Time : " + (etime - stime) + " (1/1000 sec)<hr>");
			}
		} catch(Exception e) {
			this.errMsg = db.errMsg;
			Malgn.errorLog("{ListManager.getTotalNum} " + e.getMessage());
		} finally {
			if(db != null) db.close();
		}
		return this.totalNum;
	}
	public String getTotalString() {
		return "<span style=\"font-family:arial, dotum;font-weight:normal;\">Total : <font color=\"blue\">" + Malgn.numberFormat(this.totalNum) + "</font> 건</span>";
	}

	public void setListQuery(String query) {
		this.listQuery = query;
	}

	public String getListQuery() {
		if(this.listQuery != null) return this.listQuery;

		if(listNum < 1) listNum = 10;
		if(pageNum < 1) pageNum = 1;
	

		StringBuffer sb = new StringBuffer();
		
		if("mssql".equals(dbType) || "db2".equals(dbType)) {
			sb.append("SELECT ZA.* FROM (");
			sb.append(" SELECT ROW_NUMBER() OVER(ORDER BY " + orderby + ") AS RowNum, " + this.fields);
			sb.append(" FROM " + this.table); 
			if(where != null) sb.append(" WHERE " + where);
			if(groupby != null) sb.append(" GROUP BY " + groupby);
			sb.append(") ZA WHERE ZA.RowNum BETWEEN ("+ pageNum +" - 1) * "+ listNum +" + 1 AND " + (pageNum * listNum));
		} else {

			int startNum = (pageNum - 1) * listNum;

			if("oracle".equals(this.dbType)) {
				sb.append("SELECT ZB.* FROM (SELECT  rownum as dbo_rownum, ZA.* FROM (");
			}
			sb.append("SELECT "+ fields +" FROM " + table);
			if(where != null) sb.append(" WHERE " + where); 
			if(groupby != null) sb.append(" GROUP BY " + groupby);
			if(orderby != null) sb.append(" ORDER BY " + orderby); 

			if("oracle".equals(this.dbType)) {
				sb.append(") ZA WHERE rownum  <= " + (startNum + listNum) + ") ZB WHERE dbo_rownum > "  + startNum);
			} else {
				sb.append(" LIMIT " + startNum + ", " + listNum);
			}
		}
		return sb.toString();
	}


	public RecordSet getRecordSet() throws Exception {
		
		DB db = null;
		RecordSet rs = null;
		try {
			long stime = System.currentTimeMillis();
		
			db = new DB(this.jndi);
			if(debug == true) db.setDebug(out);
			this.dbType = db.getDBType();
			rs = db.query(getListQuery());
			if(rs == null) this.errMsg = db.errMsg;
			db.close();

			long etime = System.currentTimeMillis();
			if(debug == true) {
				out.print("<hr>Execution Time : " + (etime - stime) + " (1/1000 sec)<hr>");
			}
		} catch(Exception e) {
			rs = new RecordSet(null);
			this.errMsg = db.errMsg;
			Malgn.errorLog("{ListManager.getRecordSet} " + e.getMessage());
		} finally {
			if(db != null) db.close();
		}

		return rs;
	}
	
	public DataSet getDataSet() throws Exception {
		Vector v = new Vector();
		if(listMode == 1) totalNum = this.getTotalNum();
		RecordSet rs = getRecordSet();
		if(rs != null) {
			for(int j=0; rs.next(); j++) {
				if(listMode == 1) {
					rs.put("ROW_CLASS", j%2 == 0 ? "even" : "odd");
					rs.put("__ord", totalNum - (pageNum - 1) * listNum - j);
					rs.put("__asc", (pageNum - 1) * listNum + j + 1);
				}
			}
			rs.first();
		}
		return rs;
	}

	public String getPaging(int linkType) throws Exception {

		Pager pg = new Pager(request);
		pg.setTotalNum(totalNum);
		pg.setListNum(listNum);
		pg.setPageNum(pageNum);
		pg.setNaviNum(naviNum);
		pg.linkType = linkType;
		pg.pagebar = pagebar;

		return pg.getPager();
	}

	public String getPaging() throws Exception {
		return this.getPaging(0);
	}

	public String dateFormate(String str, String param) {
		return str;
	}

	public String textFormate(String str, String param) {
		return str;
	}

	public String numberFormate(String str, String param) {
		return str;
	}

	public String cutString(String str, String param) {
		return str;
	}
}
