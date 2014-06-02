package fatworm.scan;


public abstract class UScan extends Scan {
	public Scan scan;
	public UScan(Scan scan) {
		this.scan = scan;
	}
}
