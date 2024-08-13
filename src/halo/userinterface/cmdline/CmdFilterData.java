/*
 * This file is part of the HALO 1.3 command line tool. 
 */
package halo.userinterface.cmdline;

import halo.data.Data;
import halo.data.Filter;
import halo.data.Mapping;
import halo.data.biascorrection.RCorrelationCoefficient;
import halo.normalization.LinearRegression;
import halo.normalization.Normalization;
import halo.userinterface.cmdline.cmdexceptions.InvalidFlagException;
import halo.userinterface.cmdline.cmdexceptions.NoValueException;

import java.util.ArrayList;


/**
 * Serves as interface between Data manipulation tools and command line input
 * @author Stefanie Kaufmann
 *
 */
public class CmdFilterData {

	
	public static String uracilOutput = "";
	public static String rPath;
	private static final String THRESHOLD = "threshold=";
	private static final String PRESENT   = "present=";
	private static final String PQSMIN    = "pqsmin=";
	private static final String PQS       = "pqs=";
	private static String filename;
	private static String output;
	private static String lrMethod;
	private static String pqsName;
	private static String uraMeth      = "log(e'/n')";
	private static String spotid;
	private static String genelabel;
	private static String correl;
	private static String uracilName;
	private static int     column = -1;
	private static boolean pqsPlot = false;
	private static boolean uraPlot = false;
	private static boolean log     = false;
	private static boolean turnReverseMappingOff = false;
	private static boolean turnReverseMappingOff2 = false;
	private static ArrayList<String> methods      = new ArrayList<String>();
	private static ArrayList<String> maps         = new ArrayList<String>();
	private static ArrayList<String> labelsNew    = new ArrayList<String>();
	private static ArrayList<String> labelsPre    = new ArrayList<String>();
	private static ArrayList<String> labelsTot    = new ArrayList<String>(); 
	private static ArrayList<String> labelsAttr   = new ArrayList<String>();
	private static ArrayList<String> labelsAttr2  = new ArrayList<String>();
	private static ArrayList<String> labelsNewOut = new ArrayList<String>();
	private static ArrayList<String> labelsPreOut = new ArrayList<String>();
	private static ArrayList<String> labelsTotOut = new ArrayList<String>();
	
