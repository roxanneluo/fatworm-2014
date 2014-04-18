package fatworm.absyn;

public class RenameExpr extends Expr {
	public String alias;
	public Expr expr;
	public RenameExpr(Expr expr, String alias) {
		this.expr = expr;
		this.alias = alias;
	}
	public String toString() {
		return expr+" AS "+alias;
	}
}
