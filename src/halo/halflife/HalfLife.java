/*
 * This file is part of HALO version 1.3. 
 */
package halo.halflife;

import halo.data.Data;
import halo.data.Mapping;
import halo.halflife.alpha.Alpha;
import halo.halflife.alpha.AlphaConstant;
import halo.normalization.CorrectionFactors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * Calculation of half-lives and median-based Correction Factors
 * @author Stefanie Kaufmann
 *
 */
public abstract class HalfLife {

	public    static final String NEWLY  = "new";
	public    static final String PRE    = "pre";
	public    static final String NEWPRE = "new/pre";
	protected static final int UNDEF     = -1;
	protected static double minimumHalfLife = 5;
	protected static double maximumHalfLife = 10000;
//	protected static double    maximum   = 0.999999;
	protected double[] halflives; 
	protected double[] ratio;
	protected int      replicate;
	protected double   max = 0;
	protected Data     data;
	protected Alpha    alpha;
	protected CorrectionFactors        corr;
	protected Mapping<String, Integer> newMap;
	protected String   name;
	
	/**
	 * Constructs a HalfLife object with default Alpha
	 */
	public HalfLife() {
		this.alpha = new AlphaConstant();
	}
	
	/**
	 * initializes the HalfLife object &
	 * extracts the needed values of the Data object
	 * @param data The data object for which the half-life calculation shall be done
	 */
	public abstract void initialize(Data data);
	
	/**
	 * initializes the HalfLife object for a specific replicate of the data
	 * @param data The data object for which the half-life calculation shall be done
	 * @param replicate The replicate that shall be used
	 */
	public abstract void initialize(Data data, int replicate);
	
	/**
	 * Calculation of half-lives
	 * @param t Time
	 */
	public abstract void calculateHalfLives(double t);
	
	//TODO: median-berechnung bei calculatecorrectionfactors anpassen
	
	/**
	 * Calculation of correction factors based on a median
	 * @param medianHWZ median mRNA half-life
	 * @param t Time
	 * @return CorrectionFactors c_u and c_l
	 */
	public abstract CorrectionFactors calculateCorrectionFactors(double medianHWZ, double t);
	
	/**
	 * Calculates the median for each replicate separately, and an average over all 
	 * replicates afterwards
	 * @return The average over the medians of all normalized replicates
	 */
//	public abstract double calculateMedianOverReplicates();
	
	
	/**
	 * Calculation of the median of a single dimensional array
	 * @param inp The array for which the median shall be calculated
	 * @return Median value
	 */
	public static double median(double[] inp)  {
		double[] temp = new double[inp.length];
		System.arraycopy(inp, 0, temp, 0, inp.length);
		Arrays.sort(temp);
		
		int n = temp.length;
		double median;
		
		if(n==1) {
			median = temp[0];
		}
		else if(n % 2 == 1) {
			median = temp[n/2];
		}
		else {
			median = (temp[n/2]+temp[n/2+1])/2;
		}
		
		return median;
	}
	
//	/**
//	 * filters out half-life values that are below 0 or NaN
//	 */
//	public void filterValues() {
//		getNewMapping();
//		for(int i=0; i<halflives.length; i++) {
//				if(halflives[i]<0 || Double.isNaN(halflives[i])) {
//					if(newMap.containsAttribute(i)) {
//						newMap.removeAllSpots(newMap.getSpotId(i));
//					}
//			}
//		}
//	}
	
	/**
	 * Calculates the ratio of a specific replicate
	 * @param rna Set of RNA values serving as numerator
	 * @param total Set of RNA values serving as denominator
	 * @return The ratio of the two sets of RNA restricted to a single replicate
	 */
	public double[] ratioOfReplicate(double[][] rna, double[][] total) {
		
		double[] result = new double[data.getSize()];
		int i           = 0;
		
		if(replicate == UNDEF) {
			result = null;
		} else {
			Iterator<String> iterator = data.getMap().iterate();
			while(iterator.hasNext()) {
				String id = iterator.next();
				int index = data.getMap().getAttribute(id);
				
				double rna1 = rna[index][replicate];
				double tot  = total[index][replicate];
				result[i]   = rna1/tot;
				i++;
			}
		}
		return result;
	}
	
