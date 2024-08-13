/*
 * This file is part of the HALO 1.3 usage demonstration.
 */
package halo.examples;

import halo.data.Data;
import halo.data.Filter;
import halo.data.biascorrection.RCorrelationCoefficient;
import halo.data.biascorrection.RLoessRegression;

import java.util.HashMap;

/**
 * The fourth sample use case, which demonstrates the sequence-based evaluation of the input data.
 * Please note that parameters are set arbitrary; you have to choose them carefully when using HALO 
 * functionality in your own programs!
 * @author Stefanie Kaufmann
 *
 */
public class SampleUseCase3 {
	
	/*
	 * Enter your local path to the bin-directory of your R-installation here:
	 */
	public static final String PATHTOR = "/home/proj/software/R/Unix/R-2.10.0/bin";

	/**
	 * Starts the evaluation process
	 */
	public static void run() {
		//Load and normalize data
		Data data = SampleUseCase1.run();
		
		/**************************************************************************/
		
		/*
		 * Sequence-based evaluation
		 */
		//Define parameters
		int column     = 3; //The column of the fasta header that contains gene names
		String method  = Data.LOGEN; //The method used for comparison against uracil numbers
		String evalOut = "Example_mouse_quality_uracil.txt"; //Path for the output file (optional)
		boolean histo  = true; //true if histogram should be plotted
		HashMap<String, Double> biasCorrection = null; //Bias correction factors
		
		//Start evaluation
		data.evaluate(SampleUseCase1.SEQUENCEFILE, column, method, evalOut, biasCorrection, histo);
		
		/**************************************************************************/
		
		/*
		 * Bias correction with R
		 */
		
		//calculate the correlation coefficient
		RCorrelationCoefficient cor = new RCorrelationCoefficient(PATHTOR, evalOut);
		cor.setMethod(RCorrelationCoefficient.PEARSON);
		//start correlation coefficient calculation
		double coefficient          = cor.calculateCorrelationCoefficient();
		System.out.println("The correlation coefficient: "+coefficient);
		
		//fill mapping and corresponding arrays
		String[] spots = data.getSpot();


		//Start the loess regression with R
		RLoessRegression loess = new RLoessRegression(PATHTOR, evalOut, spots);
		HashMap<String, Double> lo = loess.calculateLoessRegression();
		data.setCorrNewTot(lo);
		data = Filter.filterCorrectionBias(data, lo);

		
	}
	
	public static void main(String[] args) {
		run();
	}
}
