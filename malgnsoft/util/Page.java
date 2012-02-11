package malgnsoft.util;

import java.io.File;
import java.util.Date;
import javax.servlet.jsp.JspWriter;
import malgnsoft.util.Template;
import malgnsoft.db.*;
import java.util.*;


public class Page extends Template {

	private String _root;
	private String _layout;
	private String _body;
	private long startTime;

	public Page(String root) {
		super(root);
		_root = root;
		startTime = System.currentTimeMillis();
	}

	public void setLayout(String layout) {
		if(layout == null) _layout = null;
		else {
			_layout = "layout/layout_" + layout.replace('.', '/') + ".html";
			File file = new File(_root + "/" + _layout);
			if(!file.exists()) {
				_layout = "layout/layout_blank.html";
			}
		}
	}

	public void setBody(String body) {
		_body = body.replace('.', '/') + ".html";
	}

	public void setWriter(JspWriter out) {
		this._out = out;
	}

	public void display() throws Exception {
		display(this._out);
	}
	public void display(JspWriter out) throws Exception {
		if(_layout == null) this.print(out, _body);
		else {
			this.setVar("BODY", _body);
			this.print(out, _layout);
		}

		long endTime = System.currentTimeMillis();
		double exeTime = (double)(endTime - startTime) / 1000;
		out.print("\r\n<!-- LAYOUT : " + _layout + " -->");
		out.print("\r\n<!-- BODY : " + _body + " -->");
		out.print("\r\n<!-- EXECUTION TIME : " + exeTime + " Second -->");
	}

	public String fetchAll() throws Exception {
		return fetch(_layout);
	}
}
