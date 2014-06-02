package fatworm.logicplan;

import java.util.LinkedList;
import java.util.List;

import fatworm.absyn.Column;
import fatworm.absyn.Expr;
import fatworm.absyn.Func;
/*
 * aggregate plan
 */
public class AggPlan extends UAlgebraPlan {
	public LinkedList<Expr> to;	// can only of type Func or Rename with the expr:Func
	public LinkedList<Column> by;	// null if no group by clause present. So either by.size()>0 or by == null
	public Expr having = null;
	
	public AggPlan(Plan src, LinkedList<Expr> funcs, LinkedList<Column> by, Plan parent) {
		super(src, parent);
		this.to = funcs;
		this.by = by;
	}
	
	public AggPlan(Plan src, LinkedList<Expr> funcs, LinkedList<Column> by) {
		super(src);
		this.to = funcs;
		this.by = by;
	}
	
	
	// when having neither group by or having
	public AggPlan(Plan src) {
		super(src);
		to = new LinkedList<Expr>();
		by = null;
	}
	
	public String getString(String tabs) {
		return tabs+"AggPlan{"+by+to+"}\n"+tabs+"OVER{\n"+src.getString(tabs+"\t")+"\n"+tabs+"} GroupBy {"+by+"}";
	}
	
}
