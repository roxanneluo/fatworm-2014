package fatworm.absyn;

import fatworm.field.Field;

public abstract class Expr {
	// annotation: annotated when evaluating the value; 
	// field contains the information of both value and type,
	// so there's no need to have member type
//	so far value is useless
//	public Field value = null;
	
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
