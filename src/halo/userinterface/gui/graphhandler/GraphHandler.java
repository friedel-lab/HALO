/*
 * This file is part of the HALO 1.3 graphical user interface
 */
package halo.userinterface.gui.graphhandler;

import halo.data.Data;
import halo.data.Mapping;
import halo.halflife.HalfLife;
import halo.normalization.Normalization;
import halo.userinterface.gui.Gui;
import halo.userinterface.gui.guitools.GuiTools;
import halo.userinterface.gui.guitools.JPEGFilter;

import java.awt.Color;
import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.filechooser.FileFilter;

/**
 * Provides methods for the generation and handling of graphs created with JFreeChart.
 * @author Stefanie Kaufmann
 *
 */
public class GraphHandler {

	/**
	 * Generates the plot for normalization
	 * @param norm The normalization object
	 * @param data The raw data
	 * @return The constructor for the graph
	 */
	public static XYGraphConstructor plotNormalization(Normalization norm, Data data) {
			XYGraphConstructor normGraphConstructor = new XYGraphConstructor("Normalization results", "newly transcribed/total", "pre-existing/total");
			ArrayList<String> titles            = new ArrayList<String>();
//			titles.add("Regression line");
			titles.add("Data points");
			normGraphConstructor.addEmptySeries(titles);

			double alpha = norm.getAlpha();
			double beta  = norm.getBeta();
			double[] et  = data.calculateRatio(data.getNewRNA(), data.getTotalRNA(), data.getCorrNewTot());
			double[] ut  = data.calculateRatio(data.getPreRNA(), data.getTotalRNA(), data.getCorrPreTot());
			Mapping<String,Integer> newMap = data.calculateNewMapping();
			Iterator<String> iterator = newMap.iterate();
			double max   = 0;
			while(iterator.hasNext()) {
				String id  = iterator.next();
				int index  = newMap.getAttribute(id);
				ArrayList<Double> y = new ArrayList<Double>();
//				double normal = alpha+beta*et[index];
//				y.add(normal);
				y.add(ut[index]);
				normGraphConstructor.addData(et[index], y);
				if(et[index] > max) {
					max = et[index];
				}
			}
			System.out.println("Data loaded for graph construction...");
			normGraphConstructor.addEmptySeries("Regression line");
			for(double i=0; i<max; i+=0.001) {
//				if(!normGraphConstructor.getSeries().contains(i)) {
					double line = alpha+beta*i;
//					ArrayList<Double> y = new ArrayList<Double>();
//					y.add(line); y.add(Double.NaN);
//					normGraphConstructor.addData(i, y);
//				}
				normGraphConstructor.addData(i,line);
			}
			
			boolean[] points = {false, true};
//			boolean[] points = {true, false};
			normGraphConstructor.setPoints(points);
		normGraphConstructor.generateGraph();
		return normGraphConstructor;
	}
	
	/**
	 * plots half-lives as histograms, each half-life calculation in one graph
	 * @param lives List of half-life calculations
	 * @param time List of corresponding labeling times
	 * @param gui Parent frame
	 */
	public static void plotHalfLifeHisto(List<HalfLife> lives, List<Double> time, Frame gui) {
		//one histogram contains all plots for the printing of plotting information
		HistogramConstructor hist = new HistogramConstructor("Distribution of half-lives", 
				"Half-life", "relative frequency");
		//provide several different colors for the different plots
		ArrayList<Color> colors = new ArrayList<Color>();
		colors.add(Color.red); colors.add(Color.blue); colors.add(Color.green);
		colors.add(Color.cyan); colors.add(Color.magenta); colors.add(Color.orange);
		double max = 0;
		int i      = 0;
		//determine the overall maximal half-life value
		for(HalfLife life : lives) {
			if(life.getMax() > max) {
				max = life.getMax();
			}
		}
		for(HalfLife life : lives) {
			//generate a new graph for this half-life
			HistogramConstructor subhist = new HistogramConstructor("Distribution of half-lives: "+life.getName(), 
					"Half-life", "relative frequency");
			double[] hwz = life.getHwz();
			//add series to overall plot and to new plot
			hist.addSeries(life.getName()+", t="+time.get(i), hwz, 50, 0, max+0.1);
			subhist.addSeries(life.getName()+", t="+time.get(i), hwz, 50, 0, life.getMax()+0.1);
			//if colors are not enough for all the half-life calculations, start using them again
			if(i >= colors.size()) {
				colors.addAll(colors);
			}
			subhist.setColor(colors.get(i));
			i++;
			//generate single graph
			subhist.generateGraph();
			if(gui != null) {
				//allow user to save this graph
				FileFilter filter = new JPEGFilter();
				File output = GuiTools.displaySaveDialog(gui, "Do you want to save the half-life graph '"+life.getName()+"'?", filter);
				if(output != null) {
					subhist.saveGraph(output);
				}
			}
		}
		if(gui != null) {
			//allow user to save overall plotting information
			File plotFile = ((Gui)gui).savePlottingFileDialog();
			if(plotFile != null) {
				hist.savePlotFile(plotFile);
			}
		}
	}
	
}
