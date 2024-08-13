/*
 * This file is part of HALO version 1.3. 
 */
package halo.userinterface.gui.guitools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

/**
 * Provides a new dialog window that displays a list of components and adds scroll bars if necessary
 * @author Stefanie Kaufmann
 *
 */
public class LabelChooserDialog extends JDialog implements PropertyChangeListener {

	private static final long serialVersionUID = -3181668702954972979L;
	private ArrayList<Component> list;
	private JPanel panel;
	private boolean cancel = false;

	/**
	 * Constructs a new dialog
	 * @param aFrame The mother frame
	 * @param title The title of the dialog window
	 * @param message The first line of the dialog
	 * @param list The list of components that will be displayed below
	 */
	public LabelChooserDialog(Frame aFrame, String title, String message, ArrayList<Component> list) {
		super(aFrame, true);
		this.list = list;
		setTitle(title);
		panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		JScrollPane scroll = new JScrollPane(panel);
		scroll.getVerticalScrollBar().setUnitIncrement(10);
		int minHeight = list.size()*40+50;
		scroll.setPreferredSize(new Dimension(400,Math.min(650, minHeight)));
		scroll.setMinimumSize(new Dimension(100,100));
		getContentPane().add(scroll,BorderLayout.CENTER);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx   = 0;
		c.gridy   = 0;
		c.weightx = c.weightx;
		c.weighty = c.weighty;
		c.insets  = new Insets(0,4,4,4);
		int max   = 0;
		JLabel label = new JLabel(message);
		if(message.length() > max) {
			max = message.length();
		}
		Font font = label.getFont();
		JTextArea area;
		if(message.contains("_") && !message.contains("\\_")) {
			String[] mess = message.split("_");
			message = mess[0];
			String emphasize = mess[1];
			area = new JTextArea(message);
			JTextArea emph = new JTextArea(emphasize);
			emph.setForeground(Color.red);
			emph.setBackground(aFrame.getBackground());
			emph.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
			panel.add(area, c);
			c.gridy++;
			panel.add(emph, c);
		} else {
			area = new JTextArea(message.replace("\\_", "_"));
			panel.add(area, c);
		}
		area.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
		area.setBorder(null);
		area.setEditable(false);
		area.setBackground(aFrame.getBackground());
//		panel.add(area, c);
		c.gridy++;
		for(Component comp : list) {
			panel.add(comp, c);
			if(comp instanceof JCheckBox) {
				int length = ((JCheckBox)comp).getText().length();
				if(length > max) {
					max = length;
				}
			} else if(comp instanceof JRadioButton) {
				int length = ((JRadioButton)comp).getText().length();
				if(length > max) {
					max = length;
				}
			}
			c.gridy++;
		}
		c.anchor = GridBagConstraints.WEST;
		c.fill   = GridBagConstraints.NONE;
		JButton enter = new JButton("Enter");
		JButton cancel = new JButton("Cancel");
		c.gridy++;
		panel.add(enter,c);
		if(max <= (enter.getText().length()+cancel.getText().length()+10)) {
			c.gridx++;
		}
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
				close();
			}
		});

		panel.setPreferredSize(panel.getMinimumSize());
		
		Border padding = BorderFactory.createEmptyBorder(10, 5, 0, 0);
		scroll.setBorder(padding);
		// Make this dialog display it.
		int x = (int)(aFrame.getX()+0.5*(aFrame.getWidth()-400));
		int y = (int)(aFrame.getY()+0.5*(aFrame.getHeight()-Math.min(650, minHeight)));
		Rectangle bounds = new Rectangle(x,y,50,50);
		setBounds(bounds);

		pack();

		// Register an event handler that reacts to option pane state changes.
		panel.addPropertyChangeListener(this);
	}

	/**
	 *  Reacts to state changes in the panel. 
	 */
	public void propertyChange(PropertyChangeEvent e) {
		//	    String prop = e.getPropertyName();
		//do nothing
	}

	/**
	 * Hides the dialog window
	 */
	public void close() {
		setVisible(false);
		cancel = true;
	}
	
	/**
	 * Returns the cancel status of the dialog procedure
	 * @return TRUE if cancel was pressed
	 */
	public boolean isCancel() {
		return cancel;
	}

	/**
	 * Returns the list of components
	 * @return The list of components
	 */
	public ArrayList<Component> getList() {
		return list;
	}

}
