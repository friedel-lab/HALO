/*
 * This file is part of the HALO 1.3 graphical user interface
 */
package halo.userinterface.gui;

import halo.data.Data;
import halo.halflife.HalfLife;
import halo.halflife.HalfLifeWriter;
import halo.halflife.HalfLife_New;
import halo.halflife.HalfLife_NewPre;
import halo.halflife.HalfLife_Pre;
import halo.halflife.alpha.Alpha;
import halo.normalization.CorrectionFactors;
import halo.tools.Tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * 'Interface' which connects GUI and methods for half-life calculations
 * @author Stefanie Kaufmann
 *
 */
public class GuiHalfLife {

	private List<String>   methods   = new ArrayList<String>();
	private List<Double>  times      = new ArrayList<Double>();
	private List<Integer> replicates = new ArrayList<Integer>();
	private List<HalfLife> lives     = new ArrayList<HalfLife>();
	private String[]   header;
	private CorrectionFactors corr;
	private Data    data;
	private Alpha   alpha;
	private String  output;
	private int     ratioMethod = Data.RATIOFIRST;
	private int     which;
	private double  mD;
	public static final String NEW = "new";
	public static final String PRE = "pre";
	public static final String EU  = "new/pre";
	
	
	/**
	 * Use set variables and create HalfLife objects for every method, calculate half-lives
	 */
	public void calcHL() {
		generateHL();
		
		int index             = 0;
		for(HalfLife hl : lives) {
			hl.setCorrectionFactor(corr);
			if(alpha != null) {
				hl.setAlpha(alpha);
			}
			//perform half-life calculation
			hl.calculateHalfLives(times.get(index));
			
			index++;
		}
	}
	
	/**
	 * Generate empty HalfLife objects according to the given methods, without calculating
	 * the half-lives
	 */
	public void generateHL() {
		data.setMethod(ratioMethod);
		lives                 = new ArrayList<HalfLife>();
		Iterator<String> iter = methods.iterator();
		int index             = 0;
		header                = new String[methods.size()];
		while(iter.hasNext()) {
			String name = iter.next();
			HalfLife hl = null;
			String text = "";
			if(name.equals(NEW)) {
				hl = new HalfLife_New();
				text = "nt/t RNA";
			} else if(name.equals(PRE)) {
				hl = new HalfLife_Pre();
				text = "p/t RNA";
			} else if(name.equals(EU)) {
				hl = new HalfLife_NewPre();
				text = "nt/p RNA";
		    } 
			//form the header for the output file from the names of the methods
			header[index] = text;
			int replicate = replicates.get(index);
			//initialize half-life calculation
			if(replicate != Gui.UNDEF) {
				hl.initialize(data, replicate);
			} else {
				hl.initialize(data);
			}
			lives.add(hl);
			index++;
		}
	}
	
	/**
	 * Calculate the median for a set of half-lives
	 */
	public double calculateMedian() {
		double median    = Tools.calculateMedianForHalfLives(times.get(0), data);
		mD = median;
		return mD;
	}
	
	/**
	 * Calculates the median for each HalfLife object and the
	 * correction factors based on this median
	 * @param mD The median value
	 * @return A list of correction factors based on median, 
	 * corresponding to the list of half-lives
	 */
	public List<CorrectionFactors> getCFonMedian(double mD) {
		List<CorrectionFactors> corr = new ArrayList<CorrectionFactors>();
		int index                = 0;
		
		for(HalfLife hl : lives) {
			double time = times.get(index);
			CorrectionFactors c = hl.calculateCorrectionFactors(mD, time);
			corr.add(c);
			System.out.println("Correction factors for HalfLife object "+methods.get(index)+" ; c_l: "+c.getC_l()+", c_u: "+c.getC_u());
			index++;
		}
		return corr;
	}
	
	/**
	 * Calculates the half-life values based on a list of correction factors
	 * which correspond to the list of half-lives
	 * @param corr List containing a set of correction factors for every half-life object
	 */
	public void calcHLonMedian(List<CorrectionFactors> corr) {
		int index = 0;
		for(HalfLife h : lives) {
			h.setCorrectionFactor(corr.get(index));
			double t = times.get(index);
			index++;
			h.calculateHalfLives(t);
		}
	}
	
	/**
	 * Based on calculated half-lives, writes results into output file
	 */
	public void saveHL() {
		HalfLife[] hls = new HalfLife[methods.size()];
		lives.toArray(hls);
		StringBuffer header1 = new StringBuffer();
		StringBuffer header2 = new StringBuffer();
		for(String item : header) {
			header1.append("half-life ("+item+")\t");
			header2.append(item+" ratio\t");
		}
		
		if(output != null) {
			String headerComp = "";
			//if ratios and half-lives are wanted extend header
			if(which == HalfLifeWriter.BOTH) {
				headerComp = header1.toString()+header2.toString();
			} else if(which == HalfLifeWriter.HALFLIFE) {
				headerComp = header1.toString();
			} else {
				headerComp = header2.toString();
			}
			String head = "spotid\t"+headerComp.toString().substring(0, headerComp.length()-1);
			//print all calculated half-lives to file
			new HalfLifeWriter(output, head, which, hls);
		}
	}
	
