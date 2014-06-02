package fatworm.absyn;

import fatworm.logicplan.Plan;
import fatworm.scan.Scan;

/**
 * BQExpr is the super class of expr with binary query, i.e. the super class of anyall
 * exists, in
 * @author roxanne
 *
 */
abstract public class BQExpr extends Expr{
	public Scan scan;
	public Plan plan;
	public BQExpr(Plan plan) {
		this.plan = plan;
	}
}
