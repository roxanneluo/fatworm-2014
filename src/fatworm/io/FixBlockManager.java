package fatworm.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.SQLException;

import fatworm.driver.Schema;
import fatworm.driver.Tuple;
import fatworm.util.Constant;
import fatworm.util.Util;

public class FixBlockManager extends BlockManager {
	private long curAddr = -1;
	
	private int lastInBlock;
	private int lastInFirstBlock;
	private int tupleSize;
	private Schema schema;
	public static final int HEADERSIZE = Constant.LONG_SIZE;
	
	public FixBlockManager(File f, String mode, Schema schema) throws SQLException {
		try {
			this.mode = mode;
			this.f = f;
			buffer = new Buffer(new RandomAccessFile(f, mode), schema);
			readHeader();
			tupleSize = schema.maxTupleSize()+Util.getNullMapSize(schema);
			lastInBlock = Constant.BLOCK_SIZE-Constant.BLOCK_SIZE % tupleSize-tupleSize;
			lastInFirstBlock = Constant.BLOCK_SIZE-(Constant.BLOCK_SIZE-HEADERSIZE)%tupleSize-tupleSize;
//			System.out.println("lastTupleAddr = "+lastTupleAddr);
			this.schema = schema;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void insert(Tuple t) throws SQLException {
		int oldLastBlockIdx = (int)lastTupleAddr >> Constant.BLOCK_SHIFT;
		lastTupleAddr = nextAddr(lastTupleAddr);
		if ((lastTupleAddr>>Constant.BLOCK_SHIFT) != oldLastBlockIdx) {
			buffer.addWrite(new FixBlock(lastTupleAddr & ~(Constant.BLOCK_SIZE-1), tupleSize, schema));
		}
		insert(lastTupleAddr, t);
	}
	private void insert(long addr, Tuple t) throws SQLException {
		try {
			FixBlock b = (FixBlock) buffer.getWrite(addr);
			b.writeTuple(addr, t);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected long nextAddr(long addr) {
		if (addr < HEADERSIZE)
			return HEADERSIZE;
		
		int last = (addr>>Constant.BLOCK_SHIFT) == 0? lastInFirstBlock: lastInBlock;
		if ((addr & (Constant.BLOCK_SIZE-1)) >= last)
			return (addr & ~(Constant.BLOCK_SIZE-1))+Constant.BLOCK_SIZE;
		return addr + tupleSize;
	}

	public int getStart(long addr) {
		return (curAddr>>Constant.BLOCK_SHIFT)==0? HEADERSIZE: 0;
	}
	
	private void checkAddr(long addr) throws SQLException {
		if (addr < HEADERSIZE || addr > lastTupleAddr)
			throw new SQLException("trying to update addr:"+addr+"while file"
					+ "is from "+HEADERSIZE+" to "+lastTupleAddr);
	}
	public boolean update(long addr, Tuple t) throws SQLException {
		checkAddr(addr);
		try {
			//actually if I'm right, I don't need to know the start;
			FixBlock b = (FixBlock) buffer.getWrite(addr);
			b.writeTuple(addr,t);
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	
	private long idx2Addr(int idx) {
		int first = (Constant.BLOCK_SIZE-HEADERSIZE)/tupleSize;
		if (idx < first) return HEADERSIZE+idx*tupleSize;
		int n = Constant.BLOCK_SIZE/tupleSize;
		return ((idx-first)/n+1)*Constant.BLOCK_SIZE+(idx-first)%n*tupleSize;
	}
	public void updateIdx(int idx, Tuple t) throws SQLException {
		update(idx2Addr(idx), t);
	}
	
	
	protected long prevAddr(long addr) {
		if (addr <= HEADERSIZE)
			return -1;
		long ans;
		if ( (addr & (Constant.BLOCK_SIZE-1)) == 0) {
			int last = (addr>>Constant.BLOCK_SHIFT)==1? lastInFirstBlock: lastInBlock;
			ans =  (addr&~(Constant.BLOCK_SIZE-1))-Constant.BLOCK_SIZE+last;
		} else ans = addr-tupleSize;
		return ans < HEADERSIZE? -1:ans;
	}
	public void delete(long addr) throws SQLException {
		checkAddr(addr);
		try {
			Block last = buffer.getWrite(lastTupleAddr);
			Tuple l = last.getTuple(lastTupleAddr);
			lastTupleAddr = prevAddr(lastTupleAddr);
			FixBlock b = (FixBlock) buffer.getWrite(addr);
			b.writeTuple(addr, l);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void deleteIdx(int idx) throws SQLException {
		delete(idx2Addr(idx));
	}
	
	
}
