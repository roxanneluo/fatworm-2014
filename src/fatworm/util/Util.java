package fatworm.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fatworm.absyn.Column;
import fatworm.absyn.InvisibleFunc;
import fatworm.driver.Attribute;
import fatworm.driver.Schema;
import fatworm.driver.Tuple;
import fatworm.field.DATE;
import fatworm.field.Field;
import fatworm.scan.Scan;

public class Util {
	public static String trim(String s){
		if(((s.startsWith("'") && s.endsWith("'"))||(s.startsWith("\"") && s.endsWith("\""))) && s.length() >= 2)
			return s.substring(1,s.length()-1);
		else 
			return s;
	}
	public static int getTotalSize(Tuple t, Schema schema) {
		return getTotalSize(t, schema, getNullMapSize(schema));
	}
	public static int getTotalSize(Tuple t, Schema schema, int nullMapSize) {
		int size = 2*Constant.INT_SIZE+nullMapSize;
		Field type, f;
		for (int i = 0 ;i < schema.types.size(); ++i) {
			if ((type = schema.types.get(i)).fix()) size+=type.maxSize();
			else {
				f = t.get(i);
				if (f.isNull()) continue;
				size+=Constant.INT_SIZE+f.size();
			}
		}
		return size;
	}
	public static byte[] getVarBytes(Tuple t, Schema schema, int varSize) {
		return writeVarBytes(t, schema, new byte[varSize], 0);
	}
	public static byte[] writeVarBytes(Tuple t, Schema schema, byte[] bytes, int addr) {
		Field f;
		for (int i = 0 ;i < schema.types.size(); ++i) {
			if (schema.types.get(i).fix() || (f = t.get(i)).isNull()) continue;
			addr = writeInt(bytes, addr, f.size());
			addr = write(bytes, addr, f.toBytes());
//			System.out.println("after write "+f+"addr="+addr);
		}
		return bytes;
	}
	public static byte[] getFixBytes(Tuple t, Schema schema, int totalSize, int varAddr, int nullMapSize) {
		return writeFixBytes(t, schema, new byte[totalSize],0, totalSize, varAddr, nullMapSize);
	}
	//write the fix part of t in ans from start
	public static byte[] writeFixBytes(Tuple t, Schema schema, byte[] ans, int start, int totalSize, int varAddr, int nullMapSize) {
		boolean[] nullMap = new boolean[schema.types.size()];
		int addr = writeInt(ans, start, totalSize)+nullMapSize;
		Field type, f;
		int cnt = 0;
		for (int i = 0; i < schema.types.size(); ++i) {
			if (!(type = schema.types.get(i)).fix())
				continue;
			if ((f = t.get(i)).isNull()) {
				nullMap[cnt++] = true;
				addr+=type.maxSize();
			} else {
//				if (type instanceof DATE)
//					System.out.println("write fix field:"+f.typeValString()+" len"+f.toBytes().length+"@"+addr);
				nullMap[cnt++] = false;
				addr = write(ans, addr, f.toBytes());
			}
		}
//		System.out.println("write Fix: varAddr of "+start+" is "+varAddr+" at "+addr);
		writeInt(ans, addr, varAddr);
		for (int i = 0; i < schema.types.size(); ++i) {
			if (schema.types.get(i).fix()) continue;
			if (t.get(i).isNull())
				nullMap[cnt++] = true;
			else nullMap[cnt++] = false;
		}
		write(ans, start+Constant.INT_SIZE, bool2ByteNullMap(nullMap, nullMapSize));
		return ans;
	}
	public static byte[] bool2ByteNullMap(boolean[] nullMap, int size) {
		byte[] ans = new byte[size];
		for (int i = 0; i < nullMap.length; ++i) {
			if (!nullMap[i]) continue;
			int idx = i >> Constant.BYTE_LEN_SHIFT; int rem = i&(Constant.BYTE_LEN-1);
			ans[idx] = (byte) (ans[idx] | (1<<rem));
		}
		return ans;
	}
	public static int writeInt(byte[] b, int addr, int n) {
		return write(b, addr, int2bytes(n));
	}
	public static int write(byte[] b, int addr, byte[] fromByte) {
		return write(b, addr, fromByte, 0, fromByte.length);
	}
	public static int write(byte[] toByte , int addr, byte[] fromByte, int from, int length) {
		for (int i =  0; i < length; ++i) {
			toByte[addr++] = fromByte[from++];
		}
		return addr;
	}
	
	public static byte[] getSubBytes(byte[] b, int start, int len) {
		byte[] ans = new byte[len];
		for (int i = 0; i < len; ++i)
			ans[i] = b[start++];
		return ans;
	}
	
