package fatworm.logicplan;

import java.util.LinkedList;

import fatworm.driver.Tuple;

public class InsertValuePlan extends Plan {
	public TablePlan table;
	public Tuple value;
	
	public InsertValuePlan(TablePlan table, Tuple tuple) {
		this.table = table;
		table.parent = this;
		this.schema = table.schema;
		this.value = tuple;
	}
	public String getString(String tabs) {
		// TODO Auto-generated method stub
		return null;
	}

}
