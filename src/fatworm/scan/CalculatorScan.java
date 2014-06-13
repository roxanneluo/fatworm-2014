package fatworm.scan;

import java.sql.SQLException;
import java.util.LinkedList;

import fatworm.absyn.Column;
import fatworm.absyn.Expr;
import fatworm.driver.Tuple;
import fatworm.field.Field;
import fatworm.util.ExprManager;

public class CalculatorScan extends Scan {
	public LinkedList<Expr> columns;
	private boolean outputed = false;
	private boolean col = false;
	public CalculatorScan(LinkedList<Expr> columns) {
		this.columns = columns;
		for (Expr e:columns) {
			if (e instanceof Column) {
				col = true;
				break;
			}
		}
	}
	
	public boolean hasNext(Tuple parent) throws SQLException {
		return col || !outputed;
	}
	@Override
	public Tuple next(Tuple parent) throws SQLException {
		if (!hasNext()) 
			return null;
		Field f;
		Tuple ans = new Tuple();
		for (Expr e:columns) {
			f = ExprManager.eval(e, null, parent);
			ans.add(f);
//			System.out.println(f);
		}
		outputed = true;
		
		return ans;
	}
	@Override
	public void restart() {
		outputed = false;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