	public static int bytes2int(byte[] bytes) {
		return bytes2int(bytes,0);
	}
	public static int bytes2int(byte[] bytes, int start) {
		int ans = 0;
		int mask = ~0;
		for (int i = Constant.INT_SIZE-1+start, j = 0; i >= start; --i, mask<<=Constant.BYTE_LEN, j+=Constant.BYTE_LEN) {
			ans = (ans & ~mask)|(bytes[i]<<j);
		}
		return ans;
	}
	public static byte[] int2bytes(int n) {
		byte[] bytes = new byte[Constant.INT_SIZE];
		for (int i = Constant.INT_SIZE-1; i>=0; --i) {
			bytes[i] = (byte)n;
			n >>= Constant.BYTE_LEN;
		}
		return bytes;
	}
	public static int getNullMapSize(Schema schema) {
		int fieldcnt = schema.types.size();
		int nullMapSize = fieldcnt/Constant.BYTE_LEN;
		if (fieldcnt % Constant.BYTE_LEN != 0) nullMapSize++;
		
		return nullMapSize;
	}
	public static boolean[] bytes2boolNullMap(byte[] bytes, int start, int size) {
		boolean[] nullMap = new boolean[size];
		for (int i = 0; i< size; ++i) {
			int idx = i>>Constant.BYTE_LEN_SHIFT; int rem = i&(Constant.BYTE_LEN-1);
			nullMap[i] = (bytes[start+idx] & (1<<rem))!=0;
		}
		return nullMap;
	}
	public static java.sql.Timestamp parseTimestamp(String x) {
		try{
			return new java.sql.Timestamp(Long.valueOf(x));
		} catch (NumberFormatException e){
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			try {
				return new java.sql.Timestamp(format.parse(x).getTime());
			} catch (ParseException ee) {
				ee.printStackTrace();
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<Field> cloneFieldList(List<Field> list) {
		List<Field> ans = null;
		if (list instanceof ArrayList<?>)
			ans = new ArrayList<Field>();
		else ans = new LinkedList<Field>();
		for (Field f:list) {
			ans.add((Field)f.clone());
		}
		return ans;
	}
	
	public static List<Attribute> cloneAttributeList(List<Attribute> list) {
		List<Attribute> ans = null;
		if (list instanceof ArrayList<?>)
			ans = new ArrayList<Attribute>();
		else ans = new LinkedList<Attribute>();
		for(Attribute attr: list) {
			ans.add((Attribute) attr.clone());
		}
		return ans;
	}
	
	public static List<Attribute> cloneColumnList(List<Attribute> list, String alias/*, Scan src*/) {
		return cloneColumnList(list, alias, true);
	}
	public static List<Attribute> cloneColumnList(List<Attribute> list, String alias, boolean cloneInvisible/*, Scan src*/) {
		List<Attribute> ans = null;
		if (list instanceof ArrayList<?>)
			ans = new ArrayList<Attribute>();
		else ans = new LinkedList<Attribute>();
		Column col = null;
		for(Attribute attr: list) {
			if (attr instanceof InvisibleFunc && !cloneInvisible)
				continue;
			col = (Column) ((Column)attr).clone();
			col.tableName = alias != null? alias: col.tableName;
//			col.table = src;
			ans.add(col);
		}
		return ans;
	}

//	public static <T> List<T> cloneList(List<T> list) {
//		List<T> ans = null;
//		if (list instanceof ArrayList<?>)
//			ans = new ArrayList<T>();
//		else ans = new LinkedList<T>();
//		T first = list.get(0);
//		if (first instanceof Field)
//			for(Field f: (List<Field>)list) {
//				ans.add((T) f.clone());
//			}
//		else if (first instanceof Attribute)
//			for(Attribute attr: (List<Attribute>)list) {
//				ans.add((T) (T)attr.clone());
//			}
//		return ans;
//	}
	
	public static byte[] long2bytes(long l) {
		byte[] bytes = new byte[Constant.LONG_SIZE];
		for (int i = Constant.LONG_SIZE-1; i >= 0; --i) {
			bytes[i] = (byte) l;
			l >>= Constant.BYTE_LEN;
		}
		return bytes;
	}
	
	
	/**!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * be careful with the calculation of 
	 * long since I think if not explicitly
	 * stated, every integer calculation is
	 * cast to int
	 * so explicit long cast is necessary
	 * FIXME FIXME
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * @param bytes
	 * @param start
	 * @return
	 */
	public static long bytes2long(byte[] bytes, int start) {
		long ans = 0;
		long mask = (~0)<<Constant.BYTE_LEN;
		for (int i = Constant.LONG_SIZE-1+start, j = 0; i >= start; --i, mask<<=Constant.BYTE_LEN, j+=Constant.BYTE_LEN) {
			ans = ans |(((long)bytes[i]<<j)& ~mask); 
		}
		return ans;
	}
	
}
