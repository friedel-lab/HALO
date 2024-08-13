/*
 * This file is part of HALO version 1.3. 
 */
package halo.data.biascorrection;

import halo.userinterface.gui.session.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * An implementation to calculate the correlation coefficient from a tabular data file with R
 * @author Stefanie Kaufmann
 *
 */
public class RCorrelationCoefficient extends CorrelationCoefficient {

	private String              method   = SPEARMAN;
	public static final String  PEARSON  = "pearson";
	public static final String  KENDAL   = "kendal";
	public static final String  SPEARMAN = "spearman";
	private static final String ending   = ".corr.out";

	private double correlationCoefficient;
	private String fileName = Config.CORRELATIONCOEFFICIENT;
	private String path;
	private File   output;
	
	/**
	 * Constructs a new object for the calculation
	 * @param path The path of the R installation
	 * @param input The name of the file that holds the data table
	 */
	public RCorrelationCoefficient(String path, String input) {
		this.path = path;
		setInputFile(new File(input));
	}

	
	/**
	 * Writes the script for R to start the calculation of a correlation coefficient for the data 
	 * and write the output into an output file
	 */
	public void writeRScript() {
		RMethods rM = new RMethods();
		rM.setPath(path);

		rM.startRScript(fileName);
		rM.addLineToScript("options(echo = FALSE)");
		String path = Config.CONFIGDIR.replaceAll("\\\\","\\\\\\\\");
		rM.addLineToScript("setwd(\""+path+"\")");
		path = inputFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");
		//read in data
		rM.addLineToScript("data <- scan('"+path+"', list(0,0), skip=1)");
		rM.addLineToScript("cor.coeff <- cor(data[[1]], data[[2]], method='"+method+"')");
		rM.addLineToScript("sink('"+inputFile.getName()+ending+"')");
		rM.addLineToScript("print(cor.coeff)");
		rM.addLineToScript("sink()");
		rM.endRScript();

		rM.runR();
	}

	/**
	 * Parse the result from the output file
	 */
	public void readRResults() {
		try {
			output = new File(Config.CONFIGPATH+inputFile.getName()+ending);
			BufferedReader result = new BufferedReader(new FileReader(output));
			String line;
			
			try {
				while((line = result.readLine()) != null) {
					String coeff = line.replace("[1]", "").trim();
					correlationCoefficient = Double.parseDouble(coeff);
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			
			result.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Start the calculation of the correlation coefficient
	 * @return The correlation coefficient for the data
	 */
	@Override
	public double calculateCorrelationCoefficient() {
		System.out.println("------------------------------");
		System.out.println("Calculating correlation coefficient...");
		writeRScript();
		readRResults();
		output.deleteOnExit();
		System.out.println("Done calculating correlation coefficient");
		System.out.println("------------------------------");
		return correlationCoefficient;
	}

	/**
	 * Sets the name of the script file
	 * @param fileName The name of the R script file
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * Sets the method for the calculation of the correlation coefficient
	 * @param method The method for the calculation of the correlation coefficient; either 'pearson',
	 * 'kendal' or 'spearman' (default)
	 */
	public void setMethod(String method) {
		if(!method.equals(PEARSON) && !method.equals(KENDAL) && !method.equals(SPEARMAN)) {
			System.err.println("You have to use one of the following methods for the calculation of the \n" +
					"correlation coefficient: "+PEARSON+" for Pearson, "+KENDAL+" for Kendal, "+SPEARMAN+"\n" +
							"for Spearman. Proceeding with default: " +	this.method);
		}
	}
	
}
