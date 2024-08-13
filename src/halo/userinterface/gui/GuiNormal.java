/*
 * This file is part of the HALO 1.3 graphical user interface
 */
package halo.userinterface.gui;

import halo.data.Data;
import halo.normalization.CorrectionFactors;
import halo.normalization.LinearRegression;
import halo.normalization.Normalization;

/**
 * 'Interface' which connects GUI and methods for normalization
 * @author Stefanie Kaufmann
 *
 */
public class GuiNormal {

	private Data               data;
	private String             method;
	private int                ratioMethod;
	private int 			   replicate = Normalization.UNDEF;
	private Normalization      norm;
	private CorrectionFactors  cf;
	public static final String STANDARD = "standard";
	public static final String MEDIAN   = "median";
	public boolean             done     = false;
	
	
	/**
	 * Creates Normalization object and performs normalization
	 */
	public void calcNormalization() {
		if(method.equals(STANDARD)) {
			norm = new LinearRegression(data);
			norm.setReplicate(replicate);
			cf = norm.calculateCorrectionFactors();
			done = true;
		}
	}
	
	/**
	 * Changes the method used for ratio calculation
	 * @param method The method to be used for ratio calculation
	 */
	public void setRatioMethod(int method) {
		if(method != Data.AVERAGEFIRST && method != Data.RATIOFIRST && method != Data.REPLICATE) {
			System.err.println("Chosen method for ratio calculation does not exist!\n" +
					"Please use '"+Data.AVERAGEFIRST+"' for average calculation before ratios\n" +
							"and '"+Data.RATIOFIRST+"' for ratio calculation before averages.\n");
		} else {
			ratioMethod = method;
			data.setMethod(method);
		}
	}
	
	/**
	 * Sets the data object for normalization
	 * @param data Data object for normalization
	 */
	public void setData(Data data) {
		this.data = data;
	}
	
	/**
	 * Sets the replicate for normalization
	 * @param replicate The replicate for normalization
	 */
	public void setReplicate(int replicate) {
		this.replicate = replicate;
	}
	
	/**
	 * Sets the name of the normalization method
	 * @param method Name of the regression method
	 */
	public void setMethod(String method) {
		this.method = method;
	}
	
	/**
	 * Returns the normalization Object
	 * @return The normalization object
	 */
	public Normalization getNormalization() {
		return norm;
	}
	
	/**
	 * Returns the correction factors
	 * @return The correction factors
	 */
	public CorrectionFactors getCF() {
		return cf;
	}
	
	/**
	 * Returns the method for ratio calculation
	 * @return The ratio calculation method
	 */
	public int getRatioMethod() {
		return ratioMethod;
	}
	
	/**
	 * Checks whether normalization has been performed
	 * @return True if normalization was done
	 */
	public boolean hasNormalization() {
		return done;
	}
}
