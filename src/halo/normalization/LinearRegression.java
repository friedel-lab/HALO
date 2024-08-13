/*
 * This file is part of Halo version 1.3.
 */
package halo.normalization;

import halo.data.Data;
import halo.data.Mapping;

import java.util.Iterator;

/**
 * Implementation of normalization method=linear regression based on simple linear regression
 * Command line option: -l standard
 * @author Stefanie Kaufmann
 *
 */
public class LinearRegression extends Normalization{
	private Data data;

	/**
	 * Constructor for simple linear regression object
	 * @param data The data for which normalization will be performed
	 */
	public LinearRegression(Data data) {
		this.data = data;
	}
	
	@Override
	public CorrectionFactors calculateCorrectionFactors() {
		System.out.println("Starting linear regression...");
		double[] nt;
		double[] pt;
		
		
		nt = data.calculateRatio(data.getNewRNA(), data.getTotalRNA(), data.getCorrNewTot(), replicate);
		pt = data.calculateRatio(data.getPreRNA(), data.getTotalRNA(), data.getCorrPreTot(), replicate);
		
		
		Mapping<String, Integer> map = data.calculateNewMapping();
		double[] y                   = new double[map.mapSize()];
		double[] x                   = new double[y.length];
		
		double y_sum   = 0;
		double x_sum   = 0;
		double x_bar;
		double y_bar;
		double nenner  = 0;
		double zaehler = 0;
		int    count   = 0;
		       beta    = 0;
		       alpha   = 0;
		
		Iterator<String> iterator = map.iterate();
		
		//calculate the sums of all ratio RNA/total values (newly transcribed & pre-existing)
		while(iterator.hasNext()) {
			String id  = iterator.next();
			int index  = map.getAttribute(id);
			
			double x_i = nt[index]; 
			double y_i = pt[index];
			y[count]   = y_i;
			x[count]   = x_i;
			y_sum     += y_i;
			x_sum     += x_i;
			count++;
		}
		
		//calculate the factors x_bar and y_bar from the sum
		double n = map.mapSize();
		x_bar    = x_sum/n;
		y_bar    = y_sum/n;
		
		//calculate the denominator and numerator with simple linear regression that are needed for calculation of alpha & beta
		for(int j = 0; j < x.length; j++) {
			zaehler += (x[j]-x_bar) * (y[j]-y_bar);
			nenner  += (x[j]-x_bar)*(x[j]-x_bar);
		}
		
		//calculate alpha & beta as well as the correction factors for the half-life calculation
		beta  = zaehler/nenner;
		alpha = y_bar - beta*x_bar;
		double c_u = 1/alpha;
		double c_l = (-1)*beta*c_u;
		corr = new CorrectionFactors(c_l,c_u);
		System.out.println("Done with linear regression.");
		System.out.println("These are your correction factors: ");
		System.out.println("c_u: "+c_u);
		System.out.println("c_l: "+c_l);
		System.out.println("------------------------------");
		return corr;
	}
	
}
