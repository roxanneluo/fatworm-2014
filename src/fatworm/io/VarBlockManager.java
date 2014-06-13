package fatworm.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.SQLException;

import fatworm.driver.Schema;
import fatworm.driver.Tuple;
import fatworm.util.Constant;
import fatworm.util.Util;

public class VarBlockManager extends BlockManager {
	
	private Schema schema;
	private int fixIOSize;
	private	int maxIOSize;
	public static int HEADER_SIZE = Constant.LONG_SIZE;
	
	public VarBlockManager(File f, String mode, Schema schema) throws SQLException {
		try {
			this.mode = mode;
			this.f = f;
			buffer = new Buffer(new RandomAccessFile(f, mode), schema);
			readHeader();
			curAddr = -1;
			this.schema = schema;
			int nullMapSize = Util.getNullMapSize(schema);
			fixIOSize = VarBlock.getFixIOSize(schema, nullMapSize);
			maxIOSize = (2+schema.varCnt())*Constant.INT_SIZE+nullMapSize+schema.maxTupleSize();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void insert(Tuple t) throws SQLException {
		try {
			boolean empty = lastTupleAddr == -1;
			int totalSize = Util.getTotalSize(t, schema);
			VarBlock lastBlock;
			if (empty) {
				lastBlock = new VarBlock(0, HEADER_SIZE, schema);
				buffer.addWrite(lastBlock);
			} else {
				lastBlock = (VarBlock) buffer.getWrite(lastTupleAddr);
			}
			if (totalSize > Constant.BLOCK_SIZE-HEADER_SIZE-VarBlock.HEADER_SIZE) throw new SQLException("tuple Size "
					+ "too large: tupleSize = "+totalSize+" BlockSize = "+Constant.BLOCK_SIZE);
			if (lastBlock.freeSpace() >= totalSize) {
				lastBlock.insert(t, totalSize);
				if (empty) lastTupleAddr = HEADER_SIZE+VarBlock.HEADER_SIZE;
				else lastTupleAddr += fixIOSize;
			} else {
				lastTupleAddr = lastBlock.blockStart+Constant.BLOCK_SIZE+VarBlock.HEADER_SIZE;
				lastBlock = new VarBlock(lastBlock.blockStart+Constant.BLOCK_SIZE, 0, schema);
				buffer.addWrite(lastBlock);
				lastBlock.insert(t, totalSize);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void update(Tuple t) throws SQLException {
		if (update(curAddr, t))
			curAddr = prevAddr(curAddr);
	}
	public boolean update(long addr, Tuple t) throws SQLException {
		try {
			VarBlock b = (VarBlock) buffer.getWrite(addr);
			int totalSize = b.readInt(addr);
			int newTotalSize = Util.getTotalSize(t, schema);
			if (newTotalSize - totalSize <= b.freeSpace()) {
				b.update(addr, t, totalSize, newTotalSize);
				return false;
			} else {
				delete();	// it assumes only 1 tuple could be deleted
				insert(t);
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	
	public void delete(long addr) throws SQLException {
		try {
			VarBlock block = (VarBlock) buffer.getWrite(addr);
			int freeSize = block.delete(addr);
			Tuple t; VarBlock last;
			if(addr>>Constant.BLOCK_SHIFT != lastTupleAddr >> Constant.BLOCK_SHIFT)
				while (freeSize >= maxIOSize) {
					last = (VarBlock) buffer.getWrite(lastTupleAddr);
					t = last.getTuple(lastTupleAddr);
					last.delete(lastTupleAddr);
					freeSize = block.insert(t);
					lastTupleAddr = prevAddr(lastTupleAddr);
				}
			else lastTupleAddr = prevAddr(lastTupleAddr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected long prevAddr(long addr) {
		try {
			if (addr <= 0)
				return -1;
			if ((addr >> Constant.BLOCK_SHIFT==0)) {
				if (addr <= HEADER_SIZE+VarBlock.HEADER_SIZE) 
					return -1;
				else return addr-fixIOSize;
			}
			if ((addr & (Constant.BLOCK_SIZE-1)) <= VarBlock.HEADER_SIZE) {
				VarBlock prev = (VarBlock) buffer.getWrite(addr - Constant.BLOCK_SIZE);
				return prev.getFixEnd()-fixIOSize;
			} else return addr - fixIOSize;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
	protected long nextAddr(long addr) {
		try {
			if (addr >= lastTupleAddr) return lastTupleAddr;
			if (addr <= 0) {
//				System.out.println(addr);
				return HEADER_SIZE+VarBlock.HEADER_SIZE;
			}
			VarBlock cur = (VarBlock) buffer.getRead(addr);
			if (addr < cur.getFixEnd()-fixIOSize) 
				return addr + fixIOSize;
			long curBlockStart = addr & ~(Constant.BLOCK_SIZE-1);
			return curBlockStart+Constant.BLOCK_SIZE+VarBlock.HEADER_SIZE;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		
	}
	

}
