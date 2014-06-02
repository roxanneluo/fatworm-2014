package fatworm.absyn;

import fatworm.logicplan.Plan;
import fatworm.scan.Scan;

public class Exists extends BQExpr {
	public boolean exists;	// true if exists, false if not exists
	
	public Exists(boolean exists, Plan plan) {
		super(plan);
		this.exists = exists;
	}
	
	public String getString(String tabs) {
		String prevTabs = decTab(tabs);
		return "\n"+tabs+(exists? "EXISTS":"NOT_EXISTS")+ "{\n"+plan.getString(tabs+"\t")+"\n"+tabs+"}\n"+prevTabs;
	}
}
