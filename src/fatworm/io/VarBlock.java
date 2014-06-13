package fatworm.io;

import java.sql.SQLException;

import fatworm.driver.Schema;
import fatworm.driver.Tuple;
import fatworm.field.DATE;
import fatworm.field.Field;
import fatworm.field.NULL;
import fatworm.util.Constant;
import fatworm.util.Util;

public class VarBlock extends Block{
	private int fixEnd, freeEnd; //output
	private int nullMapSize, fixIOSize; //don't output
	public static int HEADER_SIZE = 2*Constant.INT_SIZE;
	
	public VarBlock(long blockStart, int start, Schema schema) {
		this(blockStart, new byte[Constant.BLOCK_SIZE], start, schema);
		fixEnd = start+HEADER_SIZE;
		freeEnd = Constant.BLOCK_SIZE-1;
	}
	public static int getFixIOSize(Schema schema, int nullMapSize) {
		return Constant.INT_SIZE*2+nullMapSize+schema.fixSize();
	}
	public VarBlock(long blockStart, byte[] bytes, int start, Schema schema) {
		super(blockStart, bytes, start, schema);
		nullMapSize = Util.getNullMapSize(schema);
		fixIOSize = getFixIOSize(schema, nullMapSize);
//		System.out.println("fixIOSize = "+fixIOSize);
		readHeader();
	}
	public int getFixEnd() {
		return fixEnd;
	}
	public int freeSpace() {
		return freeEnd-fixEnd+1;
	}
	public int insert(Tuple t) {
		return insert(t, Util.getTotalSize(t, schema));
	}
	public int insert(Tuple t, int totalSize) {
//		System.out.println("insert at "+blockStart+"."+fixEnd+" tuple "+t.get(0));
		
		//checksize ??
		int varSize = totalSize-fixIOSize;
//		System.out.println("totalSize = "+totalSize+", fixIOSize:"+fixIOSize);
		freeEnd -=varSize;
		Util.writeFixBytes(t, schema, bytes, fixEnd, totalSize, freeEnd+1, nullMapSize);
		fixEnd += fixIOSize;
		Util.writeVarBytes(t, schema, bytes, freeEnd+1);
//		System.out.println("then fixEnd = "+fixEnd+", freeEnd = "+freeEnd+", freeSpace="+freeSpace());
		return freeSpace();
	}
	public void update(long addr, Tuple t, int totalSize, int newTotalSize) throws SQLException {
		int inner = getAddr(addr); //check valid? witin fixspace
		int diff = newTotalSize-totalSize;
		int newVarAddr = readVarAddr(inner)-diff; 
//		System.out.println("addr:"+inner+"varAddr:"+newVarAddr);
		replace(inner, Util.getFixBytes(t, schema, totalSize, newVarAddr, nullMapSize), Util.getVarBytes(t, schema, newTotalSize-fixIOSize)); //FIXME if update is too slow
	}
	
	public int delete(long addr) throws SQLException {
		int inner = getAddr(addr);
		fixEnd -= fixIOSize;
		int varSize = readInt(inner) - fixIOSize;
		if (inner == fixEnd-fixIOSize) {
			freeEnd += varSize;
		} else {
			int lastTotalSize = readInt(fixEnd);
			int lastVarSize = lastTotalSize - fixIOSize;
			freeEnd += lastVarSize;
			replace(inner, bytes, fixEnd, Util.getSubBytes(bytes, readVarAddr(fixEnd), lastVarSize));
		}
		return freeSpace();
	}
	
