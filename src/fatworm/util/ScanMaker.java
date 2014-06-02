package fatworm.util;

import java.sql.SQLException;
import java.util.LinkedList;

import fatworm.absyn.*;
import fatworm.driver.Attribute;
import fatworm.driver.DBDataManager;
import fatworm.driver.Schema;
import fatworm.driver.Tuple;
import fatworm.field.FLOAT;
import fatworm.field.Field;
import fatworm.field.INT;
import fatworm.logicplan.*;
import fatworm.scan.*;

public class ScanMaker {
	//annotate the plan tree and create corresponding scan
	public static Scan plan2Scan(Plan plan) throws SQLException {
		return plan2Scan(plan, null);
	}
	/**
	 * 
	 * @param plan
	 * @param pSchema:parent Schema from above
	 * @return
	 * @throws SQLException
	 */
	public static Scan plan2Scan(Plan plan, Schema pSchema) throws SQLException {
		if (plan instanceof TablePlan) {
			return transTable((TablePlan)plan);
		} else if (plan instanceof ProductPlan) {
			return transProduct((ProductPlan)plan);
		} else if (plan instanceof SelectPlan) {
			return transSelect((SelectPlan) plan, pSchema);
		} else if (plan instanceof ProjectPlan) {
			return transProject((ProjectPlan)plan, pSchema);
		} else if (plan instanceof DeletePlan) {
			return transDelete((DeletePlan)plan);
		} else if (plan instanceof UpdatePlan) {
			return transUpdate((UpdatePlan)plan);
		} else if (plan instanceof AggPlan) {
			return transAgg((AggPlan) plan, pSchema);
		} else if (plan instanceof OrderPlan) {
			return transOrder((OrderPlan) plan, pSchema);
		} else if (plan instanceof DistinctPlan) {
			return transDistinct((DistinctPlan) plan, pSchema);
		}
		return null;
	}
	private static DistinctScan transDistinct(DistinctPlan plan, Schema pSchema) throws SQLException {
		DistinctScan scan = new DistinctScan(plan2Scan(plan.src, pSchema), plan.sorted);
		plan.schema = plan.src.schema.clone(plan.alias, false);
		return scan;
	}
	private static OrderScan transOrder(OrderPlan plan, Schema pSchema) throws SQLException {
		OrderScan scan = new OrderScan(plan2Scan(plan.src, pSchema), plan.asc);
		plan.schema = plan.src.schema.clone(plan.alias, false);
		for (Column col: plan.by) {
			setColIdxParent(col, plan.src.schema);
			scan.by.add(col.idx);
		}
		return scan;
	}
	private static AggScan transAgg(AggPlan plan, Schema pSchema) throws SQLException {
		AggScan scan = new AggScan(plan2Scan(plan.src, pSchema), plan.to, plan.by);
		if (scan.by != null)
			for (Column col:scan.by) 
				setColIdxParent(col, plan.src.schema);
		plan.schema = replaceColCalcSchema(scan.to, plan.src.schema, plan.alias, true, plan.having);
		return scan;
	}
	
	private static UpdateScan transUpdate(UpdatePlan plan) throws SQLException {
		UpdateScan scan = new UpdateScan(transTable((TablePlan)plan.src), plan.cond, plan.cols, plan.vals);
		setColIdxParent(plan.cond, plan.src.schema);
		for (Column col: scan.cols) {
			setColIdxParent(col, plan.src.schema);
		}
		for (Expr val:scan.vals) {
			setColIdxParent(val, plan.src.schema);
		}
		return scan;
	}
	private static TableScan transTable(TablePlan plan) {
		DBDataManager data = DBDataManager.getInstance();
		if (plan.alias != null) {
			for (Attribute attr: plan.schema.attrNames) {
				((Column)attr).tableName = plan.alias;
			}
		}
		return new TableScan(data.currentDB.tables.get((plan).tableName));
	}
	
	private static DeleteScan transDelete(DeletePlan plan) throws SQLException {
		DeleteScan scan = new DeleteScan(transTable((TablePlan)plan.src), plan.cond);
		if (plan.cond != null)
			setColIdxParent(plan.cond, plan.src.schema);
		return scan;
	}
	
