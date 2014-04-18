package fatworm.logicplan;

import java.util.LinkedList;
import java.util.List;

import fatworm.absyn.Column;
import fatworm.absyn.Expr;
import fatworm.absyn.Func;

public class AggregatePlan extends UAlgebraPlan {
	List<Expr> funcs;	// can only of type Func or Rename with the expr:Func
	List<Column> by;	// null if no group by clause present. So either by.size()>0 or by == null
	
	public AggregatePlan(Plan src, List<Expr> funcs, List<Column> by, Plan parent) {
		super(src, parent);
		this.funcs = funcs;
		this.by = by;
	}
	
	public AggregatePlan(Plan src, List<Expr> funcs, List<Column> by) {
		super(src);
		this.funcs = funcs;
		this.by = by;
	}
	
	
	// when having neither group by or having
	public AggregatePlan(Plan src) {
		super(src);
		funcs = new LinkedList<Expr>();
		by = null;
	}
	
	public String getString(String tabs) {
		return tabs+"AggPlan{"+by+funcs+"}\n"+tabs+"OVER{\n"+src.getString(tabs+"\t")+"\n"+tabs+"} GroupBy {"+by+"}";
	}
	
}
