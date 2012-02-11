package malgnsoft.db;

import java.util.*;
import javax.servlet.jsp.JspWriter;
import malgnsoft.util.Malgn;

public class DataSet extends Object implements java.io.Serializable {

	private JspWriter out = null;
	private boolean debug = false;
	private Vector<Hashtable<String, Object>> rows = null;
	private int idx = -1;
	public String[] columns;
	public int[] types;
	public int sortType = -1;
	
	public DataSet() {
		rows = new Vector<Hashtable<String, Object>>();
	}

	public DataSet(DataSet ds) {
		rows = new Vector<Hashtable<String, Object>>();
		ds.first();
		while(ds.next()) {
			this.addRow(ds.getRow());
		}
		this.first();
	}

	public void setDebug(JspWriter out) {
		this.out = out;
		this.debug = true;
	}

	public int size() {
		if(rows != null) return rows.size();
		else return 0;
	}
	
	public boolean next() {
		if(rows == null || rows.size() <= (idx + 1)) return false;
		
		idx = idx + 1;
		return true;
	}

	public void move(int id) {
		idx = id;
	}

	public int getIndex() {
		return idx;
	}

	public int addRow() {
		rows.addElement(new Hashtable<String, Object>());
		idx++;
		return idx;
	}

	public int addRow(Hashtable<String, Object> map) {
		rows.addElement(new Hashtable<String, Object>(map));
		idx++;
		return idx;
	}

	public void removeAll() {
		rows.removeAllElements();
		idx = -1;
	}

	public boolean prev() {
		idx = idx - 1;
		if(idx < 0) {
			idx = 0;
			return false;
		} else {
			return true;
		}
	}

	public boolean first() {
		idx = -1;
		return true;
	}

	public boolean last() {
		idx = this.size() - 1;
		return true;
	}

	public void put(String name, int i) {
		this.put(name, "" + i);
	}

	public void put(String name, double d) {
		this.put(name, "" + d);
	}

	public void put(String name, boolean b) {
		this.put(name, "" + b);
	}

	public void put(String name, Object value) {
		if(value == null) value = "";
		rows.get(idx).put(name, value);
	}

	public Object get(String name) {
		if(rows == null) return null;
		if(idx < 0) return null;
		Object ret = null;
		try {
			Hashtable map = rows.get(idx);
			if(map != null && map.containsKey(name)) {
				ret = map.get(name);
			}
		} catch(Exception e) {
			Malgn.errorLog("{DataSet.get} " + e.getMessage());
		}

		return ret;
	}

	public String getString(String name) {
		if(rows == null) return "";
		if(idx < 0) return "";
		String ret = "";
		try {
			Hashtable map = rows.get(idx);
			if(map != null && map.containsKey(name)) {
				ret = map.get(name).toString();
			}
		} catch(Exception e) {
			Malgn.errorLog("{DataSet.getString} " + e.getMessage());
		}

		return ret;
	}
	
	public int getInt(String name) {
		if(rows == null) return 0;
		int ret = 0;
		try {
			String val = getString(name).trim();
			if(val != null && !"".equals(val)) ret = Integer.parseInt(val);
		} catch(Exception e) {
			Malgn.errorLog("{DataSet.getInt} " + e.getMessage());
		}

		return ret;
	}

	public long getLong(String name) {
		if(rows == null) return 0;
		long ret = 0;
		try {
			String val = getString(name).trim();
			if(val != null && !"".equals(val)) ret = Long.parseLong(val);
		} catch(Exception e) {
			Malgn.errorLog("{DataSet.getLong} " + e.getMessage());
		}

		return ret;
	}

	public double getDouble(String name) {
		if(rows == null) return 0.0;
		double ret = 0.0;
		try {
			String val = getString(name).trim();
			if(val != null && !"".equals(val)) ret = Double.parseDouble(val);
		} catch(Exception e) {
			Malgn.errorLog("{DataSet.getDouble} " + e.getMessage());
		}

		return ret;
	}

	public boolean getBoolean(String name) {
		if(rows == null) return false;
		boolean ret = false;
		try {
			String val = getString(name).trim();
			if(val != null) {
				val = val.toUpperCase();
				if("Y".equals(val) || "1".equals(val) || "TRUE".equals(val)) ret = true;
			}
		} catch(Exception e) {
			Malgn.errorLog("{DataSet.getDouble} " + e.getMessage());
		}

		return ret;
	}

	public String s(String name) { return getString(name); }
	public int i(String name) { return getInt(name); }
	public long l(String name) { return getLong(name); }
	public double d(String name) { return getDouble(name); }
	public boolean b(String name) { return getBoolean(name); }

	public Date getDate(String name) {
		if(rows == null) return null;
		Date ret = null;
		try {
			ret = (Date)(rows.get(idx).get(name));
		} catch(Exception e) {
			Malgn.errorLog("{DataSet.getDate} " + e.getMessage());
		}

		return ret;
	}

	public Vector getRows() {
		return rows;
	}


	public Hashtable<String, Object> getRow() {
		if(idx > -1) {
			return new Hashtable<String, Object>(rows.get(idx));
		} else {
			return null;
		}
	}

	public String[] getColumns() {
		return columns;
	}

	public String[] getKeys() {
		if(idx > -1) {
			Hashtable map = rows.get(idx);
			if(map == null) return null;
			
			Enumeration e = map.keys();
			int i = 0;
			String keys = "";
			while(e.hasMoreElements()) {
				keys += "," + (String)(e.nextElement());
				i++;
			}
			if(i > 0) keys = keys.substring(1);
			return keys.split(",");
		} else {
			return null;
		}		
	}

	public boolean isColumn(String key) {
		if(columns != null) {
			return Malgn.inArray(key, columns);
		} else {
			return false;
		}
	}

	public boolean isKey(String key) {
		if(idx > -1) {
			Hashtable map = rows.get(idx);
			if(map == null) return false;
			return map.containsKey(key);
		} else {
			return false;
		}	
	}

	public String toString() {
		if(rows != null) {
			return rows.toString();
		} else {
			return "";
		}
	}

}