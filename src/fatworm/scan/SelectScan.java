package fatworm.scan;

import java.sql.SQLException;

import fatworm.absyn.Exists;
import fatworm.absyn.Expr;
import fatworm.driver.Tuple;
import fatworm.field.BOOL;
import fatworm.util.ExprManager;

public class SelectScan extends UScan {
	public Expr cond;
	public Tuple cur = null;
	public SelectScan(Scan scan, Expr cond) {
		super(scan);
		this.cond = cond;
	}
	@Override
	public boolean hasNext(Tuple parent) throws SQLException {
		return getNext(parent) != null;
	}
	
	private Tuple getNext(Tuple parent) throws SQLException {
		if (cur != null)
			return cur;
		while(scan.hasNext(null)) {
			Tuple t = scan.next(null);
			if (ExprManager.toFinalBool(ExprManager.eval(cond, t, parent))) {
				return cur = t;
			}
		}
		return null;
	}
	@Override
	public Tuple next(Tuple parent) throws SQLException {
		Tuple ans = getNext(parent);
		cur = null;
		return ans;
		
	}
	@Override
	public void restart() throws SQLException {
		scan.restart();
		ExprManager.restart(cond);
	}
	
}