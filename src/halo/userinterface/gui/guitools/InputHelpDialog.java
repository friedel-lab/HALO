/*
 * This file is part of the HALO 1.3 graphical user interface
 */
package halo.userinterface.gui.guitools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

/**
 * Displays a dialog with a message, a text field for user input, a help button and Enter/Cancel buttons
 * @author Stefanie Kaufmann
 *
 */
public class InputHelpDialog extends JDialog {

	private static final long serialVersionUID = 4339266350540945367L;
	private JPanel     panel;
	private JTextField field;
	private boolean    cancel;
	private int        width  = 350;
	private int        height = 150;

	/**
	 * Initializes the dialog frame
	 * @param frame The parent frame
	 * @param title The title of the dialog
	 * @param text The message of the dialog
	 * @param helptext The text of the help-popup
	 */
	public InputHelpDialog(Frame frame, String title, String text, String helptext, String addit) {
		super(frame, true);
		setTitle(title);
		panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx   = 0;
		c.gridy   = 0;
		c.weightx = c.weightx;
		c.weighty = c.weighty;
		c.insets  = new Insets(0,4,4,4);
		JTextArea area = new JTextArea(text);
		area.setBorder(null);
		area.setEditable(false);
		area.setBackground(frame.getBackground());
		panel.add(area, c);
		c.gridy++;
		field    = new JTextField();
		panel.add(field, c);
		if(!addit.isEmpty()) {
			c.gridy++;
			JTextArea secText = new JTextArea(addit);
			secText.setBorder(null); secText.setEditable(false); 
			secText.setBackground(frame.getBackground());
			secText.setBounds(area.getBounds());
			panel.add(secText, c);
		}
		c.gridy++;
		c.anchor = GridBagConstraints.WEST;
		c.fill   = GridBagConstraints.NONE;
		HelpButton help = new HelpButton(helptext);
		panel.add(help, c);
		JButton enter = new JButton("Enter");
		JButton cancel = new JButton("Cancel");
		c.gridy++;
		panel.add(enter,c);
		enter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		c.anchor = GridBagConstraints.EAST;
		panel.add(cancel, c);
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});

		panel.setPreferredSize(panel.getMinimumSize());
		JScrollPane scroll = new JScrollPane(panel);
		scroll.getVerticalScrollBar().setUnitIncrement(10);
		scroll.setPreferredSize(new Dimension(width,height));
		getContentPane().add(scroll,BorderLayout.CENTER);
		Border padding = BorderFactory.createEmptyBorder(10, 5, 0, 0);
		scroll.setBorder(padding);
		// Make this dialog display it.
		int x = (int) (frame.getX()+0.5*(frame.getWidth()-width));
		int y = (int) (frame.getY()+0.5*(frame.getHeight()-height));
		Rectangle bounds = new Rectangle(x,y,5,5);
		setBounds(bounds);

		pack();
		
	}
	
	/**
	 * Cancels the dialog window
	 */
	private void cancel() {
		cancel = true;
		setVisible(false);
	}
	
	/**
	 * Returns true if dialog was canceled
	 * @return TRUE if dialog was canceled
	 */
	public boolean isCancel() {
		return cancel;
	}
	
	/**
	 * Returns the user input
	 * @return The user input
	 */
	public String getInputText() {
		return field.getText();
	}
	
}
