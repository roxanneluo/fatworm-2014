package fatworm.driver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import fatworm.absyn.Column;
import fatworm.field.Field;
import fatworm.util.Util;

public class Schema implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2072958665721679444L;
	/**
	 * for real table the attributes are just attributes
	 * however, as to the logical plan, all Attribute are replaced with Column
	 */
	public HashMap<String, Attribute> attributes = new HashMap<String,Attribute>(); //so far not used
	public ArrayList<Attribute> attrNames = new ArrayList<Attribute>();
	public ArrayList<Field> types = new ArrayList<Field>();
//	public ArrayList<String> primaryKeys = new ArrayList<String>();
	public String primaryKey = null; 

	public Integer getColIdx(Column col) {
		//TODO: doesn't check duplicate equals
		for (int i = 0; i < attrNames.size(); ++i) {
			if (attrNames.get(i).equals(col))
				return i;
		}
		return null;
	}
	
	public Column getColumn(Column col) {
		Column attr;
		for (int i = 0; i < attrNames.size(); ++i) {
			attr = (Column)attrNames.get(i);
			if (attr.equals(col))
				return attr;
		}
		return null;
	}
	public static Schema product(Schema left, Schema right,String alias/*, Scan leftScan, Scan rightScan*/) {
		Schema ans = left.clone(alias, false/*,leftScan*/);
		ans.types.addAll(Util.cloneFieldList(right.types));
		ArrayList<Attribute> cols = (ArrayList<Attribute>) Util.cloneColumnList(right.attrNames, alias/*, rightScan*/);
		for (int i = 0; i < cols.size(); ++i) {
			cols.get(i).idx = left.attrNames.size()+i;
		}
		ans.attrNames.addAll(cols);
		for (Attribute col:cols) {
			ans.attributes.put(col.colName, col);
		}
		return ans;
	}
	public boolean fix() {
		for (int i = 0; i < types.size(); ++i) {
			if (!types.get(i).fix())
				return false;
		}
		return true;
	}
	public int maxTupleSize() {
		int size=0;
		for (int i = 0; i < types.size(); ++i){
			size += types.get(i).maxSize();
		}
		return size;
	}
	public int fixSize() {
		int ans = 0;
		Field type;
		for (int i = 0 ;i < types.size(); ++i) {
			if ((type = types.get(i)).fix())
				ans += type.maxSize();
		}
		return ans;
	}
	public int varCnt() {
		int cnt = 0;
		for (int i = 0; i < types.size(); ++i) {
			if (!types.get(i).fix())
				++cnt;
		}
		return cnt;
	}
	public Schema clone(String alias) {
		return clone(alias, true);
	}
	public Schema clone(String alias, boolean cloneInvisible/*, Scan src*/) {
		Schema ans = new Schema();
		ans.primaryKey = primaryKey;
		ans.types = (ArrayList<Field>) Util.cloneFieldList(types);
		ans.attrNames = (ArrayList<Attribute>) Util.cloneColumnList(attrNames,alias, false/*,src*/);
//		for (Attribute attr:ans.attrNames) {
//			ans.attributes.put(attr.colName, attr);
//		}
		return ans;
	}
	
	public String toString() {
		String ans = "";
		for (int i = 0; i < attrNames.size(); ++i) {
			Attribute attr = attrNames.get(i);
			Field type = types.get(i);
			ans += attr+"("+type.typeString()+")"+"\t";
		}
		return ans;
	}
	
	/**
	 * 
	 * @param s
	 * @return whether the types are compatible (not necessary equal)
	 */
	public boolean compatible(Schema s) {
		if (s.types.size() != types.size())
			return false;
		
		for (int i = 0; i < types.size(); ++i) {
			if (!types.get(i).compatible(s.types.get(i)))
				return false;
		}
		return true;
	}
}
