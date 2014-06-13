package fatworm.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import fatworm.driver.Schema;
import fatworm.util.Constant;

public class Buffer {
	private LinkedList<Block> blocks = new LinkedList<Block> ();
	private HashMap<Long, Block> writeBlocks = new HashMap<Long, Block>();
	private RandomAccessFile ra;
	private Block readBlock = null;
	
	private Schema schema;
	private boolean fix;
	private int maxTupleSize;
	public Buffer(RandomAccessFile ra, Schema schema) {
		this.ra = ra;
		this.schema = schema;
		fix = schema.fix();
		maxTupleSize = schema.maxTupleSize();
	}
	public void addWrite(Block b) {
		writeBlocks.put(b.blockStart, b);
		blocks.add(b);
	}
	public Block getWrite(long addr) throws IOException {
		addr = blockStart(addr);
		Block b = writeBlocks.get(addr);
		if (b != null) return b;
		if (readBlock!= null && readBlock.blockStart == addr) {
			b = readBlock;
			//readBlock = null;
		} else
			b = inputBlock(addr);
		addWrite(b);
		return b;
		
	}
	private long blockStart(long addr) {
		return addr & ~(Constant.BLOCK_SIZE-1);
	}
	public Block getRead(long addr) throws IOException {
		addr = blockStart(addr);
		if (readBlock!= null && readBlock.blockStart == addr) {
			return readBlock;
		}
		Block b = writeBlocks.get(addr);
		if (b != null)
			return b;
		readBlock = inputBlock(addr);
		return readBlock;
	}
	private Block inputBlock(long addr) throws IOException {
		byte[] bytes = new byte[Constant.BLOCK_SIZE];
		ra.seek(addr);
		ra.read(bytes);
		if (fix)
			return new FixBlock(addr, bytes, maxTupleSize, addr == 0? FixBlockManager.HEADERSIZE:0, schema);
		else {
			return new VarBlock(addr, bytes, addr == 0? VarBlockManager.HEADER_SIZE:0, schema);
		}
	}
	private void output() {
		Collections.sort(blocks);
		try {
			for (Block b: blocks) {
//				System.out.println("to write at "+b.blockStart);
				ra.seek(b.blockStart);
				if (!fix)
					((VarBlock)b).writeHeader();
				ra.write(b.bytes);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		blocks.clear();
		writeBlocks.clear();
	}
	public void close() throws IOException {
		output();
		ra.close();
	}
}
