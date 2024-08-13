/*
 * This file is part of HALO version 1.3. 
 */
package halo.data.biascorrection;

import java.io.File;
import java.util.HashMap;

/**
 * Abstract class for the calculation of the fitted values of a data set with 
 * local approximating regression, for the correction of a bias
 * @author Stefanie Kaufmann
 *
 */
public abstract class LoessRegression {
	
	protected File inputFile;

	/**
	 * Abstract method for the calculation of the fitted values 
	 * for correction of bias
	 * @return The fitted values of the data
	 */
	public abstract HashMap<String,Double> calculateLoessRegression();
	
	
	/**
	 * Sets the file that contains the data
	 * @param inputFile The data file
	 */
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}
}
