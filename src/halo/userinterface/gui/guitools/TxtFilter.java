/*
 * This file is part of the HALO 1.3 graphical user interface
 */
package halo.userinterface.gui.guitools;

import java.io.File;
import javax.swing.filechooser.*;
import java.util.Arrays;
import java.util.List;

/**
 * File filter that accepts only text files
 * @author Stefanie Kaufmann
 *
 */
public class TxtFilter extends FileFilter {

	private List<String> accepted = Arrays.asList(".txt", ".TXT");
	
	@Override
	public boolean accept(File pathname) {
		if(pathname.isDirectory()) {
			return true;
		}
		for(String ending : accepted) {
			if(pathname.getName().endsWith(ending)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String getDescription() {
		return "Only text-files";
	}

}
