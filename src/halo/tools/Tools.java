/*
 * This file is part of the HALO 1.3 graphical user interface.
 */
package halo.tools;

import halo.data.Data;
import halo.halflife.HalfLife;
import halo.halflife.HalfLife_New;
import halo.halflife.HalfLife_Pre;
import halo.normalization.LinearRegression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class containing several tools used for HALO analyses
 * @author Stefanie Kaufmann
 *
 */
public class Tools {

	public static final String LIBRARYNAME = "HALO";
	public static final String VERSION     = "1.3";
	public static final String HOMEPAGE    = "http://www.bio.ifi.lmu.de/software/HALO";
	public static final String HOMEPAGEDOC = "http://www.bio.ifi.lmu.de/files/Software/halo/doc/index.html";

	/**
	 * Reads the header/description line of a given file and provides all
	 * column labels (columns separated by tabulator) as a list 
	 * @param file The file containing the expression data
	 * @return A list containing all column labels
	 */
	public static ArrayList<String> getColumnLabels(File file) {
		ArrayList<String> result = new ArrayList<String>();
		try {
			BufferedReader buff = new BufferedReader(new FileReader(file));
			String line;

			while((line = buff.readLine())!=null) {
				//skip the commentary part at the beginning of the file
				if(!line.startsWith("#")) {
					//check if line has at least 3 columns (spotid, 2 RNA measurements) 
					//and if it does not contain invalid letters
					if(isLineValid(line) && hasLineColumns(line, 2)) {
						String[] header = line.trim().split("\t");
						for(int i=0; i<header.length; i++) {
							result.add(header[i]);
						}
						//if line is not valid, return no list
					} else {
						result = null;
					}
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Tests if a given line is composed of the letters a-z (case ignored), 
	 * numbers or whitespace symbols like TAB
	 * @param line The line that will be tested 
	 * @return TRUE if the line is valid, FALSE otherwise
	 */
	public static boolean isLineValid(String line) {
		Pattern pattern = Pattern.compile("^[A-Z|a-z|0-9|\\s]");
		Matcher matcher = pattern.matcher(line);
		if(matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks whether a line consists of at least the given number of columns, 
	 * whereas columns are separated through any whitespace sign
	 * @param line The line that will be checked for validity
	 * @param columns The minimal number of columns that has to be in this line
	 * @return TRUE if the line consists of this many columns or more
	 */
	public static boolean hasLineColumns(String line, int columns) {
		String[] lineInColumns = line.split("\\s");
		if(lineInColumns.length >= columns) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Tests the correctness of a given file according to format (= minimal number 
	 * of columns has to be correct) and letters (= are there any unusual letters present?)
	 * according to the first line after the commentary
	 * @param file The input file that will be tested
	 * @param columns The minimal number of columns that has to be present in the file
	 * @return TRUE if the file is valid
	 */
	public static boolean testValidityForFile(File file, int columns, 
			boolean permitUnusualLetters) {
		try {
			BufferedReader buff = new BufferedReader(new FileReader(file));
			String line;

			while((line = buff.readLine()) != null) {
				if(!line.startsWith("#") && (!isLineValid(line) || permitUnusualLetters) 
						&& !hasLineColumns(line, columns)) {
					return false;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}


	/**
	 * Calculates the average median over two available half-life calculation methods
	 * @param time The labeling time for the calculations
	 * @param data The data
	 * @return The average median
	 */
	public static double calculateMedianForHalfLives(double time, Data data)
	{
		double sumAll    = 0;
		double length    = 0;
		int nrReplicates = data.getReplicatesTotal();

		ArrayList<HalfLife> medMethods = new ArrayList<HalfLife>();

		medMethods.add(new HalfLife_New());
		medMethods.add(new HalfLife_Pre());

		LinearRegression lr;

		double[] halflives;
		double sum;
		for(int i = 1; i <= nrReplicates; i++)
		{
			sum = 0; 
			for(HalfLife hl : medMethods)
			{
				hl.initialize(data, i);
				lr = new LinearRegression(data);

				lr.setReplicate(i);
				hl.setCorrectionFactor(lr.calculateCorrectionFactors());

				hl.calculateHalfLives(time);
				halflives = hl.getHwz();

				double med = HalfLife.median(halflives);  
				sum += med;

			}
			sumAll += sum/2.0;
			length++;

		}
		double median = sumAll / length;

		return median;
	}

}
