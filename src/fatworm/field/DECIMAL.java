package fatworm.field;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.SQLException;

import fatworm.util.Util;

public class DECIMAL extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5126147875170808141L;
//	public int headLen;
//	public int tailLen;
	public BigDecimal v = null;	//TODO: BigDecimal??
	
	public DECIMAL(BigDecimal v) {
		type = java.sql.Types.DECIMAL;
		this.v = v;
	}
	public DECIMAL(int precision, int scale) {
		type = java.sql.Types.DECIMAL;
//		System.out.println("prec:"+precision);
//		this.type = java.sql.Types.DECIMAL;
//		this.headLen = headLen;
//		this.tailLen = tailLen;
//		v = new BigDecimal(0,new MathContext(precision, RoundingMode.HALF_UP)).setScale(scale);
		v = new BigDecimal(0);
	}
	public DECIMAL(String x, int precision, int scale) {
		type = java.sql.Types.DECIMAL;
//		v = new BigDecimal(Util.trim(x), new MathContext(precision)).setScale(scale);
		v = new BigDecimal(Util.trim(x));
	}
	public DECIMAL(Integer v, int precision, int scale) {
		type = java.sql.Types.DECIMAL;
//		this.v = new BigDecimal(v, new MathContext(precision)).setScale(scale);
		this.v = new BigDecimal(v);
	}
	public DECIMAL(Float v, int precision, int scale) {
		type = java.sql.Types.DECIMAL;
//		this.v = new BigDecimal(v, new MathContext(precision)).setScale(scale);
		this.v = new BigDecimal(v);
	}
	public DECIMAL(Integer v) {
		type = java.sql.Types.DECIMAL;
//		this(headLen, tailLen);
//		this.v = new BigDecimal(v, new MathContext(headLen+tailLen)).setScale(tailLen);
		this.v = new BigDecimal(v);
	}
	
	public String toString() {
		return v.toString();
	}
	public Field toMe(Field f) throws SQLException {
		if (f instanceof NULL) return NULL.getInstance();
		if (f instanceof DECIMAL) {
			DECIMAL d = (DECIMAL)f;
//			d.v.setScale(v.scale());	//FIXME: if precision is needed
			return new DECIMAL(d.v);	//FIXME: if need clone
		}
		if (f instanceof FLOAT)  {
			DECIMAL ans = (DECIMAL) new DECIMAL((Float)f.getVal(), v.precision()-v.scale(), v.scale());
			return ans;
		}
		if (f instanceof INT) return new DECIMAL((Integer)f.getVal(), v.precision()-v.scale(), v.scale());
		throw new SQLException("[ERROR] converting "+f.typeValString()+" to "+this.typeValString());
	}
	
	public int hashCode() {
		return v.hashCode();
	}
	public boolean equals(Object f) {
		if (!(f instanceof DECIMAL)) return false;
		DECIMAL d = (DECIMAL)f;
		return d.v.equals(v);
	}
	
	public String typeString() {
		return "DECIMAL("+(v.precision()-v.scale())+","+v.scale()+")";
	}
	@Override
	public int compareTo(Field f) {
		DECIMAL df;
		try {
			df = (DECIMAL) this.toMe(f);
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
		return v.compareTo(df.v);
	}
	@Override
	public BigDecimal getVal() {
		return v;
	}
	@Override
	public String typeValString() {
		return new String(typeString()+"("+v+")");
	}
	@Override
	public boolean compatible(Field f) {
		if (f instanceof NULL) return true;
		if (f instanceof DECIMAL) return true;
		if (f instanceof FLOAT) return true;
		if (f instanceof INT) return true;
		return false;
	}
	
	
}
