package fatworm.logicplan;

import fatworm.driver.Schema;

public class CreateTablePlan extends Plan {
	public String tableName;
	public Schema schema;
	
	public CreateTablePlan(String tableName, Schema schema) {
		this.tableName = tableName.toLowerCase();
		this.schema = schema;
	}
	
	
	@Override
	public String getString(String tabs) {
		// TODO Auto-generated method stub
		return null;
	}

}
