package fatworm.util;

import static fatworm.parser.FatwormParser.*;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import fatworm.absyn.AnyAll;
import fatworm.absyn.BExpr;
import fatworm.absyn.BExpr.BopType;
import fatworm.absyn.Column;
import fatworm.absyn.Default;
import fatworm.absyn.Exists;
import fatworm.absyn.Expr;
import fatworm.absyn.Func;
import fatworm.absyn.Func.FuncType;
import fatworm.absyn.In;
import fatworm.absyn.RenameExpr;
import fatworm.absyn.UExpr;
import fatworm.absyn.UExpr.UopType;
import fatworm.driver.Attribute;
import fatworm.driver.Schema;
import fatworm.driver.Tuple;
import fatworm.logicplan.AggPlan;
import fatworm.logicplan.CalculatorPlan;
import fatworm.logicplan.CreateTablePlan;
import fatworm.logicplan.DBPlan;
import fatworm.logicplan.DBPlan.DBCmdType;
import fatworm.logicplan.DeletePlan;
import fatworm.logicplan.DistinctPlan;
import fatworm.logicplan.DropTablePlan;
import fatworm.logicplan.InsertQueryPlan;
import fatworm.logicplan.InsertValuePlan;
import fatworm.logicplan.OrderPlan;
import fatworm.logicplan.Plan;
import fatworm.logicplan.ProductPlan;
import fatworm.logicplan.ProjectPlan;
import fatworm.logicplan.SelectPlan;
import fatworm.logicplan.TablePlan;
import fatworm.logicplan.UpdatePlan;
import fatworm.parser.FatwormLexer;
import fatworm.parser.FatwormParser;
import fatworm.field.*;

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
	public static Plan makePlan(String str) throws SQLException {
		CommonTree t = parse(str);
		return translate(t);
	}
	
	private static Plan translate(CommonTree t) throws SQLException{
		switch(t.getType()) {
		case SELECT:
		case SELECT_DISTINCT:
			return transSelect(t);
		case CREATE_DATABASE:
		case USE_DATABASE:
		case DROP_DATABASE:
			DBCmdType dbType = null;
			if (t.getType() == CREATE_DATABASE)
				dbType = DBCmdType.CREATE_DB;
			else if (t.getType() == USE_DATABASE)
				dbType = DBCmdType.USE_DB;
			else dbType = DBCmdType.DROP_DB;
			String database = t.getChild(0).getText();
			return new DBPlan(database, dbType);
		case DROP_TABLE:
			DropTablePlan drop = new DropTablePlan();
			for (int i = 0; i < t.getChildCount(); ++i) {
				drop.tables.add(t.getChild(i).getText());
			}
			return drop;
		case CREATE_TABLE:
			String table = t.getChild(0).getText();
			Schema schema = new Schema();
			CommonTree child;
			int cnt = 0;
			Attribute attr = null;
			for (int i = 1; i < t.getChildCount(); ++i) {
				child = (CommonTree) t.getChild(i);
				if (child.getType() == PRIMARY_KEY) {
					schema.primaryKey = child.getText();
				} else {
					String attrName = child.getChild(0).getText();
					attr = transAttribute(child, cnt++);
					schema.attrNames.add(attr);
					schema.attributes.put(attrName, attr);
					schema.types.add(attr.type);
				}
			}
			return new CreateTablePlan(table, schema);
		case INSERT_VALUES:
		case INSERT_COLUMNS:
		case INSERT_SUBQUERY:
			return transInsert(t);
		case DELETE:
			return transDelete(t);
		case UPDATE:
			return transUpdate(t);
		}
		return null;
	}
	
	private static UpdatePlan transUpdate(CommonTree t) throws SQLException {
		Plan table = transTable((CommonTree)t.getChild(0));
		Expr cond = null;
		int cnt = t.getChildCount();
		CommonTree last = (CommonTree) t.getChild(cnt-1);
		if (last.getType() != UPDATE_PAIR) {
			cond = transExpr(last.getChild(0));
			--cnt;
		}
		UpdatePlan update = new UpdatePlan(table, cond);
		CommonTree child; Column col; Expr val;
		for (int i = 1; i < cnt; ++i) {
			child = (CommonTree) t.getChild(i);
			col = transColumn(child.getChild(0));
			val = transExpr(child.getChild(1));
			update.cols.add(col);
			update.vals.add(val);
		}
		return update;
	}
	
	private static DeletePlan transDelete(CommonTree t) throws SQLException {
		Plan table = transTable((CommonTree)t.getChild(0));
		Expr cond = null;
		if (t.getChildCount() == 2) {
			cond = transExpr(t.getChild(1).getChild(0));
		}
		return new DeletePlan(table, cond);
	}
	
	
	private static Plan transInsert(CommonTree t) throws SQLException {
		TablePlan table = (TablePlan) transTable((CommonTree)t.getChild(0));
		switch(t.getType()) {
		case INSERT_SUBQUERY: 
			Plan plan = translate((CommonTree)t.getChild(1));
			return new InsertQueryPlan(t.getChild(0).getText(), plan);
		default:
			Tuple tuple = new Tuple();
			CommonTree value = null;
			if (t.getType() == INSERT_VALUES) {
				value = (CommonTree) t.getChild(1);
				//TODO
				if (value.getChildCount() != table.schema.attrNames.size())  {
					throw new SQLException("# of values' column = "+value.getChildCount()+" != "+ table.schema.attrNames.size()+"is incorrect in translating INSERT_VALUES");
				}
				CommonTree attr;
				Field type = null;
				for (int i = 0; i < value.getChildCount(); ++i) {
					attr = (CommonTree) value.getChild(i);
					if (attr.getType() == DEFAULT) {
						tuple.add(table.schema.attrNames.get(i).getDefault());
					} else {
						type = table.schema.types.get(i);
						tuple.add(ExprManager.eval(transExpr(attr, null, type),null));
//						tuple.add(Field.getField(type, attr.getText()));
					}
				}
			} else {
				value = (CommonTree) t.getChild(t.getChildCount()-1);
				if (value.getChildCount() != t.getChildCount()-2) 
					throw new SQLException("#of column != # of values","translating INSERT_COLUMN");
				
				tuple.initSize(table.schema.attributes.size());
				Column col = null;
				Attribute attribute = null;
				CommonTree attr = null;
				for (int i = 1; i < t.getChildCount()-1; ++i) {
					col = transColumn(t.getChild(i));
					attr = (CommonTree) value.getChild(i-1);
					attribute = table.schema.attributes.get(col.colName);
					if (attribute == null) {
						throw new SQLException("colname "+col.colName+"not exist");
					}
					tuple.set(attribute.idx, Field.getField(attribute.type, attr.getText()));
				}
				
			}
			return new InsertValuePlan(table, tuple);
		}
	}
	private static Field getType(CommonTree t) throws SQLException {
		switch(t.getType()) {
		case INT: return new INT();
		case FLOAT: return new FLOAT();
		case CHAR: return new CHAR(Integer.parseInt(t.getChild(0).getText()));	//FIXME:ignore the length so far
		case DATETIME: 
			return new DATE();
		case BOOLEAN: return new BOOL();
		case DECIMAL: 
			int headLen = Integer.parseInt(t.getChild(0).getText());
			int tailLen = t.getChildCount()>1? Integer.parseInt(t.getChild(1).getText()):0;
			System.out.println("head:"+headLen+", tailLen:"+tailLen);
			DECIMAL d = new DECIMAL(headLen+tailLen, tailLen);
			System.out.println(d.typeValString());
			return d;
		case TIMESTAMP:
			return new TIMESTAMP();
		case VARCHAR:
			return new VARCHAR(Integer.parseInt(t.getChild(0).getText()));	//FIXME:ignore the length so far
		default:
				throw new SQLException("[ERR] unknown type");
		
		}
	}
	
	private static Attribute transAttribute(CommonTree t, int idx) throws SQLException {
		Attribute attr = new Attribute();
		attr.colName = t.getChild(0).getText();
		attr.type = getType((CommonTree)t.getChild(1));
		attr.idx = idx;
		CommonTree child = null;
		for (int i = 2; i < t.getChildCount(); ++i) {
			child = (CommonTree) t.getChild(i);
			switch(child.getType()) {
			case NULL:
				attr.notNull = (child.getChildCount()==1);
				break;
			case AUTO_INCREMENT:
				attr.autoInc = true;
				break;
			case DEFAULT:
//				System.out.println("default:"+());
				attr.deft = Field.getField(attr.type, child.getChild(0).getText());
			}
		}
		return attr;
	}
	
	/*
	 * select query => projection, no projection, aggregate
	 * no project iff *
	 * aggregate if func (|| group by || having)
	 * product=>select=>alias=>	aggregate	=> having	=> order	=>	distinct
	 * 							|project+colAlias 
	 * tableAliasing at whatever that is the last plans
	 */
	private static Plan transSelect(CommonTree t) throws SQLException {
		LinkedList<Expr> attributes = new LinkedList<Expr>();
		BOOL needAgg = new BOOL(false);	// need aggregate instead of project
		boolean distinct = t.getType() == SELECT_DISTINCT;
		boolean projectAll = false;
		Plan from = null;
		Expr where = null;
		LinkedList<Column> groupBy = new LinkedList<Column>();
		Expr having = null;
		LinkedList<RenameExpr> renames = new LinkedList<RenameExpr>(); 
		// orderBy and asc are for order by
		ArrayList<Column> orderBy = new ArrayList<Column>();	
		ArrayList<Boolean> asc = new ArrayList<Boolean>();
		
		for (CommonTree child:(List<CommonTree>) t.getChildren()) {
			switch(child.getType()) {
			case FROM:
				from = transFrom(child);
				break;
			case WHERE:
				where = transExpr(child.getChild(0));
				break;
			case GROUP:
				needAgg.v = true;
				for (CommonTree col:(List<CommonTree>)child.getChildren())
					groupBy.add(transColumn(col));
				break;
			case HAVING:
				needAgg.v = true;
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
			case AS:
				RenameExpr rename = (RenameExpr)transExpr(child);
				attributes.add(rename);
				renames.add(rename);
				break;
			/*case AVG: case COUNT: case MIN: case MAX:case SUM:
				needAgg = true; 
				attributes.add(transFunc(child));
				break;
			*/
			default:
				if (child.getText().equals("*") && child.getChildCount() == 0){
					projectAll = true;
				} else {
					attributes.add(transExpr(child, needAgg, null)); //if there's AS, the returned attribute is a renameExpr
				}
			}
		}
//		System.out.println("transselect:"+attributes);
		Plan ans = from;
		if (where != null) 
			ans = new SelectPlan(from, where);
		
		if (from == null) {
			return new CalculatorPlan(attributes);
		}
		
		boolean ordered = false;
		if (!orderBy.isEmpty() && extraColInOrderBy(orderBy, attributes)) {
			ans = new OrderPlan(ans, orderBy, asc);
			ordered = true;
			
		}
		if (!needAgg.v) {
			if (!projectAll) 
				ans = new ProjectPlan(ans, attributes); 
				// attributes' rename are done within projection
		} else {
			// check attributes == groupBy
			
			if (groupBy.isEmpty()) groupBy = null;
			ans = new AggPlan(ans,attributes, groupBy);
			if (having != null) {
				if (!renames.isEmpty())
					having = replaceByAlias(having, renames);
				((AggPlan)ans).having = having;
				ans = new SelectPlan(ans, having);
			}
		}
		if (!ordered && !orderBy.isEmpty())
			ans = new OrderPlan(ans, orderBy, asc);
		if (distinct) ans = new DistinctPlan(ans, !orderBy.isEmpty());
		return ans;
	}
	
	private static boolean extraColInOrderBy(ArrayList<Column> orderBy, LinkedList<Expr> attributes) {
		Expr cmp;
		for (Column col:orderBy) {
			boolean exists = false;
			for (Expr attr:attributes) {
				cmp = attr instanceof RenameExpr? new Column(null,((RenameExpr)attr).alias):attr;
				if (col.equals(cmp)) {
					exists = true;
					break;
				}
			}
			if (!exists)
				return true;
		}
		return false;
	}
	
	private static Expr replaceByAlias(Expr e, LinkedList<RenameExpr> renames) throws SQLException {
		if (e instanceof Column) {
			for (RenameExpr rename:renames) {
				if (e.equals(rename.expr))
					return new Column(null, rename.alias);
			}
			return e;
		} 
		
		if (e instanceof BExpr) {
			BExpr be = (BExpr)e;
			be.left = replaceByAlias(be.left, renames);
			be.right = replaceByAlias(be.right, renames);
			return be;
		}
		
		if (e instanceof UExpr) {
			((UExpr)e).expr = replaceByAlias(((UExpr)e).expr, renames);
			return e;
		}
		throw new SQLException("replacing "+e+" be alias");
	}
	

	private static Plan transTable(CommonTree t) throws SQLException {
		switch(t.getType()) {
		case AS:
			Plan plan = transTable((CommonTree)t.getChild(0));
			plan.alias = t.getChild(1).getText();
			return plan;
		case SELECT: case SELECT_DISTINCT:
			return transSelect(t);
		default:
			assert(t.getType() == ID);
			return new TablePlan(t.getText());
		}
	}
	
	private static Plan transFrom(CommonTree t) throws SQLException {
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
	private static Func transFunc(Tree t) throws SQLException {
		Column x = (Column)transExpr(t.getChild(0));
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
	
	private static Expr transExpr(Tree t) throws SQLException {
		return transExpr(t, null, null);
	}
	
	private static Expr transExpr(Tree t, BOOL needAgg, Field type) throws SQLException {
		Expr l = null, r = null;
		BopType op;
		if (t.getText().equals(".") || t.getType() == ID)
			return transColumn(t);
		Plan table = null;
		switch(t.getType()) {
		case SELECT: case SELECT_DISTINCT:
			return transSelect((CommonTree)t);
		case AVG: case COUNT: case MIN: case MAX:case SUM:
			if (needAgg != null)
				needAgg.v = true;
			return transFunc(t);
		case AS:
			return new RenameExpr(transExpr(t.getChild(0), needAgg, type), t.getChild(1).getText());
		case INTEGER_LITERAL: 
			if (type instanceof DECIMAL) {
				DECIMAL dt = (DECIMAL)type;
				DECIMAL d =  new DECIMAL(t.getText(),dt.v.precision(), dt.v.scale());
				return d;
			} else if (type instanceof FLOAT)
				return new FLOAT(t.getText());// FIXME: need this?
			else return new INT(t.getText());	// include null
		case FLOAT_LITERAL:
			if (type instanceof DECIMAL)
				return new DECIMAL(t.getText(), ((DECIMAL) type).v.precision(), ((DECIMAL) type).v.scale());
			else return new FLOAT(t.getText());
		case STRING_LITERAL:
			String str = strip(t.getText());
			if (type instanceof VARCHAR)
				return new VARCHAR(str, ((VARCHAR) type).len);
			else if (type instanceof CHAR)
				return new CHAR(str, ((CHAR)type).len);
			else if (type instanceof DATE)
				return new DATE(str);
			else if (type instanceof TIMESTAMP)
				return new TIMESTAMP(str);
			else return new VARCHAR(str);
		case TRUE: case FALSE:
			return new BOOL(t.getType() == TRUE);
		case DEFAULT: return new Default(); 
		case NULL: return fatworm.field.NULL.getInstance();
		case EXISTS: case NOT_EXISTS:
			return new Exists(t.getType() == EXISTS, transSelect((CommonTree)t.getChild(0)));
		case IN:
			l= transExpr(t.getChild(0), null, type);
			return new In((Column)l, transSelect((CommonTree)t.getChild(1)));
		case ANY: case ALL:
			l = transExpr(t.getChild(0), null, null);
			op = extractBop(t.getChild(1).getText());
			table = transSelect((CommonTree)t.getChild(2));
			return new AnyAll((Column)l, op, t.getType()==ANY, table);
		case OR: case AND: 
			l = transExpr(t.getChild(0), needAgg, type);
			r = transExpr(t.getChild(1), needAgg, type);
			return new BExpr(l,t.getType()==OR? BopType.OR:BopType.AND,r);
		default:
			l = transExpr(t.getChild(0), needAgg, type);
			if (t.getChildCount() == 1) {
				assert(t.getText().equals("-"));
				return new UExpr(UopType.NEG,l);
			} else {
				op = extractBop(t.getText());
				r = transExpr(t.getChild(1), needAgg, type);
				return new BExpr(l, op, r);
			}
		}
	}
	
}	

