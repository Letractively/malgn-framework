package malgnsoft.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Hashtable;
import malgnsoft.util.Malgn;

public class RecordSet extends DataSet {

	public boolean labelLower = true;

	public RecordSet(ResultSet rs) throws SQLException {
		if(rs == null) return;

		ResultSetMetaData meta = rs.getMetaData();
		int	max = meta.getColumnCount();
		
		this.columns = new String[max];
		for(int i = 0; i < max; i++) {
			if(labelLower == true) {
				columns[i] = meta.getColumnLabel(i+1).toLowerCase();
			} else {
				columns[i] = meta.getColumnLabel(i+1);
			}
		}
		
		int j = 0;
		while(rs.next()) {
			this.addRow();
			for(int i = 1; i <= max; i++) {
				try {
					if(meta.getColumnType(i) == java.sql.Types.CLOB) {
						this.put(columns[i-1], rs.getString(i));
					} else if(meta.getColumnType(i) == java.sql.Types.DATE) {
						this.put(columns[i-1], rs.getTimestamp(i));
					} else {
						this.put(columns[i-1], rs.getObject(i));
					}
				} catch(Exception e) {
					this.put(columns[i-1], "");
					Malgn.errorLog("{RecordSet.constructor} " + e.getMessage());
				}
			}
			this.put("__first", j == 0);
			this.put("__ord", ++j);
			this.put("__last", false);
		}
		rs.close();
		if(j > 0) this.put("__last", true);

		this.first();
	}
}
