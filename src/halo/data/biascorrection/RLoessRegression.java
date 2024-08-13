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
import java.util.HashMap;

/**
 * Performs a local approximating regression for a data file with R
 * @author Stefanie Kaufmann
 *
 */
public class RLoessRegression extends LoessRegression {
	
	private String fileName = Config.LOESSREGRESSION;
	private String path;
	private HashMap<String, Double> lowess = new HashMap<String, Double>();
	private String[] spots;
	private File     output;
	private static final String ending = ".loess.out";
	
	/**
	 * Constructs a new object for local approaximating regression with R
	 * @param path The path of the local R installation
	 * @param input The name of the input file that holds the data
	 */
	public RLoessRegression(String path, String input, String[] spots) {
		this.spots = spots;
		this.path = path;
		inputFile = new File(input);
	}
	
	/**
	 * Writes an R script that starts the calculation and writes the output into a file
	 */
	public void writeRScript() {
		RMethods rM = new RMethods();
		rM.setPath(path);

		rM.startRScript(fileName);
		rM.addLineToScript("options(echo = FALSE)");
		String path = Config.CONFIGDIR.replaceAll("\\\\", "\\\\\\\\");
		rM.addLineToScript("setwd(\""+path+"\")");
		path = inputFile.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");
		//read in data
		rM.addLineToScript("data <- scan('"+path+"', list(0,0), skip=1)");
		rM.addLineToScript("x <- data[[1]]");
		rM.addLineToScript("y <- data[[2]]");
		rM.addLineToScript("lo1 <- loess(y ~ x)");
		rM.addLineToScript("x2 <- data[[1]]");
		rM.addLineToScript("x2_lo <- data.frame(x=x2)");
		rM.addLineToScript("y2 <- predict(loess(y ~ x), new.data=x2_lo,se=T)$fit");		
		rM.addLineToScript("sink('"+inputFile.getName()+ending+"')");
//		rM.addLineToScript("print(x)");
		rM.addLineToScript("print(y2)");
		rM.addLineToScript("sink()");
		rM.endRScript();

		rM.runR();
	}
	
	/**
	 * Reads the R output from the file and returns the fitted values
	 */
	public void readRResults() {
		try {
			output = new File(Config.CONFIGPATH+inputFile.getName()+ending);
			BufferedReader results = new BufferedReader(new FileReader(output));
			String line;
			
//			boolean first = true;
			int numbers = 0;
			while((line = results.readLine()) != null) {
				String[] content = line.trim().split(" +");
//				if(line.trim().startsWith("[1]") && first) {
//					first = false;
//				} else if(!first) {
					for(int i=1; i<content.length; i++) {
						String id = spots[numbers];
						lowess.put(id,Double.parseDouble(content[i]));
						numbers++;
					}
//				}
			}
			if(lowess.size() < spots.length) {
				int differ = spots.length - lowess.size();
				System.err.println("There are no sequences available for "+differ+" probesets. \n" +
						"Please note that only probesets with available sequences could be corrected! \n" +
						"You can use the method Filter.filterCorrectionBias to erase uncorrected probesets.");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Starts the calculation of the fitted values with R
	 * @return The fitted values of the data
	 */
	@Override
	public HashMap<String,Double> calculateLoessRegression() {
		System.out.println("------------------------------");
		System.out.println("Calculate loess regression...");
		writeRScript();
		readRResults();
		output.deleteOnExit();
		System.out.println("Done calculating loess regression");
		System.out.println("------------------------------");
		return lowess;
	}
	
	/**
	 * Sets the file name of the script file
	 * @param fileName The name of the script file
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
