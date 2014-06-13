package fatworm.driver;

import java.io.Serializable;

import fatworm.absyn.Expr;
import fatworm.field.Field;
import fatworm.field.INT;
import fatworm.field.NULL;
import fatworm.field.TIMESTAMP;

public class Attribute extends Expr implements Cloneable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7764911407031193184L;
	public String colName;
	public Integer idx;	// the idx th attribute in the table.
	// here I'm too lazy to build a class for each type,
	// since int cannot encapsulate information such as the len in char
	// I can't just use an int
	public Field type;
	public boolean notNull = false;
	public boolean autoInc = false/*, primaryKey*/;
	public INT cnt = new INT(0); // count of rows used as default for auto inc 
	public Field deft = null;	//deft for default
//	boolean hasIndex;

	public Object clone() {
		Attribute attr = null;
		try {
			attr = (Attribute) super.clone();
		} catch(Exception e) {
			e.printStackTrace();
		}
		attr.type = type != null? (Field) type.clone(): null;
		return attr;
	}
	
	public Field getDefault() {
//		System.out.println(this);
		if (deft != null)
			return deft;
		if (autoInc) {
			++cnt.v;
			return (Field) cnt.clone();
		} 
		if (type instanceof TIMESTAMP)
			return new TIMESTAMP();
		return NULL.getInstance();
	}
	
	public String toString() {
//		Field deft = getDefault();
		return colName+"("+type.typeString()+","+(notNull?"notNull,":"")+(autoInc? "autoInc,":"")+(deft!= null? deft:"")+")";
	}
}
