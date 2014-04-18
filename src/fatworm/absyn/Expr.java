package fatworm.absyn;

public abstract class Expr {
	public String getString(String tabs) {
		if (!(this instanceof AnyAll || this instanceof In || this instanceof Exists))
			return this.toString();
		return null;
	}
	
	public String toString() {
		return getString("");
	}
	
	// decerement tabs, i.e. decrease tabs by one.
	public String decTab(String tabs) {
		return tabs.length()>1? tabs.substring(0, tabs.length()-1):"";
	}
}