	/**
	 * replace column in cols with the srcSchema, and compute the schema
	 * which deals with the rename
	 * convert Func in having but not in AggPlan's to into InvisibleFunc and Add it to the schema 
	 * @param cols (project to cols), srcSchema (schema of plan src), having from AggPlan
	 * @return schema built from cols
	 */
	private static Schema replaceColCalcSchema(LinkedList<Expr> cols, Schema srcSchema, String alias, boolean agg, Expr having) throws SQLException {
		Schema schema = new Schema();
		Expr e;
//		TODO: so far set type of calculated column: double
		for (int i = 0; i < cols.size(); ++i) {
			e = cols.get(i);
			if (e instanceof RenameExpr) {
				RenameExpr rename = (RenameExpr)e;
				rename.expr = replaceCol(rename.expr, srcSchema);
			} else {
				cols.set(i, replaceCol(e, srcSchema));
			}
		}
//		System.out.println("replaceColCalc:"+cols);
		// remove rename, eval type generate Column for schema
		Column col;
		Tuple typeTuple = new Tuple(srcSchema.types);
		for (int i = 0;i < cols.size(); ++i) {
			e = cols.get(i);
			if (e.getClass() == Column.class) {
				col = getProjectCol((Column)e, alias, null, i);
			} else if (e instanceof RenameExpr) {
				RenameExpr rename = (RenameExpr)e;
				if (rename.expr.getClass() == Column.class) {
					col = getProjectCol((Column)rename.expr, alias, rename.alias,i);
				} else {
					col = getProjectCalcCol(rename.expr, alias, rename.alias, i, typeTuple, agg);
				}
				cols.set(i, rename.expr);
			} else {
//				System.out.println(e);
				col = getProjectCalcCol(e, alias, null,i, typeTuple, agg);
			}
			schema.attrNames.add(col);
			schema.types.add(col.type);
		}
		if (having != null) {
			replaceCol(having, srcSchema);
			LinkedList<Column> havingCols = getHavingCols(having);
			
			InvisibleFunc invisible;
			int size = schema.types.size();
			Column havingCol;
			for (int i = 0; i < havingCols.size(); ++i) {
				havingCol = havingCols.get(i);
				if (schema.getColumn(havingCol) == null) {
					//column from getProjCalcCol can only be func, since normal column must be within to
					invisible = new InvisibleFunc((Func)getProjectCalcCol(havingCol, alias, null,size+i, typeTuple, true));
					cols.add(invisible);
					schema.attrNames.add(invisible);
					schema.types.add(invisible.type);
				}
			}
		}
		return schema;
	}
	
	private static LinkedList<Column> getHavingCols(Expr e) throws SQLException {
		LinkedList<Column> ans = new LinkedList<Column>();
		if (e instanceof Column) {
			ans.add((Column) e);
			return ans;
		} else if (e instanceof BExpr) {
			ans.addAll(getHavingCols(((BExpr)e).left));
			ans.addAll(getHavingCols(((BExpr)e).right));
			return ans;
		} else if (e instanceof UExpr) {
			ans.addAll(getHavingCols(((UExpr)e).expr));
			return ans;
		} else if (e instanceof Field)
			return ans;
		throw new SQLException("[ERROR]getHavingCols of "+e);
	}
	/**
	 * TODO:when generating the schema, I haven't deal with the hashmap
	 */
	private static ProjectScan transProject(ProjectPlan plan, Schema pSchema) throws SQLException {
		Scan src = plan2Scan(plan.src, pSchema);
		ProjectScan scan = new ProjectScan(src, plan.columns);
		plan.schema = replaceColCalcSchema(scan.columns, plan.src.schema, plan.alias, false, null);
		return scan;
	}
	/*
	 * TODO: could type conversion here, but I'm too
	 * 		 lazy so far
	 */
	private static Expr replaceCol(Expr e, Schema schema/*, Scan src*/) {
		Column ans = null;
		if (e instanceof Func) { //TODO
			((Func)e).col = (Column) replaceCol(((Func)e).col, schema/*, src*/);
			return e;
		} else if (e.getClass() == Column.class) {
			ans = schema.getColumn((Column)e);
//			System.out.println("[replaceCol]get"+e+" from schema:"+schema+"=>"+ans);
//			ans.table = src;
			return ans;
		} else if (e instanceof UExpr) {
			((UExpr)e).expr = replaceCol(((UExpr)e).expr, schema/*, src*/);
			return e;
		} else if (e instanceof BExpr) {
			BExpr be = (BExpr)e;
			be.left = replaceCol(be.left, schema/*, src*/);
			be.right = replaceCol(be.right, schema/*, src*/);
			return be;
		} else {
			// could not be rename, or exists, anyall, in, TODO:default
			// can only be constant;
			return e;
		}
	}
	private static Column getProjectCol(Column col, String tableAlias, String colAlias, int idx) {
		Column ans = col.clone();
		ans.tableName = tableAlias!=null? tableAlias:col.tableName;
		ans.colName = colAlias!=null? colAlias:col.colName;
		ans.idx = idx;
		return ans;
	}
	
