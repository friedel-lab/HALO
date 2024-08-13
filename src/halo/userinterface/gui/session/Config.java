/*
 * This file is part of the HALO 1.3 graphical user interface
 */
package halo.userinterface.gui.session;

import halo.tools.Tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Provides a set of final parameters describing paths and file names and saves user decisions for 
 * the display of certain dialogs
 * @author Stefanie Kaufmann
 *
 */
public class Config {
	
	public static final String CONFIGDIR  = System.getProperty("user.home")  +
	File.separator +".halo";
	public static final String CONFIGPATH = CONFIGDIR + File.separator; 
	
	public static final String CORRELATIONCOEFFICIENT = CONFIGPATH+"CorrelationCoefficent.R";
	public static final String LOESSREGRESSION        = CONFIGPATH+"LoessRegression.R";
	public static final String CONFIGFILE             = CONFIGPATH+"Config";
	
	private static boolean displayHint = true;
	private static boolean displayExit = true;
	private static String  sessionFile = "";
	private static String  pathToR     = "";
	private static String  userbrowser = "";
	
	/**
	 * Generates a file that saves information about the display of the information dialog on 
	 * program start, exit dialog on program exit, and path of the session file
	 * @param hint TRUE if dialog about question mark should be displayed again
	 * @param exit TRUE if dialog about saving on exit should be displayed again
	 * @param file Path of the file that contains the saved session 
	 */
	public static void generateConfigFile(boolean hint, boolean exit, String file) {
		displayHint = hint;
		displayExit = exit;
		sessionFile = file;
		
		try {
			BufferedWriter buff = new BufferedWriter(new FileWriter(new File(CONFIGFILE)));
			
			buff.write("################# HALO version "+Tools.VERSION+" Config file ###################\n");
			buff.write("hint="+hint+"\n");
			buff.write("exit="+exit+"\n");
			buff.write("session="+sessionFile+"\n");
			buff.write("pathToR="+pathToR+"\n");
			buff.write("userbrowser="+userbrowser);
			buff.flush();
			buff.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates a file that saves information about the display of the information dialog on 
	 * program start, exit dialog on program exit, and path of the session file based on 
	 * previously loaded values
	 */
	public static void generateConfigFile() {
		generateConfigFile(displayHint, displayExit, sessionFile);
	}
	
	/**
	 * Loads a config file
	 */
	public static void loadConfigFile() {
		try {
			BufferedReader buff = new BufferedReader(new FileReader(new File(CONFIGFILE)));
			String line;
			
			while((line = buff.readLine()) != null) {
				String[] content = line.split("=");
				if(line.startsWith("hint")) {
					displayHint = Boolean.parseBoolean(content[1]);
				} else if (line.startsWith("exit")) {
					displayExit = Boolean.parseBoolean(content[1]);
				} else if (line.startsWith("session")) {
					if(content.length>1) {
						sessionFile = content[1];
					}
				} else if (line.startsWith("pathToR")) {
					if(content.length>1) {
						pathToR = content[1];
					}
				} else if (line.startsWith("userbrowser")) {
					if(content.length > 1) {
						userbrowser = content[1];
					}
				}
			}
			buff.close();
		} catch (FileNotFoundException e)
                {
			e.printStackTrace();
	    } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the information if question mark dialog should be displayed
	 * @param hint TRUE if question mark dialog should be displayed
	 */
	public static void setDisplayHint(boolean hint) {
		displayHint = hint;
	}
	
	/**
	 * Sets the information if exit dialog should be displayed
	 * @param exit TRUE if exit dialog should be displayed
	 */
	public static void setDisplayExit(boolean exit) {
		displayExit = exit;
	}
	
	/**
	 * Sets the path to the bin folder of the R installation
	 * @param pathToR The absolute path of the R bin folder
	 */
	public static void setPathToR(String pathToR) {
		Config.pathToR = pathToR;
	}
	
	/**
	 * Sets the path of the session file
	 * @param file Path of the session file
	 */
	public static void setSessionFile(String file) {
		sessionFile = file;
	}
	
	/**
	 * Sets the user browser
	 * @param userbrowser User-defined browser 
	 */
	public static void setUserbrowser(String userbrowser) {
		Config.userbrowser = userbrowser;
	}

	/**
	 * Returns true if question mark dialog should be displayed
	 * @return TRUE if question mark dialog should be displayed
	 */
	public static boolean getDisplayHint() {
		return displayHint;
	}
	
	/**
	 * Returns true if exit dialog should be displayed
	 * @return TRUE if exit dialog should be displayed
	 */
	public static boolean getDisplayExit() {
		return displayExit;
	}
	
	/**
	 * Returns the path of the session file
	 * @return Path of the session file
	 */
	public static String getSessionFile() {
		return sessionFile;
	}
	
	/**
	 * Returns the user browser
	 * @return User defined browser
	 */
	public static String getUserbrowser() {
		return userbrowser;
	}
	
	/**
	 * Returns the path of the R bin folder
	 * @return The path of the R bin folder
	 */
	public static String getPathToR() {
		return pathToR;
	}
}
