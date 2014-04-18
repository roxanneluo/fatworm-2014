package fatworm.absyn;

import fatworm.absyn.BExpr.BopType;
import fatworm.logicplan.Plan;

public class AnyAll extends Expr {
	public Expr expr;
	public BopType op;	//but can only be the comparative operators
	public boolean any;
	public Plan table;
	
	public AnyAll(Expr col, BopType op, boolean a, Plan t) {
		expr = col;
		this.op = op;
		any = a;
		table = t;
	}
	
	public String getString(String tabs) {
		String prevTabs = decTab(tabs);
		return "\n"+tabs+expr+" "+op+" "+(any? "ANY":"ALL")+" {\n"+table.getString(tabs+"\t")+"\n"+tabs+"}\n"+prevTabs;
	}
}
