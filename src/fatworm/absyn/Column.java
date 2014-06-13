package fatworm.absyn;

import fatworm.driver.Attribute;
import fatworm.driver.Table;
import fatworm.field.Field;
import fatworm.field.INT;

public class Column extends Attribute {
	public String tableName = null;
	public boolean parent = false; //denote whether the column refers to a tuple from the parent or below
	// annotations:annotated when attempting to compute schema and generate scan
//	public Scan table = null;
//	so far I think table is useless FIXME
	
	public Column(String tableName, String colName) {
		this.tableName = tableName == null? null:tableName.toLowerCase();
		this.colName = colName == null? null:colName.toLowerCase();
		idx = null;
		type = null;
	}
	public Column(String colName) {
		this(null, colName);
	}
	public Column(Attribute attr, Table table2) {
		colName = new String(attr.colName).toLowerCase();
		idx = new Integer(attr.idx);
		type = (Field) attr.type.clone();
		notNull = attr.notNull;
		autoInc = attr.autoInc;
		deft = attr.deft != null? (Field) attr.deft.clone(): null;
		cnt = attr.cnt;
	}
	public Column clone() {
		Column col = null;
		try{
			col = (Column)super.clone();
		} catch(Exception e) {
			e.printStackTrace();
		}
		col.tableName = this.tableName != null? new String(this.tableName): null;
		col.idx = idx == null? null:new Integer(idx);
//		col.table = this.table;
//		col.value = (Field)value.clone();
		return col;
	}
	public String toString() {
		return (tableName != null? tableName+"."+colName: colName)+"("+idx+")";
	}
	
	public int hashCode() {
		return colName.hashCode();
	}
	
	public boolean equals(Object b) {
		if (!(b instanceof Column)) return false;
		Column col = (Column)b;
		if (col.colName == null || colName == null)
			return false;
		if (col.tableName == null || this.tableName ==null)
			return col.colName.equals(colName);
		else 
			return col.tableName.equals(this.tableName) && col.colName.equals(colName);
	}
}
