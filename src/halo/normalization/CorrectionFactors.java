/*
 * This file is part of HALO version 1.3. 
 */
package halo.normalization;

/**
 * Class for managing the correction factors needed for half-life calculation
 * @author Stefanie Kaufmann
 *
 */
public class CorrectionFactors {

	private double c_l;
	private double c_u;
	private double c_lu;
	private static final int UNDEF = -1;
	
	/**
	 * Constructor setting the factors to the default value UNDEF = -1
	 */
	public CorrectionFactors() {
		this.c_l  = UNDEF;
		this.c_u  = UNDEF;
		this.c_lu = UNDEF;
	}
	
	/**
	 * Constructor setting only the factors c_l and c_u
	 * @param c_l The correction factor c_l
	 * @param c_u The correction factor c_u
	 */
	public CorrectionFactors(double c_l, double c_u) {
		this.c_l  = c_l;
		this.c_u  = c_u;
		this.c_lu = c_l/c_u;
	}
	
	/**
	 * Constructor setting only the factor c_lu
	 * @param c_lu The correction factor c_lu
	 */
	public CorrectionFactors(double c_lu) {
		this.c_lu = c_lu;
		this.c_l  = UNDEF;
		this.c_u  = UNDEF;
	}
	
	/**
	 * Constructor setting all three correction factors
	 * @param c_l The correction factor c_l
	 * @param c_u The correction factor c_u
	 * @param c_lu The correction factor c_lu
	 */
	public CorrectionFactors(double c_l, double c_u, double c_lu) {
		this.c_l  = c_l;
		this.c_u  = c_u;
		this.c_lu = c_lu;
	}
	
	/**
	 * Sets the correction factor c_l
	 * @param c_l The correction factor c_l
	 */
	public void setC_l(double c_l) {
		this.c_l = c_l;
	}
	
	/**
	 * Sets the correction factor c_u
	 * @param c_u The correction factor c_u
	 */
	public void setC_u(double c_u) {
		this.c_u = c_u;
	}
	
	/**
	 * Sets the correction factor c_lu
	 * @param c_lu The correction factor c_lu
	 */
	public void setC_lu(double c_lu) {
		this.c_lu = c_lu;
	}
	
	/**
	 * Returns the correction factor c_l
	 * @return The correction factor c_l
	 */
	public double getC_l() {
		return c_l;
	}
	
	/**
	 * Returns the correction factor c_u
	 * @return The correction factor c_u
	 */
	public double getC_u() {
		return c_u;
	}
	
	/**
	 * Returns the correction factor c_lu
	 * @return The correction factor c_lu
	 */
	public double getC_lu() {
		return c_lu;
	}

}
