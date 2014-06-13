package fatworm.scan;

import java.sql.SQLException;


public abstract class UScan extends Scan {
	public Scan scan;
	public UScan(Scan scan) {
		this.scan = scan;
	}
	public void close() throws SQLException {
		scan.close();
	}
}