	public void saveHLWithAttributes(int index, String file, ArrayList<String> labels) {
		HalfLife hl = lives.get(index);
		hl.printHalfLivesWithAttributes(file, labels);
	}
	
	/**
	 * Resets everything that has been used for half-life calculation of previous half-life list
	 */
	public void clearHL() {
		this.methods = new ArrayList<String>();
		this.times   = new ArrayList<Double>();
		this.lives   = new ArrayList<HalfLife>();
	}
	
	/**
	 * Checks whether there are any half-lives 
	 * @return True if already any half-lives have been set
	 */
	public boolean hasHL() {
		return (lives.size()>0);
	}
	
	/**
	 * Add a half-life object to the current list, also add corresponding time
	 * @param hlName Name of the half-life object (new/pre/eu)
	 * @param time Time point for which calculation is wanted
	 * @param replicate Replicate for which the calculation is wanted
	 */
	public void addHL(String hlName, double time, int replicate) {
		methods.add(hlName);
		times.add(time);
		replicates.add(replicate);
	}
	
	/**
	 * Changes the method used for ratio calculation
	 * @param method The method to be used for ratio calculation
	 */
	public void setRatioMethod(int method) {
		if(method != Data.AVERAGEFIRST && method != Data.RATIOFIRST) {
			System.err.println("Chosen method for ratio calculation does not exist!\n" +
					"Please use '"+Data.AVERAGEFIRST+"' for average calculation before ratios\n" +
							"and '"+Data.RATIOFIRST+"' for ratio calculation before averages.\n");
		} else {
			ratioMethod = method;
			data.setMethod(method);
		}
	}
	
	/**
	 * Sets the alpha object
	 * @param alpha Alpha object with specific alpha calculation function
	 */
	public void setAlpha(Alpha alpha) {
		for(HalfLife hl : lives) {
			hl.setAlpha(alpha);
		}
		this.alpha = alpha;
	}
	
	/**
	 * Sets the maximal value for half-life values
	 * @param max Maximum value for half-lives
	 */
	public void setMax(double max) {
		HalfLife.setMaximumHalfLife(max);
	}
	
	/**
	 * Sets the minimum value for half-life values
	 * @param min Minimum value for half-lives
	 */
	public void setMin(double min) {
		HalfLife.setMinimumHalfLife(min);
	}
	
	/**
	 * Sets all necessary variables for defining the output
	 * @param filename Name of the file in which output will be saved
	 * @param which Which results will be saved (ratio/halflife/both?)
	 */
	public void setOutput(String filename, int which) {
		this.output = filename;
		this.which  = which;
	}
	
	/**
	 * Sets the data on which the half-life calculation
	 * is based
	 * @param data The data object for the calculation
	 */
	public void setData(Data data) {
		this.data = data;
	}
	
	/**
	 * Sets the numbers of the replicates for the half-life calculations
	 * @param replicates The list of replicate numbers
	 */
	public void setReplicates(List<Integer> replicates) {
		this.replicates = replicates;
	}
	
	/**
	 * Sets the correction factors needed for half-life
	 * calculation
	 * @param corr The correction factors for the calculation
 	 */
	public void setCorr(CorrectionFactors corr) {
		this.corr = corr;
	}
	
	/**
	 * Sets the list of method descriptions for calculation
	 * @param methods The list of methods for half-life calculation
	 */
	public void setMethods(List<String> methods) {
		this.methods = methods;
	}
	
	/**
	 * Sets the list of time points for calculation,
	 * order corresponding to the list of methods
	 * @param times A list of time points for half- life calculation
	 */
	public void setTimes(List<Double> times) {
		this.times = times;
	}
	
	/**
	 * Returns the alpha function of the first half-life object, based on the 
	 * assumption that all objects use the same alpha
	 * @return The alpha function used for half-life calculation
	 */
	public Alpha getAlpha() {
		return lives.get(0).getAlpha();
	}
	
	/**
	 * Returns the list of method descriptions
	 * used for half-life calculation
	 * @return The list of methods for half-life calculation
	 */
	public List<String> getMethods() {
		return methods;
	}

	/**
	 * Returns the number of the used replicate for each half-life object
	 * @return The numbers of the used replicates
	 */
	public List<Integer> getReplicates() {
		return replicates;
	}
	
	/**
	 * Returns the last calculated median
	 * @return The last calculated median
	 */
	public double getMD() {
		return mD;
	}
	
	/**
	 * Returns the list of time points used for calculation
	 * @return The list of time points for half-life calculation
	 */
	public List<Double> getTimes() {
		return times;
	}
	
	/**
	 * Returns the list of half-life objects calculated
	 * @return The list of half-life objects
	 */
	public List<HalfLife> getHL() {
		return lives;
	}
}
