package fatworm.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import fatworm.driver.Tuple;
import fatworm.util.Constant;

public abstract class BlockManager {
	protected File f;
	protected long lastTupleAddr;
	protected long curAddr;
	protected Buffer buffer;
	protected String mode;
	protected void readHeader() throws SQLException {
		try {
			Block first;
			first = buffer.getRead(0);
			lastTupleAddr = first.readLong(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void initHeader() {
		lastTupleAddr = -1;
	}
	
	public boolean hasNext() {
		return curAddr < lastTupleAddr;
	}
	abstract protected long nextAddr(long addr);
	abstract protected long prevAddr(long addr);
	public Tuple next(boolean readComplete) throws SQLException {
		try {
			if (!hasNext())
				return null;
//			System.out.println("last Addr="+curAddr);
			curAddr = nextAddr(curAddr);
//			System.out.println("cur addr = "+curAddr);
			if (!readComplete) {
				Block b = buffer.getRead(curAddr);
				return b.getTuple(curAddr);
			} return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	public void clear() {
		try {
			FileOutputStream out = new FileOutputStream(f);
			out.write(new byte[0]);
			out.close();
			initHeader();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void close() throws SQLException {
		try {
			if (mode == "r") return;
			output();
			buffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	protected void output() throws SQLException {
		Block start;
		try {
			start = buffer.getWrite(0);
			start.writeLong(0, lastTupleAddr);
//			buffer.output();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public abstract boolean update(long addr, Tuple t) throws SQLException;
	public void update(Tuple t) throws SQLException {
		update(curAddr, t);
	}
	
	public abstract void delete(long addr) throws SQLException;
	public void delete() throws SQLException {
		delete(curAddr);
		curAddr = prevAddr(curAddr);
	}
	
	abstract public void insert(Tuple t) throws SQLException;
}
