package fatworm.field;

import java.sql.SQLException;

public class BOOL extends Field{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8382200657473904799L;
	public Boolean v = null;

	public BOOL() {
		type = java.sql.Types.BOOLEAN;
		v = false;
	}
	public BOOL(String x) {
		this();
		this.v = x.equals("TRUE");
	}
	public BOOL(Boolean v) {
		this();
		this.v = v;
	}
	public BOOL(BOOL b) {
		this();
		v = new Boolean(b.v);
	}

	public String toString() {
		return v.toString();
	}
	
	public BOOL clone() {
		return new BOOL(v);
	}
	@Override
	public String typeString() {
		return "bool";
	}
	public String typeValString() {
		return typeString()+"("+v+")";
	}
	public boolean compatible(Field f) {
		if (f instanceof NULL) return true;
		if (f instanceof BOOL) return true;
		if (f instanceof INT) return true;
		if (f instanceof FLOAT) return true; //TODO: really compatible?
		return false;
	}
	public Field toMe(Field f) throws SQLException {
		if (f instanceof NULL) return NULL.getInstance();
		if (f instanceof BOOL) return f;
		if (f instanceof INT) return new BOOL(((INT)f).v != 0);
		if (f instanceof FLOAT) return new BOOL(((FLOAT)f).v != 0); //TODO: really compatible?
		throw new SQLException("convert "+f.typeValString()+"to BOOL");
	}
	
	public int hashCode() {
		return v.hashCode();
	}
	public boolean equals(Object f) {
		if (!(f instanceof BOOL)) return false;
		return ((BOOL)f).v.equals(v);
	}
	@Override
	public int compareTo(Field f) {
		BOOL b = (BOOL)f;
		return v.compareTo(b.v);
	}
	
	public Boolean getVal() {
		return v;
	}
}
