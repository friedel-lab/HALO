/*
 * This file is part of HALO version 1.3.
 */
package halo.data;
import halo.data.dataExceptions.InconsistentColumnNumberException;
import halo.normalization.Normalization;
import halo.tools.Tuple;
import halo.userinterface.gui.graphhandler.XYGraphConstructor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Reading and containing RNA values from an input file and providing methods
 * for the calculation of RNA ratios as well as methods to save the filtered
 * values to a file.
 * @author Stefanie Kaufmann
 *
 */
public class Data {

	//1. Dimension: SpotID/Experiment, 2. Dimension: Replicate
	public static final int RATIOFIRST   = 1;
	public static final int AVERAGEFIRST = 2;
	public static final int REPLICATE    = 3;
	public static final String LOGEN     = "log(e'/n')";
	public static final String LOGUN     = "log(u'/n')";
	public static final String LOGEU	 = "log(e'/u')";
	public static final String GENEDEFAULT = "gene_name";
	
	private String     filename;
	private double[][] totalRNA;
	private double[][] newRNA;
	private double[][] preRNA;
	private String[]   totalDescr;
	private String[]   newDescr;
	private String[]   preDescr;
	private String[]   attrDescr;
	private double[]   ratio;
	private double[]   pqs;
	private double     avgUr;
	private double     avgLo;
	private double     maximum;
	private double     minimum;
	private int        replicate = Normalization.UNDEF;
	private String     plotName;
	private Mapping<String, Integer> map;
	private HashMap<String, Double> corrNewPre = null;
	private HashMap<String, Double> corrNewTot = null;
	private HashMap<String, Double> corrPreTot = null;
	private String[]  spot;
	private double[]  dat;
	private ArrayList<Mapping<String, String>> attributes;
	private String   absentLabel   = "absence";
	private String   absent        = "a";
	private String   geneName      = GENEDEFAULT;
	private String   spotid        = "probeset_id";
	private int            method        = RATIOFIRST;
//	private boolean        first         = true;
	private boolean        unequal       = false;


	/**
	 * Constructs a Data-Object based on an Input-File and the list of
	 * columns to be read with no additional attributes.
	 * @param file	Name of the Input-file
	 * @param columnsTotal List containing the labels of columns for
	 * total RNA
	 * @param columnsNew List containing the labels of columns for newly
	 * transcribed RNA
	 * @param columnsPre List containing the labels of columns for
	 * pre-existing RNA
	 */
	public Data(String file, List<String> columnsTotal, List<String> columnsNew,
			List<String> columnsPre, boolean log) {
		loadData(file, columnsTotal, columnsNew, columnsPre, null, log);
	}

	/**
	 * Constructs a Data-Object based on an Input-File and the list of
	 * columns to be read, defines columns containing attributes.
	 * @param file	Name of the Input-file
	 * @param columnsTotal List containing the labels of columns for
	 * total RNA
	 * @param columnsNew List containing the labels of columns for
	 * newly transcribed RNA
	 * @param columnsPre List containing the labels of columns for
	 * pre-existing RNA
	 * @param attr List containing the labels of columns for attributes
	 */
	public Data(String file, List<String> columnsTotal, List<String> columnsNew,
			List<String> columnsPre, List<String> attr, boolean log) {
		loadData(file, columnsTotal, columnsNew, columnsPre, attr, log);
	}

	/**
	 * Constructs an empty Data-Object, so that parameters for data
	 * loading can be set before starting.
	 */
	public Data() {
	}

	/**
	 * Loads the data from a file.
	 * @param file	Name of the Input-file
	 * @param columnsTotal List containing the labels of columns for
	 * total RNA
	 * @param columnsNew List containing the labels of columns for
	 * newly transcribed RNA
	 * @param columnsPre List containing the labels of columns for
	 * pre-existing RNA
	 * @param attr List containing the labels of columns for attributes
	 */
	public void loadData(String file, List<String> columnsTotal, List<String> columnsNew,
			List<String> columnsPre, List<String> attr, boolean log) {
		filename   = file;
		map        = new Mapping<String, Integer>();
		attributes = new ArrayList<Mapping<String, String>>();
		readInput(file, columnsTotal, columnsNew, columnsPre, attr, log);
	}

	/**
	 * Constructs a Data-Object based on already existing data.
	 * @param totalRNA Array containing all values of total RNA
	 * @param newRNA Array containing all values of newly transcribed RNA
	 * @param preRNA Array containing all values of pre-existing RNA
	 * @param totalDescr A description that matches the columns of the
	 * arrays containing the values to the labels of the columns for
	 * total RNA
	 * @param newDescr matches columns of arrays to labels for newly
	 * transcribed RNA
	 * @param preDescr matches columns of arrays to labels for pre-existing
	 * RNA
	 * @param map A Map matching SpotId to index of RNA-arrays, containing
	 * only filtered values
	 */
	public Data(double[][] totalRNA, double[][] newRNA, double[][] preRNA,
			String[] totalDescr, String[] newDescr, String[] preDescr,
			Mapping<String, Integer> map) {
		this.totalRNA   = totalRNA;
		this.newRNA     = newRNA;
		this.preRNA     = preRNA;
		this.totalDescr = totalDescr;
		this.newDescr   = newDescr;
		this.preDescr   = preDescr;
		this.map        = map;
	}

