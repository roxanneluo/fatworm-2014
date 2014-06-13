package fatworm.driver;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

public class DBMeta implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4811438503696145816L;
	public HashMap<String, Schema> tables = new HashMap<String, Schema>();
	public String getString(String tabs, String dbName) {
		String ans = tabs+"----------------start [DB]"+dbName+"----------------\n";
		for (Entry<String, Schema> entry: tables.entrySet()) {
			ans += tabs+"\t"+entry.getKey()+":"+entry.getValue()+"\n";
		}
		ans += tabs+"----------------end [DB]"+dbName+"----------------\n";
		return ans;
	}
}
