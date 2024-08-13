/*
 * This file is part of HALO version 1.3. 
 */
package halo.halflife.alpha;

/**
 * Contains the function alpha and its default value
 * extendable for definition of new alpha functions
 * @author Stefanie Kaufmann
 *
 */
public class AlphaConstant implements Alpha {

	/**
	 * Constructor for a regular alpha object, returning 1 always
	 */
	public AlphaConstant() {
		
	}
	
	/**
	 * Default function for alpha
	 * @param t Time
	 * @return Constant value 1
	 */
	public double alpha(double t) {
		return 1.0;
	}
}
