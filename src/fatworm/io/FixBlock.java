package fatworm.io;

import java.sql.SQLException;

import fatworm.driver.Schema;
import fatworm.driver.Tuple;
import fatworm.field.Field;
import fatworm.field.NULL;
import fatworm.util.Constant;
import fatworm.util.Util;
/**
 * each tuple is organized as |Tuple|nextAddr| so size = tupleSize+LONGSIZE
 * @author roxanne
 *
 */
public class FixBlock extends Block {
//	public int tupleSize;
	private int nullMapSize;
	public FixBlock(long blockStart, byte[] bytes,  int tupleSize, int start, Schema schema) {
		super(blockStart, bytes, start, schema);
//		this.tupleSize = tupleSize;
		nullMapSize = Util.getNullMapSize(schema);
	}

	public FixBlock(long blockStart, byte[] bytes, int tupleSize, Schema schema) {
		this(blockStart, bytes, tupleSize, 0, schema);
	}
	public FixBlock(int blockStart, int tupleSize, int start, Schema schema) {
		this(blockStart, new byte[Constant.BLOCK_SIZE], tupleSize, start, schema);
	}
	public FixBlock(long blockStart, int tupleSize, Schema schema) {
		this(blockStart, new byte[Constant.BLOCK_SIZE], tupleSize, 0, schema);
	}
	
	@Override
	public Tuple getTuple(long addr) throws SQLException {
		Tuple t = new Tuple();
		int innerAddr = getAddr(addr);
		boolean[] nullMap = Util.bytes2boolNullMap(bytes, innerAddr, schema.types.size());
		innerAddr+=nullMapSize;
		Field type;
		for (int i = 0;i < schema.types.size(); ++i) {
			if (nullMap[i]) {
				t.add(NULL.getInstance());
				continue;
			}
			type = schema.types.get(i);
			t.add(type.bytes2Field(bytes, innerAddr, type.maxSize()));
			innerAddr += type.maxSize();
		}
		return t;
	}

	
	public void writeTuple(long addr, Tuple t) throws SQLException {
		int innerAddr = getAddr(addr);
		innerWriteTuple(innerAddr, t);
	}
	// ||00101001||0 |1 |0 |0 |0 |0 |1 |0 ||
	// ||76543210||15|14|13|12|11|10|9 |8 ||	1-> isnull
	private int innerWriteTuple(int innerAddr, Tuple t) {
		byte[] b;
		byte[] nullMap = new byte[nullMapSize];
		for (int i = 0; i < t.size(); ++i) {
			if (t.get(i).isNull()) {
				int idx = i>>Constant.BYTE_LEN_SHIFT;
				nullMap[idx] = (byte) (nullMap[idx] | (1<<(i&(Constant.BYTE_LEN-1))));
			}
		}
		for (int i = 0; i < nullMapSize; ++i){
			bytes[innerAddr++] = nullMap[i];
		}
		Field f;
		for(int i = 0; i < t.size(); ++i) {
			f = t.get(i);
			if (f.isNull()) {
				innerAddr += f.maxSize();
				continue;
			}
			b = f.toBytes();
			for (int j = 0; j < b.length; ++j)
				bytes[innerAddr++] = b[j];
		}
		return innerAddr;
	}
	
	
	
	@Override
	public boolean inBlock(long addr) {
//		int spare = (Constant.BLOCKSIZE-start)%tupleSize;
		return blockStart <= addr && addr < blockStart+Constant.BLOCK_SIZE/*-tupleSize-spare*/;
	}
	
}
