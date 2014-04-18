package fatworm.absyn;

public class Func extends Expr {
	FuncType func;
	Expr expr;
	public Func(FuncType funcType, Expr expr) {
		this. func = funcType;
		this.expr = expr;
	}
	
	public static enum FuncType {
		 AVG, COUNT, MIN, MAX, SUM
	};
	public String toString() {
		return func+"("+expr+")";
	}
}
