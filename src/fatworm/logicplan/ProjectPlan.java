package fatworm.logicplan;

import java.util.LinkedList;
import java.util.List;

import fatworm.absyn.Expr;

public class ProjectPlan extends UAlgebraPlan {
	public LinkedList<Expr> columns;
	public ProjectPlan(Plan src, LinkedList<Expr> attributes, Plan parent) {
		super(src, parent);
		this.columns = attributes;
	}
	public ProjectPlan(Plan src, LinkedList<Expr> attributes) {
		super(src);
		this.columns = attributes;
	}
	
	public ProjectPlan(Plan src) {
		super(src);
		columns = new LinkedList<Expr>();
	}
	public String getString(String tabs) {
		String ans = tabs+"ProjectPlan{\n"
					+src.getString(tabs+"\t")+"\n"+tabs+"} to {";
		for (Expr e: columns)
			ans += e+",";
		return ans + "}";
	}
}
