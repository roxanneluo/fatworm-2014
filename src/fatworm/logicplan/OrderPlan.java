package fatworm.logicplan;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fatworm.absyn.Column;
import fatworm.absyn.Expr;

public class OrderPlan extends UAlgebraPlan {
	public List<Column> by;
	public List<Boolean> asc;	// true iff the corresponding column in by is ascending
	public OrderPlan(Plan src, List<Column> by, List<Boolean> asc) {
		super(src);
		assert(by.size() == asc.size());	//FIXME omit this?
		this.by = by;
		this.asc = asc;
	}
	
	public OrderPlan(Plan src) {
		super(src);
		by = new LinkedList<Column>();
		asc = new LinkedList<Boolean>();
	}
	
	public OrderPlan(Plan src, List<Column> by, Plan parent) {
		super(src, parent);
		this.by = by;
	}
	public String getString(String tabs) {
		String str = tabs+"OrderPlan{\n"
						+src.getString(tabs+"\t")+"\n"
						+tabs+"} by {";
		
		assert(by.size() == asc.size());
		Iterator<Column> colIter = by.iterator();
		Iterator<Boolean> ascIter = asc.iterator();
		while(colIter.hasNext()) {
			str += colIter.next()+" "+(ascIter.next()? "ASC":"DESC")+",";
		}
		str += "}";
		return str;
	}
}