	private static Column getProjectCalcCol(Expr e, String tableAlias, String colAlias, int idx, Tuple typeTuple, boolean agg) throws SQLException {
		Column ans;
		if (e instanceof Func) {
			ans = ((Func) e).clone();
			ans.colName = colAlias;
			ans.tableName = tableAlias;
		} else
			ans = new Column(tableAlias,colAlias);
		ans.idx = idx;
		if (agg)
			ExprManager.init(e, typeTuple);
//		System.out.println("after init: "+e+",t:"+typeTuple);
		ans.type = ExprManager.eval(e, typeTuple, null);
//		System.out.println("schema:"+typeTuple+",eval:"+ans.type.typeValString());
		return ans;
	}
	private static SelectScan transSelect(SelectPlan plan, Schema parentSchema) throws SQLException {
		SelectScan scan = new SelectScan(plan2Scan(plan.src),plan.condition);
		plan.schema = plan.src.schema.clone(plan.alias, false);
		setColIdxParent(scan.cond, plan.src.schema, parentSchema);
		return scan;
	}
	private static void setColIdxParent(Expr expr, Schema schema) throws SQLException {
		setColIdxParent(expr, schema, null);
	}
	/**
	 * set Parent(= true or false) to denote whether the column is from a parent tuple or src tuple
	 * @param expr
	 * @param schema
	 * @param pSchema parent Schema: schema passed from above exists, in, anyAll
	 * @throws SQLException 
	 */
	private static void setColIdxParent(Expr expr, Schema schema, Schema pSchema) throws SQLException {
//		System.out.println("get "+expr+" from schema:"+schema+", pSchema:"+pSchema);
		if (expr instanceof Func) {
			Func func = (Func)expr;
			func.idx = schema.getColIdx(func);
			if (func.idx == null)
				setColIdxParent(func.col, schema, pSchema);
			func.val = func.col.type; 
		} else if (expr.getClass() == Column.class){
			Column col = (Column) expr;
			col.idx = schema.getColIdx(col);
//			System.out.println("get "+col+" from "+schema+", idx="+col.idx);
			col.parent = (col.idx == null);
			if (col.parent)
				col.idx = pSchema.getColIdx(col);
		} else if (expr instanceof BExpr) {
			setColIdxParent(((BExpr)expr).left, schema, pSchema);
			setColIdxParent(((BExpr)expr).right, schema, pSchema);
		} else if (expr instanceof UExpr)
			setColIdxParent(((UExpr)expr).expr, schema, pSchema);
		else if (expr instanceof BQExpr) {
			BQExpr bqe = (BQExpr)expr;
			bqe.scan = plan2Scan(bqe.plan, schema);
			if (expr instanceof In) {
				setColIdxParent(((In)bqe).col, schema, pSchema);
			} else if (expr instanceof AnyAll) {
				setColIdxParent(((AnyAll)bqe).col, schema, pSchema);
			}
		}
		//TODO: haven't deal with and anyall, in
	}
	
	private static ProductScan transProduct(ProductPlan plan) throws SQLException {
		ProductScan scan = new ProductScan(plan2Scan(plan.left), plan2Scan(plan.right));
		plan.schema = Schema.product(plan.left.schema, plan.right.schema,plan.alias/*, scan.left, scan.right*/);
		return scan;
	}
}