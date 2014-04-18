package fatworm.absyn;

import fatworm.logicplan.Plan;

public class Exists extends Expr {
	public boolean exists;	// true if exists, false if not exists
	public Plan table;
	
	public Exists(boolean exists, Plan table) {
		this.exists = exists;
		this.table = table;
	}
	
	public String getString(String tabs) {
		String prevTabs = decTab(tabs);
		return "\n"+tabs+(exists? "EXISTS":"NOT_EXISTS")+ "{\n"+table.getString(tabs+"\t")+"\n"+tabs+"}\n"+prevTabs;
	}
}
