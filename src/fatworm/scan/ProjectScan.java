package fatworm.scan;

import java.sql.SQLException;
import java.util.LinkedList;

import fatworm.absyn.Column;
import fatworm.absyn.Expr;
import fatworm.driver.Tuple;
import fatworm.field.FLOAT;
import fatworm.field.Field;
import fatworm.util.ExprManager;
/**
 * projectScan will only pass the parent tuple to the selectScan below
 * if appropriate
 * it will not use this tuple itself
 * @author roxanne
 *
 */
public class ProjectScan extends UScan {
	public LinkedList<Expr> columns;
	
	public ProjectScan(Scan scan, LinkedList<Expr> columns) {
		super(scan);
		this.columns = columns;
	}
	public ProjectScan(Scan scan) {
		super(scan);
		columns = new LinkedList<Expr>();
	}
	@Override
	public boolean hasNext(Tuple parent) throws SQLException {
		return scan.hasNext(parent);
	}
	@Override
	public Tuple next(Tuple parent) throws SQLException {
		if (!scan.hasNext(parent)) return null;
		Tuple t = scan.next(parent);
		Tuple ans = new Tuple();
		Field f;
		for (Expr e:columns) {
			f = ExprManager.eval(e, t, null);
			ans.add(f);
		}
		return ans;
	}
	@Override
	public void restart() throws SQLException {
		scan.restart();
	}
	public String toString() {
		return columns.toString();
	}
}
