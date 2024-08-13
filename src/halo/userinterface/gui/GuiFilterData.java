/*
 * This file is part of the HALO 1.3 graphical user interface
 */
package halo.userinterface.gui;

import halo.data.Data;
import halo.data.Filter;
import halo.data.Mapping;
import halo.normalization.Normalization;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 'Interface' which connects GUI and loading/filtering methods for data
 * @author Stefanie Kaufmann
 *
 */
public class GuiFilterData {
	public static final String THRESHOLD = "threshold";
	public static final String PRESENT   = "present";
	public static final String PQSMIN    = "pqsMin";
	public static final String PQS       = "pqs";
	
	private Data             data;
	private String           path;
	private File             output;
	private File             fasta;
	private int              column;
	private Normalization    norm;
	private String           pqsName;
	private boolean          normal  = false;
	private boolean          pqsPlot = false;
	private boolean          log     = false;
	private boolean          first   = true;
	private boolean          attributesInFile = false;
	private boolean          turnReverseMapOff = false;
	private boolean          combined = false;
	private boolean          calls;
	private String spotid;
	private String genename = "";
	private String originalGene;
	private List<String> methods       = new ArrayList<String>();
	private List<String> labelsNew     = new ArrayList<String>();
	private List<String> labelsPre     = new ArrayList<String>();
	private List<String> labelsTot     = new ArrayList<String>(); 
	private List<String> labelsNewOut  = new ArrayList<String>();
	private List<String> labelsPreOut  = new ArrayList<String>();
	private List<String> labelsTotOut  = new ArrayList<String>();
	private List<String> attributesOut = new ArrayList<String>();
	private List<String> attributes    = new ArrayList<String>();
	private List<String> originalAtts  = new ArrayList<String>();
	
	
	/**
	 * Constructor creating an object of GuiFilterData
	 */
	public GuiFilterData() {
		
	}
	
	/**
	 * Creates a Data object from previously set variables
	 */
	public void loadData() {
		data = new Data();
		data.setSpotid(spotid);
		if(genename.isEmpty()) {
			genename = data.getGeneName();
		} else {
			data.setGeneName(genename);
		}
		originalGene = data.getGeneName();
		if(attributes == null || !attributesInFile || first) {
			data.loadData(path, labelsTot, labelsNew, labelsPre, null, log);
			first = false;
		} else {
			Mapping.setTurnReverseMapOff(turnReverseMapOff);
			data.loadData(path, labelsTot, labelsNew, labelsPre, attributes, log);
			Mapping.setTurnReverseMapOff(false);
			turnReverseMapOff = false;
		}
	}
	
	/**
	 * Filters a previously loaded Data object according to the set methods
	 */
	public void prepareData() {
		//check whether method & output file are specified, if so filter data accordingly
		if(methods.size() != 0) {
			ArrayList<String> temporary = new ArrayList<String>();
			if(!combined) {
				temporary.add(methods.get(methods.size()-1));
			} else {
				temporary.addAll(methods);
			}
			for(String method : temporary) {
				
					String[] meths = method.split(",");
					//iterate through all the given methods, filter the data according to the method
					for(int i=0; i<meths.length; i++) {
						//if a certain threshold should be exceeded, filter data
						if(meths[i].startsWith(THRESHOLD)) {
							String[] meth    = method.split("=");
							double threshold = Double.parseDouble(meth[1]);
							data             = Filter.filter(data, threshold);
						//if absent values shoud be erased, filter data
						} else if(meths[i].startsWith(PRESENT)) {
							String[] meth  = method.split("=");
							String[] cont  = meth[1].split("\\|");
							String[] label = cont[0].split(";");
							String call    = cont[1].trim();
							int threshold  = Integer.parseInt(cont[2]);
							ArrayList<String> labels = new ArrayList<String>();
							for(String lab : label) {
								labels.add(lab);
							}
							data = Filter.filterAbsent(data, labels, call, threshold);
						} 
						//if minimal PQS should be kept, others erased, filter data
						else if(meths[i].startsWith(PQSMIN)) {
							int replicate = Integer.parseInt(method.split("=")[1]);
							data = Filter.filterPQS(data, norm, replicate, pqsPlot);
							if(pqsName != null) {
								double[] pqs = data.getPqs();
								data.writeToFileArray(pqsName, pqs);
							}
						//if PQS over a certain threshold should be removed, filter data
						} else if(meths[i].startsWith(PQS)) {
								String[] meth = method.split("=");
								double threshold = Double.parseDouble(meth[1]);
								data          = Filter.filterPQS(data, norm, threshold, pqsPlot);
								if(pqsName != null) {
									double[] pqs = data.getPqs();
									data.writeToFileArray(pqsName, pqs);
								}
					}
					}
				}
		}
		System.out.println("Done");
	}
	
