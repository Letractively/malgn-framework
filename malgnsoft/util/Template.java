package malgnsoft.util;

import java.io.*;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspWriter;
import malgnsoft.db.DataSet;
import malgnsoft.util.Malgn;

public class Template {
	
	private String _root = "";
	protected Hashtable<String, String> _var = new Hashtable<String, String>();
	protected Hashtable<String, DataSet> _loop = new Hashtable<String, DataSet>();
	protected JspWriter _out = null;
	private PageContext _pageContext = null;
	private boolean _debug = false;
	private String[] _prefix = {"IN", "EX", "LO", "IF"};
	private String encoding = Config.getEncoding();

	public Template() {
		
	}

	public Template(String path) {
		setRoot(path);
	}

	public void setDebug() {
		_debug = true;
	}

	public void setDebug(JspWriter out) {
		this._out = out;
		_debug = true;
	}

	public void setRequest(HttpServletRequest request) {
		Enumeration e = request.getParameterNames();
		while(e.hasMoreElements()) {
			String key = null;
			String value = null;
			key = (String)e.nextElement();
			value = request.getParameter(key);
			_var.put(key, value);
		}
	}

	public void setPageContext(PageContext pc) {
		_pageContext = pc;
	}
	
	public void setRoot(String path) {
		_root = path + "/";
	}

	public void setVar(String name, String value) {
		if(name == null) return;
		_var.put(name, value == null ? "" : value);
	}

	public void setVar(String name, int value) {
		setVar(name, "" + value);
	}

	public void setVar(String name, long value) {
		setVar(name, "" + value);
	}
	
	public void setVar(String name, boolean value) {
		setVar(name, value == true ? "true" : "false");
	}

	public void setVar(Hashtable values) {
		if(values == null) return;
		Enumeration e = values.keys();
		while(e.hasMoreElements()) {
			String key = e.nextElement().toString();
			if(values.get(key) != null) {
				setVar(key, values.get(key).toString());
			}
		}
	}

	public void setVar(DataSet values) {
		if(values != null && values.size() > 0) {
			this.setVar(values.getRow());
		}
	}

	public void setVar(String name, DataSet values) {
		this.setVar(name, values.getRow());
	}
	
	public void setVar(String name, Hashtable values) {
		if(name == null || values == null) return;

		int sub = 0;
		Enumeration e = values.keys();
		while(e.hasMoreElements()) {
			String key = null;
			key = e.nextElement().toString();
			if(values.get(key) == null || key.length() == 0) continue;
			if(key.charAt(0) != '.') {
				setVar(name + "." + key, values.get(key).toString());
			} else {
				setLoop(key.substring(1), (DataSet)values.get(key));
			}
			/*
			if(values.get(key) instanceof DataSet) {
				setLoop(key.substring(1), (DataSet)values.get(key));
			} else {
				setVar(name + "." + key, values.get(key).toString());
			}
			*/
		}
	}

	public void setLoop(String name, DataSet rs) {
		if(rs != null && rs.size() > 0) {
			rs.first();
			_loop.put(name, rs);
			setVar(name, true);
		} else {
			_loop.put(name, new DataSet());
			setVar(name, false);
		}
	}

	public String fetch(String filename) throws Exception {
		_out = new __DummyWriter();
		parseTag(readFile(filename));
		return _out.toString();
	}
	
	public void print(JspWriter out, String filename) throws Exception {
		_out = out;
		parseTag(readFile(filename));
		clear();
	}
    
	private void parseTag(String buffer) throws Exception {
		int pos = 0, offset = 0;
		while((pos = buffer.indexOf("<!-- ", offset)) != -1) {
			parseVar(buffer.substring(offset, pos));
			offset = pos + 5;
			
			String str = buffer.substring(offset, offset + 2);
			if(!Malgn.inArray(str, this._prefix)) {
				_out.print("<!-- ");
				continue;
			}

			int end = buffer.indexOf(" -->", pos);
			if(end != -1) {
				offset = end + 4;
				if((end - pos) > 100) continue;
				String[] names = parseCmd(buffer.substring(pos+5, end));
				
				if(names == null) continue;
				if("INCLUDE".equals(names[0])) {
					if("NAME".equals(names[1])) {
						if(_var.get(names[2]) != null) {
							parseTag(readFile(_var.get(names[2]).toString()));
						}
					} else if("FILE".equals(names[1])) {
						parseTag(readFile(names[2]));
					} else {
						setError("INCLUDE tag is not correct");
					}
				} else if("EXECUTE".equals(names[0])) {
					if("FILE".equals(names[1])) {
						_pageContext.include(names[2]);
					} else if("FILE".equals(names[1])) {
						setError("EXECUTE tag is not correct");
					}
				} else if("LOOP".equals(names[0]) && "START".equals(names[1])) {
					DataSet loop = (DataSet)_loop.get(names[2]);
					int loop_end = buffer.indexOf("<!-- LOOP END '" + names[2] + "' -->", offset);
					if(loop_end != -1) {
						if(loop != null) {
							loop.first();
							while(loop.next()) {
								setVar(names[2], (Hashtable)loop.getRow());
								parseTag(buffer.substring(end + 4, loop_end));
							}
						} else {
							setError("Loop Data is not exists, name is " + names[2]);
						}
						offset = loop_end + names[2].length() + 20;
					} else {
						setError("Loop end tag is not found, name is " + names[2]);
					}
				} else if("IF".equals(names[0]) && "START".equals(names[1])) {
					int if_end = buffer.indexOf("<!-- IF END '" + names[2] + "' -->", offset);
					if(if_end != -1) {
						if(_var.get(names[2]) == null 
							|| "false".equals(_var.get(names[2])) 
							|| "".equals(_var.get(names[2])) 
							|| (names[2].indexOf("_yn") != -1 && "N".equals(_var.get(names[2])))
							|| (names[2].indexOf("is_") != -1 && "0".equals(_var.get(names[2])))
						) {
							offset = if_end + names[2].length() + 18;
						}
					} else {
						setError("If end tag is not found, name is " + names[2]);
					}
				} else if("IFNOT".equals(names[0]) && "START".equals(names[1])) {
					int if_end = buffer.indexOf("<!-- IFNOT END '" + names[2] + "' -->", offset);
					if(if_end != -1) {
						if(_var.get(names[2]) != null 
							&& !"false".equals(_var.get(names[2])) 
							&& !"".equals(_var.get(names[2]))
							&& !(names[2].indexOf("_yn") == -1 && "N".equals(_var.get(names[2])))
							&& !(names[2].indexOf("is_") == -1 && "0".equals(_var.get(names[2])))
						) {
							offset = if_end + names[2].length() + 21;
						}
					} else {
						setError("If end tag is not found, name is " + names[2]);
					}
				}
			} else {
				setError("Command end tag is not found");
				_out.print("<!-- ");
			}
		}
		parseVar(buffer.substring(offset));
	}

