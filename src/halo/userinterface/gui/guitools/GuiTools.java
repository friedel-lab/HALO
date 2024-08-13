/*
 * This file is part of the HALO 1.3 graphical user interface
 */
package halo.userinterface.gui.guitools;

import halo.userinterface.gui.session.Config;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * Class containing several tools used for Graphical User Interface
 * @author Stefanie Kaufmann
 *
 */
public class GuiTools {
	
	/**
	 * Formats a text for a dialog window 
	 * @param txt The original text
	 * @param width The width of the line
	 * @param fm The font metrics
	 * @return The formatted text
	 */
	public static String alignLabel(String txt, int width, FontMetrics fm) {
		StringBuffer sb = new StringBuffer(txt);
		int start = 0;
		int stop = 0;
		int hold = 0;
		int offset = 0;

		while(stop < txt.length()) {
			while(fm.stringWidth(txt.substring(start, stop)) < width) {
				if(txt.charAt(stop) == '\n') {
					start = ++stop;
				} else {
					stop++;
				}

				if(stop >= (txt.length() - 1)) {
					break;
				}
			}

			if(stop >= (txt.length() - 1)) {
				break;
			}

			hold = stop;
			while((txt.charAt(stop) != ' ') && (start != stop)) {
				stop--;
			}

			if(start == stop) {
				stop = hold;
			}

			if(sb.charAt(stop + offset) == ' ') {
				sb.insert(stop + offset, "<br>");
			} else {
				sb.insert(stop, "<br>");
				offset++;
			}
			start = ++stop;
		}

		sb.append("<br>");

		return "<html>"+sb.toString()+"</html>";
	}
	
	/**
	 * Displays a dialog that asks the user if he wants to save something into a file
	 * @param comp The mother component
	 * @param text The text of the dialog
	 * @param filter The file filter for the output file
	 * @return The file that has been chosen
	 */
	public static File displaySaveDialog(Component comp, String text, FileFilter filter) {
		File file  = null;
		int answer = JOptionPane.showConfirmDialog(comp, text, "Save into file?", JOptionPane.YES_NO_OPTION);
		if(answer == JOptionPane.YES_OPTION) {
			JFileChooser chooser = new JFileChooser();
			if(filter != null) {
				chooser.setFileFilter(filter);
			}
			int returnVal        = chooser.showSaveDialog(comp);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
			}
		}
		return file;
	}
	
	/**
	 * Opens a given URI in a user defined browser
	 * @param uri URI to be opened
	 * @param frame Owner frame for which it is called
	 */
	public static void showBrowser(URI uri, Frame frame) {
		boolean worked=true;
		try {
			Class<?> desktopClass = Class.forName("java.awt.Desktop");
			Method getDesktop = desktopClass.getMethod("getDesktop");
			Method browse = desktopClass.getMethod("browse", uri.getClass());
			Object desktop = getDesktop.invoke(null);
			browse.invoke(desktop, uri);
		} catch (Exception e) {
			worked = false;
                        e.printStackTrace();
		} catch (Error e) {
			worked = false;
                        e.printStackTrace();
		}
		if (!worked) {
			// could not open browser, did the user specify a browser?
			String browser = Config.getUserbrowser();

			if(!browser.isEmpty()) {
				int answer = JOptionPane.showConfirmDialog(frame, "Could not open browser. Do you want to select " +
						"the binary for your browser manually?", "Could not open browser", JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.YES_OPTION) {

					if (
							JOptionPane
							.showOptionDialog(
									null,
									"Do you want to select the browser executable in a file dialog or enter the command directly?",
									"Binary", JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE, null,
									new String[] { "Select file",
									"Enter command" }, null) == JOptionPane.YES_OPTION) {	
						// let the user select it
						JFileChooser chooser = new JFileChooser();
						int returnVal = chooser.showOpenDialog(frame);
						if(returnVal == JFileChooser.APPROVE_OPTION) {
							File file = chooser.getSelectedFile();
							browser = file.getAbsolutePath();
						} 
					} else {
						String result = JOptionPane.showInputDialog("Browser command (e.g. 'firefox'):");
						if (result != null)
							browser = result;
					}
				}
			}

			// did he select one now?
			if (!browser.isEmpty()) {
				// try to run the browser
				try {
					Runtime.getRuntime().exec(browser + " " + uri.toString());
					Config.setUserbrowser(browser);
				} catch (IOException e1) {
					DisplayWarnings.error("Could not start user-defined browser!", frame);
					Config.setUserbrowser("");
				}
			}
		}
	}
}
