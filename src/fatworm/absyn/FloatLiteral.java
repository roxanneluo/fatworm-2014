package fatworm.absyn;

public class FloatLiteral extends Constant {
	public Float value;
	public FloatLiteral(Float value) {
		this.value = value;
	}
	
	public String toString() {
		return value.toString();
	}
}
