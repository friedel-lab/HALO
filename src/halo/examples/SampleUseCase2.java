/*
 * This file is part of the HALO 1.3 usage demonstration.
 */
package halo.examples;

import halo.data.Data;
import halo.data.Filter;
import halo.halflife.HalfLife;
import halo.halflife.HalfLifeWriter;
import halo.halflife.HalfLife_New;
import halo.halflife.HalfLife_Pre;
import halo.normalization.CorrectionFactors;
import halo.normalization.LinearRegression;
import halo.normalization.Normalization;
import halo.userinterface.gui.graphhandler.GraphHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * The third sample use case, which demonstrates the calculation and plotting of half-lives. 
 * Please note that parameters are set arbitrary; you have to choose them carefully when using 
 * HALO functionality in your own programs!
 * @author Stefanie Kaufmann
 *
 */
public class SampleUseCase2 {

	/**
	 * Calculates the median half-life for a set of methods and a given time
	 * @param data The RNA data
	 * @param medMethods The list of half-life calculation methods
	 * @param time The labeling time
	 * @return Median half-life
	 */
	public static double median(Data data, ArrayList<HalfLife> medMethods, double time) {
		//Choose method for normalization: linear regression
		LinearRegression lr;

		double[] halflives;
		double sum;
		double sumAll = 0;
		double length = 0;

		for(int i = 1; i <= data.getPreDescr().length; i++) {
			sum = 0;
			for(HalfLife hl : medMethods) {

				hl.initialize(data, i);
				lr = new LinearRegression(data);
				lr.setReplicate(i);

				hl.setCorrectionFactor(lr.calculateCorrectionFactors());

				hl.calculateHalfLives(time);
				halflives = hl.getHwz();

				double med = HalfLife.median(halflives);

				sum += med;
			}
			sumAll+=sum/2.0;
			length++;
		}

		double medianHL = sumAll / length;
		return medianHL;
	}


	/**
	 * Starts the calculation and plotting of half-lives
	 * @return The calculated half-lives for three different methods
	 */
	public static List<HalfLife> run() {


		//Define the names of columns containing newly transcribed RNA
		ArrayList<String> colNew = new ArrayList<String>();
		colNew.add("E1");
		colNew.add("E2");
		colNew.add("E3");

		//Define the names of columns containing pre-existing RNA
		ArrayList<String> colPre = new ArrayList<String>();
		colPre.add("U1");
		colPre.add("U2");
		colPre.add("U3");

		//Define the names of columns containing total RNA
		ArrayList<String> colTot = new ArrayList<String>();
		colTot.add("T1");
		colTot.add("T2");
		colTot.add("T3");

		//Define the names of columns containing attributes
		ArrayList<String> colAtt = new ArrayList<String>();
		String[] attributes = new String[]{"Call_T1", "Call_T2", "Call_T3", "Call_E1", "Call_E2", "Call_E3","Call_U1", "Call_U2", "Call_U3"};
		for(String t: attributes) {
			colAtt.add(t);
		}

		//Define the name of the column containing the gene names
		ArrayList<String> geneAtt = new  ArrayList<String>();
		geneAtt.add("Gene Symbol");

		/*
		 * Load and normalize data
		 */
		Data data              = SampleUseCase1.run();

		//choose labeling time
		double time = 55;

		//Choose half-life calculation methods
		ArrayList<HalfLife> medMethods = new ArrayList<HalfLife>();
		medMethods.add(new HalfLife_New());
		medMethods.add(new HalfLife_Pre());

		/*********************************************************************/

		/*
		 * Calculate median half-life 
		 */

		double medianHL = median(data, medMethods, time);
		System.out.println("Median half-life "+medianHL);

		//Normalization by linear regression
		Normalization lr = new LinearRegression(data);
		//set method for ratio calculation (default = RATIOFIRST)
		data.setMethod(Data.AVERAGEFIRST);
		//OR 
		//set replicate 
		//		lr.setReplicate(1);
		CorrectionFactors factors = lr.calculateCorrectionFactors();

		//Filter with PQS
		data.setGeneName("Gene Symbol");
		data = Filter.filterPQS(data, lr, true);    
		data.writeOutput("Examples_mouse_filtered_pqs.txt", colTot, colNew, colPre, colAtt);

		/*******************************************************************/

		/*
		 * Calculating the half-lives
		 */

		//Choose half-life calculation method: based on newly transcribed/total RNA
		HalfLife hlNew = new HalfLife_New();
		hlNew.initialize(data);

		//Use normalization based on median half-life
		hlNew.calculateCorrectionFactors(medianHL, time);
		//calculate the half-lives
		hlNew.calculateHalfLives(time);
		//print the half-lives with gene names in an output file
		hlNew.printHalfLivesWithAttributes("Example_mouse_halflives_nt.txt", geneAtt);

		//Calculate a second half-life method: based on pre-existing/total RNA
		HalfLife hlPre = new HalfLife_Pre();
		hlPre.initialize(data);
		//Use normalization based on linear regression (see above)
		hlPre.setCorrectionFactor(factors);
		//calculate the half-lives
		hlPre.calculateHalfLives(time);


		/******************************************************************/

		/*
		 * Plot normalization results
		 */

		GraphHandler.plotNormalization(lr, data);

		/*********************************************************************/

		/*
		 * Plot the half-lives
		 */

		//Prepare the parameters necessary for plotting
		List<HalfLife> lives = new ArrayList<HalfLife>();
		lives.add(hlNew);

		//Define the names of the used methods
		List<String> methods = new ArrayList<String>();
		methods.add(HalfLife.NEWLY);

		//Define the corresponding labeling times
		List<Double> times = new ArrayList<Double>();
		times.add(time);

		//Start plotting
		GraphHandler.plotHalfLifeHisto(lives,	times, null);

		/*********************************************************************/

		lives.add(hlPre);
		/*
		 * Print the results to a file
		 */

		//Define parameters
		String header = "Spotid\tNewly transcribed/Total\tPre-existing/Total"; //header for the output file
		int which     = HalfLifeWriter.HALFLIFE; //defines if you want only half-lives, ratios or both
		String output = SampleUseCase1.DATAFILE+".halflives";

		//Start printing
		new HalfLifeWriter(output, header, which, lives.get(0), lives.get(1));

		return lives;
	}

	public static void main(String[] args) {
		run();
	}
}
