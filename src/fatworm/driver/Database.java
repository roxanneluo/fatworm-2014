package fatworm.driver;

import java.util.HashMap;

public class Database {
	String name;
	public HashMap<String, Table> tables = new HashMap<String, Table> ();
	
	public Database(String name) {
		this.name = name;
	}
	
	public boolean dropTable(String tableName) {
		return tables.remove(tableName) != null;
	}
	
	public boolean createTable(Table table) {
		Table t = tables.get(table.name);
		if (t != null) return false;
		tables.put(table.name, table);
		return true;
	}
	
	public String toString() {
		return getString("");
	}
	
	public String getString(String tabs) {
		String ans = tabs+"----------------start [DB]"+name+"----------------\n";
		for (String table: tables.keySet())
			ans += tabs+"\t"+table+"\n";
		ans += tabs+"--------------------------------------------\n";
		for (Table table:tables.values()) 
			ans += table.getString(tabs+"\t");
		ans += tabs+"----------------end [DB]"+name+"----------------\n";
		return ans;
	}
}

