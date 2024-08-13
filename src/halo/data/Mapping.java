/*
 * This file is part of HALO version 1.3. 
 */
package halo.data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Mapping of spotIDs to corresponding indices of RNA sets
 * @author Stefanie Kaufmann
 *
 */
public class Mapping<T,U> {

	private static boolean turnReverseMapOff = false;
	private HashMap<T, U> map;
	private HashMap<U, ArrayList<T>> reverseMap;
	
	/**
	 * Constructor to initialize the Mapping
	 */
	public Mapping() {
		//initialize the internal HashMaps
		map        = new HashMap<T, U>();
		/*
		 * the keys in the Mapping need to be specific, but several keys can have the same attribute
		 * the reverse Map maps one attribute to all keys with this attribute
		 */
		reverseMap = new HashMap<U, ArrayList<T>>();
	}
	
	/**
	 * Add the values to the internal map as well as to the map containing the inverted values (attribute --> spotID)
	 * @param spotId 
	 * @param attribute
	 */
	public void addMap(T spotId, U attribute) {
		if(attribute != null) {
			map.put(spotId, attribute);
			ArrayList<T> get;
			if(!turnReverseMapOff) {
				//if another key with this attribute already exists, add the new key to the list of keys with this attribute
				if(reverseMap.containsKey(attribute)) {
					get = reverseMap.get(attribute);
					if(!get.contains(spotId)) {
						get.add(spotId);
					}
				//if no entry with this attribute exists, create new list and entry for the reverse Map
				} else {
					get = new ArrayList<T>();
					get.add(spotId);
				}
				reverseMap.put(attribute, get);
			}
		}
	}
	
	/**
	 * Removes a given key from the Mapping
	 * @param spotId Key that will be removed
	 */
	public void removeSpot(T spotId) {
		reverseMap.remove(map.get(spotId));
		map.remove(spotId);
	}
	
	/**
	 * Removes all keys given in a list
	 * @param spots List of keys that will be removed
	 */
	public void removeAllSpots(ArrayList<T> spots) {
		for(T item : spots) {
			if(reverseMap.containsKey(map.get(item))) {
				reverseMap.remove(map.get(item));
			}
			map.remove(item);
		}
	}
	
	/**
	 * Removes all entries corresponding to a specific attribute from the Mapping
	 * @param attribute Attribute for which all corresponding entries will be removed
	 */
	public void removeAttribute(U attribute) {
		ArrayList<T> spots = reverseMap.get(attribute);
		for(T spot : spots) {
			map.remove(spot);
		}
		reverseMap.remove(attribute);
	}
	
	/**
	 * Change the attribute of a specific spotID
	 * @param spotId The spotID for which the attribute will be changed
	 * @param attribute The new attribute for the spotID
	 */
	public void changeMap(T spotId, U attribute) {
		U oldAttribute = map.get(spotId);
		//delete old key from the list corresponding to the old attribute in the reverse map
		ArrayList<T> get = reverseMap.get(map.get(spotId));
		for(int i=0; i<get.size(); i++) {
			if(get.get(i).equals(spotId)) {
				get.remove(i);
			}
		}
		//insert new shortened key list with old attribute or delete old entry if nothing is left
		if(get.size()>0) {
			reverseMap.put(oldAttribute, get);
		} else {
			reverseMap.remove(map.get(spotId));
		}
		
		//insert new entry into Mapping
		this.addMap(spotId, attribute);
	}
	
	/**
	 * Returns true if the given key exists in the Mapping
	 * @param spotId
	 * @return true if key exists
	 */
	public boolean containsSpot(T spotId) {
		return map.containsKey(spotId);
	}
	
	/**
	 * Returns true if the given attribute exists in the Mapping
	 * @param attribute
	 * @return true if the attribute exists
	 */
	public boolean containsAttribute(U attribute) {
		return reverseMap.containsKey(attribute);
	}
	
	/**
	 * Provides an iterator to iterate over the keys of the Mapping
	 * @return Iterator over key-set
	 */
	public Iterator<T> iterate() {
		return map.keySet().iterator();
	}
	
//	public Iterator<U> iterateAttributes() {
//		return reverseMap.keySet().iterator();
//	}
	
	/**
	 * Provides an iterator to iterate over the keys corresponding to a certain attribute
	 * @param attr The attribute for which the key-set will be produced
	 */
	public Iterator<T> iterateSpots(U attr) {
		return reverseMap.get(attr).iterator();
	}
	
	/**
	 * Retrieves the list of keys for one given attribute
	 * @param attribute The attribute for which the list of keys will be given
	 * @return The list of keys corresponding to the attribute
	 */
	public ArrayList<T> getSpotId(U attribute) {
		ArrayList<T> spotId = null;
		if(reverseMap.containsKey(attribute)) {
			spotId = reverseMap.get(attribute);
		} 
		return spotId;
	}
	
	/**
	 * Retrieves the attribute for one given key
	 * @param spotId The key for which the attribute will be given
	 * @return The attribute corresponding to the given key
	 */
	public U getAttribute(T spotId) {
		U attribute = null;
		if(map.containsKey(spotId)) {
			attribute = map.get(spotId);
		}
		return attribute;
	}
	
	/**
	 * Returns TRUE if reverse map is turned off
	 * @return TRUE if reverse map is turned off
	 */
	public static boolean isTurnReverseMapOff() {
		return turnReverseMapOff;
	}
	
	/**
	 * Allows user to turn the reverse map off for attributes like present/absent calls 
	 * with large numbers of equal attributes
	 * @param turnReverseMapOff TRUE if reverse mapping should be turned off
	 */
	public static void setTurnReverseMapOff(boolean turnReverseMapOff) {
		Mapping.turnReverseMapOff = turnReverseMapOff;
	}
	
	/**
	 * Returns the size of the Mapping (=number of entries)
	 * @return size of the Mapping
	 */
	public int mapSize() {
		return map.size();
	}
}
