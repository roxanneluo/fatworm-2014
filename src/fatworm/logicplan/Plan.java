package fatworm.logicplan;

public abstract class Plan {
	public Plan parent = null;
	public Plan(Plan parent) {
		this.parent = parent;
	}
	public Plan() {
		parent = null;
	}
	public String toString() {
		return getString("");
	}
	
	public abstract String getString(String tabs);
	
	static String getTabs(int tab) {
		String tabs = "";
		for (int i= 0; i< tab; ++i)
			tabs += "\t";
		return tabs;
	}
}
