package fatworm.io;

import fatworm.driver.Schema;
import fatworm.driver.Tuple;
import fatworm.util.Constant;

import java.sql.SQLException;

abstract public class Block implements Comparable<Block>{
	public long blockStart;
	public byte[] bytes;
	protected int start;// different from blockStart, it's the start to dicern whether there's header in the front
	protected Schema schema;
	
	protected Block(long blockStart, byte[] bytes, int start, Schema schema) {
		this.blockStart = blockStart;
		this.bytes = bytes;
		this.start = start;
		this.schema = schema;
	}
	
//	public abstract void writeTuple(long addr, Tuple t) throws SQLException;
	
	public abstract boolean inBlock(long addr);
	public abstract Tuple getTuple(long addr) throws SQLException;
//	public abstract void delete(long addr, long next) throws SQLException;
	
	public long readLong(long addr) throws SQLException {
		int innerAddr = getAddr(addr);
		long ans = 0;
		long mask = ~0;
		for (int i = innerAddr+Constant.LONG_SIZE-1, j = 0; i >= innerAddr; --i, mask<<=Constant.BYTE_LEN, j+=Constant.BYTE_LEN) {
			ans = (ans & ~mask)|(bytes[i]<<j);
		}
		return ans;
	}
	public void writeLong(long addr, long num) throws SQLException {
		int innerAddr = getAddr(addr);
//		int mask = Constant.BYTE_LEN-1;
		for (int i = innerAddr+Constant.LONG_SIZE-1; i >= innerAddr; --i) {
			bytes[i] = (byte) num;
			num = num >> Constant.BYTE_LEN;
		}
	}
	
	protected int getAddr(long addr) throws SQLException {
		if (!inBlock(addr))
			throw new SQLException("addr "+addr+" not in Block."+blockStart);
		return (int) (addr & (Constant.BLOCK_SIZE-1));
	}
	
	public int compareTo(Block b) {
		return blockStart < b.blockStart? -1: (blockStart==b.blockStart? 0:1);
	}
}
