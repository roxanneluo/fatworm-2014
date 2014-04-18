package fatworm.logicplan;

import java.util.LinkedList;
import java.util.List;

import fatworm.absyn.Expr;

public class ProjectPlan extends UAlgebraPlan {
	public List<Expr> attributes;
	public ProjectPlan(Plan src, List<Expr> attributes, Plan parent) {
		super(src, parent);
		this.attributes = attributes;
	}
	public ProjectPlan(Plan src, List<Expr> attributes) {
		super(src);
		this.attributes = attributes;
	}
	
	public ProjectPlan(Plan src) {
		super(src);
		attributes = new LinkedList<Expr>();
	}
	public String getString(String tabs) {
		String ans = tabs+"ProjectPlan{\n"
					+src.getString(tabs+"\t")+"\n"+tabs+"} to {";
		for (Expr e: attributes)
			ans += e+",";
		return ans + "}";
	}
}
