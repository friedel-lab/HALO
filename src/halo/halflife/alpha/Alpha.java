/*
 * This file is part of HALO version 1.3. 
 */
package halo.halflife.alpha;

/**
 * Contains the function alpha used for half life calculation
 * @author Stefanie Kaufmann
 *
 */
public interface Alpha {

	/**
	 * Function that calculates alpha for a given time point t
	 * @param t A time point t
	 * @return The alpha for this time point
	 */
	public double alpha(double t);
}
