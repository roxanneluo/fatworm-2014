package fatworm.driver;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;

import fatworm.logicplan.*;
import fatworm.scan.DeleteScan;
import fatworm.scan.Scan;
import fatworm.scan.TableScan;
import fatworm.scan.UpdateScan;
import fatworm.util.PlanMaker;
import fatworm.util.ScanMaker;

public class Statement implements java.sql.Statement {
	public ResultSet rs = null;
	
	
	@Override
	public boolean execute(String cmd) throws SQLException {
		Plan plan = PlanMaker.makePlan(cmd);
		if (plan == null) return true;
		else if (plan instanceof DBPlan) {
			return execute((DBPlan)plan);
		} else {
			DBDataManager data = DBDataManager.getInstance();
//			System.out.println(data);
			if (data.curDBName == null) {
				System.out.println("[ERR] currentDB is null");
				return false;
			} 
			
			
			if(plan instanceof DropTablePlan) {
				for(String table:((DropTablePlan)plan).tables) {
					if (!data.dropTable(table.toLowerCase()))
						return false;
				}
				data.outputMeta();
				return true;
			} else if (plan instanceof CreateTablePlan) {
				CreateTablePlan create = (CreateTablePlan) plan;
				Table table= new Table(create.tableName, create.schema);
				return data.createTable(table);
			} else if (plan instanceof InsertValuePlan) {
				InsertValuePlan insert = (InsertValuePlan) plan;
				TableScan scan = (TableScan) ScanMaker.transTable(insert.table, true, true);
				boolean ans = scan.insert(insert.value);
				scan.close();
				return ans;
			} else if (plan instanceof InsertQueryPlan) {
				return execute((InsertQueryPlan)plan);
			} else if (plan instanceof DeletePlan){
				DeleteScan scan = (DeleteScan)ScanMaker.plan2Scan(plan);
				boolean ans = scan.delete();
				scan.close();
				return ans;
			} else if (plan instanceof UpdatePlan) {
				return execute((UpdatePlan)plan);
			} else {
				rs = new ResultSet();
				rs.scan = ScanMaker.plan2Scan(plan);
//				System.out.println(rs.scan);
//				System.out.println(plan);
				rs.schema = plan.schema;
				return true;
			}
		}
	}
	private boolean execute(UpdatePlan plan) throws SQLException {
		UpdateScan scan = (UpdateScan) ScanMaker.plan2Scan(plan);
		boolean ans = scan.update();
		scan.close();
		return ans;
	}
	private boolean execute(InsertQueryPlan plan) throws SQLException {
		InsertQueryPlan insert = (InsertQueryPlan)plan;
		Table table = DBDataManager.getInstance().getTable(insert.tableName);
		TableScan toInsert = new TableScan(table, true, true);
		if (table == null)
			throw new SQLException("[table]"+insert.tableName+"does not exist");
		
//		System.out.println(insert.plan);
		Scan subQuery = ScanMaker.plan2Scan(insert.plan);
		if (!table.schema.compatible(insert.plan.schema))
			throw new SQLException("The schema of the subquery in insert("+insert.plan.schema+")"
					+ "is incompatible with that of [table] "+insert.tableName+"("+table.schema+")");
		/*create a copy of the table to insert, since it could probably using one of the table to be
		 * inserted
		 */
//		TableScan tempScan = new TableScan(new Table("temp",insert.plan.schema));
		Tuple t;
		while(subQuery.hasNext()) {
			t = subQuery.next();
//			if (!tempScan.insert(t)) 
//				return false;
			if (!toInsert.insert(t))
				return false;
		}
//		tempScan.restart();
//		/*really insert into desired table now*/
//		while (tempScan.hasNext()) {
//			if (!table.insert(tempScan.next()))
//				return false;
//		}
		toInsert.close();
		subQuery.close();
		return true;
	}
	private boolean execute(DBPlan plan) {
		DBDataManager data = DBDataManager.getInstance();
//		System.out.println(data);
		switch(plan.type) {
		case USE_DB:
			return data.useDB(plan.database);
		case CREATE_DB:
			return data.createDB(plan.database);
		case DROP_DB:
			return data.dropDB(plan.database);
		default:
			System.out.println("invalid database command");
			return false;
		}
	}





	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addBatch(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancel() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearBatch() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearWarnings() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean execute(String arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean execute(String arg0, int[] arg1) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean execute(String arg0, String[] arg1) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int[] executeBatch() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultSet executeQuery(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int executeUpdate(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int executeUpdate(String arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int executeUpdate(String arg0, int[] arg1) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int executeUpdate(String arg0, String[] arg1) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Connection getConnection() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getFetchDirection() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFetchSize() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxRows() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getMoreResults(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		return rs;
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getResultSetType() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getUpdateCount() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isClosed() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPoolable() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCursorName(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEscapeProcessing(boolean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFetchDirection(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFetchSize(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMaxFieldSize(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMaxRows(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPoolable(boolean arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setQueryTimeout(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
