package fatworm.field;

import java.sql.SQLException;

import fatworm.util.Util;

public class CHAR extends Field{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8167034774416008528L;
	public String v = null;
	public int len;
	
	public CHAR(int len) {
		type = java.sql.Types.CHAR;
		this.len = len;
//		v = "";
	}
	
	public CHAR(String v, int len) {
		this(len);
		this.v = v != null? Util.trim(v): null;	//TODO trim it?
	}
	public String toString() {
		return v;
	}
	
	public CHAR clone() {
		return new CHAR(this.v, this.len);
	}

	@Override
	public String typeString() {
		return "Char("+len+")";
	}
	
	public String typeValString() {
		return typeString()+"("+v+")";
	}
	
	public boolean compatible(Field f) {
		if (f instanceof NULL) return true;
		if (f instanceof CHAR || f instanceof VARCHAR)
			return true;
		return false;
	}
	
	public Field toMe(Field f) throws SQLException {
		if (f instanceof NULL) return NULL.getInstance();
		if (f instanceof CHAR) {
			CHAR ch = (CHAR)f;
			CHAR ans = this.clone();
			ans.v = ch.v; //TODO: add clone if needed in the future
//			System.out.println(f.typeValString()+" tome:"+ans);
			return ans;
		}
		throw new SQLException("[ERROR] converting "+f.typeValString()+" to "+this.typeValString());
	}
	
	public int hashCode() {
		return v.hashCode()*31+len;
	}
	public boolean equals(Object f) {
		if (!(f instanceof CHAR)) return false;
		CHAR c = (CHAR)f;
		if (len != c.len) return false;
		return c.v.equals(v);
	}
	
	public int compareTo(Field f) {
		CHAR b = (CHAR)f;
		return v.compareTo(b.v);
	}
	public String getVal() {
		return v;
	}
}
