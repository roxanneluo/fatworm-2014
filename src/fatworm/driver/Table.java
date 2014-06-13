package fatworm.driver;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;

import fatworm.field.Field;

public class Table {
	public String name;
	public Schema schema;
	public ArrayList<Tuple> table = new ArrayList<Tuple>();	// TODO: actually this should be a table scan I think
	private Random rand;
	private static int tempNum = 0;
	public File file = null;
	public boolean readComplete;
	
	public Table() {
		name = new String("/temp"+(tempNum++));
		readComplete = true;
	}
	public Table(String name, Schema schema, File file) {
		this(name, schema);
		this.file = file;
		readComplete = false;
	}
	public Table(String name, Schema schema) {
		this.name = name.toLowerCase();
		this.schema = schema;
		readComplete = true;
	}
	
	public boolean clear() {
		table.clear();
		return true;
	}
	public boolean insert(Tuple value) throws SQLException {
		Tuple t = new Tuple();
		Field type;
		for (int i = 0; i < schema.types.size(); ++i) {
			type = schema.types.get(i);
			t.add(type.toMe(value.get(i)));
		}
		table.add(t);
		return true;
	}
	public void sort(ArrayList<Integer> by, ArrayList<Boolean> asc, int size) {
		rand = new Random();
		sort(0, size-1, by, asc);
	}

	public int size() {
		return table.size();
	}
	public String toString() {
		return getString("");
	}
	
	public String getString(String tabs) {
		String ans = tabs+"===========start[table] "+name+"============\n";
		ans += tabs+"\t|"+schema+"|\n";
		for(Tuple tuple: table)
			ans += tabs+"\t"+tuple.toString()+"\n";
		ans += tabs+"===========end[table] "+name+"============\n";
		return ans;
	}
//	private int divide(int l, int r, ArrayList<Integer> by, ArrayList<Boolean> ascs) {
//		Tuple x = table.get(rand.nextInt(r-l+1)+l);
//		Tuple temp;
//		while(l < r) {
//			while (l < r && table.get(l).compareTo(x, by, ascs) <= 0) ++l;
//			while (l < r && table.get(r).compareTo(x, by, ascs) > 0) --r;
//	
//			if (l >= r)
//				break;
//			temp = table.get(l); table.set(l, table.get(r)); table.set(r, temp);
//		}
//		table.set(l, x);
//		return r;
//	}
//	private void sort(int l, int r, ArrayList<Integer> by, ArrayList<Boolean> asc) {
//		if (l >= r)
//			return;
//		int x = divide(l, r, by, asc);
//		sort(l, x-1, by, asc);
//		sort(x+1, r, by, asc);
//	}
	
	private int divide(int l, int r, ArrayList<Integer> by, ArrayList<Boolean> ascs) {
		Tuple k = table.get(l);
		while (l < r) {
			while (l < r && table.get(r).compareTo(k, by, ascs) >= 0) --r;
			if (l < r) {table.set(l, table.get(r)); ++l;}
			while (l < r && table.get(l).compareTo(k, by, ascs) <= 0) ++l;
			if (l < r) {table.set(r, table.get(l)); --r;}
		}
		table.set(l, k);
		return l;
	}
	private void sort(int l, int r, ArrayList<Integer> by, ArrayList<Boolean> asc) {
		if (l >= r) return;
		int mid = divide(l,r, by, asc);
		sort(l, mid-1, by, asc);
		sort(mid+1, r, by, asc);
	}
}