	private void replace(int addr, byte[] fix, byte[] var) {
		replace(addr, fix, 0, var);
	}
	private int readVarAddr(int innerAddr) {
		return readInt(innerAddr+fixIOSize-Constant.INT_SIZE);
	}
	private int readInt(int addr) {
		return Util.bytes2int(bytes, addr);
	}
	public int readInt(long addr) throws SQLException {
		return readInt(getAddr(addr));
	}
	private void replace(int inner, byte[] fix, int fixStart, byte[] var){
		int varAddr = readVarAddr(inner);
		int totalSize = readInt(inner); 
		int varSize = totalSize-fixIOSize;
		write(inner, fix, fixStart, fixIOSize/*length*/);
		int diff = var.length - varSize;
		if (diff==0) {
			write(varAddr, var);
			return;
		} else  {
			int newVarAddr = varAddr-diff;
			int shiftSize = -(freeEnd-varAddr-1);
			writeVarAddr(inner, newVarAddr);
			if (varSize > var.length) {
				write(newVarAddr, var);
				writeDec(newVarAddr-1,bytes, varAddr-1, shiftSize);
			} else {
				write(freeEnd-diff+1, bytes, freeEnd+1, shiftSize);
				write(newVarAddr, var);
			}
			freeEnd -= diff;
		}
		
		for (int addr = inner+fixIOSize; addr < fixEnd; addr += fixIOSize) {
			varAddr = readVarAddr(addr);
			writeVarAddr(addr, varAddr-diff);
		}
	}
	
	private int writeVarAddr(int addr, int varAddr) {
		return Util.writeInt(bytes, addr+fixIOSize-Constant.INT_SIZE, varAddr);
	}
	private void writeDec(int addr, byte[] b, int from, int len) {
//		System.out.println("writeDec: to="+addr+" from="+from+" len="+len);
		for (int i = 0 ; i < len; ++i) {
			bytes[addr--] = b[from--];
		}
	}
	private int writeInt(int addr, int n) {
		return Util.writeInt(bytes, addr, n);
	}
	
	private int write(int addr, byte[] b) {
//		System.out.println("write: to="+addr+","+b);
		return Util.write(bytes, addr,b);
	}
	private int write(int addr, byte[] b, int from, int length) {
		return Util.write(bytes, addr, b, from, length);
	}
	public void writeHeader() {
		writeInt(start,fixEnd);
		writeInt(start+Constant.INT_SIZE, freeEnd);
//		System.out.println("[writeHeader]start="+start+",fixEnd = "+fixEnd+", freeEnd="+freeEnd);
	}
	private void readHeader() {
		fixEnd = readInt(start);
		freeEnd = readInt(start+Constant.INT_SIZE);
//		System.out.println("[readHeader]start="+start+",fixEnd = "+fixEnd+", freeEnd="+freeEnd);
	}
	
	@Override
	public boolean inBlock(long addr) {
		return blockStart <= addr && addr < blockStart+Constant.BLOCK_SIZE;
	}
	@Override
	public Tuple getTuple(long addr) throws SQLException {
		int inner = getAddr(addr);
//		int totalSize = readInt(inner);
		boolean[] nullMap = Util.bytes2boolNullMap(bytes, inner+=Constant.INT_SIZE, schema.types.size());
		inner += nullMapSize;
		
		Tuple t = new Tuple(); t.initSize(schema.types.size());
		int cnt = 0; Field type;
		for (int i = 0 ; i < schema.types.size(); ++i) {
			if ((type = schema.types.get(i)).fix()) {
				if (!nullMap[cnt++]){
					Field f = type.bytes2Field(bytes, inner, type.maxSize());
//					if (type instanceof DATE)
//						System.out.println("read date "+f+" @"+inner);
					t.set(i,f);
				}
				inner += type.maxSize();
			} 
		}
		
		int varAddr = readInt(inner), len;
//		System.out.println("read ["+addr+"]varAddr = "+varAddr +"@"+blockStart+"."+inner);
		for (int i = 0; i < schema.types.size(); ++i) {
			if ((type = schema.types.get(i)).fix()) continue;
			if (!nullMap[cnt++]) {
				len = readInt(varAddr); varAddr+=Constant.INT_SIZE;
				t.set(i, type.bytes2Field(bytes, varAddr, len));
				varAddr += len;
			}
		}
		return t;
	}
	public String toString() {
		String ans = "["+blockStart+"]"+",fixEnd="+fixEnd+",freeEnd="+freeEnd+"\n";
		return ans;
	}
}