	/**
	 * Starts the evaluation using the multiple fasta file
	 */
	public void evaluateFasta(String uraMeth, String printName, HashMap<String, Double> corr, boolean plot) {
	  
            data.evaluate(fasta.getAbsolutePath(), column, uraMeth, printName, corr, plot);
	}
	
	/**
	 * Saves the Data object into a file
	 */
	public void saveData() {
		data.writeOutput(output.getAbsolutePath(), labelsTotOut, labelsNewOut, labelsPreOut, attributesOut);
	}
	
	/**
	 * Checks whether Data has been set
	 * @return TRUE if data is filled
	 */
	public boolean hasData() {
		return !(data == null);
	}
	
	/**
	 * Checks whether normalization has been performed
	 * @return TRUE if normalization has been performed, FALSE otherwise
	 */
	public boolean hasNormalization() {
		return normal;
	}
	
	/**
	 * Checks whether the multiple fasta file containing
	 * the sequences has been set and whether the attribute
	 * describing the gene names for each spot exists
	 * @return TRUE if gene names and sequences are set,
	 * FALSE otherwise
	 */
	public boolean hasGenes() {
		if(fasta == null || data.getAttrDescr() == null) {
			return false;
		} else {
			for(int i=0; i<data.getAttrDescr().length; i++) {
				if(data.getAttrDescr()[i].equals(data.getGeneName())) {
					return true;
				} 
			}
			return false;
		}
	}
	
