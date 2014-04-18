package fatworm.util;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import fatworm.absyn.*;
import fatworm.absyn.BExpr.BopType;
import fatworm.absyn.Func.FuncType;
import fatworm.absyn.UExpr.UopType;
import fatworm.logicplan.*;
import fatworm.parser.FatwormLexer;
import fatworm.parser.FatwormParser;
import static fatworm.parser.FatwormParser.*;

public class PlanMaker {
	private static CommonTree parse(String str) {
		try {
			ANTLRStringStream input = new ANTLRStringStream(str);
			FatwormLexer lexer = new FatwormLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			FatwormParser parser = new FatwormParser(tokens);
			return (CommonTree)(parser.statement().getTree());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static Plan makePlan(String str) {
		CommonTree t = parse(str);
		return translate(t);
	}
	
	private static Plan translate(CommonTree t){
		switch(t.getType()) {
		case SELECT:
		case SELECT_DISTINCT:
			return transSelect(t);
		}
		return null;
	}
	
	/*
	 * select query => projection, no projection, aggregate
	 * no project iff *
	 * aggregate if func (|| group by || having)
	 * product=>select=>alias=>order=>	aggregate	=> having	=> rename	=>distinct
	 * 									|project	=> rename	=> distinct
	 */
	private static Plan transSelect(CommonTree t) {
		List<Expr> attributes = new LinkedList<Expr>();
		List<Expr> funcs = new LinkedList<Expr>();
		List<RenameExpr> renames = new LinkedList<RenameExpr>();	//a list of rename for attributes
		boolean needAgg = false;	// need aggregate instead of project
		boolean distinct = t.getType() == SELECT_DISTINCT;
		boolean projectAll = false;
		Plan from = null;
		Expr where = null;
		LinkedList<Column> groupBy = new LinkedList<Column>();
		Expr having = null;
		// orderBy and asc are for order by
		LinkedList<Column> orderBy = new LinkedList<Column>();	
		LinkedList<Boolean> asc = new LinkedList<Boolean>();
		
		for (CommonTree child:(List<CommonTree>) t.getChildren()) {
			switch(child.getType()) {
			case FROM:
				from = transFrom(child);
				break;
			case WHERE:
				where = transExpr(child.getChild(0));
				break;
			case GROUP:
				assert(needAgg); needAgg = true;
				for (CommonTree col:(List<CommonTree>)child.getChildren())
					groupBy.add(transColumn(col));
				break;
			case HAVING:
				assert(needAgg); needAgg = true;
				having = transExpr(child.getChild(0));
				break;
			case ORDER:
				for (CommonTree col:(List<CommonTree>)child.getChildren()) {
					if (col.getType() == ASC || col.getType() == DESC) {
						orderBy.add(transColumn(col.getChild(0)));
						asc.add(col.getType()==ASC);
					} else {
						orderBy.add(transColumn(col));
						asc.add(true);
					}
				}
				break;
			case AVG: case COUNT: case MIN: case MAX:case SUM:
				needAgg = true; 
				funcs.add(transFunc(child));
				break;
			case AS:
				RenameExpr rename = (RenameExpr)transExpr(child);
				if (rename.expr instanceof Func) {
					needAgg = true;
					funcs.add(rename);
				} else attributes.add(rename);
				renames.add(rename);
				break;
			default:
				if (child.getText().equals("*")){
					projectAll = true;
				} else {
					attributes.add(transExpr(child)); //if there's AS, the returned attribute is a renameExpr
				}
			}
		}
		
		assert(from != null);
		Plan ans = from;
		if (where != null) 
			ans = new SelectPlan(from, where);
		if (!orderBy.isEmpty())
			ans = new OrderPlan(ans, orderBy, asc);
		if (!needAgg) {
			if (!projectAll) 
				ans = new ProjectPlan(ans, attributes); 
				// attributes' rename are done within projection
		} else {
			// check attributes == groupBy
			assert(attributes.equals(groupBy)); //FIXME
			if (groupBy.isEmpty()) groupBy = null;
			ans = new AggregatePlan(ans,funcs, groupBy);
			if (having != null)
				ans = new SelectPlan(ans, having);
		}
		if (distinct) ans = new DistinctPlan(ans);
		return ans;
	}
	
	

	private static Plan transTable(CommonTree t) {
		switch(t.getType()) {
		case AS:
			return new RenamePlan(transTable((CommonTree)t.getChild(0)), t.getChild(1).getText());
		case SELECT: case SELECT_DISTINCT:
			return transSelect(t);
		default:
			assert(t.getType() == ID);
			return new TablePlan(t.getText());
		}
	}
	
	private static Plan transFrom(CommonTree t) {
		Plan prod = transTable((CommonTree)t.getChild(0));
		Iterator<CommonTree> iter = ((List<CommonTree>)t.getChildren()).iterator();
		iter.next();
		while(iter.hasNext()) {
			prod = new ProductPlan(prod,transTable(iter.next()));
		}
		return prod;
	}
	
	private static Column transColumn(Tree t) {
		if (t.getText().equals("."))
			return new Column(t.getChild(0).getText(),t.getChild(1).getText());
		else return new Column(t.getText());
	}
	private static Func transFunc(Tree t) {
		Expr x = transExpr(t.getChild(0));
		FuncType func = null;
		switch(t.getType()) {
		case AVG: func = FuncType.AVG;		break;
		case COUNT: func = FuncType.COUNT;	break;
		case MIN: func = FuncType.MIN;		break;
		case MAX: func = FuncType.MAX;		break;
		default: 
			assert(t.getType() == SUM);
			func = FuncType.SUM;
		}
		return new Func(func, x);
	}
	private static BopType extractBop(String ops){
		BopType op = null;
		if(ops.equals("+"))
			op = BopType.PLUS;
		else if(ops.equals("-"))
			op = BopType.MINUS;
		else if(ops.equals("*"))
			op = BopType.TIMES;
		else if(ops.equals("/"))
			op = BopType.DIV;
		else if(ops.equals("%"))
			op = BopType.MOD;
		else if(ops.equals("="))
			op = BopType.EQ;
		else if(ops.equals("<"))
			op = BopType.LT;
		else if(ops.equals(">"))
			op = BopType.GT;
		else if(ops.equals("<="))
			op = BopType.LEQ;
		else if(ops.equals(">="))
			op = BopType.GEQ;
		else if(ops.equals("<>"))
			op = BopType.NEQ;
		return op;
	}
	
	private static String strip(String str) {
		return str.substring(1, str.length()-1);
	}
	private static Expr transExpr(Tree t) {
		Expr l = null, r = null;
		BopType op;
		if (t.getText().equals(".") || t.getType() == ID)
			return transColumn(t);
		Plan table = null;
		switch(t.getType()) {
		case AVG: case COUNT: case MIN: case MAX:case SUM:
			return transFunc(t);
		case AS:
			return new RenameExpr(transExpr(t.getChild(0)), t.getChild(1).getText());
		case INTEGER_LITERAL: 
			try {
				Integer z=Integer.parseInt(t.getText());
				return new IntLiteral(z);
			} catch (NumberFormatException e) {
				return new IntLiteral(new Integer(t.getText())); //FIXME maybe I need BigInteger someday
			}
		case FLOAT_LITERAL:
			return new FloatLiteral(Float.parseFloat(t.getText()));
		case STRING_LITERAL:
			return new StrLiteral(strip(t.getText()));
		case TRUE: case FALSE:
			return new Bool(t.getType() == TRUE);
		case DEFAULT: return new Default();
		case NULL: return new Null();
		case EXISTS: case NOT_EXISTS:
			return new Exists(t.getType() == EXISTS, transSelect((CommonTree)t.getChild(0)));
		case IN:
			l= transExpr(t.getChild(0));
			return new In(l, transSelect((CommonTree)t.getChild(1)));
		case ANY: case ALL:
			l = transExpr(t.getChild(0));
			op = extractBop(t.getChild(1).getText());
			table = transSelect((CommonTree)t.getChild(2));
			return new AnyAll(l, op, t.getType()==ANY, table);
		case OR: case AND: 
			l = transExpr(t.getChild(0));
			r = transExpr(t.getChild(1));
			return new BExpr(l,t.getType()==OR? BopType.OR:BopType.AND,r);
		default:
			l = transExpr(t.getChild(0));
			if (t.getChildCount() == 1) {
				assert(t.getText().equals("-"));
				return new UExpr(UopType.NEG,l);
			} else {
				op = extractBop(t.getText());
				r = transExpr(t.getChild(1));
				return new BExpr(l, op, r);
			}
		}
	}
	
}	

