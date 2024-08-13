/*
 * This file is part of the HALO 1.3 user interface
 */
package halo.userinterface.gui.graphhandler;

import halo.tools.Tuple;
import halo.userinterface.gui.guitools.JPEGFilter;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


/**
 * Class for the generation and displaying of an XY graph with JFreeChart
 * Provides methods for generating a plot from a file, but also for direct adding of number pairs 
 * or a whole list of number pairs
 * @author Stefanie Kaufmann
 *
 */
public class XYGraphConstructor {
	
	//TODO: graph generation geschwindigkeit
	
	private ArrayList<XYSeries> series;
	private JFreeChart          chart;
	private String              title;
	private String              xlabel;
	private String              ylabel;
	private boolean[] points;

	private boolean showPoints = false;
	private boolean showLegend = true;
	
	/**
	 * Constructs an empty graph constructor with a given title and axis labels
	 * @param title Title of the graph
	 * @param xlabel Title of the x-axis
	 * @param ylabel Title of the y-axis
	 */
	public XYGraphConstructor(String title, String xlabel, String ylabel) {
		this.title      = title;
		this.xlabel     = xlabel;
		this.ylabel     = ylabel;
		this.series     = new ArrayList<XYSeries>();
	}
	
	/**
	 * Reads in data from a given file and prepares it for plotting
	 * Structure of the input file has to be tabular, with first column giving 
	 * the x-axis value, other columns giving corresponding y-axis values
	 * @param dataSource A file containing data to be plotted
	 */
	public void readInData(File dataSource) {
		try {
			BufferedReader source = new BufferedReader(new FileReader(dataSource));
			String line;
			boolean head = false;
			
			while((line = source.readLine()) != null) {
				//is there a commentary explaining which column contains what?
				if(line.startsWith("#")) {
					String[] header   = line.trim().split("\t");
					//create a number of series with the titles from the column labels
					for(int i=1; i<header.length; i++) {
						XYSeries ser  = new XYSeries(header[i]);
						series.add(ser);
					}
					head = true;
				//is there no such commentary?
				} else if(!line.startsWith("#") && !head) {
					//the number of series has to be one less than columns, because the first column holds no y-values
					int number        = line.trim().split("\t").length-1;
					//create a number of series with numerical titles to match the number of columns
					for(int i=0; i<number; i++) {
						XYSeries ser = new XYSeries(i);
						series.add(ser);
					}
					//if default titles are used, don't display the legends in the plot
					showLegend = false;
				//rest is data
				} else if(!line.isEmpty()){
					String[] splitted = line.trim().split("\t");
					//the first column holds the x-value
					double x          = Double.parseDouble(splitted[0]);
					//generate pairs with x and all y-values
					for(int i=1; i<splitted.length; i++) {
						double y      = Double.parseDouble(splitted[i]);
						series.get(i-1).add(x,y);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a list of numerical pairs to the graph
	 * @param list A list of numerical pairs/tuples
	 * @param title The title of the list
	 */
	public void addSeries(ArrayList<Tuple<Double,Double>> list, String title) {
		XYSeries ser = new XYSeries(title);
		for(Tuple<Double,? extends Number> tuple : list) {
			ser.add(tuple.getFirst(), tuple.getSecond());
		}
		series.add(ser);
	}
	
	/**
	 * Adds one single numerical pair to the graph, but 
	 * only if there is only one series of data; this 
	 * method should be used after 'addEmptySeries' 
	 * to ensure no messing up of data that does not belong together
	 * @param x The x-axis value
	 * @param y The y-axis value
	 */
	public void addData(double x, double y) {
		if(series.size() == 1) {
			series.get(0).add(x, y);
		} else {
			series.get(series.size()-1).add(x,y);
//			System.err.println("You've got more than one series in your graph; please use the method 'addSeries' " +
//					"to add a complete list of data.");
		}
	}
	
	/**
	 * Adds a number of numerical (x,y) pairs to the graph (scatterplot) , but 
	 * only if the number is the same as the number of existing 
	 * series; this method should be used after 'addEmptySeries' 
	 * to ensure no messing up of data that does not belong together
	 * @param x A single x-value
	 * @param y A set of corresponding y-values
	 */
	public void addData(double x, ArrayList<Double> y) {
		//check if update size matches size of existing series
		if(series.size() == y.size()) {
			int index = 0;
			//add all pairs of (x,y) to the existing series
			for(XYSeries ser : series) {
				ser.add(x, y.get(index));
				index++;
			}
		} else {
			System.err.println("The number of series you would like to add data to does not \n" +
					"agree with the existing number of series!");
		}
	}
	
	/**
	 * Creates a number of empty series and adds them to those yet existing
	 * @param titles A number of titles for the empty series
	 */
	public void addEmptySeries(ArrayList<String> titles) {
		for(String title : titles) {
			XYSeries ser = new XYSeries(title);
			ser.setDescription(title);
			series.add(ser);
		}
	}
	
	/**
	 * Creates one empty series and adds them to the existing ones
	 * @param title The title of this series
	 */
	public void addEmptySeries(String title) {
		XYSeries ser = new XYSeries(title);
		ser.setDescription(title);
		series.add(ser);
	}
	
	/**
	 * Generates a graph from the loaded data and displays it
	 */
	public void generateGraph() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		//add the series to the dataset
//		for(XYSeries ser : series) {
//			dataset.addSeries(ser);
//		}
		for(int i=series.size()-1; i>=0; i--) {
			dataset.addSeries(series.get(i));
		}
		
		
		//Generate the graph
		chart = ChartFactory.createXYLineChart(title, xlabel, ylabel, dataset, PlotOrientation.VERTICAL, showLegend, //Show legend
				true, //Use tooltips
				false); //configure chart to generate URLs?
//		chart.setAntiAlias(false);
		System.out.println("Graph generated.");
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setForegroundAlpha(0.75f); //change transparency of foreground
		plot.setBackgroundAlpha(0.5f); //change transparency of background
		
		//change line style if wanted
		if(showPoints) {
	        
	        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

	        for(int i=0; i<points.length; i++) {
	        	boolean pt = points[i];
	        	if(pt) {
	        		renderer.setSeriesLinesVisible(i, false);
	        		renderer.setSeriesShapesFilled(i, false);
	        	} else {
	        		//shapes are drawn in a second step and thus overlay every previously drawn line; fake a line made of shapes
	        		renderer.setSeriesShapesVisible(i, true);
	        		renderer.setSeriesLinesVisible(i, true);
	        		renderer.setSeriesStroke(0, new BasicStroke(0.5f));
	        		Shape shape = new Ellipse2D.Double(0,0,0.005,0.005);
	        		renderer.setSeriesShape(i,shape);
	        	}
	        }
	        plot.setRenderer(renderer);
		}
		//display the graph
		ChartFrame frame = new ChartFrame(title, chart);
		frame.pack();
		frame.setVisible(true);
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
	 * Generates a plotting file with tab separations
	 */
	public void savePlotFile(File output) {
		try {
			BufferedWriter outBuff = new BufferedWriter(new FileWriter(output));
			
			for(int i=0; i<series.size(); i++) {
				String descr = series.get(i).getDescription();
				outBuff.write("#"+descr+":x\ty\n");
				for(int j=0; j<series.get(i).getItemCount(); j++) {
					XYDataItem item = series.get(i).getDataItem(j);
					outBuff.write(item.getXValue()+"\t"+item.getYValue()+"\n");
				}
			}
			outBuff.flush();
			outBuff.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the point definitions
	 * @param points An array defining which series will be printed as points (TRUE) and which as lines (FALSE)
	 */
	public void setPoints(boolean[] points) {
		this.showPoints = true;
		this.points = points;
	}
	
	/**
	 * Returns the list of series
	 * @return The list of series
	 */
	public ArrayList<XYSeries> getSeries() {
		return series;
	}
}