	/**
	 * Checks whether all needed values are set
	 * @return true if every essential flag was used in the input, false otherwise
	 */
	public static boolean isValid() {
		if(filename == null || labelsNew.size()==0 || labelsPre.size()==0 || labelsTot.size()==0) {
			System.err.println("You forgot at least one necessary flag!");
			CommandLine.printHelp();
			return false;
		}
		return true;
	}
	
	
	/**
	 * Takes the input options and sets all necessary variables accordingly
	 * @param input The options
	 */
	public static void extractFlags(String[] input) throws InvalidFlagException {
		try {
			//for each flag add corresponding value to the specific variable
			for(int i=0; i<input.length; i+=2) {
				
				if(input[i+1].startsWith("-") || input.length<i) {
					throw new NoValueException(input[i]);
				}
				input[i+1] = input[i+1].replace("~", " ");
				if(input[i].equals("-i")) {
					filename = input[i+1];
				} else if(input[i].equals("-of")) {
					output = input[i+1];
				} else if(input[i].equals("-f")) {
					String method = input[i+1];
					methods.add(method);
				} else if(input[i].equals("-l")) {
					lrMethod = input[i+1];
				} else if(input[i].equals("-pqs")) {
					pqsName = input[i+1];
				} else if(input[i].equals("-uf")) {
					uracilName = input[i+1];
				} else if(input[i].equals("-uc")) {
					try {
						column = Integer.parseInt(input[i+1]);
					} catch (NumberFormatException e) {
						System.err.println("The column defined with flag -uc needs to be an integer!");
					}
				} else if(input[i].equals("-R")) { 
					rPath = input[i+1];
				} else if(input[i].equals("-ur")) {
					uraMeth = input[i+1];
				} else if(input[i].equals("-ufo")) { 
					uracilOutput = input[i+1];
			    } else if(input[i].equals("-ps")) {
					spotid = input[i+1];
				} else if(input[i].equals("-correl")) {
					correl = input[i+1];
				} else if(input[i].equals("-log")) { 
					try {
						log = Boolean.parseBoolean(input[i+1]);
					} catch (Exception e) {
						System.err.println("You have to use a BOOLEAN value with the flag -log; input ignored.");
					}
				} else if(input[i].equals("-genelabel")) {
					genelabel = input[i+1];
				} else if(input[i].equals("-pp")) {
					try {
						pqsPlot = Boolean.parseBoolean(input[i+1]);
					} catch (Exception e) {
						System.err.println("You have to use a BOOLEAN value with the flag -pp; input ignored.");
					}
					
				} else if(input[i].equals("-up")) { 
					try {
						uraPlot = Boolean.parseBoolean(input[i+1]);
					} catch (Exception e) {
						System.err.println("You have to use a BOOLEAN value with the flag -up; input ignored.");
					}
			    } else {
					String[] list = input[i+1].split(",");
					
					if(input[i].equals("-map")) {
						for(String item : list) {
							maps.add(item);
						}
					} else if(input[i].equals("-ct")) {
						for(String item : list) {
							labelsTot.add(item);
						}
					} else if(input[i].equals("-cn")) {
						for(String item : list) {
							labelsNew.add(item);
						}
					} else if(input[i].equals("-cp")) {
						for(String item : list) {
							labelsPre.add(item);
						}
					} else if(input[i].equals("-ca")) { 
						for(String item : list) {
							labelsAttr.add(item);
						}
					
			    	} else if(input[i].equals("-pc")) {
			    		try {
			    			turnReverseMappingOff = Boolean.parseBoolean(input[i+1]);
			    		} catch (Exception e) {
			    			System.err.println("You have to use a BOOLEAN value with the flag -pc; input ignored.");
			    		}
			    	} else if(input[i].equals("-ca2")) {
			    		for(String item : list) {
			    			labelsAttr2.add(item);
			    		}
			    	
			    	} else if(input[i].equals("-pc2")) {
			    		try {
			    			turnReverseMappingOff2 = Boolean.parseBoolean(input[i+1]);
			    		} catch (Exception e) {
			    			System.err.println("You have to use a BOOLEAN value with the flag -pc2; input ignored.");
			    		}
			    	} else if(input[i].equals("-cto")) {
						for(String item : list) {
							labelsTotOut.add(item);
						}
					} else if(input[i].equals("-cno")) {
						for(String item : list) {
							labelsNewOut.add(item);
						}
					} else if(input[i].equals("-cpo")) {
						for(String item : list) {
							labelsPreOut.add(item);
						}
					} else {
						throw new InvalidFlagException(input[i]);
					}
				}
			}
		} catch (NoValueException e) {
			e.printMessage();
			System.exit(1);
		}
		
	}
	/**
	 * Creates a data object from the information given as options array
	 * @param input The options
	 * @return The created Data object
	 */
	public static Data prepareData(String[] input) throws InvalidFlagException{
		//translate input options into variables
		extractFlags(input);
			
		//check if input is valid
		if(!isValid()) {
			System.exit(1);
		}
		System.out.println("Reading data...");
		Data data = new Data();
		if(spotid != null) {
			data.setSpotid(spotid);
		}
		if(labelsAttr.size() == 0) {
			data.loadData(filename, labelsTot, labelsNew, labelsPre, null, log);
		} else {
			Mapping.setTurnReverseMapOff(turnReverseMappingOff);
			data.loadData(filename, labelsTot, labelsNew, labelsPre, labelsAttr, log);
			Mapping.setTurnReverseMapOff(false);
		}
		
		if(labelsAttr2.size() != 0) {
			data.loadAttributesFromDatafile(labelsAttr2, turnReverseMappingOff2);
		}
		if(genelabel != null) {
			data.setGeneName(genelabel);
		}
		if(maps.size() != 0) {
			for(String mapName : maps) {
				data.addAttributes(mapName);
			}
		}
		System.out.println("Done reading data");
		if(uracilName != null && column != -1) {
			System.out.println("Evaluating data...");
			data.evaluate(uracilName, column, uraMeth, uracilOutput, null, uraPlot);
			System.out.println("Done evaluating");
			
			if(correl != null) {
				if(!rPath.endsWith("bin")) {
					System.err.println("Your path to R has to lead to the bin directory!");
				} else if(uracilOutput.isEmpty()) {
					System.err.println("You have to use the -ufo flag together with -correl!");
				
				} else if(correl.equals(RCorrelationCoefficient.KENDAL) ||
						correl.equals(RCorrelationCoefficient.PEARSON) ||
						correl.equals(RCorrelationCoefficient.SPEARMAN)) {
					//calculate the correlation coefficient
					RCorrelationCoefficient cor = new RCorrelationCoefficient(rPath, uracilOutput);
					cor.setMethod(correl);
					//start correlation coefficient calculation
					double coefficient          = cor.calculateCorrelationCoefficient();
					System.out.println("The correlation coefficient: "+coefficient);
				} else {
					System.err.println("You are not allowed to use "+correl+" as a method to calculate\n" +
							"the correlation coefficient! Please use "+RCorrelationCoefficient.KENDAL+
							", "+RCorrelationCoefficient.PEARSON+" or "+RCorrelationCoefficient.SPEARMAN+".");
				}
			}
		}
		//check whether method & output file are specified, if so filter data accordingly
		if(methods.size() != 0) {
			try {
				System.out.println("Filtering data...");
				//iterate through all the given methods, filter the data according to the method
				for(String method : methods) {
						//if a certain threshold should be exceeded, filter data
						if(method.startsWith(THRESHOLD)) {
							String[] meth = method.split("=");
							int threshold = Integer.parseInt(meth[1]);
							data          = Filter.filter(data, threshold);
						//if absent values should be erased, filter data
						} else if(method.startsWith(PRESENT)) {
							String[] meth = method.split("=");
							String[] cont  = meth[1].split(":");
							String call;
							int threshold;
							if(cont.length < 2) {
								System.err.println("You did not call the method correctly. Please use this methods in\n" +
										"the following syntax: \n" +
										"-f present='label1,label2,...:call:threshold'. Proceeding with default \n" +
										"call ('A') and threshold (1)");
								call      = "A";
								threshold = 1;
							} else {
								call       = cont[1].trim();
								threshold  = Integer.parseInt(cont[2].trim());
							}
							String[] label = cont[0].split(",");
							ArrayList<String> labels = new ArrayList<String>();
							for(String lab : label) {
								labels.add(lab);
							}
							data = Filter.filterAbsent(data, labels, call, threshold);
						} else if(method.equals(PQSMIN)) {
							
							if(checkGenes(data)) {
								if(lrMethod != null) {
									Normalization lr = performNormalization(data);
									int replicate = Integer.parseInt(method.split("=")[1]);
									if(method.endsWith("=") || method.equals(PQSMIN.substring(0, PQSMIN.length()-1))) {
										replicate = Normalization.UNDEF;
									}
									data = Filter.filterPQS(data, lr, replicate, false);
									double[] pqs = data.getPqs();
									if(pqsName != null) {
										data.writeToFileArray(pqsName, pqs);
									}
									if(pqsPlot) {
										lr.generateHistogram(pqs, lr.getPqsMax());
									}
								}  			
							} else {
								System.err.println("You cannot filter according to minimal probeset quality score \n" +
										"if you have not loaded the genes first!");
							}
							
									
						} else if(method.startsWith(PQS)) {
							if(lrMethod != null) {
								Normalization lr = performNormalization(data);
								String[] meth = method.split("=");
								double threshold = Double.parseDouble(meth[1]);
								data          = Filter.filterPQS(data, lr, threshold, false);
								double[] pqs = data.getPqs();
								if(pqsName != null) {
									data.writeToFileArray(pqsName, pqs);
								}
								if(pqsPlot) {
									lr.generateHistogram(pqs, lr.getPqsMax());
								}
							}
						//if method used is not described
						} else {
							System.err.println("You tried to use an undefined filtering method. \n" +
									"Please use one of the following data filtering methods: \n" +
									"-f threshold='value' || present='label' || pqs=min || pqs='threshold'\n" +
									"Proceeding with ignoring method "+method);
						}
					}
			} catch (NumberFormatException e) {
				System.err.println("You defined a non-numerical threshold for one of your filtering methods!");
				System.exit(1);
			}
			
			System.out.println("Done filtering data");
		}
		
		if(methods.size() != 0 && output != null) {
			System.out.println("Writing filtered data into file...");
			//write the filtered data into a file
			data.writeOutput(output, labelsTotOut, labelsNewOut, labelsPreOut, null);
			System.out.println("Done writing filtered data");
		}
		return data;
	}
	
