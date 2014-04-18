package fatworm.logicplan;


public class RenamePlan extends UAlgebraPlan {
	String alias;
	public RenamePlan(Plan src, String alias, Plan parent) {
		super(src, parent);
		this.alias = alias;
	}
	
	public RenamePlan(Plan src, String alias) {
		super(src);
		this.alias = alias;
	}
	
	public String getString(String tabs) {
		return tabs+"RenamePlan{\n"
				+src.getString(tabs+"\t")+"\n"
				+tabs+"} AS {"+alias+"}";
	}

}
