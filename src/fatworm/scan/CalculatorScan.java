package fatworm.scan;

import java.sql.SQLException;
import java.util.LinkedList;

import fatworm.absyn.Expr;
import fatworm.driver.Tuple;
import fatworm.field.Field;
import fatworm.util.ExprManager;

public class CalculatorScan extends Scan {
	public LinkedList<Expr> columns;
	private boolean outputed = false;
	public CalculatorScan(LinkedList<Expr> columns) {
		this.columns = columns;
	}
	
	public boolean hasNext(Tuple parent) throws SQLException {
		return !outputed;
	}
	@Override
	public Tuple next(Tuple parent) throws SQLException {
		if (outputed)
			return null;
		Field f;
		Tuple ans = new Tuple();
		for (Expr e:columns) {
			f = ExprManager.eval(e, null, null);
			ans.add(f);
		}
		outputed = true;
		return ans;
	}
	@Override
	public void restart() {
		outputed = false;
	}

}
