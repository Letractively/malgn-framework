package malgnsoft.util;

import javax.servlet.jsp.JspWriter;
import java.io.File;
import java.util.Hashtable;
import malgnsoft.db.DataSet;
import malgnsoft.util.Malgn;

/**
 * <pre>
 * Cache cache = new Cache();
 * //cache.setDebug(out);
 * if(cache.print(out, "key")) return true;
 * ....
 * cache.savePrint("key", "data", out);
 * </pre>
 */
public class Cache {

	private int timeOut = 300; //second

	private JspWriter out = null;
	private boolean debug = false;

	public String errMsg = "";

	public Cache() { }

	public void setDebug(JspWriter out) {
		this.out = out;
		this.debug = true;
	}

	private void setError(String msg) {
		this.errMsg = msg;
		try {
			if(out != null && debug == true) {
				out.print("<hr>" + msg + "<hr>\n");
			}
		} catch(Exception e) {}
	}

	public void setTimeOut(int t) {
		this.timeOut = t;
	}

	public Object get(String key) {
		File f = new File(getCachePath(key));
		if(!f.exists()) {
			setError(key + " NOT EXISTS");
			return null;
		}
		if(System.currentTimeMillis() - f.lastModified() < (timeOut * 1000)) {
			return Malgn.unserialize(f);
		} else {
			if(debug) setError("Time:" + (System.currentTimeMillis() - f.lastModified()));
			return null;
		}
	}

	public String getString(String key) {
		return (String)get(key);
	}

	public Hashtable getMap(String key) {
		return (Hashtable)get(key);
	}

	public DataSet getDataSet(String key) {
		return (DataSet)get(key);
	}

	public boolean print(JspWriter out, String key) throws Exception {
		String data = getString(key);
		if(data != null) {
			out.print(data);
			return true;
		} else {
			return false;
		}
	}

	public void save(String key, Object data) {
		File dir = new File(Malgn.dataDir + "/cache");
		if(!dir.exists()) dir.mkdirs();

		Malgn.serialize(getCachePath(key), data);
	}

	public void savePrint(String key, Object data, JspWriter out) throws Exception {
		save(key, data);
		out.print(data);
	}

	private String getCachePath(String key) {
		return Malgn.dataDir + "/cache/" + Malgn.md5(key);
	}
}