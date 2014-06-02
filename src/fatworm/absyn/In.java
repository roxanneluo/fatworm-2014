package fatworm.absyn;

import fatworm.logicplan.Plan;

public class In extends BQExpr {
	public Column col;
	public In(Column col, Plan t) {
		super(t);
		this.col = col;
	}
	
	public String getString(String tabs) {
		String prevTabs = decTab(tabs);
		return "\n"+tabs+col+" IN {\n"+plan.getString(tabs+"\t")+"\n"+tabs+"}\n"+prevTabs;
	}
	
}
