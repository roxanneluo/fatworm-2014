package fatworm.scan;

import java.sql.SQLException;
import java.util.LinkedList;

import fatworm.absyn.Column;
import fatworm.absyn.Expr;
import fatworm.driver.Tuple;
import fatworm.util.ExprManager;

public class UpdateScan extends UScan {
	public LinkedList<Column> cols;
	public LinkedList<Expr> vals;
	public Expr cond;
	public UpdateScan(Scan scan, Expr cond, LinkedList<Column> cols, LinkedList<Expr> vals) {
		super(scan);
		this.cond = cond;
		this.cols = cols;
		this.vals = vals;
	}
	public boolean update() throws SQLException {
		Tuple t;
		while(scan.hasNext()) {
			t = scan.next();
			if (ExprManager.toFinalBool(ExprManager.eval(cond, t, null))) {
				Column col; Expr val;
				for (int i = 0; i < cols.size(); ++i) {
					col = cols.get(i);
					val = vals.get(i);
					t.set(col.idx, ExprManager.eval(val, t, null));
				}
				if (!((TableScan)scan).update(t)) //FIXME: remove this if not need
					return false;
			}
		}
		return true;
	}
	@Override
	public boolean hasNext(Tuple parent) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Tuple next(Tuple parent) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void restart() throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
