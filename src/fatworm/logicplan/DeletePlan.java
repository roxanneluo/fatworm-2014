package fatworm.logicplan;

import fatworm.absyn.Expr;

public class DeletePlan extends UAlgebraPlan{
	public Expr cond;
	public DeletePlan(Plan src, Expr cond) {
		super(src);
		this.cond = cond;
	}
	@Override
	public String getString(String tabs) {
		// TODO Auto-generated method stub
		return null;
	}
	
}