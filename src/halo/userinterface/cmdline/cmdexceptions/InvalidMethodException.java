/*
 * This file is part of the HALO 1.3 command line tool. 
 */
package halo.userinterface.cmdline.cmdexceptions;

/**
 * Exception which is thrown when an invalid method is used
 * @author Stefanie Kaufmann
 *
 */
public class InvalidMethodException extends Exception{
	
	private static final long serialVersionUID = 1769333775967410986L;

	/**
	 * Creates a new exception
	 */
	public InvalidMethodException() {
		
	}
	
	/**
	 * Creates a new exception
	 * @param s
	 */
	public InvalidMethodException(String s) {
		super(s);
	}
}
