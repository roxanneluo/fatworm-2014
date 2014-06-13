package fatworm.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import fatworm.io.BlockManager;
import fatworm.io.FixBlockManager;

public class DBDataManager {
	public Meta meta = null;
	public String curDBName = null;
	public DBMeta curDBMeta = null;
	public HashMap<String, Table> tables = new HashMap<String, Table>();
	private static DBDataManager instance = null;
	
	public static String path = null;
	public static final String metaFileName = "meta.meta";
	public File metaFile = null;
	
	
	private DBDataManager() {
	}
	
	public static DBDataManager getInstance() {
		if (instance == null)
			instance = new DBDataManager();
		return instance;
	}
	
	public boolean createTable(Table table) throws SQLException {
		Schema schema = curDBMeta.tables.get(table.name);
		String fileName = curDBName+"."+table.name;
		if (schema != null) 
			throw new SQLException("create table failed."+fileName+" already exists");
		
		File f = new File(path+fileName);
		if (f.exists())
			throw new SQLException("[ERROR] "+fileName+"'s meta does not exist but the file does");
		try {
			if (!f.createNewFile()) return false;
		} catch(IOException e) {
			e.printStackTrace();
		}
		table.file = f;
		writeHeader(f, table.schema.fix(), table.schema);
		curDBMeta.tables.put(table.name, table.schema);
		tables.put(fileName, table);
		outputMeta();
		return true;
	}
	private void writeHeader(File f, boolean fix, Schema schema) throws SQLException {
			BlockManager bm = new FixBlockManager(f,"rw", schema);
			bm.initHeader();
			bm.close();
	}
	public boolean dropTable(String tableName) {
		Schema schema = curDBMeta.tables.get(tableName);
		if (schema == null) return false;
		curDBMeta.tables.remove(tableName);
		String tableFileName = curDBName+"."+tableName;
		tables.remove(tableFileName);
		File f = new File(path+tableFileName);
		if (!f.delete()) return false;
		outputMeta();
		return true;
	}
	public Table getTable(String tableName) throws SQLException {
		tableName = tableName.toLowerCase();
		String fileName = (curDBName+"."+tableName).toLowerCase();
		Table table = tables.get(fileName);
		if (table != null)
			return table;
		
		Schema schema = curDBMeta.tables.get(tableName);
		if (schema == null) 
			throw new SQLException("[table] "+fileName+" does not exist (in DBDataManager.getTable)");
		File f = new File(path+fileName);
		if (!f.exists())
			throw new SQLException("[table]"+fileName+" exists but its file does not");
		table = new Table(tableName, schema, f);
		tables.put(fileName, table);
		return table;
	}
	public boolean useDB(String database) {
		database = database.toLowerCase();
		DBMeta db = meta.dbs.get(database);
		if (db==null) {
			System.out.println("[database] "+database+" not exist. USE failed");
			return false;
		}
		curDBName = database;
		curDBMeta = db;
		return true;
	}
	
	public boolean createDB(String database) {
		database = database.toLowerCase();
		DBMeta db = meta.dbs.get(database);
		if (db != null) {
			System.out.println("[database] "+database+" already exist. CREATE failed");
			return false;
		} else {
			meta.dbs.put(database, new DBMeta());
			outputMeta();
			return true;
		}
	}
	
	public boolean dropDB(String database) {
		database = database.toLowerCase();
		DBMeta db = meta.dbs.get(database);
		if (db == null) {
			System.out.println("[database] "+database+" not exist. DROP failed");
			return false;
		} else {
			for (String tableName: db.tables.keySet()) {
				String fileName = path+database+"."+tableName;
				File f = new File(fileName);
				f.delete();
			}
			meta.dbs.remove(database);
			LinkedList<String> fileNames = new LinkedList<String>(tables.keySet());
			for (String fileName: fileNames) {
				if (fileName.startsWith(database)) {
					tables.remove(fileName);
				}
			}
			outputMeta();
			return true;
		}
	}
	public void outputMeta() {
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(metaFile, false));
			out.writeObject(meta);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}	
	
	public boolean inputMeta() throws FileNotFoundException, IOException, ClassNotFoundException {
		if (meta != null) return true;
		metaFile = new File(path+metaFileName);
		if (!metaFile.exists()) {
			metaFile.createNewFile();
			meta = new Meta();
		} else {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(metaFile));
			meta = (Meta) in.readObject();
			in.close();
		}
		return true;
	}
	
	
	public String toString() {
		String ans = "====================DBDataManager Start======================\n";
		ans += "\tcurrentDB:"+(curDBName!= null? curDBName:"null")+"\n";
		for (String db: meta.dbs.keySet()) {
			ans += "\t[DB]"+db+"\n";
		}
		ans += "-----------------------------------------------------------\n";
		for (Entry<String, DBMeta> entry: meta.dbs.entrySet()) {
			ans += entry.getValue().getString("\t",entry.getKey());
		}
		ans += "====================DBDataManager End======================\n\n";
		return ans;
	}
}
