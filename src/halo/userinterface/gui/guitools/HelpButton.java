/*
 * This file is part of the HALO 1.3 graphical user interface
 */
package halo.userinterface.gui.guitools;

import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

/**
 * Provides a button that displays a help dialog upon clicking.
 * @author Jan Krumsiek
 */

public class HelpButton extends JButton {
	
	private static final long serialVersionUID = -2533991156073064622L;
	
	private static ImageIcon icon = new ImageIcon(HelpButton.class.getResource("question.png"), null);
//	private static ImageIcon icon = new ImageIcon("lib/img/question.png");
	
	/**
	 * Constructs a new help button
	 * @param helpText The text that will be displayed after clicking
	 */
	public HelpButton(final String helpText) {
		super(icon);
		setHorizontalAlignment(SwingConstants.LEFT);
		setContentAreaFilled(false);
		setBorder(BorderFactory.createEmptyBorder());
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		final JButton button = this;
		
		addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
			//	JMultilineLabel label = new JMultilineLabel(helpText);
			//	label.setMaxWidth(500);
				String x = wordRap(helpText, 500,  Toolkit.getDefaultToolkit().getFontMetrics(button.getFont()));
				JOptionPane.showMessageDialog(null, x, "HALO help", JOptionPane.INFORMATION_MESSAGE );
			}
		});
	}
	
	/**
	 * Formats the text
	 * @param txt The original text
	 * @param width The width of the line
	 * @param fm The font metrics
	 * @return The formatted text
	 */
	 protected String wordRap(String txt, int width, FontMetrics fm) {
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
	                sb.setCharAt(stop + offset, '\n');
	            } else {
	                sb.insert(stop, '\n');
	                offset++;
	            }
	 
	            start = ++stop;
	        }
	 
	        sb.append('\n');
	 
	        return sb.toString();
	    }


}
