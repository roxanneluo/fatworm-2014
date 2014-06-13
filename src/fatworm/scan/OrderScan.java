package fatworm.scan;

import java.sql.SQLException;
import java.util.ArrayList;

import fatworm.absyn.Column;
import fatworm.driver.Table;
import fatworm.driver.Tuple;

public class OrderScan extends UScan{
	public ArrayList<Integer> by = new ArrayList<Integer>(); 	// integer is enough
	private ArrayList<Boolean> asc; 
	private TableScan sortedTable = null;

	public OrderScan(Scan scan, ArrayList<Boolean> asc) {
		super(scan);
		this.asc = asc;
	}
	@Override
	public boolean hasNext(Tuple parent) throws SQLException {
		if (sortedTable == null)
			sort(parent);
		return sortedTable.hasNext();
	}
	@Override
	public Tuple next(Tuple parent) throws SQLException {
		if (sortedTable == null)
			sort(parent);
		return sortedTable.next();
	}
	@Override
	public void restart() throws SQLException {
		if (sortedTable != null)
			sortedTable.restart();
	}
	private void sort(Tuple parent) throws SQLException {
//		System.out.println("beforesort:\n");
		if (scan instanceof TableScan) {
//			System.out.println(scan);
			((TableScan)scan).sort(by, asc);
			sortedTable = (TableScan) scan;
		} else {
			sortedTable = new TableScan(new Table("temp", null));
			while(scan.hasNext(parent)) {
				sortedTable.insert(scan.next(parent), true);
			}
//			System.out.println(sortedTable);
			sortedTable.sort(by, asc);
			sortedTable.restart();
		}
//		System.out.println("sortedTable:\n"+sortedTable);
	}
}
