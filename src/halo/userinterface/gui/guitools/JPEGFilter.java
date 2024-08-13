/*
 * This file is part of the HALO 1.3 graphical user interface
 */
package halo.userinterface.gui.guitools;

import java.io.File;
import java.util.ArrayList;

import javax.swing.filechooser.FileFilter;

/**
 * File filter that accepts only jpg files
 * @author Stefanie Kaufmann
 *
 */
public class JPEGFilter extends FileFilter {
	
	private ArrayList<String> acceptedEndings;
	
	/**
	 * Generates a new filter, initializes the list of accepted endings
	 */
	public JPEGFilter() {
		acceptedEndings = new ArrayList<String>();
		acceptedEndings.add("jpg");
		acceptedEndings.add("jpeg");
		acceptedEndings.add("JPG");
		acceptedEndings.add("JPEG");
	}

	/**
	 * Adds an accepted ending to the list
	 * @param ending The file ending that is also accepted
	 */
	public void addEnding(String ending) {
		if(ending.startsWith(".")) {
			ending = ending.substring(1);
		}
		acceptedEndings.add(ending);
	}
	
	/**
	 * Tests if a given file is accepted
	 * @param f File that will be tested
	 * @return True if file is accepted
	 */
	@Override
	public boolean accept(File f) {
		String name    = f.getName();
		String[] split = name.split("\\.");
		String ending  = split[split.length-1];
		if(acceptedEndings.contains(ending)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the description of the filter
	 * @return The description of the filter
	 */
	@Override
	public String getDescription() {
		return ".jpg, .jpeg, .JPG, .JPEG";
	}

	/**
	 * Returns the list of accepted endings
	 * @return The list of accepted endings
	 */
	public ArrayList<String> getAcceptedEndings() {
		return acceptedEndings;
	}
	
}
