package malgnsoft.util;

import java.io.File;
import jxl.*;
import jxl.write.*;
import jxl.write.Number;
import javax.servlet.http.HttpServletResponse;
import malgnsoft.db.DataSet;
import malgnsoft.util.Malgn;

/**
 * <pre>
 * // Excel File Save
 * ExcelWriter ex = new ExcelWriter("/data/test.xls");
 * ex.put(1, 1, "aaa");
 * ex.put(1, 2, "bbb");
 * ex.write();
 * 
 * // Excel Export
 * String[] cols = { "col1=>Name", "col2=>Email" };
 * ExcelWriter ex = new ExcelWriter(response, "test.xls");
 * ex.setData(rs, cols);
 * ex.write();
 * </pre>
 */
public class ExcelWriter {

	private WritableWorkbook workbook = null;
	private WritableSheet sheet = null; 

	public ExcelWriter(String path) throws Exception {
		File f = new File(path);
		if(!f.getParentFile().isDirectory()) {
			f.getParentFile().mkdirs();
		}
		
		if(!f.exists()) f.createNewFile();
		workbook = Workbook.createWorkbook(f);
		sheet = workbook.createSheet("Sheet1", 0);
	}

	public ExcelWriter(HttpServletResponse response, String filename) throws Exception {
		response.setContentType("application/vnd.ms-excel");
	    response.setHeader("Content-Disposition", "attachment; filename=\"" + new String(filename.getBytes("KSC5601"),"8859_1") + "\"");
		workbook = Workbook.createWorkbook(response.getOutputStream());
		sheet = workbook.createSheet("Sheet1", 0);
	}

	public void setData(DataSet rs, String[] cols) throws Exception {

		for(int i=0; i<cols.length; i++) {
			String[] arr = cols[i].split("=>");
			this.put(i, 0, arr[1]);
			cols[i] = arr[0];
		}

		int y = 1;
		rs.first();
		while(rs.next()) {
			for(int x=0; x<cols.length; x++) {
				this.put(x, y, rs.getString(cols[x]));
			}
			y++;
		}
	}

	public void put(int x, int y, String str) throws Exception {
		if(sheet != null) {
			Label label = new Label(x, y, str); 
			sheet.addCell(label); 
		}
	}

	public void put(int x, int y, int num) throws Exception {
		if(sheet != null) {
			Number number = new Number(x, y, num); 
			sheet.addCell(number);
		}
	}

	public void put(int x, int y, double num) throws Exception {
		if(sheet != null) {
			Number number = new Number(x, y, num); 
			sheet.addCell(number);
		}
	}

	public void merge(int x, int y, int c, int r) throws Exception {
		if(sheet != null) {
			sheet.mergeCells(x, y, c, r);
		}
	}

	public void write() throws Exception {
		if(workbook != null) {
			workbook.write();
			workbook.close();
		}
	}
}