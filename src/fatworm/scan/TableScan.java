package fatworm.scan;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import fatworm.absyn.Column;
import fatworm.driver.Attribute;
import fatworm.driver.Table;
import fatworm.driver.Tuple;
import fatworm.field.Field;

public class TableScan extends Scan{
	Table table;
	ListIterator<Tuple> iter;
	public TableScan(Table table) {
		this.table = table;
		iter = table.table.listIterator();
	}
	
	@Override
	public Tuple next(Tuple parent) {
		return next();
	}
	@Override
	public Tuple next() {
		if (!hasNext())
			return null;
		return iter.next();
	}
	
	@Override
	public boolean hasNext(Tuple parent) {
		return hasNext();
	}
	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public void restart() {
		iter = table.table.listIterator();
	}
	
	public boolean clear() {
		iter = null;
		return table.clear();
//		iter = table.table.iterator();
	
	}
	
	public boolean delete() throws SQLException {
		if (iter == null) throw new SQLException("delete while iter == null");
		iter.remove();
		return true;
	}
	
	public boolean update(Tuple t) throws SQLException {
//		if (iter == null) throw new SQLException("update while iter == null"); 
		//FIXME add this if need in the future
		iter.set(t);
		return true;
	}
	
	public boolean insert(Tuple t) throws SQLException {
		Attribute attr;
		if (table.schema != null) {
			for (int i = 0; i < table.schema.attrNames.size(); ++i) {
				attr = table.schema.attrNames.get(i);
				if (t.get(i).isNull()) {
					Field deft = attr.getDefault();
	//				System.out.println(attr+"'s deft = "+deft);
					if (deft != null)
						t.set(i, deft);
					else if (attr.notNull)
						throw new SQLException(attr+" should be not null");
				}
			}
		}
		iter.add(t);
//		System.out.println("after adding "+t+":"+table);
		return true;
	}
	public void sort(ArrayList<Integer> by, ArrayList<Boolean> asc) {
		table.sort(by, asc);
	}
	public int size() {
		return table.size();
	}
	
	public String toString() {
		return "[TABLESCAN]\n"+table;
	}
}
