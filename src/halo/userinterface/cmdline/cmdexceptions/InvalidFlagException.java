/*
 * This file is part of the HALO 1.3 command line tool. 
 */
package halo.userinterface.cmdline.cmdexceptions;

/**
 * Exception that is called if a method is called with a flag that is not specified
 * @author Stefanie Kaufmann
 *
 */
public class InvalidFlagException extends Exception {
	
	private static final long serialVersionUID = 1194462908251849445L;
	private String flag;

	/**
	 * Creates a new exception
	 */
	public InvalidFlagException() {
	}
	
	/**
	 * Creates a new exception with the causing flag
	 * @param s Flag which caused the exception
	 */
	public InvalidFlagException(String s) {
		flag = s;
	}
	
	/**
	 * Returns the causing flag
	 * @return The causing flag
	 */
	public String getFlag() {
		return flag;
	}
	
}
