/*
 * This file is part of the HALO 1.3 command line tool. 
 */
package halo.userinterface.cmdline;

import halo.data.Data;
import halo.data.Filter;
import halo.data.biascorrection.RLoessRegression;
import halo.halflife.HalfLife;
import halo.halflife.HalfLifeWriter;
import halo.halflife.HalfLife_New;
import halo.halflife.HalfLife_NewPre;
import halo.halflife.HalfLife_Pre;
import halo.normalization.CorrectionFactors;
import halo.normalization.Normalization;
import halo.userinterface.cmdline.cmdexceptions.InvalidFlagException;
import halo.userinterface.cmdline.cmdexceptions.InvalidMethodException;
import halo.userinterface.cmdline.cmdexceptions.NoValueException;
import halo.userinterface.gui.graphhandler.GraphHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * Serves as interface between command line input and calculation of half-lives
 * @author Stefanie Kaufmann
 *
 */
public class CmdHalfLife {

	private static String[]           halfL;
	private static ArrayList<String>  methods   = new ArrayList<String>();
	private static int                time      = -1;
	private static boolean            corr;
	private static boolean            bias = false;
	private static boolean            plot = false;
	private static double             median = Normalization.UNDEF;
	private static int                which;
	private static String             output;
	private static ArrayList<Integer> methodsOut = new ArrayList<Integer>();
	public static final String        NEW        = "new";
	public static final String        PRE        = "pre";
	public static final String        NEWPRE         = "new/pre";
	
	private static boolean isValid() {
		if(time == -1 || methods == null) {
			return false;
		}
		return true;
	}
	
	/**
	 * Splits the complete options array into one array containing all necessary information for half-life calculation
	 * and one array which contains information for data manipulation and normalization
	 * @param input complete options array
	 * @return Array containing information for data manipulation and normalization
	 */
	private static String[] split(String[] input) {
		String[] options;
		StringBuffer linreg = new StringBuffer("");
		StringBuffer half   = new StringBuffer("");
		
		for(int i=0; i<input.length; i+=2) {
			
			try {
				if(input[i+1].startsWith("-") || input.length<i) {
					throw new NoValueException(input[i]);
				}
			} catch (NoValueException e) {
				e.printMessage();
				System.exit(1);
			}
			
			String flag = input[i];
			//is the flag part of the flags needed for calculation of half-lives?
			if(flag.startsWith("-h") || flag.equals("-t") || flag.equals("-o") 
					|| flag.equals("-w") || flag.equals("-m") || flag.equals("-corr") || flag.equals("-n") 
					|| flag.equals("-bias") || flag.equals("-plot")) {
				half.append(flag+" # "+input[i+1]+" # ");
			} else {
				//flag is a part of the input needed for calculation of normalization and data manipulation
				linreg.append(flag+" # "+input[i+1]+" # ");
			}
		}
		if(!half.toString().isEmpty()) {
			halfL   = half.toString().split(" # ");
		} else {
			halfL   = new String[0];
		}
		options = linreg.toString().split(" # ");		
		return options;
	}
	