	/**
	 * Takes an input file, reads the values and saves them in the class
	 * variables of the Data object.
	 * @param file Name of the input file
	 * @param columnsTotal List containing labels of columns for total RNA
	 * @param columnsNew List containing labels of columns for newly
	 * transcribed RNA
	 * @param columnsPre List containing labels of columns for pre-existing
	 * RNA
	 * @param addit List containing labels of columns with additional
	 * information; null if no additional information is wanted
	 */
	public void readInput(String file, List<String> columnsTotal, List<String> columnsNew,
			List<String> columnsPre, List<String> addit, boolean log) {
		if(!file.endsWith(".txt") && !file.endsWith(".TXT")) {
			System.err.println("HALO requires the input file to be a '.txt'-file!\n" +
					"If your file is in ASCII text format but has a different ending, please rename it " +
					"in [filename].txt. System will exit now.");
			System.exit(1);
		}
		try {
			System.out.println("Loading data...");
			
			if(log) {
				System.out.println("You chose logarithmic scale; your values are corrected with 2 to the power of [value].");
			}
			boolean reverseMap   = Mapping.isTurnReverseMapOff();
			Mapping.setTurnReverseMapOff(false);
			BufferedReader input = new BufferedReader(new FileReader(new File(file)));
			boolean header       = false;
			boolean spotId		 = false;
			boolean existAttr    = false;
			int     counter      = 0;
			maximum = 0;
			minimum = Double.MAX_VALUE;
			String line;
			final int startSize = 1000;

			totalRNA     = new double[startSize][columnsTotal.size()];
			newRNA       = new double[startSize][columnsNew.size()];
			preRNA       = new double[startSize][columnsPre.size()];
			int[] colTot = new int[columnsTotal.size()];
			int[] colNew = new int[columnsNew.size()];
			int[] colPre = new int[columnsPre.size()];
			int iTot     = 0;
			int iNew     = 0;
			int iPre     = 0;
			int index    = 0;
			int[] attr   = null;
			int iAttr    = 0;

			String[][] allAttributes = null;
			String[]   ids           = new String[startSize];

			if (addit != null) {
				existAttr = true;
				attr   = new int[addit.size()];
				if (addit.contains(spotid)) {
					attrDescr = new String[addit.size() - 1];
					allAttributes = new String[startSize][addit.size() - 1];
				} else {
					attrDescr = new String[addit.size()];
					allAttributes = new String[startSize][addit.size()];
				}
			}

			while ((line = input.readLine()) != null) {

				//skip the commentary information
				if (!line.startsWith("#") && !line.isEmpty()) {
					//first line after commentary is the header
					if (!header) {
						header         = true;
						String[] head  = line.split("\t");
						//Descriptor arrays for reproducing the header for
						//specific columns when printing the output file
						totalDescr     = new String[columnsTotal.size()];
						newDescr	   = new String[columnsNew.size()];
						preDescr       = new String[columnsPre.size()];


						//save information about which label corresponds to which column
						for (int i=0; i<head.length; i++) {
							if(columnsTotal.contains(head[i])) {
								colTot[iTot] = i;
								totalDescr[iTot] = head[i];
								iTot++;
							} else if (columnsNew.contains(head[i])) {
								colNew[iNew] = i;
								newDescr[iNew] = head[i];
								iNew++;
							} else if (columnsPre.contains(head[i])) {
								colPre[iPre] = i;
								preDescr[iPre] = head[i];
								iPre++;
							} else if (existAttr && i != 0) {
								if(addit.contains(head[i])) {
									attr[iAttr] = i;
									attrDescr[iAttr] = head[i];
									iAttr++;
								}
							} else if(i == 0) {
								//value of first column is no RNA value, therefore serves as or is spotID
								spotId = true;
							}
						}

						/* checking whether the given labels appear in the data 
						 * & whether the number of total, pre-existing and newly transcribed RNA is equal (essential for further calculation)
						 */
						if(!unequal) {
							if(iTot == 0 || iNew == 0 || iPre == 0) {
								System.err.println("There are no columns with the given labels in the dataset!");
								System.exit(0);
							} else if(iTot != iNew || iTot != iPre) {
								System.err.println("The number of total RNA columns has to be equal to the number of \n" +
										"columns with newly transcribed and pre-existing RNA, respectively!");
								System.exit(0);
							}
						}
						

						//below the header are the values, read in those
					} else {
						String[] numbers = line.split("\t");
						String id;

						//checks if there is a spotId given, else uses a counter as ID
						if(spotId) {
							id = numbers[0];
						} else {
							id = String.valueOf(counter);
							counter++;
						}

						//enlarging the arrays if too small
						if(index>=totalRNA.length) {
							int size         = totalRNA.length*2;
							double[][] temp1 = new double[size][totalRNA[0].length];
							System.arraycopy(totalRNA, 0, temp1, 0, totalRNA.length);
							totalRNA         = temp1;
							double[][] temp2 = new double[size][newRNA[0].length];
							System.arraycopy(newRNA, 0, temp2, 0, newRNA.length);
							newRNA           = temp2;
							double[][] temp3 = new double[size][preRNA[0].length];
							System.arraycopy(preRNA, 0, temp3, 0, preRNA.length);
							preRNA           = temp3;
							if(existAttr) {
								String[][] temp4 = new String[size][allAttributes[0].length];
								System.arraycopy(allAttributes, 0, temp4, 0, allAttributes.length);
								allAttributes    = temp4;
							}
							String[]   temp5 = new String[size];
							System.arraycopy(ids, 0, temp5, 0, ids.length);
							ids              = temp5;
						}
						
						ids[index]       = id;
						
						//reading in values
						for(int i=0; i<colTot.length; i++) {
							if(colTot[i] >= numbers.length || numbers[colTot[i]].isEmpty()) {
								throw new InconsistentColumnNumberException();
							}
							double value       = Double.parseDouble(numbers[colTot[i]]);
							if(log) {
								value = Math.pow(2, value);
							}
							totalRNA[index][i] = value;
							if(value > maximum) { maximum = value; }
							if(value < minimum) { minimum = value; }
						}
						for(int i=0; i<colNew.length; i++) {
							if(colNew[i] >= numbers.length || numbers[colNew[i]].isEmpty()) {
								throw new InconsistentColumnNumberException();
							}
							double value       = Double.parseDouble(numbers[colNew[i]]);
							if(log) {
								value = Math.pow(2, value);
							}
							newRNA[index][i]   = value;
							if(value > maximum) { maximum = value; }
							if(value < minimum) { minimum = value; }
						}
						for(int i=0; i<colPre.length; i++) {
							if(colPre[i] >= numbers.length || numbers[colPre[i]].isEmpty()) {
								throw new InconsistentColumnNumberException();
							}
							double value       = Double.parseDouble(numbers[colPre[i]]);
							if(log) {
								value = Math.pow(2, value);
							}
							preRNA[index][i]   = value;
							if(value > maximum) { maximum = value; }
							if(value < minimum) { minimum = value; }
						}
						
						//building of mapping object to find corresponding spotID-ArrayIndex-Pairs
						map.addMap(id, index);
						//include additional information if columns containing attributes are specified
						if(existAttr) {
							for(int i=0; i<attr.length; i++) {
								if(numbers[attr[i]].isEmpty() || attr[i] >= numbers.length) {
									allAttributes[index][i] = null;
									System.err.println("Warning: Missing attribute for " +
											"probeset "+map.getSpotId(index).get(0)+"!");
								} else {
									allAttributes[index][i] = numbers[attr[i]];
								}
							}
						}
						
						index++;
					}
				}
			}
			//restricts array size to number of occupying elements
			double[][] temp1 = new double[index][totalRNA[0].length];
			System.arraycopy(totalRNA, 0, temp1, 0, index);
			totalRNA         = temp1;
			double[][] temp2 = new double[index][newRNA[0].length];
			System.arraycopy(newRNA, 0, temp2, 0, index);
			newRNA           = temp2;
			double[][] temp3 = new double[index][preRNA[0].length];
			System.arraycopy(preRNA, 0, temp3, 0, index);
			preRNA           = temp3;
			
			String[]   temp5 = new String[index];
			System.arraycopy(ids, 0, temp5, 0, index);
			ids              = temp5;
			if(existAttr) {
				String[][] temp4 = new String[index][allAttributes[0].length];
				System.arraycopy(allAttributes, 0, temp4, 0, index);
				allAttributes    = temp4;
				Mapping.setTurnReverseMapOff(reverseMap);
				//generate mappings for attributes
				calculateMappingForAttributes(allAttributes, ids);
				Mapping.setTurnReverseMapOff(false);
			}
			
			System.out.println("Done loading data.");
			System.out.println("You have "+map.mapSize()+" probesets.");
			System.out.println("------------------------------");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch(InconsistentColumnNumberException e) { 
			System.err.println("The number of RNA values per probeset isn't consistent!\n" +
			"At least one line in your datafile contains not enough columns. System will exit now.");
			System.exit(1);
		} catch(NumberFormatException e) {
			System.err.println("Your RNA data contains values of the wrong type (not numerical)!");
			System.exit(1);
		}
	}

	/**
	 * Transcribes the attributes into a mapping id -> attribute.
	 * @param allAttributes A list of attributes for each id
	 * @param ids The ids corresponding to the indices
	 */
	public void calculateMappingForAttributes(String[][] allAttributes, String[] ids) {
		
		for(int column=0; column<allAttributes[0].length; column++) {
			Mapping<String, String> ma = new Mapping<String, String>();
			
			for(int row=0; row<allAttributes.length; row++) {
				ma.addMap(ids[row], allAttributes[row][column]);
			}
			attributes.add(column, ma);
		}
	}

	/**
	 * Reads in every attribute from a given file.
	 * @param inputfile Name of the file that contains attributes
	 */
	public void addAttributes(String inputfile) {
		addAttributes(inputfile, null);
	}

	/**
	 * Reads in regular attributes from a file.
	 * @param inputfile Name of the file that contains attributes
	 * @param columnLabels Labels of relevant columns
	 */
	public void addAttributes(String inputfile, ArrayList<String> columnLabels) {
		addAttributes(inputfile, columnLabels, false);
	}

	/**
	 * Reads in attributes from a file. Specific for attributes with a
	 * very small number of values (e.g. present calls) to speed up
	 * the loading. Please note that with this method the reverse mapping
	 * from attribute to list of IDs is not possible.
	 * @param inputfile Name of the file that contains attributes
	 * @param columnLabels Labels of relevant columns
	 */
	public void addPresenceCalls(String inputfile, ArrayList<String> columnLabels) {
		addAttributes(inputfile, columnLabels, true);
	}

	/**
	 * Reads in a file containing several columns, of which one contains
	 * the spotID, the others contain attributes.
	 * The resulting Mappings are added to the List attrMaps and accessible
	 * over the index received from attrMapsDescr, where the name of the
	 * column is stored
	 * @param inputfile Name of the file that contains attributes
	 * @param columnLabels Labels of relevant columns
	 * @param turnReverseMapOff TRUE it attributes are present calls or
	 * similar attributes with a very small number of values
	 */
	public void addAttributes(String inputfile, ArrayList<String> columnLabels, boolean turnReverseMapOff) {
		try {
			Mapping.setTurnReverseMapOff(turnReverseMapOff);
			System.out.println("Loading attributes...");
			BufferedReader inp          = new BufferedReader(new FileReader(new File(inputfile)));
			String[] desc               = null;
			int previousNumber          = 0;
			int column                  = 0;
			boolean header              = false;
			String line;
			ArrayList<Integer> relevantColumns     = new ArrayList<Integer>();
			ArrayList<Mapping<String, String>> nex = new ArrayList<Mapping<String, String>>();

			//read in attribute file
			while((line = inp.readLine()) != null) {
				if(!line.startsWith("#") && !line.isEmpty()) {
					//the first line after the commentary is the header
					if(!header) {
						header        = true;
						String[] head = line.split("\t");
						//number of new attributes is number of provided attributes if no list is given
						int newLength  = head.length-1; 
						if(columnLabels != null) {
							newLength  = columnLabels.size();
						}
						
						//if there already are attributes loaded, make descriptor array larger
						if(attrDescr != null && attrDescr.length>0) {
							//remember number of previously existing attributes
							previousNumber = attrDescr.length;
							desc = new String[attrDescr.length+newLength];
							System.arraycopy(attrDescr, 0, desc, 0, attrDescr.length);
						//if no attributes are loaded yet, generate new descriptor array
						} else {
							desc = new String[newLength];
							previousNumber = 0;
						}
						int index = 0;
						//save column labels in descriptor array
						for(int i=0; i<head.length; i++) {
							//adds descriptor to list of attribute descriptors as well as 
							//to list of descriptors used to map indices to list
							if(!head[i].equals(spotid) && columnLabels == null) { //no labels given
								desc[previousNumber+index]  = head[i];
								index++;
								nex.add(new Mapping<String, String>());
							} else if(!head[i].equals(spotid) && columnLabels != null) { //labels given
								if(columnLabels.contains(head[i])) {
									desc[previousNumber+index]  = head[i];
									index++;
									nex.add(new Mapping<String, String>());
									relevantColumns.add(i);
								}
								
							} else {
								column = i;
//								previousLength = Math.max(0, previousLength-1);
							}
						}
					} else {
						//add line to mapping
						String[] content = line.split("\t");
						String id = content[column];
						int index = 0;
						for(int i=0; i<content.length-1; i++) {
							//no labels given or label wanted
							if(columnLabels == null || relevantColumns.contains(i+1)) { //i+1 because of spotid-column
								//if there are more attributes than was defined in the header, add default name
								if(index >= nex.size()) {
									nex.add(new Mapping<String, String>());
									String[] temp = new String[desc.length+1];
									System.arraycopy(desc, 0, temp, 0, desc.length);
									temp[temp.length-1] = "attribute_"+temp.length;
									desc = temp;
								}
								//if column is empty, add undefined attribute
								if(content[i+1].isEmpty() && relevantColumns.contains(i+1)) {
									System.err.println("Warning: Missing attribute for probeset "+id+"!");
									nex.get(index).addMap(id, null);
									index++;
								//otherwise save attribute value
								} else {
									nex.get(index).addMap(id, content[i+1].trim());
									index++;
								}
							}
						}
					}
				} 
			}
			//if no header is given, add neutral header to descriptors
			if(!header) {
				for(int i=0; i<nex.size(); i++) {
					desc[previousNumber+i]  = "attribute_"+desc.length;
				}
				
			}
			//add mapping to list
			attrDescr = desc;
			attributes.addAll(nex);
			System.out.println("Done loading attributes.");
			System.out.println("------------------------------");
			Mapping.setTurnReverseMapOff(false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Loads attributes from original data file.
	 * @param newAttributes The list of attributes that will be loaded
	 */
	public void loadAttributesFromDatafile(ArrayList<String> newAttributes) {
		loadAttributesFromDatafile(newAttributes, false);
	}

	/**
	 * Loads present calls or other attributes with a small number of
	 * values from the original data file.
	 * Please note that reverse mapping (attribute to list of IDs) is
	 * not available for attributes loaded with this method for the
	 * sake of speed.
	 * @param presentCallAttributes The list of attributes that will
	 * be loaded
	 */
	public void loadPresentCallsFromDatafile(ArrayList<String> presentCallAttributes) {
		loadAttributesFromDatafile(presentCallAttributes, true);
	}

	/**
	 * Loads attributes from the original data file.
	 * @param newAttributes The list of attributes that will be loaded
	 * @param turnReverseMapOff TRUE if attributes are present calls or
	 * similar attributes with very few possible values
	 */
	public void loadAttributesFromDatafile(ArrayList<String> newAttributes, boolean turnReverseMapOff) {
		try {
			Mapping.setTurnReverseMapOff(turnReverseMapOff);
			System.out.println("Loading attributes...");
			BufferedReader inp = new BufferedReader(new FileReader(new File(filename)));
			String line;
			boolean header = false;
			String[] desc  = null;
			int newLength  = newAttributes.size();
			int prevLength = -1;
			int index      = 1;
			
			ArrayList<Integer> indices             = new ArrayList<Integer>();
			ArrayList<Mapping<String, String>> nex = new ArrayList<Mapping<String, String>>();
			String[] values;
                        String[] temp;
                        String[] head;
			if(attrDescr != null && attrDescr.length>0) {
				desc = new String[attrDescr.length+newLength];
				System.arraycopy(attrDescr, 0, desc, 0, attrDescr.length);
				prevLength = attrDescr.length-1;
			} else {
				desc = new String[newLength];
			}
			while((line = inp.readLine()) != null) {
				if(!line.startsWith("#")) {
					if(!header) {
						header = true;
						head = line.split("\t");
						//find the label in the header
						for(int i=0; i<head.length; i++) {
							if(newAttributes.contains(head[i])) {
								desc[prevLength+index] = head[i];
								index++;
								indices.add(i);
								nex.add(new Mapping<String, String>());
							} 
						}
						//if the label isn't contained in the header, exit
						if(indices.size() == 0) {
							System.err.println("None of your attributes are part of the datafile!");
							System.exit(1);
						} else if(indices.size() != newAttributes.size()) {
							System.err.println("Some of your attributes are not part of the datafile; " +
									"ignoring the following attributes:");
							for(int i=0; i<newAttributes.size(); i++) {
								if(!indices.contains(i)) {
									System.err.println(newAttributes.get(i));
								}
							}
							//shrink descriptor array so that it fits actual size of arguments
							int differ    = newAttributes.size()-indices.size();
							temp = new String[desc.length-differ];
							System.arraycopy(desc, 0, temp, 0, temp.length);
							desc          = temp;
						}
					} else {
						values = line.split("\t");
						String id = values[0];
						int i     = 0;
						
						for(int ix : indices) {
							if(values[ix].isEmpty()) {
								System.err.println("Warning: Missing attributes for probeset "+id+"!");
								nex.get(i).addMap(id, null);
							} else {
								String attribute = values[ix].trim();
								nex.get(i).addMap(id, attribute);
							}
							i++;
						}
					}
				}
			}
			//add mapping to list
			attrDescr = desc;
			attributes.addAll(nex);
			System.out.println("Done loading attributes.");
			System.out.println("------------------------------");
			Mapping.setTurnReverseMapOff(false);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Desired columns are written to a file; former order is not kept,
	 * instead all values of one type are added sequentially.
	 * @param output Name of the output file
	 * @param columnsTotal List containing labels of columns for total
	 * RNA that shall be exported
	 * @param columnsNew List containing labels of columns for newly
	 * transcribed RNA that shall be exported
	 * @param columnsPre List containing labels of columns for pre-existing
	 * RNA that shall be exported
	 * @param addit List containing labels of columns for attributes
	 */
	public void writeOutput(String output, List<String> columnsTotal,
			List<String> columnsNew, List<String> columnsPre, List<String> addit) {
		try {
			BufferedWriter outp = new BufferedWriter(new FileWriter(new File(output)));
			boolean existAttr   = false;

			int[] colTot = new int[columnsTotal.size()];
			int[] colNew = new int[columnsNew.size()];
			int[] colPre = new int[columnsPre.size()];
			int[] attr   = null;
			int iTot  	 = 0;
			int iNew	 = 0;
			int iPre	 = 0;
			int iAttr    = 0;

			outp.write(spotid+"\t");
			
			//determine the names and indices of the wanted columns
			for(int i=0; i<totalDescr.length; i++) {
				if(columnsTotal.contains(totalDescr[i])) {
					colTot[iTot] = i;
					iTot++;
					outp.write(totalDescr[i]+"\t");
				}
			}
			for(int i=0; i<newDescr.length; i++) {
				if(columnsNew.contains(newDescr[i])) {
					colNew[iNew] = i;
					iNew++;
					outp.write(newDescr[i]+"\t");
				}
			}
			for(int i=0; i<preDescr.length; i++) {
				if(columnsPre.contains(preDescr[i])) {
					colPre[iPre] = i;
					iPre++;
					outp.write(preDescr[i]+"\t");
				}
			}
			if(addit != null && attributes != null) {
				if(addit.size() > 0) {
					existAttr = true;
					attr = new int[addit.size()];

					for(int i=0; i<attrDescr.length; i++) {
						if(addit.contains(attrDescr[i])) {
							attr[iAttr] = i;
							iAttr++;
							outp.write(attrDescr[i]+"\t");
						}
					}
				}
			}

			//printing the filtered values (i.e. those that are saved in the map)
			Iterator<String> iterator = map.iterate();

			while(iterator.hasNext()) {
				String id = iterator.next();
				int i = map.getAttribute(id);
				outp.write("\n"+id+"\t");

				//first printing all 'total' rna values, then all 'newly transcribed' and last all 'pre-existing' rna
				for(int j=0; j<columnsTotal.size(); j++) {
					outp.write(totalRNA[i][colTot[j]]+"\t");
				}
				for(int j=0; j<columnsNew.size(); j++) {
					outp.write(newRNA[i][colNew[j]]+"\t");
				}
				for(int j=0; j<columnsPre.size(); j++) {
					outp.write(preRNA[i][colPre[j]]+"\t");
				}
				if(existAttr) {
					for(int k=0; k<attr.length; k++) {
						outp.write(attributes.get(attr[k]).getAttribute(id)+"\t");
					}
				}
			}
			outp.flush();
			outp.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Copies the content of the Mapping object.
	 * @return copy of the map that maps SpotID to indices
	 */
	public Mapping<String, Integer> copyMap() {
		Mapping<String, Integer> newMap = new Mapping<String, Integer>();
		Iterator<String> iterator       = map.iterate();

		while(iterator.hasNext()) {
			String spotId = iterator.next();
			int index     = map.getAttribute(spotId);
			newMap.addMap(spotId, index);
		}
		return newMap;
	}

	/**
	 * Calculates filtered Ratio of a set of RNA values (e.g. newly
	 * transcribed RNA) to a second set of values (e.g. total RNA)
	 * based on the specified method to calculate the average of
	 * multiple replicates. If none specified the default method is
	 * 'calculateRatioFirst'. Please perform the filtering method
	 * 'filterCorrectionBias' before using this method.
	 * @param rna A set of RNA values serving as numerator
	 * @param total A second set of RNA values serving as denominator
	 * @param corr The map for ID and bias correction, null if no
	 * correction should take place
	 * @param replicate The number of the replicate (starting from 0)
	 * that will be used for method REPLICATE; -1 if average should
	 * be calculated
	 * @return The ratio of the two averaged sets of RNA
	 */
	public double[] calculateRatio(double[][] rna, double[][] total, 
			HashMap<String,Double> corr, int replicate) {
			if(method == Data.AVERAGEFIRST && replicate == Normalization.UNDEF) {
				//use the second method which calculates the ratio based on the average values or
				ratio = calculateAverageFirst(rna, total, corr);
			} else if(method == Data.RATIOFIRST && replicate == Normalization.UNDEF){
				//start with calculating the ratio for the RATIOFIRST method
				double[][] allRatio        = new double[map.mapSize()][rna[0].length];
				Iterator<String> iterator  = map.iterate();
				int i                      = 0;

				/* 
				 * calculation of ratio for all values in the multidimensional arrays
				 * that are present in the map (i.e. not filtered out)
				 */
				while(iterator.hasNext()) {
					String id = iterator.next();
					int index = map.getAttribute(id);
					for(int j=0; j<allRatio[i].length; j++) {
						double co      = 0.0;
						if(corr != null) {
							if(corr.containsKey(id)) {
								co = corr.get(id);
							}
							//calculate normalized ratio if correction factors are given
							allRatio[i][j] = (rna[index][j]/total[index][j])/Math.pow(co,2);
						} else {
							allRatio[i][j] = (rna[index][j]/total[index][j]);
						}
						
						
					}
					i++;
				}
				//restricting the ratio to a one dimensional array, either
				if(rna[0].length == 1) {
					//restrict the array if there is only one column occupied or
					ratio = restrictRatio(allRatio, 0);
				} else if(method == 1) {
					//use the (standard) method to calculate the average of the ratios
					ratio = calculateRatioFirst(allRatio);
				}
			} else {
			ratio = new double[map.mapSize()];
			Iterator<String> iterator = map.iterate();
			int i = 0;
			
			while(iterator.hasNext()) {
				String id = iterator.next();
				int index = map.getAttribute(id);
				double co      = 0.0;
				if(corr != null) {
					if(corr.containsKey(id)) {
						co = corr.get(id);
					}
				} 
				ratio[i] = (rna[index][replicate]/total[index][replicate])-co;
				i++;
			}
		}
		return ratio;
	}

	/**
	 * Calculates the ratios for an average over a set of replicates.
	 * @param rna A set of RNA values serving as numerator
	 * @param total A second set of RNA values serving as denominator
	 * @param corr The map for ID and bias correction, null if no
	 * correction should take place
	 * @return The ratio of the two averaged sets of RNA
	 */
	public double[] calculateRatio(double[][] rna, double[][] total, 
			HashMap<String,Double> corr) {
		return calculateRatio(rna, total, corr, replicate);
	}

	/**
	 * Calculates the Mapping corresponding to the ratio.
	 * @return Mapping which matches SpotID to index of ratio array
	 */
	public Mapping<String, Integer> calculateNewMapping() {
		Iterator<String> iterator      = map.iterate();
		Mapping<String, Integer> newMap = new Mapping<String, Integer>();
		int i                           = 0;

		//fill the Mapping equivalently to the calculation of ratios
		while(iterator.hasNext()) {
			String id = iterator.next();
			newMap.addMap(id, i);
			i++;
		}
		return newMap;
	}

	/**
	 * Restricts a multidimensional array onto an array with one
	 * dimension containing the wanted replicate, while keeping
	 * the order.
	 * @param input Ratio or half-life array
	 * @param replicate The replicate to which the ratio will be restricted
	 * @return single dimensional array
	 */
	public double[] restrictRatio(double[][] input, int replicate) {

		double[] result = new double[input.length];
		if(replicate == -1) {
			result = null;
		} else {
			for(int i=0; i<input.length; i++) {
				result[i] = input[i][replicate];
			}
		}
		return result;
	}

	/**
	 * calculates the average values from all replicates for the
	 * linear regression.
	 * calculates the average of the ratios (RATIOFIRST method)
	 * @param allRatio The array containing the previously calculated ratios
	 * @return The average of the different ratios
	 */
	public double[] calculateRatioFirst(double[][] allRatio) {
		double[] result = new double[allRatio.length];

		for(int i=0; i<allRatio.length; i++) {
			double sum    = 0.0;
			double number = 0.0;

			for(int j=0; j<allRatio[i].length; j++) {
				sum   += allRatio[i][j];
				number = (double) j+1;
			}
			double avg = sum/number;
			result[i]  = avg;
		}

		return result;
	}

	/**
	 * Calculates the average values from all replicates for the
	 * linear regression through calculating the ratio of the
	 * averages (AVERAGEFIRST method).
	 * @param rna A set of RNA that serves as numerator
	 * @param total A set of RNA that serves as denominator
	 * @param corr The map for ID and bias correction, null if no
	 * correction should take place
	 * @return The ratio of the averages of the given sets
	 */
	public double[] calculateAverageFirst(double[][] rna, double[][] total,
			HashMap<String,Double> corr) {
		double[][] temp        = new double[map.mapSize()][2];
		double[] result        = new double[map.mapSize()];
		Iterator<String> iter = map.iterate();
		int i                  = 0;

		while(iter.hasNext()) {
			String id = iter.next();
			int index = map.getAttribute(id);

			double sum    = 0.0;
			double number = 0.0;

			//calculate the sum of all 'total' items for one spotid
			for(int j=0; j<total[index].length; j++) {
				sum   += total[index][j];
				number = (double) j+1;
			}
			//calculate the average of these
			double avg = sum/number;
			temp[i][0] = avg;
			sum        = 0.0;
			number     = 0.0;

			//repeat the same for the second rna array (e.g. newly transcribed or pre-existing)
			for(int j=0; j<rna[index].length; j++) {
				sum   += rna[index][j];
				number = (double) j+1;
			}
			avg        = sum/number;
			temp[i][1] = avg;

			//calculate the fraction of the two (rna average to total average)
			double co      = 0.0;
			if(corr != null) {
				if(corr.containsKey(id)) {
					co = corr.get(id);
				}
			} 
			result[i]  = temp[i][1]/temp[i][0]-co;
			i++;
		}

		return result;
	}

	/**
	 * Writes the half-life values contained in the array and in the
	 * map together with corresponding spotIds into a file.
	 * Appropriate for half-lives, ratios & pqs (for which the new
	 * mapping applies)
	 * @param output Name of output file
	 * @param array List containing any set of values
	 */
	public void writeToFileArray(String output, double[] array) {
		try {
			BufferedWriter    outp     = new BufferedWriter(new FileWriter(new File(output)));
			Mapping<String,Integer> newMap = calculateNewMapping();
			Iterator<String> iterator = newMap.iterate();

			//gets all the values that are contained in the map (i.e. not filtered out) and writes them into the file
			while(iterator.hasNext()) {
				String id = iterator.next();
				int index  = newMap.getAttribute(id);
				outp.write(id+"\t");
				outp.write(array[index]+"\n");
			}
			outp.flush();
			outp.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method for the Uracil-based evaluation of the values: Compares
	 * (average) uracil number of the sequence to logarithm of the ratio.
	 * For multiple transcript entries per gene an average uracil number
	 * is calculated.
	 * @param fasta The fasta file which contains the sequences
	 * @param column The number of the column (starting with 1) of the
	 * header in which the genename is contained (i.e. header:
	 * > genename | attribute | attribute leads to column = 1)
	 * @param uraMeth Either "log(e'/n')", or "log(e'/u')", or
	 * "log(u'/n')", according to the ratio which will be evaluated
	 * @param printName Name of the output file for plotting information
	 * @param corr The map for ID and bias correction, null if no
	 * correction should take place
	 * @param plot TRUE if histogram plot is wanted, FALSE otherwise
	 * @return result Average uracil number
	 */
	@SuppressWarnings("unchecked")
	public double evaluate(String fasta, int column, String uraMeth, String printName, 
			HashMap<String, Double> corr, boolean plot) {
//		int method = this.method;
//		this.method = AVERAGEFIRST;
                
		XYGraphConstructor graphConstructor = new XYGraphConstructor("Evaluation of data quality", uraMeth, "#Uracil");
		graphConstructor.addEmptySeries("#uracil vs. "+uraMeth);
		System.out.println("------------------------------");
		System.out.println("Starting quality control...");
		
		spot = new String[map.mapSize()];
		dat  = new double[map.mapSize()];
		
		try {
			BufferedWriter outp = null;
			if(!printName.isEmpty()) {
				outp = new BufferedWriter(new FileWriter(new File(printName)));
			}
			String plotfile     = fasta+".plot";
			BufferedReader buff = new BufferedReader(new FileReader(new File(fasta)));
			double[] uracils    = new double[map.mapSize()];
			String line;
			String gene   = "";
			double sumU   = 0;
			double sumL   = 0;
			double number = 0;
			boolean done  = false;
			double[] rat;
			if(uraMeth.equals(LOGEN)) {
				rat = calculateRatio(newRNA, totalRNA, corrNewTot);
			} else if(uraMeth.equals(LOGUN)) {
				rat = calculateRatio(preRNA, totalRNA, corrPreTot);
			} else if(uraMeth.equals(LOGEU)) {
				rat = calculateRatio(newRNA, preRNA, corrNewPre);
			} else {
				System.err.println("The ratio method you defined is not valid for this method!\n" +
						"Please use one of the following: "+LOGEN+", "+LOGUN+", "+LOGEU+".");
				rat = null;
				System.exit(1);
			}
			Mapping<String, Integer> newMapping = calculateNewMapping();
			int index     = -1;
			int j         = -1;
			StringBuffer sequence = new StringBuffer("");
			if(attrDescr.length > 0) {
				//checking which of the attributes mappings contains the gene name
				for(int i=0; i<attrDescr.length; i++) {
					if(attrDescr[i].equals(geneName)) {
						index = i;
					} 
				}
			} else {
				System.err.println("You need to add the genenames to the data before you can \n" +
						"evaluate the probeset quality!");
				System.exit(1);
			}
			
			if(index == -1) {
				System.err.println("You need to add the genenames to the data before you can \n" +
				"evaluate the probeset quality!");
				System.exit(1);
			}
			
			//writing the header for the plotfile
			if(!printName.isEmpty()) {
				outp.write("uracil\t"+"#"+uraMeth+"\n");
			}
			//getting the mapping that maps spotid to genename
			Mapping<String, String> atts = attributes.get(index);
			ArrayList<String> spots      = null;
			int i = 0;
			Tuple<Double,Double>[] tupel = new Tuple[map.mapSize()];
			//Save total sum and number of entries for multiple-transcript-genes
			HashMap<String, Tuple<Double,Double>> multiple = new HashMap<String, Tuple<Double,Double>>();
			//Mapping for gene to uracil-array index for multiple-transcript-genes
			Mapping<String, Integer> spotToUracilIndex = new Mapping<String, Integer>();

			//reading in the fasta file
			while((line = buff.readLine()) != null || !done) {
				
				//check if end of file is reached
				if(line == null) {
					done = true;
					line = ">";
				}
				//starting evaluation for current sequence (and gene name from previous line)
				if(line.startsWith(">")) {
                                     
					spots         = atts.getSpotId(gene);
					
					//getting the last sequence and calculating values
					if(spots != null) {
						
						//if this is the first appearance of this gene, set sum and number of entries to 0
						if(!multiple.containsKey(gene)) {
							Tuple<Double,Double> tuple = new Tuple<Double, Double>(0.0, 0.0);
							multiple.put(gene, tuple);
						}
						
						Tuple<Double,Double> tuple = multiple.get(gene);
						int length     = sequence.length();
						String without = "";
						String seq     = sequence.toString();
						//deleting the uracils/thymosins from the sequence for an easy counting of the number of U/T
						if(seq.contains("U")) {
							without = seq.replace("U", "");
						} else if(seq.contains("T")) {
							without = seq.replace("T", "");
						}
						//compare the number of U/T with all the spotids for this gene and write into plotfile
						for(String spotId : spots) {
							//check if this spot is contained in the map
							if(map.containsSpot(spotId)) {
								//initialize mapping to index
								if(!spotToUracilIndex.containsSpot(spotId)) {
									spotToUracilIndex.addMap(spotId, i);
									i++;
								} 
								//newI is either next index for empty field or index for this gene
								int newI     = spotToUracilIndex.getAttribute(spotId);
								//retrieving the index of each spotID
								j  = newMapping.getAttribute(spotId);
								
								//the sum of all previous transcripts' uracil numbers for this gene
								double previousSum = tuple.getFirst();
								//the number of all previous transcripts for this gene
								double previousCou = tuple.getSecond();
								//the number of uracils for this transcript
								double uraNumber   = length-without.length();
								//add previous values and current values together
								Tuple<Double, Double> newTuple = new Tuple<Double, Double>(previousSum+uraNumber, previousCou+1);
								multiple.put(gene, newTuple);
								
								//calculate average over all uracil numbers
								uracils[newI]  = (previousSum+uraNumber)/(previousCou+1);
								//TODO: check if correction is correct
								double log  = Math.log(rat[j])/Math.log(2);
								double co   = 0.0;
								if(corr != null) {
									if(corr.containsKey(spotId)) {
										co     -= corr.get(spotId);
									} else {
										map.removeSpot(spotId);
										continue;
									}
								} 
								log        -= co;
								sumU       += uracils[newI];
								sumL       += log;
								
								spot[newI] = spotId;
								dat[newI]  = log;
								number++;
							}
						}
					} 
					
					if(line.length()>1) {
						String[] head = line.substring(1).split("\\|");
						gene          = head[column-1].trim();
						sequence = new StringBuffer("");
						//retrieving the list of spotIds that correspond to this gene
						spots = atts.getSpotId(gene);
					}

					//getting the sequence over all lines
				} else if(!line.isEmpty()) {
					sequence.append(line.trim());
				}
			}
			//start the generation of a plotting file and plotting
			if(plot || !printName.isEmpty()) {
				for(int k=0; k<Math.min(uracils.length, i); k++) {
					String id   = spotToUracilIndex.getSpotId(k).get(0);
					j  = newMapping.getAttribute(id);
					double log  = Math.log(rat[j])/Math.log(2);
					double co   = 0.0;
					if(corr != null) {
						if(corr.containsKey(id)) {
							co     -= corr.get(id);
						} else {
							map.removeSpot(id);
							continue;
						}
					} 
					log        -= co;
					Tuple<Double,Double> tup   = new Tuple<Double,Double>(log, uracils[k]);
					tupel[k]    = tup;
					//add data to graph
					graphConstructor.addData(uracils[k],log);
					if(!printName.isEmpty()) {
						outp.write(uracils[k]+"\t"+log+"\n");
					}
				}
				if(plot) {
					graphConstructor.generateGraph();
					//calculating the average number of uracils and the average log(e'/n')
					double avgU = sumU/(double)uracils.length;
					double avgL = sumL/(double)uracils.length;
					System.out.println("Average uracil number: "+avgU+"; Average "+uraMeth+": "+avgL);
					avgUr       = avgU;
					avgLo       = avgL;
					plotName    = plotfile;
				}
			}
			if(!printName.isEmpty()) {
				outp.flush();
				outp.close();
			}
			buff.close();
			
			if(i < spot.length) {
				int differ    = spot.length-i;
				int numb      = i+1;
				System.err.println("\n"+differ+" probesets had to be discarded, because no sequence " +
						"data was available for them.");
				System.out.println("You have "+numb+" probesets.");
				String[] temp = new String[i];
				System.arraycopy(spot, 0, temp, 0, i);
				spot = temp;
			}
			
			System.out.println("Done with quality control");
			System.out.println("------------------------------");
			
//		this.method = method;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			System.err.println("The column number you specified for sequence loading exceeds the information content \n" +
					"of the fasta header! Please check which field of the fasta header contains the gene name (starting " +
					"with 1 for the first field) and start again.");
			System.exit(1);
		}
		return avgUr;
	}
	
	/**
	 * Sets the new minimum and maximum
	 */
	public void findMiniMaxi() {
		Iterator<String> iterator = map.iterate();
		maximum = 0;
		minimum = Double.MAX_VALUE;
		while(iterator.hasNext()) {
			String id = iterator.next();
			int index = map.getAttribute(id);
			double[] newly = newRNA[index];
			for(double item : newly) {
				if(item > maximum) {
					maximum = item;
				} 
				if(item < minimum) {
					minimum = item;
				}
			}
			double[] pre = preRNA[index];
			for(double item : pre) {
				if(item > maximum) {
					maximum = item;
				} 
				if(item < minimum) {
					minimum = item;
				}
			}
			double[] total = totalRNA[index];
			for(double item : total) {
				if(item > maximum) {
					maximum = item;
				} 
				if(item < minimum) {
					minimum = item;
				}
			}
		}
	}

	/**
	 * Sets the correction bias values for the ratio newly transcribed/pre-existing.
	 * @param corr The correction bias values for the ratio newly transcribed/pre-existing
	 */
	public void setCorrNewPre(HashMap<String, Double> corr) {
		this.corrNewPre = corr;
	}

	/**
	 * Sets the correction bias values for the ratio newly transcribed/total.
	 * @param corrNewTot The correction bias values for the ratio
	 * newly transcribed/total
	 */
	public void setCorrNewTot(HashMap<String, Double> corrNewTot) {
		this.corrNewTot = corrNewTot;
	}

	/**
	 * Sets the correction bias values for the ratio pre-existing/total.
	 * @param corrPreTot The correction bias values for the ratio
	 * pre-existing/total
	 */
	public void setCorrPreTot(HashMap<String, Double> corrPreTot) {
		this.corrPreTot = corrPreTot;
	}

	/**
	 * Sets the method used for calculating the average ratios
	 * over all replicates.
	 * @param i The integer value for the method
	 */
	public void setMethod(int i) {
		if(i!=AVERAGEFIRST && i!=RATIOFIRST && i!=REPLICATE) {
			System.err.println("The method you tried to set for calculation of average ratios \n" +
					"does not exist. Please use '"+RATIOFIRST+"' for Method RATIOFIRST, '"+AVERAGEFIRST+"\n" +
							"' for Method AVERAGEFIRST and '"+REPLICATE+"' for usage of a replicate \n" +
									"instead of an average. Set to default: RATIOFIRST");
			i = RATIOFIRST;
		}
		method = i;
	}

	/**
	 * Sets the mapping corresponding to the whole data and filtered values.
	 * @param map The mapping corresponding to data
	 */
	public void setMap(Mapping<String, Integer> map) {
		this.map = map;
	}

	/**
	 * Sets the maximum RNA value
	 * @param maximum Maximum RNA value
	 */
	public void setMaximum(double maximum) {
		this.maximum = maximum;
	}
	
	/**
	 * Sets the minimum RNA value
	 * @param minimum Minimum RNA value
	 */
	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}
	
	/**
	 * Sets the name of the file holding the data information.
	 * @param filename Name of the file with the data
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Sets the attributes list for the data.
	 * @param attributes The list of attribute mappings
	 */
	public void setAttributes(ArrayList<Mapping<String, String>> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Sets the description of all attributes.
	 * @param attrDescr The description of attributes
	 */
	public void setAttrDescr(String[] attrDescr) {
		this.attrDescr = attrDescr;
	}

	/**
	 * Sets the name of the column (column label) which holds information
	 * on present/absent.
	 * @param absent Column label for present/absent calls
	 */
	public void setAbsent(String absent) {
		this.absent = absent;
	}

	/**
	 * Sets the name of the column which holds information on the gene name.
	 * @param geneName Column label for gene name
	 */
	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}

	/**
	 * Sets the name of the column with probeset ids.
	 * @param probesetId Name of the column with probeset ids
	 */
	public void setSpotid(String probesetId) {
		this.spotid = probesetId;
	}

	/**
	 * Set the value of the unequal variable, meaning if there is an
	 * unequal number of measurements (new, pre, total).
	 * @param unequal True, if there is an unequal number of measurements
	 */
	public void setUnequal(boolean unequal) {
		this.unequal = unequal;
	}

	/**
	 * Sets the replicate used for ratio calculations, if method is
	 * restricting of ratios to one replicate.
	 * @param replicate The replicate used for ratio calculations
	 */
	public void setReplicate(int replicate) {
		this.replicate = Math.max(replicate-1, Normalization.UNDEF);
	}

	/**
	 * Sets the probe set quality scores.
	 * @param pqs The probe set quality scores
	 */
	public void setPqs(double[] pqs) {
		this.pqs = pqs;
	}

	/**
	 * Returns the probe set quality scores.
	 * @return The probe set quality scores
	 */
	public double[] getPqs() {
		return pqs;
	}

	/**
	 * Returns the call used for present/absent filtering.
	 * @return The call used for present/absent filtering
	 */
	public String getAbsent() {
		return absent;
	}

	/**
	 * Returns the number of the method used for ratio calculation.
	 * @return Number of the method used for ratio calculation
	 */
	public int getMethod() {
		return method;
	}

	/**
	 * Returns the number of the replicate used for calculations.
	 * @return Number of the replicate used for calculations
	 */
	public int getReplicate() {
		return replicate;
	}

	/**
	 * Returns the name of the column with present/absent calls.
	 * @return The name of the column with present/absent calls
	 */
	public String getAbsentLabel() {
		return absentLabel;
	}

	/**
	 * Returns the name of the column with gene names.
	 * @return The name of the column with gene names
	 */
	public String getGeneName() {
		return geneName;
	}

	/**
	 * Returns the name of the column with probeset ids.
	 * @return The name of the column with probeset ids
	 */
	public String getSpotid() {
		return spotid;
	}

	/**
	 * Returns the maximal expression value present in the data
	 * @return The maximal expression value
	 */
	public double getMaximum() {
		return maximum;
	}
	
	/**
	 * Returns the minimal expression value present in the data
	 * @return The minimal expression value
	 */
	public double getMinimum() {
		return minimum;
	}
	
	/**
	 * Returns the name of the file containing the data.
	 * @return The name of the data file
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Returns the list of spots.
	 * @return The list of spots
	 */
	public String[] getSpot() {
		return spot;
	}

	/**
	 * Returns the evaluated normalized data.
	 * @return The evaluated normalized data
	 */
	public double[] getDat() {
		return dat;
	}

	/**
	 * Returns the attribute lists.
	 * @return The list of attribute mappings
	 */
	public ArrayList<Mapping<String, String>> getAttributes() {
		return attributes;
	}

	/**
	 * Returns the map corresponding to the data.
	 * @return The map for the data
	 */
	public Mapping<String, Integer> getMap() {
		return map;
	}

	/**
	 * Returns the description of the attributes.
	 * @return The description of attributes
	 */
	public String[] getAttrDescr() {
		return attrDescr;
	}

	/**
	 * Returns the description = column labels of newly transcribed RNA.
	 * @return The description of newly transcribed RNA
	 */
	public String[] getNewDescr() {
		return newDescr;
	}

	/**
	 * Returns all the values for newly transcribed RNA.
	 * @return The measured values for newly transcribed RNA
	 */
	public double[][] getNewRNA() {
		return newRNA;
	}

	/**
	 * Returns the correction bias values for the ratio newly transcribed/pre-existing.
	 * @return The correction bias values for the ratio newly transcribed/pre-existing
	 */
	public HashMap<String, Double> getCorrNewPre() {
		return corrNewPre;
	}

	/**
	 * Returns the correction bias values for the ratio newly transcribed/total.
	 * @return The correction bias values for the ratio newly transcribed/total
	 */
	public HashMap<String, Double> getCorrNewTot() {
		return corrNewTot;
	}

	/**
	 * Returns the correction bias values for the ratio pre-existing/total.
	 * @return The correction bias values for the ratio pre-existing/total
	 */
	public HashMap<String, Double> getCorrPreTot() {
		return corrPreTot;
	}

	/**
	 * Returns the description = column labels of pre-existing RNA.
	 * @return The description of pre-existing RNA
	 */
	public String[] getPreDescr() {
		return preDescr;
	}

	/**
	 * Returns all the values for pre-existing RNA.
	 * @return The measured values for pre-existing RNA
	 */
	public double[][] getPreRNA() {
		return preRNA;
	}

	/**
	 * Returns the description = column labeles of total RNA.
	 * @return The description of total RNA
	 */
	public String[] getTotalDescr() {
		return totalDescr;
	}

	/**
	 * Returns all the values for total RNA.
	 * @return The measured values for total RNA
	 */
	public double[][] getTotalRNA() {
		return totalRNA;
	}

	/**
	 * Returns the number of measured experiments
	 * (not changeable through filtering!).
	 * @return The number of measured experiments
	 */
	public int getNumberExperiments() {
		return totalRNA.length;
	}

	/**
	 * Returns the number of probesets present after filtering.
	 * @return The number of (filtered) probesets
	 */
	public int getSize() {
		return map.mapSize();
	}

	/**
	 * Returns the average log(e'/n').
	 * @return The average log(e'/n')
	 */
	public double getAvgLo() {
		return avgLo;
	}

	/**
	 * Returns the average number of Uracil.
	 * @return The average number of Uracil
	 */
	public double getAvgUr() {
		return avgUr;
	}

	/**
	 * Returns the total number of replicates.
	 * @return The total number of replicates
	 */
	public int getReplicatesTotal() {
		return totalRNA[0].length;
	}

	/**
	 * Returns the name of the file into which evaluation
	 * results will be printed as a plottable table.
	 * @return The name of the file containing the plot
	 * information for evaluation
	 */
	public String getPlotName() {
		return plotName;
	}

	/**
	 * Returns TRUE if unequal number of RNA measurements has been loaded.
	 * @return TRUE if unequal number of RNA measurements has been loaded
	 */
	public boolean isUnequal() {
		return unequal;
	}

}
