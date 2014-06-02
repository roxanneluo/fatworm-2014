package fatworm.field;

import java.sql.SQLException;
import java.sql.Timestamp;

import fatworm.util.Util;

public class TIMESTAMP extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = 326560853055280090L;
	public java.sql.Timestamp v;
	
	public TIMESTAMP() {
		type = java.sql.Types.TIMESTAMP;
	}
	
	public Object clone() {
		TIMESTAMP d = null;
		try {
			d = (TIMESTAMP) super.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
		d.v = v != null?(Timestamp) v.clone():null;
		return d;
	}
	
	public TIMESTAMP(String x) {
		this();
		v = Util.parseTimestamp(Util.trim(x));
	}
	
	public String toString() {
		return v.toString();
	}
	
	public String typeString() {
		return "Timestamp";
	}
	
	public String typeValString() {
		return typeString()+"("+v+")";
	}
	
	public boolean compatible(Field f) {
		if (f instanceof NULL) return true;
		if (f instanceof TIMESTAMP) return true;
		return false;
	}
	
	public Field toMe(Field f) throws SQLException {
		if (f instanceof NULL) return NULL.getInstance();
		if (f instanceof TIMESTAMP) return f;
		throw new SQLException("[ERROR] converting "+f.typeValString()+" to "+this.typeValString());
	}
	
	public int hashCode() {
		return v.hashCode();
	}
	public boolean equals(Object f) {
		if (!(f instanceof TIMESTAMP)) return false;
		return ((TIMESTAMP)f).v.equals(v);
	}
	
	public int compareTo(Field f) {
		TIMESTAMP b = (TIMESTAMP)f;
		return v.compareTo(b.v);
	}
}