	/**
	 * Translates the options array into values for the variables needed for calculating half-lives
	 * @param input An options array containing only information that is needed for half-life calculation
	 */
	private static void extractFlags(String[] input) {
		try {
			//translate all flags into corresponding variables
			for(int i=0; i<input.length; i+=2) {
				
				try {
					if(input[i+1].startsWith("-") || input.length<i) {
						throw new NoValueException(input[i]);
					}
				} catch (NoValueException e) {
					e.printMessage();
					System.exit(1);
				}
				
				String flag = input[i];
				input[i+1] = input[i+1].replace("~", " ");
				if(flag.equals("-t")) {
					time = Integer.parseInt(input[i+1]);
				} else if(flag.startsWith("-h")) {
					methods.add(input[i+1]);
				} else if(flag.equals("-bias")) {
					try {
						bias = Boolean.parseBoolean(input[i+1]);
					} catch (Exception e) {
						System.err.println("You have to use the -bias flag with a boolean value!");
					}
				
				} else if(flag.equals("-o")) {
					output = input[i+1];
				} else  if(flag.equals("-plot")) { 
					try {
						plot = Boolean.parseBoolean(input[i+1]);
					} catch (Exception e) {
						System.err.println("You need to use a boolean value with flag '-plot'!");
					}
				} else if(flag.equals("-w")) {
					if(input[i+1].equals("both")) {
						which = HalfLifeWriter.BOTH;
					} else if(input[i+1].equals("ratio")) {
						which = HalfLifeWriter.RATIO;
					} else if(input[i+1].equals("halflife")) {
						which = HalfLifeWriter.HALFLIFE;
					} else {
						System.err.println("Please specify with '-w' which information you want to be printed into the output file: " +
								"'"+HalfLifeWriter.HALFLIFE+"' for half-lives, '"+HalfLifeWriter.RATIO+"' for ratios or \n'" +
										""+HalfLifeWriter.BOTH+"' for both. Proceeding with default: half-lives.");
						which = HalfLifeWriter.HALFLIFE;
					}
				} else if(flag.equals("-m")) {
					//for each of the given methods for half-life calculation add name to methods list
					String[] meths = input[i+1].split(",");
					for(String item : meths) {
						if(methods.contains(item)) {
							int index = methods.lastIndexOf(item);
							methodsOut.add(index);
						}
					}
				} else if(flag.equals("-corr")) {
					corr = true;
					try {
						median = Double.parseDouble(input[i+1]);
					} catch (NumberFormatException e) {
						System.err.println("Your given median has to be numerical!");
						System.exit(1);
					}
				} else {
					throw new InvalidFlagException(input[i]);
				}
			}
		} catch (InvalidFlagException e) {
			System.err.println(e.getFlag()+" is not a valid flag!");
			CommandLine.printHelp();
			System.exit(1);
		}

	}
	
