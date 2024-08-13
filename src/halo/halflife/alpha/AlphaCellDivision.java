/*
 * This file is part of HALO version 1.3. 
 */
package halo.halflife.alpha;

/**
 * Provides an alpha calculation method for the modeling of cell division
 * @author Stefanie Kaufmann
 *
 */
public class AlphaCellDivision implements Alpha {

	private double ccl;
	
	/**
	 * Constructor for an alpha object for modeling of cell division
	 * @param ccl The parameter ccl used for the calculation
	 */
	public AlphaCellDivision(double ccl) {
		this.ccl = ccl;
	}
	
	/**
	 * The function that calculates alpha according to the cell division model
	 * function
	 * @param t The time for which the alpha will be calculated
	 */
	public double alpha(double t) {
		return Math.pow(2, t/ccl);
	}
	
	/**
	 * Returns the parameter ccl
	 * @return The parameter ccl
	 */
	public double getCcl() {
		return ccl;
	}
}
