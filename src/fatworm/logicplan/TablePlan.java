package fatworm.logicplan;

import java.sql.SQLException;
import java.util.ArrayList;

import fatworm.absyn.Column;
import fatworm.driver.Attribute;
import fatworm.driver.DBDataManager;
import fatworm.driver.Schema;
import fatworm.driver.Table;
import fatworm.field.Field;
import fatworm.util.Util;

public class TablePlan extends Plan {
	public String tableName;
	public TablePlan(String name, Plan parent) throws SQLException {
		super(parent);
		this.tableName = name;
		
		DBDataManager data = DBDataManager.getInstance();
		
		Table table = data.currentDB.tables.get(tableName);
		if (table == null)
			throw new SQLException("table "+tableName+" does not exist");
		
		Schema tableSchema = table.schema; 
		schema = new Schema();
		schema.primaryKey = tableSchema.primaryKey;
		schema.types = (ArrayList<Field>) Util.cloneFieldList(tableSchema.types);
		Column col = null;
		for (Attribute attr:tableSchema.attrNames) {
			col = new Column(attr, table);
			col.tableName = alias != null? alias:tableName;
			schema.attrNames.add(col);
			schema.attributes.put(col.colName, col);
		}
	}
	public TablePlan(String name) throws SQLException {
		this(name,null);
	}
	
	public String getString(String tabs) {
		return tabs+tableName;
	}
}
