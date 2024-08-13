/*
 * This file is part of HALO version 1.3. 
 */
package halo.halflife;

import halo.data.Mapping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;


/**
 * Class for exporting half-life values
 * @author Stefanie Kaufmann
 *
 */
public class HalfLifeWriter {
	
	public static final int HALFLIFE = 1;
	public static final int RATIO    = 2;
	public static final int BOTH     = 3;
	
	/**
	 * Writes the half-life values of a number of different HalfLife objects into a file,
	 * thereby giving the choice to eliminate lines that are not saturated
	 * @param output Name of output file
	 * @param header Header line of the output file
	 * @param which Defines if half-life, ratio or both values should be written into output file; should be HALFLIFE, RATIO or BOTH
	 * @param hl A set of HalfLife objects
	 */
	public HalfLifeWriter(String output, String header, int which, HalfLife...hl) {
		try {
			if(which != HALFLIFE && which != RATIO && which != BOTH) {
				System.err.println("Please define which values you want: '"+HALFLIFE+"for half-lives,\n" +
						"'"+RATIO+"' for ratios or '"+BOTH+"' for both. Proceeding with Default: Half-lives.");
				which = HALFLIFE;
			}
			System.out.println("Writing results into file...");
			BufferedWriter buff          = new BufferedWriter(new FileWriter(new File(output)));
			Mapping<String, Integer> map = hl[0].getData().getMap();
			/*
			 * multidimensional array which will contain all the spotIds that were originally present
			 * as well as the corresponding half-life values of the different HalfLife objects
			 * or NaN if no corresponding value is present
			 */
			double[][] values            = new double[hl[0].getData().getNumberExperiments()][hl.length];
			double[][] ratios            = new double[hl[0].getData().getNumberExperiments()][hl.length];
			
			
			//iterate through the HalfLife objects
			for(int j=0; j<hl.length; j++) {
				Iterator<String> iter           = map.iterate();
				Mapping<String, Integer> newMap = hl[j].getNewMapping();
				
				//iterate through the map that maps spotID to index of original data (i.e. array values here)
				while(iter.hasNext()) {
					String spotId = iter.next();
					int index     = map.getAttribute(spotId);
					
					//check whether current spotID is present in the map that maps spotID to half-life values of the current object
					if(newMap.containsSpot(spotId)) {
						int newIndex     = newMap.getAttribute(spotId);
						values[index][j] = hl[j].getHwz()[newIndex];
						ratios[index][j] = hl[j].getRatio()[newIndex];
					} else {
						//else insert NaN
						values[index][j] = Double.NaN;
						ratios[index][j] = Double.NaN;
					}
				}
			}
			
			buff.write(header+"\n");
			Iterator<String> iter = map.iterate();
			
			/*
			 * print the contents of the values array into the file
			 * to do this iterate through the spotIDs present in the map (spotID --> index of values)
			 */
			while(iter.hasNext()) {
				String spotId     = iter.next();
				int index         = map.getAttribute(spotId);
				StringBuffer half = new StringBuffer("");
				StringBuffer rati = new StringBuffer("");
				
				//concatenate the different values for each line, check whether NaN is contained
				for(int j=0; j<values[index].length; j++) {
					half.append(values[index][j]+"\t");
					rati.append(ratios[index][j]+"\t");
				}
				
				if(which == HALFLIFE) {
					buff.write(spotId+"\t"+half+"\n");
				} else if(which == RATIO) {
					buff.write(spotId+"\t"+rati+"\n");
				} else if(which == BOTH) {
					buff.write(spotId+"\t"+half+"\t"+rati+"\n");
				}
			}
			buff.flush();
			buff.close();
			System.out.println("Done writing results.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
