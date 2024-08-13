/*
 * This file is part of the HALO 1.3 usage demonstration.
 */
package halo.examples;

import halo.data.Data;
import halo.data.Filter;

import java.util.ArrayList;

/**
 * First sample use case to demonstrate the handling of data from a given file. Examples for 
 * loading, filtering and printing data to output are given. Please note that parameters are 
 * set arbitrary; you have to choose them carefully when using HALO functionality in your own
 * programs!
 * @author Stefanie Kaufmann
 *
 */
public class SampleUseCase1 {
	
	public static final String DATAFILE      = "data/Example_mouse.txt";
//	public static final String ATTRIBUTEFILE = ""; //no example given here
	public static final String SEQUENCEFILE  = "data/sequences_mouse.txt";
	
	/**
	 * Starts the loading, filtering and printing of data
	 * @return The loaded data
	 */
	public static Data run() {
		
		/*
		 * Load the data
		 */

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
		
		//Define the name of the column containing the gene names
        ArrayList<String> geneAtt = new  ArrayList<String>();
        geneAtt.add("Gene Symbol");
        
        //Is the data in log scale? 
        boolean log = false;
		
        
		//Read in the data
        Data data = new Data("data/Example_mouse.txt", colTot, colNew, colPre, geneAtt, log); 
        data.setGeneName("Gene Symbol");
		
		/*****************************************************************************/
		
		/*
		 * Load additional attributes
		 */

      //Define the names of columns containing present call attributes
		ArrayList<String> colAtt = new ArrayList<String>();
		String[] attributes = new String[]{"Call_T1", "Call_T2", "Call_T3", "Call_E1", "Call_E2", "Call_E3","Call_U1", "Call_U2", "Call_U3"};
        for(String t: attributes) {
            colAtt.add(t);
        }
        
        //Load present calls separately from the data file
        data.loadPresentCallsFromDatafile(colAtt);
        
		//load attributes from the given file
//		data.addAttributes(ATTRIBUTEFILE);

		/*****************************************************************************/

		/*
		 * Filter the data
		 */
		
		//Filter according to a given threshold
		double threshold  = 50;
		data = Filter.filter(data, threshold);
		
		String call       = "A"; //The present/absent call used for filtering
		int    callNumber = 1;   //The number of appearances of this call requested to discard the probeset
		
		//Filter according to present/absent calls		
		data = Filter.filterAbsent(data, colAtt, call, callNumber);
		data = Filter.filterAbsent(data, colAtt, "M", callNumber);
		//Filtering for probe sets with no annotated gene name
		data = Filter.filterAbsent(data, geneAtt, "---", callNumber);
		
		
		/*****************************************************************************/
		
		/*
		 * Output data
		 */
		
		//choose name for output
		String output = "Example_mouse_filtered.txt";
		
		//write output
		colAtt.addAll(geneAtt);
        data.writeOutput(output, colTot, colNew, colPre,colAtt );
		
		return data;
	}
	
	
	public static void main(String[] args) {
		run();
	}

}
