package fatworm.scan;

import java.sql.SQLException;

import fatworm.absyn.Expr;
import fatworm.driver.Tuple;
import fatworm.util.ExprManager;

public class DeleteScan extends Scan{
	private TableScan scan;
	private Expr cond;
	
	public DeleteScan(TableScan scan, Expr cond) {
		this.scan = scan;
		this.cond = cond;
	}
	
	public DeleteScan(TableScan scan) {
		this.scan = scan;
		this.cond = null;
	}
	
	public boolean delete() throws SQLException {
		if (cond == null)
			return scan.clear();
		else {
			while (scan.hasNext()) {
				if (ExprManager.toFinalBool(ExprManager.eval(cond, scan.next(), null)))
					if (!scan.delete())
						return false;
			}
			return true;
		}
	}


	@Override
	public void restart() throws SQLException {
		// TODO Auto-generated method stub
		
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
	public void close() throws SQLException {
		scan.close();
		
	}

	
}
