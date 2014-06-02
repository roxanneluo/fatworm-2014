package fatworm.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fatworm.absyn.Column;
import fatworm.absyn.InvisibleFunc;
import fatworm.driver.Attribute;
import fatworm.field.Field;
import fatworm.scan.Scan;

public class Util {
	public static String trim(String s){
		if(((s.startsWith("'") && s.endsWith("'"))||(s.startsWith("\"") && s.endsWith("\""))) && s.length() >= 2)
			return s.substring(1,s.length()-1);
		else 
			return s;
	}
	
	public static java.sql.Timestamp parseTimestamp(String x) {
		try{
			return new java.sql.Timestamp(Long.valueOf(x));
		} catch (NumberFormatException e){
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			try {
				return new java.sql.Timestamp(format.parse(x).getTime());
			} catch (ParseException ee) {
				ee.printStackTrace();
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<Field> cloneFieldList(List<Field> list) {
		List<Field> ans = null;
		if (list instanceof ArrayList<?>)
			ans = new ArrayList<Field>();
		else ans = new LinkedList<Field>();
		for (Field f:list) {
			ans.add((Field)f.clone());
		}
		return ans;
	}
	
	public static List<Attribute> cloneAttributeList(List<Attribute> list) {
		List<Attribute> ans = null;
		if (list instanceof ArrayList<?>)
			ans = new ArrayList<Attribute>();
		else ans = new LinkedList<Attribute>();
		for(Attribute attr: list) {
			ans.add((Attribute) attr.clone());
		}
		return ans;
	}
	
	public static List<Attribute> cloneColumnList(List<Attribute> list, String alias/*, Scan src*/) {
		return cloneColumnList(list, alias, true);
	}
	public static List<Attribute> cloneColumnList(List<Attribute> list, String alias, boolean cloneInvisible/*, Scan src*/) {
		List<Attribute> ans = null;
		if (list instanceof ArrayList<?>)
			ans = new ArrayList<Attribute>();
		else ans = new LinkedList<Attribute>();
		Column col = null;
		for(Attribute attr: list) {
			if (attr instanceof InvisibleFunc && !cloneInvisible)
				continue;
			col = (Column) ((Column)attr).clone();
			col.tableName = alias != null? alias: col.tableName;
//			col.table = src;
			ans.add(col);
		}
		return ans;
	}
//	public static <T> List<T> cloneList(List<T> list) {
//		List<T> ans = null;
//		if (list instanceof ArrayList<?>)
//			ans = new ArrayList<T>();
//		else ans = new LinkedList<T>();
//		T first = list.get(0);
//		if (first instanceof Field)
//			for(Field f: (List<Field>)list) {
//				ans.add((T) f.clone());
//			}
//		else if (first instanceof Attribute)
//			for(Attribute attr: (List<Attribute>)list) {
//				ans.add((T) (T)attr.clone());
//			}
//		return ans;
//	}
	
}
