package fatworm.scan;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import fatworm.absyn.Column;
import fatworm.absyn.Expr;
import fatworm.absyn.Func;
import fatworm.driver.Table;
import fatworm.driver.Tuple;
import fatworm.field.FLOAT;
import fatworm.util.ExprManager;

public class AggScan extends UScan{
	public LinkedList<Expr> to; //project to these columns, including column, func and etc.
	public LinkedList<Column> by;
	private HashMap<Tuple, Scan> buckets = new HashMap<Tuple, Scan>();
	private Iterator<Scan> iter = null;
	private boolean hashed = false;
	public AggScan(Scan scan, LinkedList<Expr>to, LinkedList<Column> by) {
		super(scan);
		this.to = to;
		this.by = by;
	}
	
	@Override
	public boolean hasNext(Tuple parent) throws SQLException {
		if (!hashed)
			init(parent);
		return iter.hasNext();
	}
	@Override
	/**
	 * when evaluating each column in to
	 * only evaluate the funcs when taking new tuple one by one
	 * after taking all the tuples, evaluate the whole col expr, where
	 * the value of each referred column if that of the first col.
	 */
	public Tuple next(Tuple parent) throws SQLException {
		if (!hashed)
			init(parent);
		if (!iter.hasNext()) return null;
		Scan scan = iter.next();
		Tuple first = scan.next(parent); // if by != null, tableScan would not use parent; else scan may use this
		initFunc(first);
		int cnt = 0;
		while (scan.hasNext(parent)) {
			evalFunc(scan.next(parent));
			++cnt;
		}
		Tuple ans = new Tuple();
		for (Expr col:to) {
			ans.add(ExprManager.eval(col,first, null));
		}
		int i = 0;
		for (Expr col:to) {
			if (col instanceof Func && ((Func)col).func == Func.FuncType.AVG && !ans.get(i).isNull()) {
				Float avg = ((FLOAT)FLOAT.toFloat(ans.get(i))).v/cnt; 
				ans.set(i, new FLOAT(avg));
			}
			++i;
		}
//		System.out.println(ans);
		return ans;
	}
	private void evalFunc(Tuple t) throws SQLException {
		for (Expr col:to) {
			ExprManager.evalFunc(col, t);
		}
	}
	private void initFunc(Tuple first) throws SQLException {
		for (Expr col:to) {
			ExprManager.init(col, first);
		}
	}
	@Override
	public void restart() throws SQLException {
		if (hashed) {
			if (by == null)
				scan.restart();
			else 
				initIter();
		}
	}
	private void init(Tuple parent) throws SQLException {
		hash(parent);
		initIter();
	}
	private void initIter() {
		iter = buckets.values().iterator();
	}
	private void hash(Tuple parent) throws SQLException {
		Tuple t, key;
		if (by == null) {
			buckets.put(new Tuple(), scan);
		} else{
			while(scan.hasNext(parent)) {
				t = scan.next(parent);
				key = new Tuple();
				for (Column col:by) {
					key.add(t.get(col.idx));
				}
				TableScan scan = (TableScan)buckets.get(key);
				if (scan != null) {
					scan.insert(t);
					//FIXME: because scan is a reference, I didn't use put
				} else {
					scan = new TableScan(new Table("temp",null)); // FIXME: use null as schema so far; add schema if needed in the future
					scan.insert(t);
					buckets.put(key, scan);
				}
			}
			for (Scan scan: buckets.values()) {
				scan.restart();
			}
		}
		hashed = true;
	}

}
