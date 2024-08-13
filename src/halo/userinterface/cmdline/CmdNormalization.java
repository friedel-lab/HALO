/*
 * This file is part of the HALO 1.3 command line tool. 
 */
package halo.userinterface.cmdline;

import halo.data.Data;
import halo.normalization.CorrectionFactors;
import halo.normalization.LinearRegression;
import halo.normalization.Normalization;
import halo.tools.Tools;
import halo.userinterface.cmdline.cmdexceptions.InvalidFlagException;
import halo.userinterface.cmdline.cmdexceptions.InvalidMethodException;
import halo.userinterface.cmdline.cmdexceptions.NoValueException;

/**
 * Serves as interface between command line input and tools for calculation of linear regression
 * @author Stefanie Kaufmann
 *
 */
public class CmdNormalization {
	public static final String    STANDARD = "standard";
	private static String[]       norm;
	private static String         method;
	private static double         median = Normalization.UNDEF;
	private static double         time = Normalization.UNDEF;

	/**
	 * checks whether all essential variables have been set
	 * @return true if the options are valid, false otherwise
	 */
	public static boolean isValid() {
		if(method == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Splits the options array into one array that provides input for the data manipulation
	 * and one array that provides needed input for calculation of normalization
	 * @param input complete options array
	 * @return options array needed for data manipulation
	 */
	public static String[] split(String[] input) {
		StringBuffer dataInp   = new StringBuffer("");
		StringBuffer linRegInp = new StringBuffer("");
		boolean check = false;


		for(int i=0; i<input.length; i+=2) {
			try {
				if(input[i+1].startsWith("-") || input.length<i) {
					throw new NoValueException(input[i]);
				}
			} catch (NoValueException e) {
				e.printMessage();
				System.exit(1);
			}

			input[i+1] = input[i+1].replace("~", " ");
			//is flag a part of the flags needed for normalization?
			if(input[i].equals("-l")) {
				linRegInp.append(input[i]+" # "+input[i+1]+" # ");
			} else if(input[i].equals("-f") && input[i+1].startsWith("pqs")) { 
				dataInp.append(input[i]+" # "+input[i+1]+" # ");
				check = true;
			} else if(input[i].equals("-median")) {
				try {
					time = Double.parseDouble(input[i+1]);
				} catch (NumberFormatException e) {
					System.err.println("Your time for half-life calculation has to be numerical!");
				}
			} else {
				//flag is part of input for data manipulation
				dataInp.append(input[i]+" # "+input[i+1]+" # ");
			}
		}
		if(check) {
			dataInp.append(linRegInp.toString());
		}
		String[] dat = dataInp.toString().split(" # ");
		if(!linRegInp.toString().isEmpty()) {
			norm = linRegInp.toString().split(" # ");
		} else {
			norm = new String[0];
		}
		return dat;
	}

	/**
	 * Translates the flags of the options array into values for the variables used for calculation
	 * @param input options array containing only the important information
	 */
	public static void extractFlags(String[] input) {
		try {
			for(int i=0; i<input.length; i+=2) {
				String flag = input[i];
				if(flag.equals("-l")) {
					method = input[i+1];
					if(!method.equals(STANDARD)) {
						throw new InvalidMethodException();
					}
				} else {
					throw new InvalidFlagException(input[i]);
				}
			}
		} catch (InvalidFlagException e) {
			System.err.println("You need to specify a method for normalization! \n" +
					"Proceeding with method set to "+STANDARD+" (linear regression).");
			method = STANDARD;
		} catch(InvalidMethodException e) {
			System.err.println("The method you set is not implemented yet; \n" +
					"Proceeding with method set to "+STANDARD+" (linear regression).");
			method = STANDARD;
		}

	}

	/**
	 * Contains all the methods for calculation of normalization
	 * @param data A data object for which the calculation should be done
	 * @return The Correction Factors returned from the normalization
	 */
	public static CorrectionFactors prepareNormalization(Data data) {
		//translate input into variable values
		extractFlags(norm);
		Normalization normal;
		CorrectionFactors factors = null;

		System.out.println("Performing normalization...");
		//checks which method shall be used for calculation
		if(method.equals(STANDARD) && median == Normalization.UNDEF) {
			normal      = new LinearRegression(data);
			factors = normal.calculateCorrectionFactors();
		}  
		System.out.println("Done with normalization");
		
		//		else {
		//			System.err.println("You need to use a valid method for normalization. At the moment only one method is implemented; " +
		//					"please start over using this standard method ('standard') or implement your own method for normalization");
		//			System.exit(1);
		//		}
		
		return factors;
	}

	/**
	 * Uses the complete options array to split it and forward the necessary information to the interface for data manipulation
	 * @param input The complete options array
	 * @return The Data object received from data manipulation
	 */
	public static Data getData(String[] input) throws InvalidFlagException{
		//forward the input needed for data manipulation
		String[] dat = split(input);
		Data data    = CmdFilterData.prepareData(dat);
		return data;
	}
	
	/**
	 * Contains all necessary methods to calculate normalization from an options array
	 * @param args The options array
	 */
	public static void main(String[] args) {
		String[] dat = split(args);


		if(norm.length > 0) {
			if(args.length == 0) {
				CommandLine.printHelp();
			}
			Data data = null;
			try {
				data = getData(args);
			} catch (InvalidFlagException e) {
				System.err.println(e.getFlag()+" is not a valid flag!");
				CommandLine.printHelp();
				System.exit(1);
			}
			//perform the normalization
			CorrectionFactors factors = prepareNormalization(data);
			if(!isValid()) {
				System.err.println("You forgot to define a method for normalization! Aborting calculation.");
				System.exit(1);
			}
			//print the results to STDOUT
			System.out.println("------------------------------------------------------------------");
			System.out.println("The resulting correction factors are:" +
					"\n c_u: "+factors.getC_u()+", c_l: "+factors.getC_l()+", c_lu: " +factors.getC_lu()+
					"\n Method used: "+method);
			System.out.println("------------------------------------------------------------------");
			
			if(time != Normalization.UNDEF) {
				double median = Tools.calculateMedianForHalfLives(time, data);
				System.out.println("The median for your labeling time "+time+" is: "+median);
				System.out.println("------------------------------------------------------------------");
			}
		} else {
			CmdFilterData.main(dat);
		}

	}
}
