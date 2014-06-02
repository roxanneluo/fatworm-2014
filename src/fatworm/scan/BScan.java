package fatworm.scan;

public abstract class BScan extends Scan {
	public Scan left, right;
	public BScan(Scan left, Scan right) {
		this.left = left;
		this.right = right;
	}
}
