/*
 * This file is part of HALO version 1.3.
 */
package halo.normalization;

import halo.data.Data;
import halo.userinterface.gui.graphhandler.HistogramConstructor;

/**
 * Provides methods for normalization and retrieving of correction factors
 * @author Stefanie Kaufmann
 *
 */
public abstract class Normalization {

	protected CorrectionFactors corr;
	protected double   alpha;
	protected double   beta;
	protected int      replicate = UNDEF;
	private   double   pqsMax;
	public static final int UNDEF = -1;
	
	/**
	 * calculates correction factors via normalization methods
	 * @return Correction factors c_u, c_l and c_lu
	 */
	public abstract CorrectionFactors calculateCorrectionFactors();

	/**
	 * Calculates the correction factors for a certain replicate
	 * @param replicate The replicate for which the correction factors will be calculated
	 * @return The Correction factors for this replicate
	 */
	public CorrectionFactors calculateCorrectionFactors(int replicate) {
		this.replicate = replicate - 1;
		corr = calculateCorrectionFactors();
		this.replicate = UNDEF;
		return corr;
	}
	
	/**
	 * Calculates the array containing the quality control values; these are calculated as distance from the
	 * normalization line
	 * @param data The data object containing the RNA values
	 * @param histogram TRUE if a histogram should be created for the quality scores, FALSE otherwise
	 * @return probe-set quality control values
	 */
	public double[] calculateQualityControl(Data data, boolean histogram) {
		double[] et;
		double[] ut;
		double max = 0;
		et = data.calculateRatio(data.getNewRNA(), data.getTotalRNA(), data.getCorrNewTot(), replicate);
		ut = data.calculateRatio(data.getPreRNA(), data.getTotalRNA(), data.getCorrPreTot(), replicate);
		double[] pqs = new double[et.length];
		
		//calculate the deviation from the linear regression line
		for(int i=0; i<pqs.length; i++) {
			pqs[i] = Math.abs(1-(et[i]*corr.getC_l()+ut[i]*corr.getC_u()));
			if(pqs[i] > max) {
				max = pqs[i];
			}
		}
		if(histogram) {
			generateHistogram(pqs, max);
		}
		pqsMax = max;
		return pqs;
	}
	
	/**
	 * Uses the PQS data and generates a histogram from it
	 * @param pqs The probe-set quality control values
	 * @param max The maximum value among these (for definition of the x-range)
	 */
	public void generateHistogram(double[] pqs, double max) {
		HistogramConstructor hist = new HistogramConstructor("Histogram of probe set quality scores", 
				"Score", "relative frequency");
		hist.addSeries("pqs scores", pqs, 30, 0, max+0.1);
		hist.generateGraph();
	}
	
	
	/**
	 * Sets the correction factors
	 * @param corr The correction factors
	 */
	public void setCorr(CorrectionFactors corr) {
		this.corr = corr;
	}
	
	/**
	 * Sets the replicate for calculation
	 * @param replicate The replicate for calculation
	 */
	public void setReplicate(int replicate) {
		if(replicate != UNDEF) {
			replicate--;
		}
		this.replicate = replicate;
	}
	
	/**
	 * Returns the factor alpha from normalization
	 * @return The factor alpha
	 */
	public double getAlpha() {
		return alpha;
	}
	
	/** 
	 * Returns the factor beta from normalization
	 * @return The factor beta
	 */
	public double getBeta() {
		return beta;
	}
	
	/**
	 * Returns the correction factors
	 * @return The correction factors
	 */
	public CorrectionFactors getCorr() {
		return corr;
	}
	
	/**
	 * Returns the highest value of the probe set quality scores
	 * @return The highest value of the probe set quality scores
	 */
	public double getPqsMax() {
		return pqsMax;
	}
	
	/**
	 * Returns the replicate for calculation
	 * @return The replicate for calculation
	 */
	public int getReplicate() {
		return replicate+1;
	}
	
}
