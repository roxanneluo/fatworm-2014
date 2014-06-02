package fatworm.scan;

import java.sql.SQLException;

import fatworm.driver.Tuple;

abstract public class Scan {
	public abstract boolean hasNext(Tuple parent) throws SQLException;
	public boolean hasNext() throws SQLException {
		return hasNext(null);
	}
	public abstract Tuple next(Tuple parent) throws SQLException;
	public  Tuple next() throws SQLException {
		return next(null);
	}
	public abstract void restart() throws SQLException;
}
