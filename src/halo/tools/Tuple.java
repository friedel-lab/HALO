/*
 * This file is part of HALO version 1.3. 
 */
package halo.tools;

/**
 * Class to save a tuple of two values that can be sorted numerically
 * over the second value
 * @author Stefanie Kaufmann
 *
 */
@SuppressWarnings("hiding")
public class Tuple<Double,B> implements Comparable<Object>{

	private B second;
	private double first;
	
	/**
	 * Constructs a tuple containing a pair of values
	 * @param first The first (numerical) value
	 * @param second The second value
	 */
	public Tuple(double first, B second) {
		this.first    = first;
		this.second   = second;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	/**
	 * Compares two tuples over the first (numerical) entry
	 */
	public int compareTo(Object o) {
		if(!(o instanceof Tuple)) {
			System.err.println("You cannot compare a non-Tuple with a Tuple!");
			System.exit(1);
		}
		Tuple<Double,B> tup = (Tuple<Double,B>) o;
		double lo = tup.getFirst();
		if (this.first == lo)
            return 0;
        else if ((this.first) > lo)
            return 1;
        else
            return -1;
	}
	
	/**
	 * Returns the first value for the tuple
	 * @return The first value
	 */
	public double getFirst() {
		return first;
	}
	
	/**
	 * Returns the second value for the tuple
	 * @return The second value
	 */
	public B getSecond() {
		return second;
	}
	
}
