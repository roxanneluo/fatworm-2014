package fatworm.field;

import java.sql.SQLException;


public class NULL extends Field {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1696045312925700686L;
	private static NULL instance = null;
	private NULL() {
		type = java.sql.Types.NULL;
	}
	public static NULL getInstance() {
		if (instance == null)
			instance = new NULL();
		return instance;
	}
	
	public String toString() {
		return "NULL";
	}
	
	public String typeString() {
		return "NULL";
	}
	public String typeValString() {
		return typeString();
	}
	
	public boolean compatible(Field f) {
		return true;
	}
	
	public Field toMe(Field f) throws SQLException {
		return NULL.getInstance();
	}
	
	public int hashCode() {
		return 31;//FIXME
	}
	/**
	 * @return true if both are null
	 */
	public boolean equals(Object f) {
		return f instanceof NULL;
	}
	
	public int compareTo(Field f) {
		if (f instanceof NULL) return 0;
		return -1;
	}
	
	public Object getVal() {
		return null;
	}
}

