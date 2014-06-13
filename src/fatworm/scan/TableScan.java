package fatworm.scan;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import fatworm.driver.Attribute;
import fatworm.driver.Table;
import fatworm.driver.Tuple;
import fatworm.field.Field;
import fatworm.io.BlockManager;
import fatworm.io.FixBlockManager;
import fatworm.io.VarBlockManager;

public class TableScan extends Scan{
	Table table;
//	ListIterator<Tuple> iter;
	int nextIdx = 0;
	Integer size;
	
	boolean fix = true;
	BlockManager io = null;
	boolean update = false;
	
	private void loadFile() throws SQLException {
		while(io.hasNext()) {
			table.table.add(io.next(false));
		}
		table.readComplete = true;
	}
	public TableScan(Table table, boolean loadFile, boolean write) throws SQLException {
		this.table = table;
		// only temp table's schema could be null
		if (table.schema != null)
			fix = table.schema.fix();
//		System.out.println("[tableScan "+table.name+"]readComplete="+table.readComplete);
		if (table.readComplete) {
			size = table.size();
//			System.out.println("[tableScan "+table.name+"]init size="+size);
			if (write)
				if (fix)
					io = new FixBlockManager(table.file, "rw", table.schema);
				else io =  new VarBlockManager(table.file, write? "rw":"r", table.schema);
		} else {
			if (fix)
				io = new FixBlockManager(table.file,  write?"rw":"r", table.schema);
			else io = new VarBlockManager(table.file, write? "rw":"r", table.schema);
			if (loadFile) {
				table.table.clear();
				loadFile();
			}
		}
	}
	public TableScan(Table table) throws SQLException {
		this(table, false, false);
	}
	
	@Override
	public Tuple next(Tuple parent) throws SQLException {
		return next();
	}
	@Override
	public Tuple next() throws SQLException {
//		System.out.println("[tableScan "+table.name+"]nextIdx="+nextIdx+"readCompelete="+table.readComplete+"size="+size);
		if (table.readComplete) {
			if (!hasNext())
				return null;
			if (io!=null) io.next(table.readComplete);
			return table.table.get(nextIdx++);
		} else {
			if (!io.hasNext()) {
				table.readComplete = true;
				size = table.size();
				return null;
			}
			Tuple t = io.next(table.readComplete);
			if (!update && !fix)
				table.table.add(t);
//			System.out.println(t);
			return t;
		}
	}
	
	@Override
	public boolean hasNext(Tuple parent) {
		return hasNext();
	}
	@Override
	public boolean hasNext() {
		if (table.readComplete){
			if (nextIdx >= size)
				return false;
			return true;
		} else return io.hasNext();
	}

	@Override
	// only read queries will call this
	public void restart() throws SQLException {
		if (!table.readComplete) {
			loadFile();
			size = table.size();
			// since restart won't be called by insert into table (subquery), it's safe to set size in restart
		}
		nextIdx = 0;
	}
	
	public boolean clear() {
		nextIdx = -1;
		io.clear();
		return table.clear();
//		iter = table.table.iterator();
	
	}
	
	public boolean delete() throws SQLException {
		if (fix) {
			if (nextIdx < 0 || nextIdx > size) throw new SQLException("delete while iter == null");
			table.table.set(--nextIdx, table.table.get(size-1));
			table.table.remove(size-1);
		
//		table.table.remove(--nextIdx);
			--size;
		}
//		io.deleteIdx(nextIdx);
		io.delete();
		if (!fix) table.readComplete = false; //fix me if too slow
		return true;
	}
	
	public boolean update(Tuple t) throws SQLException {
		if (fix) {
			if (nextIdx < 0 || nextIdx > size) throw new SQLException("update while iter == null"); 
			//FIXME add this if need in the future
			table.table.set(nextIdx-1, t);
	//		iter.set(t);
	//		io.updateIdx(nextIdx-1,t);
		}
//		System.out.println(t);
		io.update(t);
		if (!fix) table.readComplete = false; // FIXME if too slow
		return true;
	}
	
	public boolean insert(Tuple t) throws SQLException {
		return insert(t, false);
	}
	/**
	 * 
	 * @param t
	 * @param temp whether it's a temp table, if so, insert => inc size
	 * @return
	 * @throws SQLException
	 */
	public boolean insert(Tuple t, boolean temp) throws SQLException {
//		System.out.println("insert ["+temp+"] "+t);
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
		table.table.add(t);
//		System.out.println("after adding "+t+":"+table);
		if (io != null)
			io.insert(t);
		if (temp) ++size;
		return true;
	}
	public void close() throws SQLException {
		if (io != null)
			io.close();
	}
	public void sort(ArrayList<Integer> by, ArrayList<Boolean> asc) {
		table.sort(by, asc, size);	// FIXME: if need to sort only the part [0,size), change it.
	}
	
	public String toString() {
		return "[TABLESCAN]\n"+table;
	}
	
}
