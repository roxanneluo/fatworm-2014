package fatworm.driver;

import java.util.ArrayList;

import fatworm.field.Field;
import fatworm.field.NULL;
import fatworm.util.Util;

public class Tuple implements Cloneable{
	public ArrayList<Field> fields = new ArrayList<Field>();
	
	public Tuple (ArrayList<Field> types) {
		fields = (ArrayList<Field>)Util.cloneFieldList(types);
	}
	public Tuple() {
		
	}
	public Tuple(Tuple t) {
		fields = (ArrayList<Field>) Util.cloneFieldList(t.fields);
	}
	public int size() {
		return fields.size();
	}
	public boolean initSize(int size) {
		if (!fields.isEmpty()) return false;
		for (int i = 0;i < size; ++i)
			fields.add(NULL.getInstance());
		return true;
	}
	public void set(int idx, Field f) {
		fields.set(idx, f);
	}
	public Field get(int idx) {
		return (Field) fields.get(idx).clone();
	}
	public void add(Field f) {
		fields.add(f);
	}
	public void addAll(Tuple t) {
		fields.addAll(t.fields);
	}
	
	public String toString() {
		String ans = "";
		for (Field field: fields)
			ans += field.toString()+"\t";
		return ans;
	}
	public boolean equals(Object t) {
		if (!(t instanceof Tuple)) return false;
		Tuple tt = (Tuple)t;
		if (tt.size() != size()) return false;
		for (int i = 0; i < fields.size(); ++i) {
			if (!get(i).equals(tt.get(i)))
				return false;
		}
		return true;
	}
	public int compareTo(Tuple t, ArrayList<Integer> by, ArrayList<Boolean> ascs) {
		int idx, ans;
		boolean asc;
		for (int i = 0; i < by.size(); ++i) {
			idx = by.get(i);
			asc = ascs.get(i);
			ans = get(idx).compareTo(t.get(idx));
			if (ans != 0)
				return asc? ans: -ans;
		}
		for (int i = 0; i < size(); ++i) {
			ans = get(i).compareTo(t.get(i));
			if (ans != 0)
				return ans;
		}
		return 0;
	}
	public int hashCode() {
		return fields.hashCode();
	}
	
}
