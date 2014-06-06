package fatworm.field;

import java.sql.SQLException;

public class INT extends Field{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7226384986868514746L;
	public Integer v = null;
	public INT() {
		this.type = java.sql.Types.INTEGER;
		v = new Integer(0);
	}
	public INT(String v) {
		this();
		this.v = Integer.parseInt(v);
	}
	public INT(Float d) {
		this();
		v = d.intValue();
	}
	public INT(Integer v) {
		this();
		this.v = v;
	}
	public INT(int i) {
		this();
		v = i;
	}
	
	public String toString() {
		return v==null?"null":v.toString();
	}
	
	public String typeString() {
		return "Int";
	}
	public String typeValString() {
		return typeString()+"("+v+")";
	}
	
	public boolean compatible(Field f) {
		if (f instanceof NULL) return true;
		return f instanceof FLOAT || f instanceof INT;
	}
	public static Field toInt(Field f) throws SQLException {
		if (f instanceof NULL) return NULL.getInstance();
		if (f instanceof INT) return f;
		if (f instanceof FLOAT) return new INT(((FLOAT)f).v.intValue());
		return null;
	}
	
	public Field toMe(Field f) throws SQLException {
		if (f instanceof NULL) return NULL.getInstance();
		if (f instanceof INT) return f;
		if (f instanceof FLOAT) return new INT(((FLOAT)f).v.intValue());
		throw new SQLException("[ERROR] converting "+f.typeValString()+" to "+this.typeValString());
	}
	public int hashCode() {
		return v.hashCode();
	}
	public boolean equals(Object f) {
		if (!(f instanceof INT)) return false;
		return ((INT)f).v.equals(v);
	}
	
	public int compareTo(Field f) {
		INT b = (INT)f;
		return v.compareTo(b.v);
	}
	
	public Integer getVal() {
		return v;
	}
}
