package fatworm.logicplan;

public class ProductPlan extends Plan {
	public Plan left, right;
	public ProductPlan(Plan left, Plan right, Plan parent) {
		super(parent);
		this.left = left;
		this.right = right;
		left.parent = right.parent = this;
	}
	
	public ProductPlan( Plan left, Plan right) {
		super();
		this.left = left;
		this.right = right;
		left.parent = right.parent = this;
	}
	
	public String getString(String tabs) {
		return tabs+"ProductPlan{\n"
				+left.getString(tabs+"\t")+"\n"+tabs+"}*{\n"
				+right.getString(tabs+"\t")+"\n"+tabs+"}";
	}
}
