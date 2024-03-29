package fatworm.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import fatworm.driver.Connection;
import fatworm.driver.DBDataManager;
import fatworm.driver.Statement;

class Main {
//	static String flname = "test.fwt";
	public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException {
//		try {
			
			Class.forName("fatworm.driver.Driver");
			String folder  = "testsuit/infraTest/";
			String[] files = {new String(folder+"isVarCharStable2.fwt")};
//			String[] files = {new String(folder+"fatworm-create.fwt"), new String(folder+"fatworm-update.fwt")};
			for (int i = 0; i < files.length; ++i) {
				execute(files[i]);
			}
//		} catch (Exception e) {
//			System.err.print(e);
//			e.printStackTrace();
//		}
	}
	
	private static void displayResultSet(ResultSet res) throws SQLException {
		ResultSetMetaData schema = res.getMetaData();
		while (res.next()) {
//			System.out.println("hasNext");
			outputOneRecord(res, schema);
		}
	}
	
	private static void execute(String flname) throws IOException, SQLException {
		fatworm.driver.Connection conn = (Connection) DriverManager.getConnection("jdbc:fatworm:/fatworm");
		File file = new File(flname);
		@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		String command;
		while ((line = reader.readLine()) != null) {
			if (line.isEmpty() || line.charAt(0) == '@' || line.charAt(0)=='#') {
				System.out.println(line);
				continue;
			}
			command = "";
			while (line!=null && !line.contains(";")) {
				command += line+" ";
				line = reader.readLine();
			}				
			command += prefixOf(line, ';');
			System.out.println(command);
			fatworm.driver.Statement stmt = (Statement) conn.createStatement();
			System.out.println(stmt.execute(command));
			
			ResultSet res = stmt.getResultSet();
			

			if (res == null || res.getMetaData() == null)
				continue;
			
			displayResultSet(res);
			res.close();
//			System.out.println(PlanMaker.makePlan(command));
			conn.close();
		}
		
		System.out.println(DBDataManager.getInstance());
	}

	private static void outputOneRecord(ResultSet res, ResultSetMetaData schema)
			throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 1; i <= schema.getColumnCount(); ++i) {
			if (i != 1)
				sb.append(", ");
			int type = schema.getColumnType(i);
			Object o = res.getObject(i);
			sb.append(getFieldString(o, type));
		}
		sb.append(")");
		System.out.println(sb.toString());
	}

	private static String getFieldString(Object o, int type) {
		if (o == null)
			return "null";
		switch (type) {
		case java.sql.Types.INTEGER:
			return ((Integer)o).toString();
		case java.sql.Types.BOOLEAN:
			return ((Boolean)o).toString();
		case java.sql.Types.CHAR:
		case java.sql.Types.VARCHAR:
			return "'" + (String)o + "'";
		case java.sql.Types.FLOAT:
			return ((Float)o).toString();
		case java.sql.Types.DATE:
			return ((Date)o).toString();
		case java.sql.Types.DECIMAL:
			return ((BigDecimal)o).toString();
		case java.sql.Types.TIMESTAMP:
			return ((java.sql.Timestamp)o).toString();
		default:
//			logger.error("Undefined Type Number " + type);
//			System.out.println(type);
			return null;
		}
	}
	private static String prefixOf(String str, char split) {
		assert(str != null);
		String ans = "";
		for (int i = 0; i < str.length(); ++i) 
			if (str.charAt(i)==split)
				break;
			else ans += str.charAt(i);
		return ans;
	}
}