package fatworm.logicplan;

public class DistinctPlan extends UAlgebraPlan {
	public boolean sorted = false;
	public DistinctPlan(Plan src, Plan parent) {
		super(src, parent);
	}
	
	public DistinctPlan (Plan src, boolean sorted) {
		super(src);
		this.sorted = sorted;
	}
	
	public String getString(String tabs) {
		return tabs+"DistinctPlan{\n"+src.getString(tabs+"\t")+"\n"+tabs+"}";
	}
}
