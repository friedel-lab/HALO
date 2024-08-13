/*
 * This file is part of HALO version 1.3. 
 */
package halo.data.biascorrection;

import java.io.File;

/**
 * Abstract class for the calculation of the correlation coefficient of a data set
 * @author Stefanie Kaufmann
 *
 */
public abstract class CorrelationCoefficient {

	protected File inputFile;
	
	/**
	 * Abstract method for the calculation of the correlation coefficient
	 * for the data
	 * @return The correlation coefficient of the data
	 */
	public abstract double calculateCorrelationCoefficient();
	
	
	/**
	 * Sets the input file that holds the data
	 * @param file The input file that holds the data
	 */
	public void setInputFile(File file) {
		inputFile = file;
	}
}
