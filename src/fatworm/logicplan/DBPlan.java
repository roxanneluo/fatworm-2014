package fatworm.logicplan;

public class DBPlan extends Plan {

	public String database;
	public DBCmdType type;
	public static enum DBCmdType {
		CREATE_DB, DROP_DB, USE_DB,	// DB stands for database
	};
	
	public DBPlan(String database, DBCmdType type) {
		this.database = database;
		this.type = type;
	}
	@Override
	public String getString(String tabs) {
		// TODO Auto-generated method stub
		return null;
	}

}