	/**
	 * Writes the half-life values contained in the object together with corresponding spotIds into a file
	 * @param output Name of output file
	 */
	public void writeToFileHalfLives(String output) {
		try {
			BufferedWriter    outp     = new BufferedWriter(new FileWriter(new File(output)));
			Iterator<String> iterator = getNewMapping().iterate();
			
			//gets all the values that are contained in the map (i.e. not filtered out) and writes them into the file
			while(iterator.hasNext()) {
				String id = iterator.next();
				int index  = newMap.getAttribute(id);
				outp.write(id+"\t");
				outp.write(halflives[index]+"\n");
			}
		outp.flush();
		outp.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes Ratio array into a file with two columns: column 1: spotID, column 2: corresponding ratio
	 * @param output The name of the file for the output
	 */
	public void writeToFileRatios(String output) {
		try {
			BufferedWriter    outp     = new BufferedWriter(new FileWriter(new File(output)));
			Iterator<String> iterator = getNewMapping().iterate();
			
			//gets all the values that are contained in the map (i.e. not filtered out) and writes them into the file
			while(iterator.hasNext()) {
				String id  = iterator.next();
				int index  = newMap.getAttribute(id);
				outp.write(id+"\t");
				
				outp.write(ratio[index]+"\n");
			}
		outp.flush();
		outp.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	 /**
	  * Prints half-life values together with a given list of attributes into an output file
	  * @param file The name of the output file
	  * @param labels The labels of attributes
	  */
	 public void printHalfLivesWithAttributes(String file, ArrayList<String> labels) {
	        try {
	            String[] descr = data.getAttrDescr();
	            ArrayList<Integer> indices = new ArrayList<Integer>();
	            Mapping<String, Integer> map             = data.calculateNewMapping();
	            List<Mapping<String, String>> attributes = data.getAttributes();
	            StringBuffer header = new StringBuffer("");
	            header.append("probeset_id\t");

	            //checking which of the attributes mappings contains the gene name
	            for(int i = 0; i < descr.length; i++) {
	                if(labels.contains(descr[i])) {
	                    indices.add(i);
	                    header.append(descr[i]+"\t");
	                }
	            }
	            header.append(name+"\n");
	            if(indices.size() != 0) {
	            	 //getting the mapping that maps spotid to genename

		            ArrayList<String> spots;
		            BufferedWriter wr = new BufferedWriter(new FileWriter(file));
		            wr.write(header.toString());
		            
		            for(int i = 0; i < halflives.length; i++) {
		            	spots = map.getSpotId(i);

		            	for(String spot : spots) {
		            		wr.write(spot + "\t");
		            		for(int k=0; k<indices.size(); k++) {
		            			wr.write(attributes.get(k).getAttribute(spot)+"\t");
		            		}
		            		wr.write(halflives[i] + "\n");
		            	}
		            }
		            wr.close(); 	
	            } else {
	            	System.err.println("The attributes you want to print with the half-lives do \n" +
	            			"not exist. Printing of output file was aborted.");
	            }
	        }
	        catch(IOException ex) {
	            ex.printStackTrace();
	        }
	    }
	
	/**
	 * Sets the mapping corresponding to the ratio values
	 * @param newMap The mapping corresponding to the ratios
	 */
	public void setNewMap(Mapping<String, Integer> newMap) {
		this.newMap = newMap;
	}
	
	/**
	 * Sets the Alpha object defining the method for calculation of alpha(t)
	 * @param alpha The alpha object
	 */
	public void setAlpha(Alpha alpha) {
		this.alpha = alpha;
	}
	
	/**
	 * Sets the set of correction factors
	 * @param corr The correction factors
	 */
	public abstract void setCorrectionFactor(CorrectionFactors corr);
	
	/**
	 * Sets the half-life values
	 * @param hwz The half-life values
	 */
	public void setHwz(double[] hwz) {
		this.halflives = hwz;
	}
	
	/**
	 * Sets the correction factors c_u and c_l
	 * @param c_u The correction factor c_u
	 * @param c_l The correction factor c_l
	 */
	public void setCorrectionFactors(double c_u, double c_l) {
		corr.setC_u(c_u);
		corr.setC_l(c_l);
	}
	
	/**
	 * Sets the maximum half-life value to be used
	 * @param maximumHalfLife Maximum half-life
	 */
	public static void setMaximumHalfLife(double maximumHalfLife) {
		HalfLife.maximumHalfLife = maximumHalfLife;
	}
	
	/**
	 * Sets the minimal half-life value to be used
	 * @param minimumHalfLife Minimum half-life
	 */
	public static void setMinimumHalfLife(double minimumHalfLife) {
		HalfLife.minimumHalfLife = minimumHalfLife;
	}
	
	/**
	 * Returns the alpha object defining the calculation method of alpha
	 * @return The alpha object
	 */
	public Alpha getAlpha() {
		return alpha;
	}
	
	/**
	 * Returns the highest present half-life value
	 * @return Highest present half-life value
	 */
	public double getMax() {
		return max;
	}
	
	/**
	 * Returns the name of this method
	 * @return The name of this method
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the correction factor c_l
	 * @return The correction factor c_l
	 */
	public double getC_l() {
		return corr.getC_l();
	}
	
	/**
	 * Returns the correction factor c_u
	 * @return The correction factor c_u
	 */
	public double getC_u() {
		return corr.getC_u();
	}
	
	/**
	 * Returns the mapping corresponding to ratios/half-lives
	 * @return The mapping corresponding to ratios and half-life values
	 */
	public Mapping<String, Integer> getNewMapping() {
		//if the Map that maps spotIds to ratio/hwz-indices doesn't exist already, calculate it 
		if(newMap == null) {
			newMap = data.calculateNewMapping();
		} 
		return newMap;
	}
	
	/**
	 * Returns the data object holding the original data
	 * @return The data object
	 */
	public Data getData() {
		return data;
	}
	
	/**
	 * Returns the half-life values
	 * @return The half-life values
	 */
	public double[] getHwz() {
		return halflives;
	}
	
	/**
	 * Returns the ratio values
	 * @return The ratio values
	 */
	public double[] getRatio() {
		return ratio;
	}
	
	/**
	 * Returns the number of the used replicate
	 * @return The number of the replicate
	 */
	public int getReplicate() {
		return replicate;
	}
	
	/**
	 * Returns the set minimum half-life
	 * @return Minimum half-life
	 */
	public static double getMinimumHalfLife() {
		return minimumHalfLife;
	}
	
	/**
	 * Returns the set maximum half-life
	 * @return Maximum half-life
	 */
	public static double getMaximumHalfLife() {
		return maximumHalfLife;
	}
	
}
