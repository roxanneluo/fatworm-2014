package fatworm.field;

import java.sql.SQLException;

import fatworm.util.Util;

public class DECIMAL extends FLOAT {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5126147875170808141L;
	public int headLen;
	public int tailLen;
	public Float v = null;	//TODO: BigDecimal??
	
	
	public DECIMAL(int headLen, int tailLen) {
		this.type = java.sql.Types.DECIMAL;
		this.headLen = headLen;
		this.tailLen = tailLen;
		v = new Float(0);
	}
	public DECIMAL(String x, int headLen, int tailLen) {
		this(headLen, tailLen);
		v = Float.parseFloat(Util.trim(x));
	}
	
	public DECIMAL(Float v, int headLen, int tailLen) {
		this(headLen, tailLen);
		this.v = v;
	}
	public String toString() {
		return v.toString();
	}
	public Field toMe(Field f) throws SQLException {
		if (f instanceof NULL) return NULL.getInstance();
		if (f instanceof DECIMAL || f instanceof FLOAT)  {
			DECIMAL ans = (DECIMAL) this.clone();
			ans.v = ((FLOAT)f).v;
			return ans;
		}
		if (f instanceof INT) return new FLOAT(((INT)f).v);
		throw new SQLException("[ERROR] converting "+f.typeValString()+" to "+this.typeValString());
	}
	
	public int hashCode() {
		return (v.hashCode()*31+headLen)*31+tailLen;
	}
	public boolean equals(Object f) {
		if (!(f instanceof DECIMAL)) return false;
		DECIMAL d = (DECIMAL)f;
		if ((headLen != d.headLen) || (tailLen != d.tailLen))
			return false;
		return d.v.equals(v);
	}
}
