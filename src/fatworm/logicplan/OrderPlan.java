package fatworm.logicplan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fatworm.absyn.Column;
import fatworm.absyn.Expr;

public class OrderPlan extends UAlgebraPlan {
	public ArrayList<Column> by;
	public ArrayList<Boolean> asc;	// true iff the corresponding column in by is ascending
	public OrderPlan(Plan src, ArrayList<Column> by, ArrayList<Boolean> asc) {
		super(src);
		assert(by.size() == asc.size());	//FIXME omit this?
		this.by = by;
		this.asc = asc;
	}
	
	public OrderPlan(Plan src) {
		super(src);
		by = new ArrayList<Column>();
		asc = new ArrayList<Boolean>();
	}
	
	public OrderPlan(Plan src, ArrayList<Column> by, Plan parent) {
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
