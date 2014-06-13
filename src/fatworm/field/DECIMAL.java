package fatworm.field;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.SQLException;

import fatworm.util.Constant;
import fatworm.util.Util;

public class DECIMAL extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5126147875170808141L;
	public Integer bytesNum = null;
	public int precision;
	public BigDecimal v = null;	//TODO: BigDecimal??
	
	public DECIMAL(BigDecimal v, int precision) {
		type = java.sql.Types.DECIMAL;
		this.v = v;
		this.precision = precision;
	}
	public DECIMAL(int precision) {
		type = java.sql.Types.DECIMAL;
//		System.out.println("prec:"+precision);
//		this.type = java.sql.Types.DECIMAL;
//		this.headLen = headLen;
//		this.tailLen = tailLen;
//		v = new BigDecimal(0,new MathContext(precision, RoundingMode.HALF_UP)).setScale(scale);
		this.precision = precision;
		v = new BigDecimal(0);
	}
	public DECIMAL(String x, int precision) {
		this(precision);
//		v = new BigDecimal(Util.trim(x), new MathContext(precision)).setScale(scale);
		v = new BigDecimal(Util.trim(x));
	}
	public DECIMAL(Integer v, int precision) {
		this(precision);
//		this.v = new BigDecimal(v, new MathContext(precision)).setScale(scale);
		this.v = new BigDecimal(v);
	}
	public DECIMAL(Float v, int precision) {
		this(precision);
//		this.v = new BigDecimal(v, new MathContext(precision)).setScale(scale);
		this.v = new BigDecimal(v);
	}
//	public DECIMAL(Integer v) {
//		this(precision, scale);
////		this(headLen, tailLen);
////		this.v = new BigDecimal(v, new MathContext(headLen+tailLen)).setScale(tailLen);
//		this.v = new BigDecimal(v);
//	}
	
	public String toString() {
		return v.toString();
	}
	public Field toMe(Field f) throws SQLException {
		if (f instanceof NULL) return NULL.getInstance();
		if (f instanceof DECIMAL) {
			DECIMAL d = (DECIMAL)f;
//			d.v.setScale(v.scale());	//FIXME: if precision is needed
			return new DECIMAL(d.v, precision);	//FIXME: if need clone
		}
		if (f instanceof FLOAT)  {
			DECIMAL ans = (DECIMAL) new DECIMAL((Float)f.getVal(),precision);
			return ans;
		}
		if (f instanceof INT) return new DECIMAL((Integer)f.getVal(), precision);
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
		return "DECIMAL("+precision+")";
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
	@Override
	public Field bytes2Field(byte[] bytes, int start, int l) {
		byte[] s = new byte[Constant.INT_SIZE], num = new byte[maxSize()-Constant.INT_SIZE];
		for (int i = 0; i < Constant.INT_SIZE; ++i)
			s[i] = bytes[start++];
		for (int i = Constant.INT_SIZE; i < maxSize(); ++i) 
			num[i-Constant.INT_SIZE] = bytes[start++];
		int scale = Util.bytes2int(s);
		return new DECIMAL(new BigDecimal(new BigInteger(num),scale),precision);
	}
	@Override
	public int maxSize() {
		if (bytesNum != null) return bytesNum;
		return bytesNum= (int) Math.ceil(Math.ceil(((double)precision)/3)*10/Constant.BYTE_LEN)+Constant.INT_SIZE;
	}
	@Override
	public byte[] toBytes() {
//		byte[] prec = Util.int2bytes(precision);
		byte[] bytes = new byte[maxSize()], s = Util.int2bytes(v.scale());
		for (int i = 0 ; i < Constant.INT_SIZE; ++i) {
			bytes[i] = s[i];
		}
		byte[] a = v.unscaledValue().toByteArray();
		for (int i = 0;i < a.length; ++i) {
			bytes[bytesNum-i-1] = a[a.length-i-1];
		}
		if (v.signum() < 0) {
			for (int i = Constant.INT_SIZE; i < bytesNum-a.length; ++i) {
				bytes[i] = (byte) Constant.BYTE_LEN-1;
			}
		}
		return bytes;
	}
	@Override
	public boolean fix() {
		return true;
	}
	
	
}
