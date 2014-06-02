package fatworm.absyn;

/**
 * inivisible func is used for having only,
 * since some value used in having does not appear in
 * select, I use invisible Func to represent those col
 * whose value appears in having but not in  select
 * so invisibleFunc appears in the schema of aggPlan 
 * but not in having's selectPlan
 * @author roxanne
 *
 */
public class InvisibleFunc extends Func{
	public InvisibleFunc(FuncType funcType, Column expr) {
		super(funcType, expr);
	}
	
	public InvisibleFunc(Func f){
		super(f.func, f.col);
		val = f.val;
		tableName = f.tableName;
		idx = f.idx;
		type = f.type;
		notNull = f.notNull;
		autoInc = f.autoInc;
		deft = f.deft;
	}
}
