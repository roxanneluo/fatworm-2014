package fatworm.absyn;

import fatworm.field.Field;

public class Func extends Column {
	public FuncType func;
	public Column col;
	public Field val = null;
	// among all expr, only func's value is calculated by taking new tuple one by one
	// its partial result is stored in the val; 
	// when evaluating func, its value is replaced by field val;
	public Func(FuncType funcType, Column expr) {
		super(null);
		this. func = funcType;
		this.col = expr;
	}
	
	public static enum FuncType {
		 AVG, COUNT, MIN, MAX, SUM
	};
	public String toString() {
		return func+"("+col+")"+"<"+idx+">";
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof Column))
			return false;
		if (super.equals(o))
			return true;
		if (!(o instanceof Func))
			return false;
		Func f = (Func)o;
		return func == f.func && col.equals(f.col);
	}
}
