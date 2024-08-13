/*
 * This file is part of HALO version 1.3. 
 */
package halo.data;

import halo.normalization.Normalization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Provides methods for filtering the values contained in a Data object
 * @author Stefanie Kaufmann
 *
 */
public class Filter {

	/**
	 * removes all experiments where at least one of the values is beneath a certain threshold
	 * Commandline option -f threshold='value'
	 * @param data Data object which shall be filtered
	 * @param threshold Cutoff-threshold
	 * @return Filtered Data object
	 */
	public static Data filter(Data data, double threshold) {
		System.out.println("Filtering data...");
		double newmax = data.getMaximum();
		double newmin = data.getMinimum();
		if(newmin < threshold) {
			newmin = threshold;
		}
		double[][] totalRNA = data.getTotalRNA();
		double[][] newRNA   = data.getNewRNA();
		double[][] preRNA   = data.getPreRNA();
		
		Mapping<String, Integer> map = data.copyMap();
		
		for(int i=0; i<totalRNA.length; i++) {
			
			/*
			 * check whether any value of total RNA for the current experiment i is below threshold
			 * and if so, remove attribute from the map
			 */
			for(int j=0; j<totalRNA[i].length; j++) {
				if(totalRNA[i][j]<threshold && map.containsAttribute(i)) {
					map.removeAttribute(i);
				} 
			}
			/*
			 * if previous values were above threshold (map still contains attribute), 
			 * check whether values of new RNA are below threshold
			 * and if so, remove attribute from the map
			 */
			if(map.containsAttribute(i)) {
				for(int j=0; j<newRNA[i].length; j++) {
					if(newRNA[i][j]<threshold && map.containsAttribute(i)) {
						map.removeAttribute(i);
					}
				}
				
				/*
				 * if previous values were above threshold do the same for pre-existing RNA
				 */
				if(map.containsAttribute(i)) {
					for(int j=0; j<preRNA[i].length; j++) {
						if(preRNA[i][j]<threshold && map.containsAttribute(i)) {
							map.removeAttribute(i);
						}
					}
				} 
			} 
		}
		Data newData;
		//construct new data object with references to the old contents but for the map, which is the updated map
		newData = new Data(totalRNA, newRNA, preRNA, data.getTotalDescr(), data.getNewDescr(), 
				data.getPreDescr(), map);
		newData = transferAllValues(data, newData);
		newData.setMinimum(newmin);
		newData.setMaximum(newmax);
		System.out.println("Done filtering data.");
		System.out.println("You have "+map.mapSize()+" probesets.");
		System.out.println("------------------------------");
		return newData;
	}
	
