package fatworm.field;

import java.io.Serializable;
import java.sql.SQLException;

import fatworm.absyn.Expr;

public abstract class Field extends Expr implements Serializable, Cloneable, Comparable<Field> {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8141698977631590977L;
	public int type;	
	
	public static Field getField(Field type, String x) {
		switch(type.type) {
		case java.sql.Types.INTEGER:
			return new INT(x);
		case java.sql.Types.FLOAT:
			return new FLOAT(x);
		case java.sql.Types.CHAR:
			return new CHAR(x, ((CHAR)type).len);
		case java.sql.Types.DATE:
			return new DATE(x);
		case java.sql.Types.BOOLEAN:
			return new BOOL(x);
		case java.sql.Types.DECIMAL:
			return new DECIMAL(x, ((DECIMAL)type).headLen,((DECIMAL)type).tailLen);
		case java.sql.Types.TIMESTAMP:
			return new TIMESTAMP(x);
		case java.sql.Types.VARCHAR:
			return new VARCHAR(x, ((VARCHAR)type).len);
		default:
			System.out.println("[ERR] unknown type "+type);
			return null;
		}
	}
	public Field convert(Field from, Field to) throws SQLException {
		return to.toMe(from);
	}
	public boolean isNull() {
		return this instanceof NULL;
	}
	public Object clone() {
		try {
			return super.clone();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public abstract String typeString();
	public abstract String typeValString();
	/**
	 * 
	 * @param f
	 * @return whether the fields (i.e. the types of the fields) are compatible
	 * 			so far the rule is determined by me.
	 */
	public abstract boolean compatible(Field f);
	/**
	 * convert the Field of f to my type
	 * @param f
	 * @return field of my type or null if incompatible
	 */
	public abstract Field toMe(Field f) throws SQLException;
	
}
