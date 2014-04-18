package fatworm.absyn;

/*
 * class for binary expression
 */
public class BExpr extends Expr {
	public BopType op;
	public Expr left, right;
	
	public BExpr(Expr l, BopType op, Expr r) {
		this.op = op;
		left = l;
		right = r;
	}
	
	public static enum BopType {
		LEQ,GEQ,LT,GT,AND, OR, EQ, NEQ,
		PLUS,MINUS,TIMES,DIV,MOD	//bop
	};
	
	public String toString() {
		return left+" "+op+" "+right; 
	}
}
