package fatworm.util;

import java.sql.SQLException;
import java.util.LinkedList;

import fatworm.absyn.*;
import fatworm.absyn.BExpr.BopType;
import fatworm.absyn.UExpr.UopType;
import fatworm.driver.Tuple;
import fatworm.field.*;
import fatworm.scan.Scan;

public class ExprManager {
	private static final double err = 0.000001;
	public static void restart(Expr e) throws SQLException {
		if (e instanceof UExpr) {
			UExpr ue = (UExpr)e;
			restart(ue.expr);
		} else if (e instanceof BExpr) {
			BExpr be = (BExpr)e;
			restart(be.left);
			restart(be.right);
		} else if (e instanceof Exists){
			return;
		} else if (e instanceof BQExpr) {
			((BQExpr)e).scan.restart();
			if (e instanceof In) {
				((In)e).scan.restart();
			} else if (e instanceof AnyAll) {
				((AnyAll)e).scan.restart();
			}
		}
	}
	
	public static Field eval(Expr e, Tuple t) throws SQLException {
		return eval(e,t,null);
	}
	/*
	 * TODO: so far haven't dealt with anyall, in
	 */
	public static Field eval(Expr e, Tuple t, Tuple parent) throws SQLException {
//		System.out.println(e);
		if (e instanceof Func) {
			Func func = (Func)e;
			if (func.idx != null) {
				return t.get(func.idx);
			} else
				return func.val;
		} else if (e instanceof Column) {
//			System.out.println("idx = "+((Column)e).idx+" ,t = "+t);
			Column col = (Column)e;
			if (col.parent) return parent.get(col.idx);
			else {
				if (t == null) return NULL.getInstance();
				return t.get(col.idx);
			}
		} else if (e instanceof UExpr) {
			UExpr ue = (UExpr)e;
			Field f = eval(ue.expr, t, parent);
			return evalUExpr(ue.op, f);
		} else if (e instanceof BExpr) {
			BExpr be = (BExpr)e;
			Field l = eval(be.left, t, parent), r = eval(be.right, t, parent);
//			System.out.println("left = "+l+", right = "+r);
			return evalBExpr(l, be.op, r);
		} else if (e instanceof Field) {
			return (Field) e;
		} else if (e instanceof Exists) {
			return evalExists((Exists)e, t);
		} else if (e instanceof In) {
			return evalIn((In)e, t, parent);
		} else if (e instanceof AnyAll) {
			return evalAnyAll((AnyAll)e, t, parent);
		} else if (e instanceof Scan) {
			return evalScan((Scan)e, t);
		}
		
		return null;
	} 
	
	private static Field evalScan(Scan scan, Tuple parent) throws SQLException {
		if (!scan.hasNext(parent))
 			return null;	// FIXME: or NULL.getInstance()?
		else return scan.next(parent).get(0);
	}
	private static Field evalAnyAll(AnyAll e, Tuple t, Tuple parent) throws SQLException {
		Field col = eval(e.col, t, parent);
		e.scan.restart();
		if (e.any) {
			while (e.scan.hasNext(t)) {
				if (toFinalBool(evalBExpr(col, e.op, e.scan.next(t).get(0))))
					return new BOOL(true);
			}
			return new BOOL(false);
		} else {
			while (e.scan.hasNext(t)) {
				Field next = e.scan.next(t).get(0);
				boolean ans = toFinalBool(evalBExpr(col, e.op, next));
//				System.out.println(col.toString()+e.op+"all"+next);
				if (!ans)
					return new BOOL(false);
			}
			return new BOOL(true);
		}
	}
	private static Field evalIn(In in, Tuple t, Tuple parent) throws SQLException {
		Field col = eval(in.col, t, parent);
		in.scan.restart();
		while(in.scan.hasNext(t)) {
			if (col.equals(in.scan.next().get(0)))
				return new BOOL(true);
		}
		return new BOOL(false);
	}
	
