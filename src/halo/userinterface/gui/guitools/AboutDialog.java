/*
 * This file is part of the HALO 1.3 graphical user interface
 */
package halo.userinterface.gui.guitools;

import halo.tools.Tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Displays a dialog with information about HALO
 * @author Jan Krumsiek
 */
public class AboutDialog extends JFrame  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8007797170913184546L;
	private Frame owner;
	
	public AboutDialog(Frame owner) {
		
		super("About HALO");
		
		this.owner = owner;
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		initializeGUI();
		setResizable(false);
		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
		
	}
	
	private void initializeGUI() {
		JPanel pane = new JPanel(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx=0;
		c.gridy=0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5 ,5,2,5);
		
		// name of the program and version
		c.gridwidth = 2;
		pane.add(boldLabel(Tools.LIBRARYNAME), c);
		c.insets = new Insets(0 ,5,2,5);
		c.gridy++;
		pane.add(normalLabel("Half-life Organizer"), c);
		c.gridy++;
		pane.add(normalLabel("Version: " + Tools.VERSION),c);
		c.gridy++;
		pane.add(new JLabel(" "),c);
		c.gridy++;
		
		// contact and web
		c.gridwidth = 1;
		c.gridx=0;
		c.gridy++;
		pane.add(boldLabel("Web:"),c);
		c.gridx=1;
		final JLabel web; 
		pane.add(web = linkLabel(Tools.HOMEPAGE),c);
		
		// close button
		c.gridwidth = 2;
		c.gridx=0;
		c.gridy++;
		c.insets = new Insets(7 ,5,6,5);
		c.anchor = GridBagConstraints.EAST;
		final JButton close;
		pane.add(close = new JButton("Close"),c);
		
		web.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try {
					GuiTools.showBrowser(new URI(web.getText()), owner);
				} catch (URISyntaxException e1) {
                                   e1.printStackTrace();
                                }
			}
		});
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		
		
		
		
		setContentPane(pane);
	}
	
	private void close() {
		dispose();
	}
	
	private JLabel normalLabel(String text) {
		JLabel label = new JLabel(text);
		Font font = label.getFont();
		label.setFont(new Font(font.getName(), Font.PLAIN, font.getSize()));
		return label;
	}
	
	private JLabel boldLabel(String text) {
		JLabel label = new JLabel(text);
		Font font = label.getFont();
		label.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
		return label;
	}

	private JLabel linkLabel(String text) {
		JLabel label = new JLabel(text);
		Font font = label.getFont();
		label.setFont(new Font(font.getName(), Font.PLAIN, font.getSize()));
		label.setForeground(Color.BLUE);
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return label;
	}
	
	
	public static void main(String[] args) {
		new AboutDialog(null);
	}
	
	

}
