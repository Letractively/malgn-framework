package malgnsoft.util;

import javax.servlet.*;
import java.io.File;
import java.util.Hashtable;
import java.util.Enumeration;
import malgnsoft.db.DataSet;
import malgnsoft.util.SimpleParser;

public class Config extends GenericServlet {
	
	private static String docRoot;
	private static String tplRoot;
	private static String dataDir;
	private static String logDir;
	private static String webUrl = "";
	private static String dataUrl = "/data";
	private static String jndi = "jdbc/malgn";
	private static String mailFrom = "webmaster@test.com";
	private static String secretId = "malgn-23ywx-20x05-s7399";
	private static String was = "resin";
	private static String encoding = "UTF-8";
	private static String md5Encoding = "KSC5601";
	private static Hashtable<String, String> data = new Hashtable<String, String>();

	public void init() throws ServletException {
		ServletContext sc = getServletContext();
		load(sc);
	}

	public static void load(ServletContext sc) throws ServletException {
		docRoot = sc.getRealPath("/").replace('\\', '/');
		tplRoot = docRoot + "/html";
		dataDir = docRoot + "/data";
		logDir = dataDir + "/log";

		reload();
	}

	public static void reload() {
		try { 
			String path = Config.getDocRoot() + "/WEB-INF/config.xml";
			if((new File(path)).exists()) {

				SimpleParser sp = new SimpleParser(path);
				DataSet rs = sp.getDataSet("//config/env");
				if(rs.next()) {
					Enumeration e = rs.getRow().keys();
					while(e.hasMoreElements()) {
						String key = (String)e.nextElement();
						data.put(key, rs.getString(key));
					}
				}
				if(data.containsKey("docRoot")) docRoot = get("docRoot");
				if(data.containsKey("webUrl")) webUrl = get("webUrl");
				if(data.containsKey("tplRoot")) tplRoot = get("tplRoot");
				if(data.containsKey("dataDir")) dataDir = get("dataDir");
				if(data.containsKey("logDir")) logDir = get("logDir");
				if(data.containsKey("jndi")) jndi = get("jndi");
				if(data.containsKey("mailFrom")) mailFrom = get("mailFrom");
				if(data.containsKey("was")) was = get("was");
				if(data.containsKey("encoding")) encoding = get("encoding");
				if(data.containsKey("secretId")) secretId = get("secretId");
			}
		} catch(Exception ex) {
			System.out.print(ex.getMessage());
		}
	}

	public void service(ServletRequest req, ServletResponse res) throws ServletException {
	}

	public static String getSecretId() {
		return secretId;
	}

	public static String getDocRoot() {
		return docRoot;
	}

	public static String getWebUrl() {
		return webUrl;
	}

	public static String getTplRoot() {
		return tplRoot;
	}

	public static String getDataDir() {
		return dataDir;
	}
	
	public static String getDataUrl() {
		return dataUrl;
	}
	
	public static String getLogDir() {
		return logDir;
	}

	public static String getJndi() {
		return jndi;
	}

	public static String getMailFrom() {
		return mailFrom;
	}

	public static String getWas() {
		return was;
	}

	public static String getEncoding() {
		return encoding;
	}

	public static String getMd5Encoding() {
		return md5Encoding;
	}

	public static void set(String key, String value) {
		data.put(key, value);
	}

	public static String get(String key) {
		return data.get(key);
	}
	public static int getInt(String key) {
		int ret = 0;
		try { ret = Integer.parseInt(data.get(key)); } catch(Exception e) { }
		return ret;
	}

}