	private static Field evalExists(Exists e, Tuple t) throws SQLException {
//		System.out.println(t);
//		System.out.println(e.scan);
		e.scan.restart();
		boolean hasNext = e.scan.hasNext(t);
		e.scan.next();
		return new BOOL(e.exists? hasNext:!hasNext);
	}
	
	public static void init(Expr e, Tuple t) throws SQLException {
		if (e instanceof Func) {
			Func func = (Func)e;
			Field f = eval(func.col, t, null);
			switch(func.func) {
			case COUNT:
				if (f.isNull())
					func.val = new INT(0);
				else func.val = new INT(1);
				break;
			case AVG: case SUM:
				if (f instanceof INT)
//					func.val = new DECIMAL((Integer)f.getVal());
					func.val = new FLOAT((Integer)f.getVal());
				else func.val = f;
				break;
			default:
				func.val = f;
			}
			return;
		}
		if (e instanceof Column)
			return;
		if (e instanceof BExpr) {
			BExpr be = (BExpr)e;
			init(be.left, t);
			init(be.right, t);
		} else if (e instanceof UExpr) {
			init(((UExpr)e).expr, t);
		}
	}
	
//	public static Field eval(Expr e, Tuple t) throws SQLException {
//		return eval(e, t, null);
//	}
	
	/**
	 * evaluate aggregation function on the fly
	 * @param ans
	 * @param t
	 * @param to
	 */
	public static void evalFunc(Expr col, Tuple t) throws SQLException {
		if (col.getClass() == Column.class)
			return;
		if (col instanceof BExpr) {
			BExpr be = (BExpr)col;
			evalFunc(be.left, t);
			evalFunc(be.right, t);
			return;
		}
		if (col instanceof UExpr) {
			evalFunc(((UExpr)col).expr, t);
			return;
		}
		
		Func func = (Func)col;
		Field tf = eval(((Func)col).col, t);
		if (tf.isNull()) 
			return;

		Field af = func.val;
		if (func.func == Func.FuncType.COUNT) {
			++((INT)af).v;
			return;
		} else if (func.func == Func.FuncType.SUM || func.func == Func.FuncType.AVG) {
			double tff;
			if (tf instanceof INT) { 
//				tff = new DECIMAL((Integer)tf.getVal()).getVal();
				tff = new FLOAT((Integer)tf.getVal()).getVal();
			} else 
				tff = (Float)tf.getVal();
			func.val = new FLOAT(evalAgg(func.func, (Float)FLOAT.toFloat(af).getVal(), tff));
			return;
		}
		if (tf instanceof INT) {
			af = INT.toInt(af);
			func.val = new INT(evalAgg(func.func, af.isNull()? null:(float)(((INT)af).v), ((INT)tf).v));
		} else if (tf instanceof DECIMAL) {
			DECIMAL td = (DECIMAL)tf;
			int prec = td.v.precision(), scale = td.v.scale();
			func.val = new DECIMAL(evalAgg(func.func, af.isNull()? null:((DECIMAL)af).v.floatValue(), td.v.floatValue()), prec, scale); //FIXME: if need to write an exact version for decimal
		} else if (tf instanceof FLOAT) {
			func.val = new FLOAT(evalAgg(func.func, af.isNull()? null:((FLOAT)af).v, ((FLOAT)tf).v));
		} else 
			throw new SQLException("[ERROR]evaluating "+func.func+"("+tf.typeValString()+")");
	}
	
	private static Float evalAgg(Func.FuncType func, Float ans, double t) throws SQLException{
		if (ans == null)
			return new Float(t);
		switch(func) {
		case AVG: case SUM:
			return new Float(ans+t);
		case MIN:
			return new Float((ans <= t)? ans: t);
		case MAX:
			return new Float((ans >= t)? ans: t);
		default:
			throw new SQLException(func+" in eval Agg");
		}
	}
	
