/*
 * This file is part of HALO version 1.3.
 */
package halo.data.dataExceptions;

/**
 * Is thrown when the number of columns containing expression values is inconsistent
 * throughout the data file, meaning that there are RNA values missing for some probesets
 * @author Stefanie Kaufmann
 *
 */
public class InconsistentColumnNumberException extends Exception {
	
	private static final long serialVersionUID = 1967991737081990623L;

	/**
	 * Creates a new exception
	 */
	public InconsistentColumnNumberException() {
		super();
	}
	
	/**
	 * Creates a new exception
	 * @param s
	 */
	public InconsistentColumnNumberException(String s) {
		super(s);
	}

}
