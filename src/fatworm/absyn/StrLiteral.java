package fatworm.absyn;

public class StrLiteral extends Constant {
	public String str;
	public StrLiteral(String str) {
		this.str = str;
	}
	public String toString() {
		return str;
	}
}
