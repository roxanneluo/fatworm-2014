package fatworm.absyn;

import fatworm.logicplan.Plan;

public class In extends Expr {
	public Expr expr;
	public Plan table;
	public In(Expr col, Plan t) {
		expr = col;
		table = t;
	}
	
	public String getString(String tabs) {
		String prevTabs = decTab(tabs);
		return "\n"+tabs+expr+" IN {\n"+table.getString(tabs+"\t")+"\n"+tabs+"}\n"+prevTabs;
	}
	
}
