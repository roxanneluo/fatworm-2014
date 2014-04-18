package fatworm.absyn;

public class UExpr extends Expr {
	public UopType op;
	public Expr expr;
	
	public UExpr(UopType op, Expr expr) {
		this.op = op;
		this.expr = expr;
	}
	
	public static enum UopType {
		NOT, NEG	//uop
	};
	public String toString() {
		return op.name()+" "+expr;
	}
}
