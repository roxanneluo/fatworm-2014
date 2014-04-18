package fatworm.logicplan;

public class TablePlan extends Plan {
	public String tableName;
	public TablePlan(String name, Plan parent) {
		super(parent);
		this.tableName = name;
	}
	public TablePlan(String name) {
		super();
		tableName = name;
	}
	
	public String getString(String tabs) {
		return tabs+tableName;
	}
}
