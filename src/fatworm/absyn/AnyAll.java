package fatworm.absyn;

import fatworm.absyn.BExpr.BopType;
import fatworm.logicplan.Plan;

public class AnyAll extends BQExpr {
	public Column col;
	public BopType op;	//but can only be the comparative operators
	public boolean any;
	
	public AnyAll(Column col, BopType op, boolean a, Plan t) {
		super(t);
		this.col = col;
		this.op = op;
		any = a;
	}
	
	public String getString(String tabs) {
		String prevTabs = decTab(tabs);
		return "\n"+tabs+col+" "+op+" "+(any? "ANY":"ALL")+" {\n"+plan.getString(tabs+"\t")+"\n"+tabs+"}\n"+prevTabs;
	}
}