	/**
	 * Checks whether the attribute containing gene names
	 * for each spot has been set
	 * @return TRUE if there are gene names for the spots,
	 * FALSE otherwise
	 */
	public boolean hasGeneNames() {
		if(data.getAttrDescr() == null) {
			return false;
		} 
		for(int i=0; i<data.getAttrDescr().length; i++) {
			if(data.getAttrDescr()[i].equals(data.getGeneName())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Extracts a new attribute from a given file and 
	 * adds it to the data
	 * @param file The file containing the attribute
	 */
	public void addAttr(File file, ArrayList<String> labels) {
		data.addAttributes(file.getAbsolutePath(), labels, turnReverseMapOff);
		turnReverseMapOff = false;
	}
	
	public void addOriginalAttr(ArrayList<String> labels, boolean calls) {
		data.loadAttributesFromDatafile(labels, calls);
		this.originalAtts = labels;
	}
	
	/**
	 * Adds a list of attributes to the data, as well as the corresponding
	 * descriptors
	 * @param list A list of different attributes, each mapped to spots
	 * @param attrDescr A descriptor which holds the name of each attribute
	 */
	public void addAttributesList(ArrayList<Mapping<String, String>> list, String[] attrDescr) {
		data.setAttributes(list);
		data.setAttrDescr(attrDescr);
	}
	
	/**
	 * Adds the necessary information for the multiple fasta file; its
	 * location and the column containing the gene name
	 * @param file The multiple fasta file containing sequences of the data
	 * @param column The 'column' of the fasta header which contains the
	 * gene name; columns are divided by '|'
	 */
	public void addFasta(File file, int column) {
		this.fasta  = file;
		this.column = column;
	}
	
	/**
	 * Adds a filtering method and a corresponding value, e.g. a threshold
	 * @param method The name of a filtering method
	 * @param filterValue The value used in this filtering method, e.g. a threshold
	 */
	public void addMethod(String method, String filterValue) {
		this.methods.add(method+"="+filterValue);
	}
	
	/**
	 * Sets the list of attributes for output
	 * @param attributesOut List of attributes for output
	 */
	public void setAttributesOut(List<String> attributesOut) {
		this.attributesOut = attributesOut;
	}
	
	/**
	 * Sets the attributes from the data file
	 * @param originalAtts Attributes from the data file
	 */
	public void setOriginalAtts(List<String> originalAtts) {
		this.originalAtts = originalAtts;
	}
	
	/**
	 * Sets the file containing the initial data
	 * @param file The file containing the expression data
	 */
	public void setFile(File file) {
		this.path = file.getAbsolutePath();
	}
	
	/**
	 * Sets a variable that remembers if attributes were loaded from original file
	 * @param attributesInFile TRUE if attributes are contained in data file
	 */
	public void setAttributesInFile(boolean attributesInFile) {
		this.attributesInFile = attributesInFile;
	}
	
	/**
	 * Sets the pqs-plot variable
	 * @param pqsPlot TRUE if pqs should be plotted, FALSE otherwise
	 */
	public void setPqsPlot(boolean pqsPlot) {
		this.pqsPlot = pqsPlot;
	}
	
	/**
	 * Should be set true for present calls
	 * @param turnReverseMapOff TRUE for present calls
	 */
	public void setTurnReverseMapOff(boolean turnReverseMapOff) {
		this.turnReverseMapOff = turnReverseMapOff;
	}
	
	/**
	 * Sets the scale; true for log scale, false for linear
	 * @param log TRUE for log scale, false for linear scale
	 */
	public void setLog(boolean log) {
		this.log = log;
	}
	
	/**
	 * Sets the name of the gene name attribute
	 * @param genename The name of the gene name attribute in the data
	 */
	public void setGenename(String genename) {
		this.genename = genename;
		data.setGeneName(genename);
	}
	
	/**
	 * Sets the name of the column with probe-set-ids
	 * @param spot The name of the column with spot-ids
	 */
	public void setSpotid(String spot) {
		this.spotid = spot;
//		data.setProbesetId(spotid);
	}
	
	/**
	 * Sets the column labels for newly transcribed RNA that
	 * will be read in
	 * @param lab The list of column labels for newly transcribed RNA
	 */
	public void setLabelsNew(List<String> lab) {
		this.labelsNew = lab;
	}
	
	/**
	 * Sets the column labels for pre-existing RNA that
	 * will be read in
	 * @param lab The list of column labels for pre-existing RNA
	 */
	public void setLabelsPre(List<String> lab) {
		this.labelsPre = lab;
	}
	
	/**
	 * Sets the column labels for total RNA that
	 * will be read in
	 * @param lab The list of column labels for total RNA
	 */
	public void setLabelsTot(List<String> lab) {
		this.labelsTot = lab;
	}
	
	/**
	 * Sets the column labels for newly transcribed RNA that will be
	 * written into the output
	 * @param lab The list of column labels for newly transcribed RNA
	 */
	public void setNewOut(List<String> lab) {
		this.labelsNewOut = lab;
	}
	
	/**
	 * Sets the column labels for pre-existing RNA that will be
	 * written into the output
	 * @param lab The list of column labels for pre-existing RNA
	 */
	public void setPreOut(List<String> lab) {
		this.labelsPreOut = lab;
	}
	
	/**
	 * Sets the column labels for total RNA that will be
	 * written into the output
	 * @param lab The list of column labels for total RNA
	 */
	public void setTotOut(List<String> lab) {
		this.labelsTotOut = lab;
	}
	
	/**
	 * Sets the list of attributes corresponding to the data
	 * @param attributes The list of attributes
	 */
	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * Sets the list of filtering methods
	 * @param methods The list of filtering methods
	 */
	public void setMethods(List<String> methods) {
		this.methods = methods;
	}
	
	/**
	 * Sets the file containing the sequences for the data,
	 * as well as the column defining where the gene name
	 * is found in the header of each sequence
	 * @param fasta The name of the multiple fasta file containing 
	 * sequences corresponding to the data
	 * @param column The 'column' of the fasta header which keeps
	 * the gene name; columns are separated by '|'
	 */
	public void setFasta(String fasta, int column) {
		this.fasta  = new File(fasta);
		this.column = column;
	}
	
	/**
	 * Sets the file in which the filtered data will be written
	 * @param outputFile The file which will hold the filtered data
	 */
	public void setOutput(File outputFile) {
		this.output = outputFile;
	}
	
	/**
	 * Sets status if data is loaded for the first time
	 * @param first TRUE if data is loaded for the first time
	 */
	public void setFirst(boolean first) {
		this.first = first;
	}
	
	/**
	 * Sets the normalization object needed for evaluation
	 * @param norm The normalization object needed for evaluation
	 */
	public void setNorm(Normalization norm) {
		this.norm = norm;
		normal    = true;
	}

	/**
	 * Defines whether single or combined filtering should be performed
	 * @param combined TRUE if all filtering steps should be performed together
	 */
	public void setCombined(boolean combined) {
		this.combined = combined;
	}
	
	/**
	 * Sets the name of the file in which the quality scores
	 * will be written
	 * @param pqs The name of the file holding the quality scores
	 */
	public void setPQSName(String pqs) {
		this.pqsName = pqs;
	}
	
	/**
	 * Returns the complete Data object
	 * @return The complete Data object 
	 */
	public Data getData() {
		return data;
	}
	
	/**
	 * Returns the name of the column with spotIDs
	 * @return The name of the column with spotIDs
	 */
	public String getSpotid() {
		return spotid;
	}
	
	/**
	 * Returns status of attributes in original file: present calls?
	 * @return TRUE if attributes in original file are present calls
	 */
	public boolean isCalls() {
		return calls;
	}
	
	/**
	 * Returns scale of data (log or linear)
	 * @return TRUE if data is in log scale
	 */
	public boolean isLog() {
		return log;
	}
	
	/**
	 * Returns the attributes from the datafile
	 * @return Attributes from the datafile
	 */
	public List<String> getOriginalAtts() {
		return originalAtts;
	}
	
	/**
	 * Returns the name of the column with gene names
	 * @return The name of the column with gene names
	 */
	public String getGenename() {
		return genename;
	}
	
	/**
	 * Returns the original name of the gene attribute
	 * @return originalGene The original name of the gene attribute
	 */
	public String getOriginalGene() {
		return originalGene;
	}
	
	/**
	 * Returns the file containing the sequences for the data
	 * @return The multiple fasta file for the data
	 */
	public File getFasta() {
		return fasta;
	}
	
	/**
	 * Returns the column of the fasta header that
	 * keeps the gene name
	 * @return The column of the gene name in the fasta header
	 */
	public int getColumn() {
		return column;
	}
	
	/**
	 * Returns the size of the data after the performed filtering steps 
	 * @return The number of experiments contained in the data
	 */
	public int getSize() {
		return data.getSize();
	}
	
	/**
	 * Returns the list of filtering methods
	 * @return The list of filtering methods
	 */
	public List<String> getMethods() {
		return methods;
	}

	/**
	 * Returns the descriptors for the attribute list
	 * @return The descriptors for the attribute list
	 */
	public String[] getAttrDescr() {
		return data.getAttrDescr();
	}
	
	/**
	 * Returns the list of attributes for the data
	 * @return The list of attributes
	 */
	public ArrayList<Mapping<String, String>> getAttributesList() {
		return data.getAttributes();
	}

	/**
	 * Returns the average number of uracils
	 * @return The average number of uracils
	 */
	public double getAvgU() {
		return data.getAvgUr();
	}
	
	/**
	 * Returns the average log(e'/n')
	 * @return The average log(e'/n') 
	 */
	public double getAvgL() {
		return data.getAvgLo();
	}
	
	/**
	 * Returns the name of the file containing the plotting information for evaluation
	 * @return The name of the plotting file for evaluation
	 */
	public String getPlotName() {
		return data.getPlotName();
	}
	
}