	/**
	 * Removes values that are defined as 'absent' in the attribute list
	 * Commandline option -f present='label1,label2,...:call:threshold'
	 * @param data Data object that will be filtered
	 * @param relevantColumns List of the labels of all columns that contain call information
	 * @param call The call according to which will be filtered, usually 'A' for absent
	 * @param threshold Defines how often the $call has to be noted for one spotid for it to be filtered  
	 * @return Filtered Data object
	 */
	public static Data filterAbsent(Data data, ArrayList<String> relevantColumns, String call, int threshold) {
		System.out.println("Filtering data...");
		Mapping<String, Integer> map    = data.copyMap();
		ArrayList<Mapping<String, String>> attributes = data.getAttributes();
		String[] attrDescr              = data.getAttrDescr();
		boolean exists                  = false;
//		int index                       = -1;
		ArrayList<Integer> indices      = new ArrayList<Integer>();
		//get index of attribute mapping containing absence information
		if(attrDescr != null) {
			for(int i = 0; i<attrDescr.length; i++) {
				if(relevantColumns.contains(attrDescr[i])) {
					exists = true;
					indices.add(i);
				}
			}
		} else {
			System.err.println("Warning: You did not load any attributes! Will try to\n" +
					"load present/absent calls from original file.");
		}
		
		int[] callNumbers = new int[data.getMap().mapSize()];
		for(int i=0; i<callNumbers.length; i++) {
			callNumbers[i] = 0;
		}
		
		if(exists) {
		for(int index : indices) {
			//get the Mapping for this attribute
			Mapping<String, String> attr = attributes.get(index);
			
				Iterator<String> iter = data.getMap().iterate();
				int callIndex         = 0;
				
				//iterate through all valid spotids
				while(iter.hasNext()) {
					String id = iter.next();
					
					if(attr.containsSpot(id)) {
						String abs = attr.getAttribute(id);
						//remove spot if defined as absent in at least $threshold call lists
						if(abs.equals(call)) {
							callNumbers[callIndex]++;
							if(callNumbers[callIndex] >= threshold) {
								map.removeSpot(id);
							}
						}
					//remove spot if not defined at all in attribute list
					} else {
						map.removeSpot(id);
					}
					callIndex++;
				}
			//if there is no attribute for absence, check whether there is a column in the original data file describing it
			} 
		} else {
			try {
				BufferedReader inp = new BufferedReader(new FileReader(new File(data.getFilename())));
				String line;
				boolean header = false;
				
				while((line = inp.readLine()) != null) {
					if(!line.startsWith("#")) {
						if(!header) {
							header = true;
							String[] head = line.split("\t");
							//find the label in the header
							for(int i=0; i<head.length; i++) {
								if(relevantColumns.contains(head[i])) {
									indices.add(i);
								} 
							}
							
							//if the label isn't contained in the header, exit
							if(indices.size() == 0) {
								System.err.println("Your given labels for present/absent call attributes \n" +
										"are neither loaded as attributes nor part of the original data file.\n" +
										"For filtering according to present/absent calls you need to load these\n" +
										"attributes first!");
								System.exit(1);
							} else if(indices.size() != relevantColumns.size()) {
								System.err.println("Some of your present/absent call attributes are not part of the datafile;\n" +
								"ignoring the following attributes:");
						for(int i=0; i<relevantColumns.size(); i++) {
							if(!indices.contains(i)) {
								System.err.println(relevantColumns.get(i));
							}
						}
					}
						} else {
							String[] values = line.split("\t");
							String id = values[0];
							
							for(int index : indices) {
								int calls = 0;
								if(values[index].equals(call)) {
									calls++;
								}
								if(calls >= threshold) {
									map.removeSpot(id);
								}
							}
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Data newData = new Data(data.getTotalRNA(), data.getNewRNA(), data.getPreRNA(), data.getTotalDescr(), 
				data.getNewDescr(), data.getPreDescr(), map);
		newData = transferAllValues(data, newData);
		newData.findMiniMaxi();
		System.out.println("Done filtering data.");
		System.out.println("You have "+map.mapSize()+" probesets.");
		System.out.println("------------------------------");
		return newData;
	}
	
	/**
	 * Please note that this method can only be used after normalization! The normalization object in which the results 
	 * from this step are stored has to be given as parameter, but normalization has to be already performed!
	 * Filters the data in such a way that all remaining entries have a quality control value lower than 
	 * the given threshold
	 * Commandline option -f pqs='threshold'
	 * @param data Data object that shall be filtered
	 * @param l Linear Regression object which correction factors will be used for filtering
	 * @param threshold A threshold which defines which values shall be kept
	 * @param histogram TRUE if a histogram about the PQS values should be produced, FALSE otherwise
	 * @return The new Data object where probe sets with low quality are not contained anymore
	 */
	public static Data filterPQS(Data data, Normalization l, double threshold, boolean histogram) {
		if(checkForGenes(data)) {
			System.out.println("Filtering data...");
			Mapping<String, Integer> map    = data.copyMap();
			Mapping<String, Integer> newMap = data.calculateNewMapping();
			Iterator<String> iter           = newMap.iterate();
			//calculation of probeset quality control values
			double[] pqs                    = l.calculateQualityControl(data, histogram);
			
			//removal of all values higher than the given threshold
			while(iter.hasNext()) {
				String id = iter.next();
				int index = newMap.getAttribute(id);
				if(pqs[index]>threshold) {
					map.removeSpot(id);
				}
			}
			Data newData;
			//construct new data object with references to the old contents but for the map, which is the updated map
			newData = new Data(data.getTotalRNA(), data.getNewRNA(), data.getPreRNA(), data.getTotalDescr(), 
					data.getNewDescr(), data.getPreDescr(), map);
			newData = transferAllValues(data, newData);
			newData.findMiniMaxi();
			newData.setPqs(pqs);
			System.out.println("Done filtering data.");
			System.out.println("You have "+map.mapSize()+" probesets.");
			System.out.println("------------------------------");
			return newData;
		} else {
			System.err.println("You did not load an attribute containing gene names. For the calculation of probeset \n" +
					"quality scores gene names are necessary! PQS-filtering could not be done.");
			return data;
		}
	}
	
	/**
	 * Please note that this method can only be used after normalization! The normalization object in which the results 
	 * from this step are stored has to be given as parameter, but normalization has to be already performed!
	 * Filters data such that from each gene only one entry, the one with the lowest quality control value, is kept; 
	 * Calculation is performed for only one replicate
	 * Commandline option -f pqsmin=replicate (from 1 to #replicates)
	 * @param data Data object that shall be filtered
	 * @param l The normalization object which will be used for the filtering
	 * @param replicate The replicate for which the ratio calculation will be performed
	 * @param histogram TRUE if a histogram about the PQS values should be produced, FALSE otherwise
	 * @return The filtered Data object
	 */
	public static Data filterPQS(Data data, Normalization l, int replicate, boolean histogram) {
		if(checkForGenes(data)) {
			System.out.println("Filtering data...");
			Mapping<String, Integer> map    = data.copyMap();
			Mapping<String, Integer> newMap = data.calculateNewMapping();
			String[] descr                  = data.getAttrDescr();
			Iterator<String> iter           = newMap.iterate();
			l.setReplicate(replicate);
			double[] pqs                    = l.calculateQualityControl(data, histogram);
			int index                       = -1;
			ArrayList<Mapping<String, String>> attributes = data.getAttributes();
			
			//checking which of the attributes mappings contains the gene name
			for(int i=0; i<descr.length; i++) {
				if(descr[i].equals(data.getGeneName())) {
					index = i;
				}
			}
			//getting the mapping that maps spotid to genename
			Mapping<String, String> atts = attributes.get(index);

			while(iter.hasNext()) {
				String id                   = iter.next();
				
				if(atts.containsSpot(id)) {
					//the gene that corresponds to the momentary spotid
					String gene                 = atts.getAttribute(id);
					//the spotids that correspond to the same gene
					ArrayList<String> spotList = atts.getSpotId(gene);
					double min                   = Double.POSITIVE_INFINITY;
					int minIndex                 = 0;
					String minId                 = spotList.get(0);
					
					if(atts.containsSpot(id) && map.containsSpot(id)) {
						
						//iterate over all spotids for the momentary gene
						Iterator<String> spots = spotList.iterator();
						while(spots.hasNext()) {
							String spot = spots.next();
							
							if(newMap.containsSpot(spot)) {
								int ind     = newMap.getAttribute(spot);
								int mapInd  = data.getMap().getAttribute(spot);
								//find the minimum
								if(pqs[ind]<min) {
									min      = pqs[ind];
									minIndex = mapInd;
									minId    = spot;
								} 
							}
							
						}
						//delete ALL spotids that correspond to the gene
						map.removeAllSpots(spotList);
						//add the minimum again
						map.addMap(minId, minIndex);
				}
				}
				
			}
			Data newData;
			//construct new data object with references to the old contents but for the map, which is the updated map
			newData = new Data(data.getTotalRNA(), data.getNewRNA(), data.getPreRNA(), data.getTotalDescr(), 
					data.getNewDescr(), data.getPreDescr(), map);
			newData = transferAllValues(data, newData);
			newData.findMiniMaxi();
			newData.setPqs(pqs);
			System.out.println("Done filtering data.");
			System.out.println("You have "+map.mapSize()+" probesets.");
			System.out.println("------------------------------");
			return newData;
		} else {
			System.err.println("You did not load an attribute containing gene names. For the calculation of probeset " +
			"quality scores gene names are necessary! PQS-filtering could not be done.");
			return data;
		}
	}
	
	/**
	 * Please note that this method can only be used after normalization! The normalization object in which the results 
	 * from this step are stored has to be given as parameter, but normalization has to be already performed!
	 * Filters data such that from each gene only one entry, the one with the lowest quality control value, is kept; 
	 * Calculation is performed for all replicates
	 * Commandline option -f pqsmin=-1 
	 * @param data Data object that shall be filtered
	 * @param l The normalization object which will be used for the filtering
	 * @param histogram TRUE if a histogram about the PQS values should be produced, FALSE otherwise
	 * @return The filtered Data object
	 */
	public static Data filterPQS(Data data, Normalization l, boolean histogram) {
		return filterPQS(data, l, Normalization.UNDEF, histogram);
	}
	
	/**
	 * Filters the data according to given values for bias correction in such a way that only those probeset 
	 * ids are kept that have corresponding correction values
	 * @param data The original data object
	 * @param corr The correction values, mapped from id -> value
	 * @return The filtered Data object
	 */
	public static Data filterCorrectionBias(Data data, HashMap<String, Double> corr) {
		System.out.println("Filtering data...");
		Mapping<String, Integer> map    = data.copyMap();
		Iterator<String> iterator       = data.getMap().iterate();
		
		while(iterator.hasNext()) {
			String id = iterator.next();
			if(!corr.containsKey(id)) {
				map.removeSpot(id);
			}
		}
		
		Data newData;
		//construct new data object with references to the old contents but for the map, which is the updated map
		newData = new Data(data.getTotalRNA(), data.getNewRNA(), data.getPreRNA(), data.getTotalDescr(), 
				data.getNewDescr(), data.getPreDescr(), map);
		newData = transferAllValues(data, newData);
		newData.findMiniMaxi();
		System.out.println("Done filtering data.");
		System.out.println("You have "+map.mapSize()+" probesets.");
		System.out.println("------------------------------");
		return newData;
	}
	
	/**
	 * Transfers all values that are not directly used in filtering 
	 * from the old data object to the new data object
	 * @param oldData The data object before filtering
	 * @param newData The new data object, created while filtering
	 * @return The new data object with all the values of the old data object
	 */
	public static Data transferAllValues(Data oldData, Data newData) {
		//if old data object contains attributes, set same list of attributes for new data object
		if(oldData.getAttributes()!= null) {
			newData.setAttributes(oldData.getAttributes());
			newData.setAttrDescr(oldData.getAttrDescr());
		}
		newData.setFilename(oldData.getFilename());
		newData.setGeneName(oldData.getGeneName());
		newData.setAbsent(oldData.getAbsent());
		newData.setCorrNewPre(oldData.getCorrNewPre());
		newData.setCorrNewTot(oldData.getCorrNewTot());
		newData.setCorrPreTot(oldData.getCorrPreTot());
		newData.setMethod(oldData.getMethod());
		newData.setPqs(oldData.getPqs());
		newData.setReplicate(oldData.getReplicate());
		newData.setSpotid(oldData.getSpotid());
		newData.setUnequal(oldData.isUnequal());
		return newData;
	}
	
	/**
	 * Checks if the data object has the gene name attribute loaded and 
	 * can be used for PQS filtering
	 * @param data The data object
	 * @return TRUE if gene names are loaded and PQS filtering can proceed
	 */
	public static boolean checkForGenes(Data data) {
		for(int i=0; i<data.getAttrDescr().length; i++) {
			if(data.getAttrDescr()[i].equals(data.getGeneName())) {
				return true;
			}
		}
		return false;
	}
	
}
