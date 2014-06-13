package fatworm.driver;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

public class Meta implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5819828310337816468L;
	public HashMap<String, DBMeta> dbs = new HashMap<String, DBMeta>();
	public String toString() {
		String ans = "~~~~~~~~~~~~~~~~~~~~~~~~~~meta~~~~~~~~~~~~~~~~~~~~\n";
		for (String dbName: dbs.keySet()) {
			ans += "\t"+dbName+"\n";
		}
		ans += "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n";
		for (Entry<String, DBMeta> entry:dbs.entrySet()) {
			ans += entry.getValue().getString("\t", entry.getKey());
		}
		ans += "~~~~~~~~~~~~~~~~~~~~~~~~~~meta~~~~~~~~~~~~~~~~~~~~";
		return ans;
	}
}
