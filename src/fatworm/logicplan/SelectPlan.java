package fatworm.logicplan;

import fatworm.absyn.Expr;

public class SelectPlan extends UAlgebraPlan {
	public Expr condition;

	public SelectPlan(Plan src, Expr cond, Plan parent) {
		super(src, parent);
		condition = cond;
	}
	public SelectPlan(Plan src, Expr cond) {
		super(src);
		condition = cond;
	}
	public String getString(String tabs) {
		return tabs+"SelectPlan{\n"
				+src.getString(tabs+"\t")+"\n"+tabs+"} WHERE {"+condition.getString(tabs+"\t")+"}";
	}
}
