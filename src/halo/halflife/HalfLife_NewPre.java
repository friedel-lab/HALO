/*
 * This file is part of HALO version 1.3. 
 */
package halo.halflife;

import halo.data.Data;
import halo.normalization.CorrectionFactors;

/**
 * Class that provides half-life calculation methods based on the ratio of newly transcribed (labeled) RNA 
 * to pre-existing (unlabeled)RNA
 * @author Stefanie Kaufmann
 *
 */
public class HalfLife_NewPre extends HalfLife {

	@Override
	public void initialize(Data data) {
		this.name      = "Newly transcribed/Pre-existing RNA";
		this.data      = data;
		this.replicate = UNDEF;
		this.corr      = new CorrectionFactors();
		this.ratio     = data.calculateRatio(data.getNewRNA(), data.getPreRNA(), data.getCorrNewPre());
	}

	@Override
	public void initialize(Data data, int replicate) {
		this.name      = "Newly transcribed/Pre-existing RNA";
		this.data      = data;
		this.replicate = replicate-1;
		this.corr      = new CorrectionFactors();
		this.ratio     = data.calculateRatio(data.getNewRNA(), data.getPreRNA(), data.getCorrNewPre());
	}
	
	@Override
	public void calculateHalfLives(double t) {
		System.out.println("Starting half-life calculation...");
		if(corr.getC_l() == 0.0) {
			System.err.println("The correction factor c_l is undefined; \n" +
					"please check whether you used linear regression before half life calculation");
		}
		if(corr.getC_u() == 0.0) {
			System.err.println("The correction factor c_u is undefined; \n" +
					"please check whether you used linear regression before half life calculation");
		}
		double alpha_t    = alpha.alpha(t);
		halflives               = new double[ratio.length];
		double[] newRatio = new double[ratio.length];
		corr.setC_lu(corr.getC_l()/corr.getC_u());

		//if a specific replicate is given use the ratio of this replicate, else use the previously calculated ratio
		if(replicate == UNDEF) {
			newRatio = ratio;
		} else {
			newRatio = ratioOfReplicate(data.getNewRNA(), data.getPreRNA());
		}
		for(int i=0; i<halflives.length; i++) {

			//calculate the half life based on ratio of newly transcribed RNA to preexisting RNA, time t and correction factor c_lu
			double r1 = newRatio[i]*corr.getC_lu();
			double h1 = t * Math.log(2)/Math.log((r1+1)/alpha_t);
			if(h1 <0 || h1 > maximumHalfLife) {
				h1 = maximumHalfLife;
			} else if(Double.isNaN(h1) || h1 < minimumHalfLife) {
				h1 = minimumHalfLife;
			}
			halflives[i] = h1;
			if(h1>max) {
				max = h1;
			}
		}
		System.out.println("Done calculating half-lives.");
		System.out.println("------------------------------");
	}
	
	@Override
	public CorrectionFactors calculateCorrectionFactors(double medianHWZ, double t) {
		double alpha_t           = alpha.alpha(t);
		
		//calculate the median of the ratio
		double medNP             = median(ratio);
		double pow               = Math.pow(2, t/medianHWZ);
		//calculate correction factor c_lu based on this median and the given median half life
		double c_lu              = (double) (alpha_t*pow-1)/medNP;		
		corr.setC_lu(c_lu);
		return corr;
	}


	@Override
	public void setCorrectionFactor(CorrectionFactors corr) {
		this.corr = corr;
	}

//	@Override
//	public double calculateMedianOverReplicates() {
//		int rep = replicate;
//		double medSum = 0;
//		double total  = data.getNewRNA()[0].length;
//		for(int i=0; i<data.getNewRNA()[0].length; i++) {
//			replicate = i;
//			double med = median(ratioOfReplicate(data.getNewRNA(), data.getPreRNA()));
//			medSum += med;
//		}
//		replicate = rep;
//		
//		double medNT             = medSum/total;
//		return medNT;
//	}

}
