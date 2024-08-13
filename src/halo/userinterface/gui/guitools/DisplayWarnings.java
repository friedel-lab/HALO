/*
 * This file is part of the HALO 1.3 graphical user interface
 */
package halo.userinterface.gui.guitools;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * Shows warning and error dialogs with a given text message
 * @author Stefanie Kaufmann
 *
 */
public class DisplayWarnings {

	/**
	 * Shows a warning dialog
	 * @param text The warning text that is displayed
	 * @param gui The component in which the dialog will be opened
	 */
	public static void warning(String text, Component gui) {
		JOptionPane.showMessageDialog(gui, text, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	/**
	 * Shows an error dialog
	 * @param text The error text that is displayed
	 * @param gui The component in wich the dialog will be opened
	 */
	public static void error(String text, Component gui) {
		JOptionPane.showMessageDialog(gui, text, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
