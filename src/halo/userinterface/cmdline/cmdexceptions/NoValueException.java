/*
 * This file is part of the HALO 1.3 command line tool. 
 */
package halo.userinterface.cmdline.cmdexceptions;

/**
 * Exception which is thrown when no value is given for a flag
 * @author Stefanie Kaufmann
 *
 */
public class NoValueException extends Exception {

	private static final long serialVersionUID = 3931226977233536492L;
	private String flag;

	/**
	 * Creates a new exception
	 */
	public NoValueException() {
		super();
	}
	
	/**
	 * Creates a new exception
	 * @param s
	 */
	public NoValueException(String s) {
		flag = s;
	}
	
	public void printMessage() {
		System.err.println("Please check your call again; you have to call every flag with a value!");
		System.err.println("You forgot the value of "+flag);
	}
}