package fatworm.field;

import java.sql.SQLException;
import java.sql.Timestamp;

import fatworm.util.Util;


public class DATE extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5093950036525951844L;
	public java.sql.Timestamp v;
	
	public DATE() {
		type = java.sql.Types.TIMESTAMP;
	}
	
	public DATE clone() {
		DATE d = null;
		try {
			d = (DATE) super.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
		d.v = v != null? (Timestamp) v.clone():null;
		return d;
	}
	public DATE(String x) {
		this();
		v = Util.parseTimestamp(Util.trim(x));
	}
	
	public String toString() {
		return v!= null? v.toString():"null";
	}
	
	public String typeString() {
		return "Date";
	}
	public String typeValString() {
		return typeString()+"("+v+")";
	}
	
	public boolean compatible(Field f) {
		if (f instanceof NULL) return true;
		if (f instanceof VARCHAR) return true;
		return f instanceof DATE; // FIXME is TIMESTAMP considered compatible
	}
	
	public static Field toDate(Field f) throws SQLException {
		if (f instanceof NULL) return NULL.getInstance();
		if (f instanceof DATE) return f;
		if (f instanceof CHAR) //allow varchar
			return new DATE(((CHAR) f).v);
		throw new SQLException("[ERROR] converting "+f.typeValString()+" to DATE");
	}
	
	public Field toMe(Field f) throws SQLException {
		if (f instanceof NULL) return NULL.getInstance();
		if (f instanceof DATE) return f;
		if (f instanceof CHAR) //allow varchar
			return new DATE(((CHAR) f).v);
		throw new SQLException("[ERROR] converting "+f.typeValString()+" to "+this.typeValString());
	}
	public int hashCode() {
		return v.hashCode();
	}
	public boolean equals(Object f) {
		if (!(f instanceof DATE)) return false;
		return ((DATE)f).v.equals(v);
	}
	
	public int compareTo(Field f) {
		DATE d;
		if (f instanceof CHAR)
			d = new DATE(((CHAR) f).v);
		else d= (DATE)f;
		return v.compareTo(d.v);
	}

	@Override
	public Timestamp getVal() {
		return v;
	}
}
