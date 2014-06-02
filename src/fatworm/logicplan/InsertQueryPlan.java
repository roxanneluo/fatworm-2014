package fatworm.logicplan;

public class InsertQueryPlan extends Plan {
	public String tableName;
	public Plan plan;
	public InsertQueryPlan(String tableName, Plan plan) {
		this.tableName = tableName;
		this.plan = plan;
	}
	@Override
	public String getString(String tabs) {
		// TODO Auto-generated method stub
		return null;
	}

}
