package fatworm.absyn;

public class Column extends Expr {
	public String tableName;
	public String colName;
	public Column(String tableName, String colName) {
		this.tableName = tableName;
		this.colName = colName;
	}
	public Column(String colName) {
		tableName = null;
		this.colName = colName;
	}
	public String toString() {
		return tableName != null? tableName+"."+colName: colName;
	}
}