	public static boolean toFinalBool(Field f) throws SQLException {
		if (f.isNull())
			return false;
		if (f instanceof BOOL)
			return ((BOOL)f).v;
		throw new SQLException("[ERROR]toFinalBool("+f.typeString()+"("+f+"))");
	}
	private static boolean compare(int l, BopType op, int r) {
		switch (op) {
		case LEQ: 	return l <= r;
		case GEQ: 	return l >= r;
		case LT:	return l < r;
		case GT:	return l > r;
		case EQ: 	return l == r;
		case NEQ: 	return l != r;
		default:
			return false;
		}
	}
	
	private static boolean compare(Float l, BopType op, Float r) {
//		System.out.println(l.toString()+op+r);
		switch (op) {
		case LEQ: 	return l <= r;
		case GEQ: 	return l >= r;
		case LT:	return l < r;
		case GT:	return l > r;
		case EQ: 	return Math.abs(l-r) < err;
		case NEQ: 	return Math.abs(l-r) >= err;
		default:
			return false;
		}
	}
	
	
	private static Boolean getBool(Field f) {
		if (f instanceof INT) return ((INT)f).v != 0;
		if (f instanceof FLOAT) return ((FLOAT)f).v != 0;
		if (f instanceof BOOL) return ((BOOL)f).v;
		return null;
	}
	
	private static Field andOr(Boolean lb, BopType op, Boolean rb) throws SQLException {
		if (lb == null && rb == null)
			return NULL.getInstance();
		if (lb == null || rb == null) {
			if (lb == null) lb = rb;
			switch(op) {
			case AND: 
				if (!lb)
					return new BOOL(false);
				return NULL.getInstance();
			case OR:
				if (lb)
					return new BOOL(true);
				return NULL.getInstance();
			}
		}
		
		switch(op) {
		case AND: return new BOOL(lb && rb);
		case OR: return new BOOL(lb || rb);
		}
		
		unreachableError("andOr");
		return null;
	}
	private static boolean compare(String l, BopType op, String r) {
		switch (op) {
		case LEQ: 	return l.compareTo(r) <= 0;
		case GEQ: 	return l.compareTo(r) >= 0;
		case LT:	return l.compareTo(r) < 0;
		case GT:	return l.compareTo(r) > 0;
		case EQ: 	return l.equals(r);
		case NEQ: 	return !l.equals(r);
		default:
			return false;
		}
	}
	
