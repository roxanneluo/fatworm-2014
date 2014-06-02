package fatworm.scan;


import java.sql.SQLException;
import java.util.ArrayList;

import fatworm.driver.Tuple;
import fatworm.field.Field;

public class ProductScan extends Scan{
	private Scan left, right;
	private Tuple curLeft = null;
	public ProductScan(Scan left, Scan right) throws SQLException {
		this.left = left;
		this.right = right;
		if (this.left.hasNext())
			curLeft = left.next();
	}
	@Override
	public Tuple next() throws SQLException {
		if (curLeft == null)
			return null;
		
		Tuple ans = new Tuple(curLeft);
		
		if (right.hasNext()) {
			Tuple r = right.next();
			ans.addAll(r);
			return ans;
		} else {
			if (!left.hasNext()) return null;
			curLeft = left.next();
			right.restart();
			if (!right.hasNext()) return null;
			Tuple r = right.next();
			ans = new Tuple(curLeft);
			ans.addAll(r);
			return ans;
		}
	}
	@Override
	public boolean hasNext() throws SQLException {
		if (right.hasNext()) return true;
		if (left.hasNext()) return true;
		return false;
	}
	@Override
	public void restart() throws SQLException {
		left.restart();
		right.restart();
		if (left.hasNext())
			curLeft = left.next();
	}
	@Override
	public boolean hasNext(Tuple parent) throws SQLException {
		return hasNext();
	}
	@Override
	public Tuple next(Tuple parent) throws SQLException {
		return next();
	}
	
}
