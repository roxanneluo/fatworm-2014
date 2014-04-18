package fatworm.logicplan;

public abstract class UAlgebraPlan extends Plan {
	public Plan src;
	public UAlgebraPlan(Plan src) {
		super();
		this.src = src;
		src.parent = this;
	}
	
	public UAlgebraPlan(Plan src, Plan parent) {
		super(parent);
		this.src = src;
		src.parent = this;
	}
}