	private static boolean compare(java.sql.Timestamp l, BopType op, java.sql.Timestamp r) {
		switch (op) {
		case LEQ: 	return l.compareTo(r) <= 0;
		case GEQ: 	return l.compareTo(r) >= 0;
		case LT:	return l.compareTo(r) < 0;
		case GT:	return l.compareTo(r) > 0;
		case EQ: 	return l.equals(r);
		case NEQ: 	return !l.equals(r);
		default:
			return false;
		}
	}
	/*
	 * in all evaluation cases, so far I treat decimal the same as float
	 * FIXME if they need to be distinguished
	 */
	private static Field evalBExpr(Field l, BopType op, Field r) throws SQLException {
		switch(op) {
		case EQ: 
			return new BOOL(l.compareTo(r) == 0);
		case NEQ:
			return new BOOL(l.compareTo(r) != 0);
		case LEQ: case GEQ: case LT: case GT:
			if (l instanceof NULL || r instanceof NULL)
				return NULL.getInstance();
			if (l instanceof INT && r instanceof INT) {
				return new BOOL(compare(((INT)l).v,op,((INT)r).v));
			} else if (l instanceof CHAR && r instanceof CHAR) {
				return new BOOL(compare(((CHAR)l).v, op, ((CHAR)r).v));
			} else if (l instanceof TIMESTAMP || r instanceof TIMESTAMP) {
				TIMESTAMP ld = (TIMESTAMP) TIMESTAMP.toTimestamp(l), rd = (TIMESTAMP) TIMESTAMP.toTimestamp(r);
				if (ld == null || rd == null)
					typeError(l,op,r);
				return new BOOL(compare(ld.v, op, rd.v));
			} else if (l instanceof DATE || r instanceof DATE) {
				DATE ld = (DATE) DATE.toDate(l), rd = (DATE) DATE.toDate(r);
				if (ld == null || rd == null)
					typeError(l,op,r);
				return new BOOL(compare(ld.v, op, rd.v));
			} else if (l instanceof CHAR && r instanceof CHAR) {
				return new BOOL(compare(((CHAR)l).v, op, ((CHAR)r).v));
			} 
//			System.out.println(l+op.toString()+r);
			 
			Float lf = (Float) FLOAT.toFloat(l).getVal();
			Float rf = (Float) FLOAT.toFloat(r).getVal();
			if (lf == null || rf == null)
				typeError(l, op, r);
			return new BOOL(compare(lf, op, rf));
		
		case AND: case OR:
			Boolean lb = getBool(l);
			Boolean rb = getBool(r);
			if ((lb != null) && (rb != null)
					|| (l.isNull() && rb != null) 
					|| (r.isNull() && lb != null)
					|| (l.isNull() && r.isNull())) {
				return andOr(lb, op, rb);
			} else 
				typeError(l,op, r);
		default:
//			System.out.println("l:"+l+",r:"+r);
			if (l.isNull() || r.isNull())
				return NULL.getInstance();
			if (l instanceof INT && r instanceof INT) {
				if (op == BopType.DIV) {
					Float lff = (Float) FLOAT.toFloat(l).getVal(), rff = (Float)FLOAT.toFloat(r).getVal();
					return new FLOAT(arithmetic(lff, op, rff));
				}
				return new INT(arithmetic(((INT)l).v, op, ((INT)r).v));
			}
			Float lff = (Float) FLOAT.toFloat(l).getVal(), rff = (Float)FLOAT.toFloat(r).getVal();
			if (lff == null || rff == null)
				typeError(l,op, r);
			else return new FLOAT(arithmetic(lff, op, rff));
		}
		return null;
	}
	private static void unreachableError(String func) throws SQLException {
		throw new SQLException("[ERROR] should be unreachable "
				+ "in "+func);
	}
	private static int arithmetic(int l, BopType op, int r) throws SQLException{
		switch(op) {
		case PLUS:	return l+r;
		case MINUS: return l-r;
		case TIMES: return l*r;
		case DIV:	return l/r;
		case MOD: 	return l % r;
		}
		unreachableError("arithmetic");
		return -1;
	}
	private static double arithmetic(Float l, BopType op, Float r) throws SQLException {
		switch(op) {
		case PLUS:	return l+r;
		case MINUS: return l-r;
		case TIMES: return l*r;
		case DIV:	return l/r;
		case MOD: 	return l % r;
		}
		unreachableError("arithmetic");
		return -1;
	}
	private static void typeError(Field l,BopType op, Field r) throws SQLException {
		throw new SQLException("[ERROR]evaluating "+l.typeString()+"("+l+") "
				+op+" "+r.typeString()+"("+r+")");
	}
	private static Field evalUExpr(UopType op, Field f) throws SQLException {
			switch(op) {
			case NOT:
				if (f instanceof BOOL) 
					return new BOOL(!((BOOL)f).v);
				
				if (f instanceof FLOAT) 
					return new BOOL(((FLOAT)f).v==0);
				
				if (f instanceof INT) 
					return new BOOL(((INT)f).v == 0);
				
				if (f instanceof NULL)
					return NULL.getInstance();
				
				throw new SQLException("[ERROR]evaluating !"+f.typeString());
			case NEG:
				if (f instanceof FLOAT) 
					return new FLOAT(-((FLOAT)f).v);
				if (f instanceof INT) 
					return new INT(-((INT)f).v);
				if (f instanceof NULL)
					return NULL.getInstance();
				throw new SQLException("[ERROR]evaluating -"+f.typeString());
			default:
				throw new SQLException("[ERROR]evaluating "+op);
			}
	}
}
