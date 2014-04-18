package fatworm.logicplan;

public class DistinctPlan extends UAlgebraPlan {
	public DistinctPlan(Plan src, Plan parent) {
		super(src, parent);
	}
	
	public DistinctPlan (Plan src) {
		super(src);
	}
	
	public String getString(String tabs) {
		return tabs+"DistinctPlan{\n"+src.getString(tabs+"\t")+"\n"+tabs+"}";
	}
}
