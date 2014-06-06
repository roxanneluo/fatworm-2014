package fatworm.logicplan;

import java.util.LinkedList;

import fatworm.absyn.Expr;

public class CalculatorPlan extends Plan {
	public LinkedList<Expr> columns;
	public CalculatorPlan(LinkedList<Expr> attributes, Plan parent) {
		super(parent);
		this.columns = attributes;
	}
	public CalculatorPlan(LinkedList<Expr> attributes) {
		super(null);
		this.columns = attributes;
	}
	
	public String getString(String tabs) {
		String ans = tabs+"ProjectPlan{\n"+tabs+"\t";
		for (Expr e: columns)
			ans += e+",";
		return ans + "\n"+tabs+"}";
	}
}
