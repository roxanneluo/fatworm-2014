package fatworm.field;

import java.sql.SQLException;

import fatworm.util.Util;

public class VARCHAR extends CHAR {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4284120208202982412L;
	public int len;
	
	public VARCHAR(String v) {
		this(v.length());
		this.v = Util.trim(v);
	}
	public VARCHAR(int len) {
		super(len);
		this.type = java.sql.Types.VARCHAR;
	}
	
	public VARCHAR(String v, int len) {
		this(len);
		this.v = v != null? Util.trim(v):null;
	}
	
	public String toString() {
		return v;
	}
	
	public String typeString() {
		return "Varchar("+len+")";
	}
	
	public String typeValString() {
		return typeString()+"("+v+")";
	}
	
	public boolean compatible(Field f) {
		if (f instanceof NULL) return true;
		if (f instanceof CHAR || f instanceof VARCHAR) return true;
		return false;
	}
	public VARCHAR clone() {
		return new VARCHAR(v, len);
	}
	public Field toMe(Field f) throws SQLException {
		if (f instanceof NULL) return NULL.getInstance();
		if (f instanceof CHAR) {
			VARCHAR ans = (VARCHAR) this.clone();
			ans.v = ((CHAR)f).v;
			return ans;
		}
		throw new SQLException("[ERROR] converting "+f.typeValString()+" to "+this.typeValString());
	}
	public int hashCode() {
		return v.hashCode()*31+len;
	}
	public boolean equals(Object f) {
		if (!(f instanceof VARCHAR)) return false;
		VARCHAR d = (VARCHAR)f;
		if (d.len != len) return false;
		return d.v.equals(v);
	}
	
	public int compareTo(Field f) {
		VARCHAR b = (VARCHAR)f;
		return v.compareTo(b.v);
	}
}
