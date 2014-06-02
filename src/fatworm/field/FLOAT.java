package fatworm.field;

import java.sql.SQLException;

import fatworm.util.Util;

public class FLOAT extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7263412675509459725L;
	public Float v = null;
	
	public FLOAT() {
		type = java.sql.Types.FLOAT;
		v = new Float(0);
	}
	public FLOAT(String x) {
		this();
		v = Float.parseFloat(Util.trim(x));
	}
	
	public FLOAT(double f) {
		this();
		v = new Float(f);
	}
	public String toString() {
		return v.toString();
	}
	public String typeString() {
		return "Float";
	}
	public String typeValString() {
		return typeString()+"("+v+")";
	}
	public boolean compatible(Field f) {
		if (f instanceof NULL) return true;
		return f instanceof FLOAT || f instanceof INT;
	}
	public Field toMe(Field f) throws SQLException {
		if (f instanceof NULL) return NULL.getInstance();
		if (f instanceof DECIMAL) return new FLOAT(((DECIMAL)f).v); 
		if (f instanceof FLOAT) return f;
		if (f instanceof INT) return new FLOAT(((INT)f).v);
		throw new SQLException("[ERROR] converting "+f.typeValString()+" to "+this.typeValString());
	}
	public static Field toFloat(Field f) throws SQLException {
		if (f instanceof NULL) return NULL.getInstance();
		if (f instanceof DECIMAL) return new FLOAT(((DECIMAL)f).v); 
		if (f instanceof FLOAT) return f;
		if (f instanceof INT) return new FLOAT(((INT)f).v);
		throw new SQLException("[ERROR] converting "+f.typeValString()+" to Float");
	}
	
	public int hashCode() {
		return v.hashCode();
	}
	public boolean equals(Object f) {
		if (!(f instanceof FLOAT)) return false;
		return ((FLOAT)f).v.equals(v);
	}
	
	public int compareTo(Field f) {
		FLOAT b = (FLOAT)f;
		return v.compareTo(b.v);
	}
}
