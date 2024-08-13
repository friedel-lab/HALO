/*
 * This file is part of HALO version 1.3. 
 */
package halo.halflife;
import halo.data.Data;
import halo.normalization.CorrectionFactors;

/**
 * Implementation of HalfLife providing methods for the usual calculation of  half-life values for pre-existing RNA,
 * based on the ratio of pre-existing to total RNA
 * @author Stefanie Kaufmann
 *
 */
public class HalfLife_Pre extends HalfLife{
	
	
	@Override
	public void initialize(Data data) {
		this.name      = "Pre-existing/Total RNA";
		this.data      = data;
		this.corr      = new CorrectionFactors();
		this.replicate = UNDEF;
		this.ratio     = data.calculateRatio(data.getPreRNA(), data.getTotalRNA(), data.getCorrPreTot());
	}
	
	@Override
	public void initialize(Data data, int replicate) {
		this.name      = "Pre-existing/Total RNA";
		this.data      = data;
		this.corr      = new CorrectionFactors();
		this.replicate = replicate-1;
		this.ratio     = data.calculateRatio(data.getPreRNA(), data.getTotalRNA(), data.getCorrPreTot());
	}
	
	@Override
	public void calculateHalfLives(double t) {
		System.out.println("Starting half-life calculation...");
		if(corr.getC_u() == 0.0) {
			System.err.println("The correction factor c_u is undefined; \n" +
					"please check whether you used linear regression before half-life calculation");
		}
		double alpha_t    = alpha.alpha(t);
		halflives               = new double[ratio.length];
		double[] newRatio = new double[ratio.length];

		//if a specific replicate is given use the ratio of this replicate, else use the previously calculated ratio
		if(replicate == UNDEF) {
			newRatio = ratio;
		} else {
			newRatio = ratioOfReplicate(data.getPreRNA(), data.getTotalRNA());
		}

		for(int i=0; i<halflives.length; i++) {

			//calculate the half-life based on ratio of pre-existing RNA to total RNA, time t and correction factor c_u
			double r2 = newRatio[i]*corr.getC_u();
			double h2 = -t * Math.log(2)/Math.log(r2*alpha_t);
			if(h2 < 0 || h2 > maximumHalfLife) {
				h2 = maximumHalfLife;
			} else if(Double.isNaN(h2) || h2 < minimumHalfLife) {
				h2 = minimumHalfLife;
			}
			halflives[i] = h2;
			if(h2 > max) {
				max = h2;
			}
		}
		System.out.println("Done calculating half-lives.");
		System.out.println("------------------------------");
	}
	
	@Override
	public CorrectionFactors calculateCorrectionFactors(double medianHWZ, double t) {
		double alpha_t           = alpha.alpha(t);
		double pow               = (double) Math.pow(2, (double) (-t/medianHWZ));
		double pow_alpha         = (double) Math.pow(alpha_t, -1);
		
		//calculate the median of the ratio
		double medPT             = median(ratio);
		//calculate correction factor c_u based on this median and the given median half-life
		double c_u               = (double) (pow*pow_alpha)/medPT;
		corr.setC_u(c_u);
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
//		double total  = data.getPreRNA()[0].length;
//		for(int i=0; i<data.getPreRNA()[0].length; i++) {
//			replicate = i;
//			double med = median(ratioOfReplicate(data.getPreRNA(), data.getTotalRNA()));
//			medSum += med;
//		}
//		replicate = rep;
//		
//		double medNT             = medSum/total;
//		return medNT;
//	}
	
}
