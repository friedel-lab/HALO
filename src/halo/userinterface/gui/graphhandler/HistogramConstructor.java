/*
 * This file is part of the HALO 1.3 user interface
 */
package halo.userinterface.gui.graphhandler;

import halo.userinterface.gui.guitools.JPEGFilter;

import java.awt.Color;
import java.awt.GradientPaint;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

/**
 * Provides methods for the construction and handling of histograms.
 * @author Stefanie Kaufmann
 *
 */
public class HistogramConstructor {

	private String title;
	private String xlabel;
	private String ylabel;
	private Color  color;
	private double[] values = new double[100];
	private int index = 0;
	private HistogramDataset dataset = new HistogramDataset();
	private JFreeChart chart;
	
	/**
	 * Constructs a new histogram constructor object
	 * @param title The title of the histogram
	 * @param xlabel The name of the x-axis
	 * @param ylabel The name of the y-axis
	 */
	public HistogramConstructor(String title, String xlabel, String ylabel) {
		//set the histogram type so that relative frequency is shown on y-axis
		dataset.setType(HistogramType.RELATIVE_FREQUENCY);
        this.title  = title;
        this.xlabel = xlabel;
        this.ylabel = ylabel;
    }
	
	/**
	 * Adds a new value to the set of values
	 * @param val The new entry
	 */
	public void addValue(double val) {
		if(index >= values.length) {
			values = enlargeArray(values);
		}
		values[index] = val;
		index++;
	}
	
	/**
	 * Adds all values in an array to the set of values
	 * @param val The array with all new entries
	 */
	public void addValues(double[] val) {
		while((val.length + index) >= values.length) {
			values = enlargeArray(values);
		}
		
		for(int i=0; i< val.length; i++) {
			values[index] = val[i];
			index++;
		}
	}
	
	/**
	 * Makes an array larger by factor 2
	 * @param array Original array
	 * @return Array twice as large, with half empty values
	 */
	private double[] enlargeArray(double[] array) {
		double[] temp = new double[array.length * 2];
		System.arraycopy(array, 0, temp, 0, array.length);
		return temp;
	}
	
	/**
	 * Adds a complete data series to the graph
	 * @param title Name of the series
	 * @param val Value entries
	 * @param bins Number of bins
	 * @param start Lower x-axis limitation
	 * @param stop Upper x-axis limitation
	 */
	public void addSeries(String title, double[] val, int bins, double start, double stop) {
		dataset.addSeries(title, val, bins, start, stop);
	}
	
	/**
	 * Saves the plotting information of the histogram
	 * @param output The output destination
	 */
	public void savePlotFile(File output) {
		try {
			BufferedWriter op = new BufferedWriter(new FileWriter(output));
			HashMap<Double,ArrayList<Double>> list = new HashMap<Double,ArrayList<Double>>();
			//iterate over all series
			for(int i=0; i<dataset.getSeriesCount(); i++) {
				//iterate over all items in the current series
				for(int j=0; j<dataset.getItemCount(i); j++) {
					double x = dataset.getXValue(i, j);
					double y = dataset.getYValue(i, j);
					//save x and corresponding y values in a list that holds x and y for ALL series
					if(list.containsKey(x)) {
						ArrayList<Double> temp = list.get(x);
						//replace former y-value for this series with actual one
						temp.remove(i);
						temp.add(i, y);
						list.put(x, temp);
						//if there is no such list yet, generate one and initialize every y-value with 0
					} else {
						ArrayList<Double> temp = new ArrayList<Double>();
						for(int k=0; k<dataset.getSeriesCount(); k++) {
							temp.add(0.0);
						}
						temp.remove(0);
						temp.add(0, y);
						list.put(x, temp);
					}
				}
			}
			//iterate over all x-y lists and print to file
			Iterator<Double> iterator = list.keySet().iterator();
			while(iterator.hasNext()) {
				double x = iterator.next();
				ArrayList<Double> temp = list.get(x);
				op.write(String.valueOf(x));
				for(Double item : temp) {
					op.write("\t"+item);
				}
				op.write("\n");
			}
			op.flush();
			op.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves the graph as a JPEG picture
	 * @param output The output file where it will be saved
	 */
	public void saveGraph(File output) {
		try {
			//before saving check if ending is accepted
			String fileName = output.getAbsolutePath();
			String[] name   = fileName.split("\\.");
			String ending = name[name.length-1];
			ArrayList<String> acceptedEndings = new JPEGFilter().getAcceptedEndings();
			//if ending is not accepted or there is no ending generate new file with accepted ending
			if(!acceptedEndings.contains(ending)) {
				StringBuffer makeName = new StringBuffer("");
				for(int i=0; i<name.length-1; i++) {
					makeName.append(name[i]);
				}
				if(name.length == 1) {
					makeName.append(name[name.length-1]);
				}
				makeName.append(".jpg");
				fileName = makeName.toString();
				output = new File(fileName);
			}
			//save chart as jpg
			ChartUtilities.saveChartAsJPEG(output, chart, 500, 300);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates a graph from the given series
	 */
	public void generateGraph() {
        chart = ChartFactory.createHistogram(
            title,
            xlabel,
            ylabel,
            dataset, 
            PlotOrientation.VERTICAL, 
            true, 
            false, 
            false
        );
        chart.getXYPlot().setForegroundAlpha(0.5f);
        chart.getXYPlot().setBackgroundAlpha(0.5f);
        
        if(color != null) {
        	XYBarRenderer renderer = (XYBarRenderer) chart.getXYPlot().getRenderer();
            // set up gradient paints for series
            GradientPaint gp0 = new GradientPaint(
            0.0f, 0.0f, color,
            0.0f, 0.0f, Color.lightGray
            );
            renderer.setSeriesPaint(0, gp0);
        }
        
        ChartFrame frame = new ChartFrame(title, chart);
		frame.pack();
		frame.setVisible(true);
    }
	
	public void setColor(Color color) {
		this.color = color;
	}
}
