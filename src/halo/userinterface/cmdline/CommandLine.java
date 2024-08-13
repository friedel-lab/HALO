/*
 * This file is part of the HALO 1.3 command line tool. 
 */
package halo.userinterface.cmdline;

import halo.data.biascorrection.RCorrelationCoefficient;

/**
 * Class which forwards information from command line input to further classes which serve as interface between 
 * command line and tools for calculation
 * @author Stefanie Kaufmann
 *
 */
public class CommandLine {

	/**
	 * Allows to print a String[] into a human readable format
	 * @param list Any list in form of an array
	 * @return Array as String
	 */
	public static String formatList(String[] list) {

		StringBuffer result = new StringBuffer("");
		result.append(list[0]);
		for(int i=1; i<list.length; i++) {
			result.append(", "+list[i]);
		}
		return result.toString();
	}

	/**
	 * Shifts the content of a given String array one position to the left, thereby erasing the first element
	 * @param input The parameter list
	 * @return  An array missing the first element of the original input
	 */
	public static String[] shift(String[] input) {
		String[] result = new String[input.length-1];
		for(int i=1; i<input.length; i++) {
			result[i-1] = input[i];
		}
		return result;
	}
	
	/**
	 * Print a detailed description of all available flags for cmdline interface onto the console
	 */
	public static void printHelp() {
		System.out.println();
		System.out.println("This application provides methods for filtering expression data, performing normalization and calculating half-lives");
		System.out.println("For the usage of the different methods type 'commandline.sh' followed by a set of flags:");
		System.out.println("For detailed explanation of the different flags see below.");
		System.out.println("#####################################################################");
		System.out.println("Data extraction and filtering:");
		System.out.println("-i\tinputfile\tA file containing expression data for newly transcribed, pre-existing and total RNA in different columns;" +
				"more than 1 replicate is possible");
		System.out.println("-of\toutputfile\tThe filename for the filtered data");
		System.out.println("-f\tfiltermethod\tOne of several filtering methods that are used as follows: 'method=value'," +
				"e.g. threshold=50. For more methods see documentation. For usage of more than one method add another -f flag.");
		System.out.println("-ct\tcolumn labels\tThe labels of the columns that contain expression data for total RNA;" +
				"it is possible to use only the labels of the wanted replicates. For more than one label please use " +
				"',' as separation");
		System.out.println("-cn\tcolumn labels\tColumn labels for newly transcribed RNA; for details see '-ct'");
		System.out.println("-cp\tcolumn labels\tColumn labels for pre-existing RNA; for details see '-ct'");
		System.out.println("OPTIONAL:");
		System.out.println("-log\tBOOLEAN\tTRUE if your data is in logarithmic scale and values should be loaded\n" +
				"as 2 to the power of [value].");
		System.out.println("-ca\tcolumn labels\tColumn labels for attributes from the original file, separated by ','");
		System.out.println("-pc\tBOOLEAN\tTRUE if attributes with -ca are present/absent calls. \n" +
				"This is necessary to speed up the loading procedure.");
		System.out.println("-ca2\tcolumn labels\tColumn labels for attributes from the original file that \n" +
				"will be loaded separately (e.g. if present calls and other attributes should be loaded from\n" +
				"the file, you should load them separately), separated by ','.");
		System.out.println("-pc2\tBOOLEAN\tTRUE if second list of attributes are present calls");
		System.out.println("-ps\tcolumn labels\tThe label of the column containing the probeset_id; default = 'probeset_id'");
		System.out.println("-cto\tcolumn labels\tThe labels of the columns containing total RNA that will be written into the output file");
		System.out.println("-cno\tcolumn labels\tOutput labels for newly transcribed RNA");
		System.out.println("-cpo\tcolumn labels\tOutput labels for pre-existing RNA");
		System.out.println("-pqs\tfilename\tName of the file in which the quality control values will be written");
		System.out.println("-pp\tBOOLEAN\tTRUE if histogram of probeset quality scores should be created after filtering with " +
				"probeset quality scores.\n");
		System.out.println("-map\tfilename\tName of the file containing more attributes that will be added to the data;" +
				"Structure of this file has to be: probeset ids in the first column, corresponding attribute in the second column." +
				"The first line should describe the columns, e.g.: '#spotid\tattribute1'. You can give multiple attribute files separated " +
				"by comma; e.g. '-map filename1,filename2,filename3");
		System.out.println("-uf\tfilename\tName of the fasta file containing the sequences corresponding to the data");
		System.out.println("-uc\tcolumn number\tNumber of the column of the fasta header that contains the genename; e.g." +
				"'> genename|attribute|attribute' would result in '-uc 1'.");
		System.out.println("-ur\tmethod\tEither 'log(e'/n')', 'log(u'/n')' or 'log(e'/u')'; if -uf, -uc and -ur are given the average " +
				"uracil number and average defined ratio are calculated and" +
				" a file containing information for plotting is provided.");
		System.out.println("-ufo\tfilename\tName of the output file for the plotting information (ratio and uracil" +
				"number).");
		System.out.println("-up\tBOOLEAN\tTRUE if plot about uracil number vs. ratio should be created.\n");
		System.out.println("-genelabel\tcolumn label\tIf your gene label is not 'gene_name' you have to call \n" +
				"this flag with the correct attribute label for genes.");
		System.out.println("-R\tSystem path\tThe path to your R bin directory, which is needed for flags -correl\n" +
				"and -bias.");
		System.out.println("-correl\tmethod\tIf you define this flag, a correlation coefficient will be calculated.\n" +
				"In order for this to work you have to use the -ur, -ufo and -R flag also. Allowed methods:\n" +
				"'"+RCorrelationCoefficient.SPEARMAN+"', '"+RCorrelationCoefficient.PEARSON+"' or '"+
				RCorrelationCoefficient.KENDAL+"'.\n");
		System.out.println("\n");
		System.out.println("Normalization:");
		System.out.println("-l\tmethod\tMethod for normalization; at the moment only the 'standard' method for linear regression is implemented");
		System.out.println("OPTIONAL:");
		System.out.println("-median\tnumerical\tTime for the calculation of the median, based on correction factors calculated with " +
				"Normalization and on data.");
		System.out.println("\nHalf-lives:");
		System.out.println("-h1\tmethod\tFirst method for calculation of half-lives (e.g. 'pre' for pre-existing/total based, " +
				"'new' for newly transcribed/total based or 'new/pre' for newly transcribed/pre-existing based calculations)");
		System.out.println("...");
		System.out.println("-hn\tmethod\tn-th method for half-life calculation");
		System.out.println("-t\ttime\tLabeling time point for which calculation will be done");
		System.out.println("OPTIONAL:");
		System.out.println("-plot\tBOOLEAN\tTRUE if you want to plot the half-lives.");
		System.out.println("-bias\tBOOLEAN\tTRUE if bias correction should be performed and used before half-life calculation.\n" +
		"For this method to work you have to also use the -R, -ur and -ufo flags.");
		System.out.println("-corr\tnumerical\tThe half-life median that will be used for median-based correction factor calculation; " +
				"for '-corr -1' the median will be calculated from your half-life calculations");
		System.out.println("-o\tfilename\tName of the file in which the half-life values will be written");
		System.out.println("-w\tprinting information\tWhich information will be printed: 'halflife', 'ratio' or 'both'");
		System.out.println("-m\tmethods\tThe half-life calculation methods whose results should be included in the output; these " +
				"methods have to be a subset of the previously defined methods for calculation. If no list of methods is " +
				"specified, every calculation will be printed. The methods are called 'pre', 'new' and 'new/pre' (see above); " +
				"for more than one method please separate the methods with commas (e.g. '-m\tpre,new')");
		System.out.println("Please note that -o, -w and -m can only be used together.");
		System.out.println("\nPlease note that all essential flags for filtering (filtering + normalization) except for " +
				"'-of' are also needed for normalization (halfLife)");
	}
	
	/**
	 * Run the program with the parameters given
	 * @param args List of parameters
	 */
	public static void run(String[] args) {
		if(args[0].equals("help")) {
			printHelp();
		} else if(!args[0].startsWith("-")){
			CmdHalfLife.main(shift(args));
		} else {
			CmdHalfLife.main(args);
		}
	}
	
	/**
	 * Main method which is called upon starting the command line input
	 * @param args Parameter list
	 */
	public static void main(String[] args) {
		try {
			//check if there are no arguments
			if(args.length == 0 || args[0].equals("-h") || args[0].equals("help")) {
				printHelp();
			} else {
				run(args);
			}
		} catch (OutOfMemoryError e) {
			System.err.println("Please start the application again with a higher amount of memory.");
		}
		
	}
}
