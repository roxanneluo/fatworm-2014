package fatworm.logicplan;

import java.util.LinkedList;

import fatworm.absyn.Column;
import fatworm.absyn.Expr;

public class UpdatePlan extends UAlgebraPlan {
	public LinkedList<Column> 	cols = new LinkedList<Column>();
	public LinkedList<Expr> 	vals = new LinkedList<Expr>();
	public Expr cond;
	public UpdatePlan(Plan src, Expr cond) {
		super(src);
		this.cond = cond;
	}

	@Override
	public String getString(String tabs) {
		// TODO Auto-generated method stub
		return null;
	}
}
