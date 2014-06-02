package fatworm.driver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class DBDataManager {
	public HashMap<String, Database> DBs = new HashMap<String, Database>();
	public Database currentDB = null;
	
	private static DBDataManager instance = null;
	
	private DBDataManager() {
	}
	
	public static DBDataManager getInstance() {
		if (instance == null)
			instance = new DBDataManager();
		return instance;
	}
	
	public boolean useDB(String database) {
		Database db = DBs.get(database);
		if (db==null) {
			System.out.println("[database] "+database+" not exist. USE failed");
			return false;
		}
		currentDB = db;
		return true;
	}
	
	public boolean createDB(String database) {
		Database db = DBs.get(database);
		if (db != null) {
			System.out.println("[database] "+database+" already exist. CREATE failed");
			return false;
		} else {
			DBs.put(database, new Database(database));
			return true;
		}
	}
	
	public boolean dropDB(String database) {
		Database db = DBs.get(database);
		if (db == null) {
			System.out.println("[database] "+database+" not exist. DROP failed");
			return false;
		} else {
			DBs.remove(database);
			return true;
		}
	}
	
	public String toString() {
		String ans = "====================DBDataManager Start======================\n";
		ans += "\tcurrentDB:"+currentDB.name+"\n";
		for (String db: DBs.keySet()) {
			ans += "\t[DB]"+db+"\n";
		}
		ans += "-----------------------------------------------------------\n";
		for (Database db: DBs.values()) {
			ans += db.getString("\t");
		}
		ans += "====================DBDataManager End======================\n\n";
		return ans;
	}
}
