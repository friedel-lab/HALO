/*
 * This file is part of HALO version 1.3. 
 */
package halo.halflife;
import halo.data.Data;
import halo.normalization.CorrectionFactors;

/**
 * Implementation of HalfLife providing methods for the usual calculation of half-life values for newly transcribed RNA,
 * based on the ratio of newly transcribed to total RNA
 * @author Stefanie Kaufmann
 *
 */
public class HalfLife_New extends HalfLife{
	
	
	@Override
	public void initialize(Data data) {
		this.name      = "Newly transcribed/Total RNA";
		this.data      = data;
		this.replicate = UNDEF;
		this.corr      = new CorrectionFactors();
		this.ratio     = data.calculateRatio(data.getNewRNA(), data.getTotalRNA(), data.getCorrNewTot());
	}
	
	@Override
	public void initialize(Data data, int replicate) {
		this.name      = "Newly transcribed/Total RNA";
		this.data      = data;
		this.replicate = replicate-1;
		this.corr      = new CorrectionFactors();
		this.ratio     = data.calculateRatio(data.getNewRNA(), data.getTotalRNA(), data.getCorrNewTot());
	}
	
	@Override
	public void calculateHalfLives(double t) {
		System.out.println("Starting half-life calculation...");
		if(corr.getC_l() == 0.0) {
			System.err.println("The correction factor c_l is undefined;\n" +
					"please check whether you used linear regression before half-life calculation.");
		}
		double alpha_t    = alpha.alpha(t);
		halflives         = new double[ratio.length];
		double[] newRatio = new double[ratio.length];

		//if a specific replicate is given use the ratio of this replicate, else use the previously calculated ratio
		if(replicate == UNDEF) {
			newRatio = ratio;
		} else {
			newRatio = ratioOfReplicate(data.getNewRNA(), data.getTotalRNA());
		}
		for(int i=0; i<halflives.length; i++) {

			//calculate the half-life based on ratio of newly transcribed RNA to total RNA, time t and correction factor c_l
			double r1 = newRatio[i]*corr.getC_l();
			double h1 = -t * Math.log(2)/Math.log((1-r1)*alpha_t);
			if(h1 < 0 || h1 > maximumHalfLife) {
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
		double pow               = (double) Math.pow(2, (double) (-t/medianHWZ));
		double pow_alpha         = (double) Math.pow(alpha_t, -1);
		
		//calculate the median of the ratio
		double medNT             = median(ratio);
//		double medNT             = calculateMedianOverReplicates();
		//calculate correction factor c_l based on this median and the given median half-life
		double c_l               = (double) (1.0- pow*pow_alpha)/medNT;		
		corr.setC_l(c_l);
		return corr;
	}
	
	@Override
	public void setCorrectionFactor(CorrectionFactors corr) {
		this.corr = corr;
	}

}