	private String[] parseCmd(String buffer) {
		buffer = buffer.trim();
		String[] arr1 = buffer.split(" ");
		if(arr1.length != 3) return null;
		String[] ret = new String[3];
		ret[0] = arr1[0].toUpperCase();
		ret[1] = arr1[1].toUpperCase();
		ret[2] = parseString(arr1[2].substring(1, arr1[2].length() - 1));
		return ret;
	}

	public String parseString(String buffer) {
        String arr1[] = buffer.split("\\}\\}");
        StringBuffer sb = new StringBuffer();

        for(int i=0; i<arr1.length; i++) {
            String arr2[] = arr1[i].split("\\{\\{");
			sb.append(arr2[0]);
            if(arr2.length == 2) {
                if(_var.containsKey(arr2[1])) {
					sb.append(_var.get(arr2[1]).toString());
				}
            }
        }
		return sb.toString();
    }

	private void parseVar(String buffer) throws Exception {
		int tail = 0, offset = buffer.length() - 2;
		if(offset >= 0 && buffer.substring(offset).equals("}}")) {
			buffer += " "; tail = 1;
		}
        String arr1[] = buffer.split("\\}\\}");
		if(arr1.length > 1) {
			for(int i=0, len=arr1.length - tail; i<len; i++) {
				String arr2[] = arr1[i].split("\\{\\{");
				_out.print(arr2[0]);
				if(arr2.length == 2) {
					if(_var.containsKey(arr2[1])) {
						_out.print(_var.get(arr2[1]).toString());
					}
				} else if(arr2.length > 2) {
					int max = arr2.length - 1;
					for(int j=1; j<max; j++) {
						_out.print("{{" + arr2[j]);
					}
					if(_var.containsKey(arr2[max])) {
						_out.print(_var.get(arr2[max]).toString());
					}
				} else if(i != (arr1.length - 1)) {
					_out.print("}}");
				}
			}
		} else {
			_out.print(buffer);
		}
		_out.flush();
    }

    public String readFile(String filename) throws Exception {
        File f = new File(_root + filename);
        if(!f.exists()) {
            f = new File(filename);
            if(!f.exists()) {
                setError("File not found!!, filename is " + _root + filename);
                return "";
            }
        }

        FileInputStream fin = new FileInputStream(f);
        Reader reader = new InputStreamReader(fin, encoding);
        BufferedReader br = new BufferedReader(reader);

        char[] chars = new char[(int) f.length()];
        br.read(chars);
        br.close();
        reader.close();
        fin.close();

        return new String(chars);
    }

	private void setError(String msg) throws Exception {
		if(_out != null && _debug == true) {
			_out.print("<br><font color='red'><b>Error</b></font> : ");
			_out.print(msg);
			_out.print("<br>\n");
		}
	}

	public void clear() {
		_var.clear();
		_loop.clear();
	}
}

class __DummyWriter extends JspWriter {
	
	private StringBuffer _sb = new StringBuffer();
	
	public __DummyWriter() {
		super(0, false);
	}

	public void print(String str) {
		_sb.append(str);
	}

	public int getRemaining() { return 0; }
	public void flush() {}
	public void clearBuffer() {}
	public void clear() {}
	public void close() {}
	public void newLine() {}
	public void print(boolean b) {}
	public void print(char c) {}
	public void print(char[] s) {}
	public void print(double d)  {}
	public void print(float f) {}
	public void print(int i) {}
	public void print(long l) {}
	public void print(Object obj) {}
	public void println() {}
	public void println(boolean x) {}
	public void println(char x) {}
	public void println(char[] x) {}
	public void println(double x) {}
	public void println(float x) {}
	public void println(int x) {}
	public void println(long x) {}
	public void println(Object x) {}
	public void println(String x) {}
	public void write(char[] c,int i,int j) {}

	public String toString() {
		return _sb.toString();
	}
}