	/**
	 * Main method which takes an options array as input and starts all necessary calculations for half-lives
	 * Prints the results into a file, if '-corr TRUE' also provides correction factors based on median
	 * @param args The complete options array given in the command line
	 */
	public static void main(String[] args) {
		
		//split input in options needed for half-life calculation and options forwarded for data manipulation and normalization
		String[] options = split(args);
		
		if(halfL.length > 0) {
			//translate input for half-life calculation into corresponding variables
			extractFlags(halfL);
			try {
				if(!isValid()) {
					throw new InvalidMethodException();
				}
			} catch(InvalidMethodException e) {
				e.printStackTrace();
				System.err.println("You didn't specify a method/methods for half-life calculation or a time t\n" +
						" for which the half-life should be calculated!");
				System.exit(1);
			}
			
			//use normalization combined with data manipulation to form a data object and correction factors
			Data data                 = null;
			CorrectionFactors factors = null;
			try {
				data    = CmdNormalization.getData(options);
				factors = CmdNormalization.prepareNormalization(data);
			} catch(InvalidFlagException e) {
				System.err.println(e.getFlag()+" is not a valid flag!");
				CommandLine.printHelp();
				System.exit(1);
			}
			
			if(bias) {
				String rPath = CmdFilterData.rPath;
				String uraNm = CmdFilterData.uracilOutput;
				
				if(rPath != null && uraNm != null) {
					//fill mapping and corresponding arrays
					String[] spots = data.getSpot();

					//Start the loess regression with R
					RLoessRegression loess = new RLoessRegression(rPath, uraNm, spots);
					HashMap<String, Double> lo = loess.calculateLoessRegression();
					if(methods.contains(HalfLife.NEWLY)) {
						data.setCorrNewTot(lo);
					} 
					if(methods.contains(HalfLife.PRE)) {
						data.setCorrPreTot(lo);
					}
					if(methods.contains(HalfLife.NEWPRE)) {
						data.setCorrNewPre(lo);
					}
					data = Filter.filterCorrectionBias(data, lo);
				} else {
					System.err.println("You can only use -bias in combination with -R, -ur and -ufo!");
				}
			}
			
			double[] medians          = new double[methods.size()];
			ArrayList<HalfLife> lives = new ArrayList<HalfLife>();
			int i                     = 0;
			double sum                = 0.0;
			StringBuffer header       = new StringBuffer("");
			
			System.out.println("Calculating half-lives...");
			//for each given method create a new half-life object with corresponding calculation methods
			try {
				Iterator<String> iter = methods.iterator();
				while(iter.hasNext()) {
					String name = iter.next();
					HalfLife hl = null;
					if(name.equals(NEW)) {
						hl = new HalfLife_New();
					} else if(name.equals(PRE)) {
						hl = new HalfLife_Pre();
					} else if(name.equals(NEWPRE)) {
						hl = new HalfLife_NewPre();
				    } else {
						throw new InvalidMethodException();
					}
					//form the header for the output file from the names of the methods
					header.append(name+"\t");
					//initialize half-life calculation
					hl.initialize(data);
					hl.setCorrectionFactor(factors);
					//perform half-life calculation
					hl.calculateHalfLives(time);
					lives.add(hl);
					
//					//if correction factors based on median values are wanted, calculate the median values for all half-lives
					if(corr) {
						double[] hwz = hl.getHwz();
						medians[i]   = HalfLife.median(hwz);
						sum         += medians[i];
						i++;
					}
					//plot half-lives if wanted
					if(plot) {
						List<HalfLife> hllist = lives;
						List<Double> times   = new ArrayList<Double>();
						for(int j=0; j<lives.size(); j++) {
							times.add((double)time);
						}
						GraphHandler.plotHalfLifeHisto(hllist, times, null);
					}
				}
			} catch(InvalidMethodException e) {
				System.err.println("You need to specify a valid method for half-life calculation; \n" +
						"at the moment only the default methods for calculation of half-lives of newly \n" +
						"transcribed RNA ('new') and for calculation of half-lives of preexisting \n" +
						"RNA ('pre') are available. Please choose one of those or implement your own method");
				System.exit(1);
			}
			
			System.out.println("Done calculating half-lives");
			
			//After all calculations are done, calculate the correction factors based on median (only if wanted)
			if(corr) {
				System.out.println("Calculating correction factors based on median...");
				double med               = sum/(double)medians.length;
				
				if(median != Normalization.UNDEF) {
					med = median;
				}
				
				Iterator<HalfLife> iter2 = lives.iterator();
				int index                = 0;
				//calculate correction factors based on median for each HalfLife object and print to STDOUT
				System.out.println("------------------------------------------------------------------");
				while(iter2.hasNext()) {
					HalfLife hl         = iter2.next();
					CorrectionFactors c = hl.calculateCorrectionFactors(med, time);
					System.out.println("Correction factors for HalfLife object "+methods.get(index)+" ; c_l: "+c.getC_l()+", c_u: "+c.getC_u()+", c_lu: "+c.getC_lu());
					index++;
				}
				System.out.println("------------------------------------------------------------------");
				System.out.println("Done calculating correction factors based on median");
			}
			HalfLife[] hls = new HalfLife[methods.size()];
			lives.toArray(hls);
			
			if(output != null) {
				
				if(methodsOut.size() > 0) {
					HalfLife[] temp = new HalfLife[methodsOut.size()];
					int index       = 0;
					String[] split  = header.toString().split("\t");
					StringBuffer he = new StringBuffer("");
					for(int meth : methodsOut) {
						temp[index] = hls[meth];
						he.append("\t"+split[meth]);
						index++;
					}
					hls = temp;
					header = he;
				}
				
				//if ratios and half-lives are wanted extend header
				if(which == HalfLifeWriter.BOTH) {
					StringBuffer header2 = new StringBuffer("|Ratios: ");
					header2.append(header);
					header.append(header2);
				}
				String head = "spotid\t|"+header.toString();
				System.out.println("Writing results into file...");
				//print all calculated half-lives to file
				new HalfLifeWriter(output, head, which, hls);
				System.out.println("Done writing results");
			}
		} else {
			CmdNormalization.main(options);
		}
		
	}
}
