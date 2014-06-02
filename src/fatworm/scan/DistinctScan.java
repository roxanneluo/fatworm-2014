package fatworm.scan;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;

import fatworm.driver.Tuple;

public class DistinctScan extends UScan {
	private boolean sorted;
	
	private Tuple cur = null;
	private Tuple last = null;
	
	private boolean hashed = false;
	private HashSet<Tuple> set = null;
	private Iterator<Tuple> iter = null;

	public DistinctScan(Scan scan, boolean sorted) {
		super(scan);
		this.sorted = sorted;
	}
	
	@Override
	public boolean hasNext(Tuple parent) throws SQLException {
		Tuple t = getNext(parent);
		if (t != null)
			return true;
		return false;
	}

	@Override
	public Tuple next(Tuple parent) throws SQLException {
		Tuple t = getNext(parent);
		cur = null;
		return t;
	}

	@Override
	public void restart() throws SQLException {
		if (sorted)
			scan.restart();
		else 
			if (hashed)
				initIter();
		cur = last = null;
	}
	private void initIter() {
		iter = set.iterator();
	}
	private Tuple getNext(Tuple parent) throws SQLException {
		if (cur != null)
			return cur;
		if (sorted) {
			if (last == null) {
				if (scan.hasNext(parent)) {
					return last = cur = scan.next(parent);
				} else return last = cur = null;
			}
			
			while (scan.hasNext(parent) && (cur = scan.next(parent)).equals(last));
			last = cur;
			return cur;
		} else {
			if (!hashed) {
				hash(parent);
				initIter();
			}
			if (iter.hasNext())
				return cur = iter.next();
			else return cur = null;
		}
	}
	
	private void hash(Tuple parent) throws SQLException {
		set = new HashSet<Tuple> ();
		while(scan.hasNext(parent)) {
			Tuple t = scan.next(parent);
			set.add(t);
		}
		hashed = true;
	}
	

}