	/**
	 * Checks if gene names are available
	 * @param data The data object 
	 * @return TRUE if gene names are loaded, FALSE otherwise
	 */
	private static boolean checkGenes(Data data) {
		
		if(data.getAttrDescr() == null) {
			return false;
		}
		for(int j=0; j<data.getAttrDescr().length; j++) {
			if(data.getAttrDescr()[j].equals(data.getGeneName())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Performs normalization on a given Data object with the previously specified method
	 * @param data The data object
	 * @return The normalization object
	 */
	public static Normalization performNormalization(Data data) {
		if(lrMethod.equals(CmdNormalization.STANDARD)) {
			Normalization lr = new LinearRegression(data);
			lr.calculateCorrectionFactors();
			return lr;
		} else {
			return null;
		}
	}
	
	/**
	 * Main method which calls all necessary methods to get from a given options array 
	 * to a finished output file
	 * @param args Parameter list
	 */
	public static void main(String[] args) {
		//parse input and create a (filtered) Data object
		Data data = null;
		try {
			 data = prepareData(args);
		} catch (InvalidFlagException e) {
			System.err.println(e.getFlag()+" is no valid flag!");
			CommandLine.printHelp();
			System.exit(1);
		}
		
		//print data to file only if there was any filtering
		if((methods.size() == 0 && output != null)) {
			System.err.println("You didn't specify a data filtering method or output file, please use \n" +
					"one of the following: \n" +
					"-f threshold='value' || present='label' || pqs=min || pqs='threshold' and -of outputfile");
		} else if(methods.size() > 0 && output != null) {
			System.out.println("Writing filtered data into file...");
			
			//check if any labels have been defined to be used in output, otherwise print every column
			if(labelsTotOut.size() == 0 && labelsNewOut.size() == 0 && labelsPreOut.size() == 0) {
				labelsTotOut = labelsTot;
				labelsNewOut = labelsNew;
				labelsPreOut = labelsPre;
			}
			
			//write the filtered data into a file
			data.writeOutput(output, labelsTotOut, labelsNewOut, labelsPreOut, null);
			System.out.println("Done writing filtered data");
		}

		
	}
}
