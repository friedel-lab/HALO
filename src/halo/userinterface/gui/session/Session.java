/*
 * This file is part of the HALO 1.3 graphical user interface
 */
package halo.userinterface.gui.session;

import halo.data.Data;
import halo.halflife.alpha.Alpha;
import halo.halflife.alpha.AlphaCellDivision;
import halo.halflife.alpha.AlphaConstant;
import halo.normalization.CorrectionFactors;
import halo.normalization.Normalization;
import halo.tools.Tools;
import halo.userinterface.gui.GuiFilterData;
import halo.userinterface.gui.GuiHalfLife;
import halo.userinterface.gui.GuiNormal;
import halo.userinterface.gui.guitools.DisplayWarnings;

import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Contains all information of a current session, as well as methods for saving and loading
 * @author Stefanie Kaufmann
 *
 */
public class Session {
	
	//TODO: gene label wird nicht richtig gespeichert
	//TODO: ArrayIndexOutOfBoundsException: -1 bei Data: 1234
	//TODO: mehrere attribute aus originalfile laden mit unterschiedlichen calls

	public static final String ALPHACONSTANT = "alphaConstant";
	public static final String ALPHACELLDIV  = "alphaCellDiv";
	
	private File file;
	private int posX;
	private int posY;
	private boolean maxim;
	private final String DEFAULT = Config.CONFIGPATH+"Halo_temp.sess";

	private File dataFile = null;
	private Data data;
	private List<String> columnsNew      = new ArrayList<String>();
	private List<String> columnsPre      = new ArrayList<String>();
	private List<String> columnsTotal    = new ArrayList<String>();
	private List<String> dataMethods     = new ArrayList<String>();
	private List<String> attributeFiles  = new ArrayList<String>();
	private List<String> orgLabelsCalls  = new ArrayList<String>();
	private List<String> orgLabelsOther  = new ArrayList<String>();
	private ArrayList<ArrayList<String>> attributeLabels = new ArrayList<ArrayList<String>>();
	private ArrayList<String> labels     = new ArrayList<String>();
	private ArrayList<String> attributes = new ArrayList<String>();
	private String[]      attrDescr;
	private StringBuffer  display   = new StringBuffer("");
	private GuiFilterData guiData;
	private GuiNormal     guiNorm;
	private String        fasta     = "";
	private String        geneLabel = Data.GENEDEFAULT;
	private int           column;
	private int           ratioMethod;
	private int           replicate = Normalization.UNDEF;
	private String        pathR = "";
	private double        labelingTime = Normalization.UNDEF;
	private String        normMethod  = "";
	private String        alpha = ALPHACONSTANT;
	private double        ccl = 0;
//	private String        spotid;
	private GuiHalfLife   guiHL;
	private List<String>  hlMethods = new ArrayList<String>();
	private List<Double> times      = new ArrayList<Double>();
	private List<Integer> replicates = new ArrayList<Integer>();
	private boolean log = false;
	private boolean checkFilt;
	private boolean checkNorm;
	private boolean checkHL;
	private boolean checkThresh;
	private boolean checkAbs;
	private boolean checkPQS;
	private boolean checkPQSMin;
	private boolean calls = false;
	private boolean plotQuality = false;

	/**
	 * Constructs a new session
	 * @param file The file into which the session will be saved
	 */
	public Session(File file) {
		this.file = file;
	}

	/**
	 * Constructs a new session with a default file name
	 */
	public Session() {
		this.file = new File(DEFAULT);
	}
	

	
	/**
	 * Transforms a list into a string, different entries divided by comma
	 * @param list The list which will be turned into a string
	 * @return The list as string
	 */
	private String printList(List<String> list) {
		StringBuffer result = new StringBuffer("");
		for(String item : list) {
			result.append(item+",");
		}
		return result.substring(0,result.length()-1).toString();
	}
	
	/**
	 * Transforms a list of numbers into a string
	 * @param list List which will be turned into a string
	 * @return The list as string
	 */
	private String printListInt(List<? extends Number> list) {
		StringBuffer result = new StringBuffer("");
		for(Number item : list) {
			result.append(String.valueOf(item)+",");
		}
		return result.substring(0,result.length()-1).toString();
	}
	
	
	/**
	 * Stores the session in the previously defined file
	 */
	public void store() {
		try {
			String name = file.getAbsolutePath();
			file.delete();
			BufferedWriter buff = new BufferedWriter(new FileWriter(new File(name)));
			buff.write("################HALO version "+Tools.VERSION+"#################\n");
			buff.write("x:"+posX+"\n");
			buff.write("y:"+posY+"\n");
			buff.write("max:"+maxim+"\n");
			
			if(dataFile != null) {
				buff.write("datafile:"+dataFile.getAbsolutePath()+"\n");
				buff.write("columnsNew:"+printList(columnsNew)+"\n");
				buff.write("columnsPre:"+printList(columnsPre)+"\n");
				buff.write("columnsTotal:"+printList(columnsTotal)+"\n");
				buff.write("labels:"+printList(labels)+"\n");
				buff.write("ratioMethod:"+ratioMethod+"\n");
			}
			if(normMethod.length() > 0) {
				buff.write("normMethod:"+normMethod+"\n");
			}
			if(hlMethods.size() > 0) {
				buff.write("hlMethods:"+printList(hlMethods)+"\n");
				buff.write("times:"+printListInt(times)+"\n");
				buff.write("replicates:"+printListInt(replicates)+"\n");
				buff.write("display:"+display.toString()+"\n");
			}
			if(dataMethods.size()>0) {
				buff.write("dataMethods:"+printList(dataMethods)+"\n");
			}
			buff.write("checkFilt:"+checkFilt+"\n");
			buff.write("checkAbs:"+checkAbs+"\n");
			buff.write("checkHL:"+checkHL+"\n");
			buff.write("checkNorm:"+checkNorm+"\n");
			buff.write("checkPQS:"+checkPQS+"\n");
			buff.write("checkPQSMin:"+checkPQSMin+"\n");
			buff.write("checkThresh:"+checkThresh+"\n");
			buff.write("spotid:"+data.getSpotid()+"\n");
			buff.write("genelabel:"+geneLabel+"\n");
			buff.write("alpha:"+alpha+"\n");
			buff.write("log:"+log+"\n");
			buff.write("plotQuality:"+plotQuality+"\n");
			if(alpha.equals(ALPHACELLDIV)) {
				buff.write("="+ccl+"\n");
			}
			
			if(!pathR.isEmpty()) {
				buff.write("pathR:"+pathR+"\n");
			}
			if(replicate != Normalization.UNDEF) {
				buff.write("replicate:"+replicate+"\n");
			}
			if(labelingTime != Normalization.UNDEF) {
				buff.write("labelingTime:"+labelingTime+"\n");
			}
			if(orgLabelsCalls.size() > 0) {
				buff.write("originalsCalls:"+printList(orgLabelsCalls)+"\n");
			}
			if(orgLabelsOther.size() > 0) {
				buff.write("originalsOthers:"+printList(orgLabelsOther)+"\n");
			}
			if(!fasta.isEmpty()) {
				buff.write("fasta:"+fasta+"\n");
				buff.write("column:"+column+"\n");
			}
			if(attributeFiles.size() > 0) {
				buff.write("attributes:"+printList(attributeFiles)+"\n");
				buff.write("attributeLabels:"+printList(attributes)+"\n");
				buff.write("attrDescr:");
				for(String desc : attrDescr) {
					buff.write(desc+",");
				}
				buff.write("\n");
			}
			buff.flush();
			buff.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads a session from a given file
	 * @param sessFile File containing session information
	 * @return TRUE if the sessions file has been loaded successfully
	 */
	public boolean load(File sessFile) {
		try {
			BufferedReader buff = new BufferedReader(new FileReader(sessFile));
			String line;
			String spotid = "";
			
			while((line = buff.readLine()) != null) {
				String[] content = line.split(":");
				if(line.startsWith("x:")) {
					this.posX = Integer.parseInt(content[1]);
				} else if(line.startsWith("y:")) {
					this.posY = Integer.parseInt(content[1]);
				} else if(line.startsWith("max:")) {
					this.maxim = Boolean.parseBoolean(content[1]);
				} else if(line.startsWith("calls:")) {
					this.calls = Boolean.parseBoolean(content[1]);
				} else if(line.startsWith("log:")) { 
					this.log = Boolean.parseBoolean(content[1]);
				} else if(line.startsWith("datafile")) {
					StringBuffer cont = new StringBuffer("");
					//if a ':' is part of the path, reconstruct it after splitting
					for(int i=1; i<content.length; i++) {
						cont.append(content[i]+":");
					}
					//if data file address is URL, download file
					if(cont.toString().startsWith("http:")) {
						System.out.println("Downloading data file.");
						String name = Config.CONFIGDIR+File.separator+"data.txt";
						File data = new File(name);
						this.dataFile = downloadFile(data, cont.toString());
                                                
                                                if(this.dataFile==null) {
                                                        DisplayWarnings.error(
                                                                "Data file could not be downloaded to your local file system.\n " +
                                                                "Please download the data file manually and replace the web address\n" +
                                                                "in the session file by the local address of the downloaded file. ", new Frame());
                                                        return false;
                                                }
						
					//else generate file
					} else {
						this.dataFile = new File(cont.substring(0, cont.length()-1));
					}
				} else if(line.startsWith("genelabel:")) { 
					geneLabel = content[1];
				} else if(line.startsWith("plotQuality:")) { 
					plotQuality = Boolean.parseBoolean(content[1]);
				} else if(line.startsWith("spotid")) {
					spotid = content[1];
				} else if (line.startsWith("columnsNew")) {
					String[] content2 = content[1].split(",");
					for(String item : content2) {
						columnsNew.add(item);
					}
				} else if(line.startsWith("columnsPre")) {
					String[] content2 = content[1].split(",");
					for(String item : content2) {
						columnsPre.add(item);
					}
				} else if(line.startsWith("columnsTotal")) {
					String[] content2 = content[1].split(",");
					for(String item : content2) {
						columnsTotal.add(item);
					}
				} else if(line.startsWith("originalsCalls")) {
					String[] content2 = content[1].split(",");
					for(String item : content2) {
						orgLabelsCalls.add(item);
					}
				
				} else if(line.startsWith("originalsOthers:")) {
					String[] content2 = content[1].split(",");
					for(String item : content2) {
						orgLabelsOther.add(item);
					}
				} else if(line.startsWith("labels")) {
					String[] content2 = content[1].split(",");
					for(String item : content2) {
						labels.add(item);
					}
				} else if(line.startsWith("display")) {
					display = new StringBuffer(content[1]);
				} else if(line.startsWith("normMethod")) {
					normMethod = content[1];
				} else if(line.startsWith("ratioMethod")) {
					ratioMethod = Integer.parseInt(content[1]);
				} else if(line.startsWith("hlMethods")) {
					String[] content2 = content[1].split(",");
					for(String item : content2) {
						hlMethods.add(item);
					}
				} else if(line.startsWith("times")) {
					String[] content2 = content[1].split(",");
					for(String item : content2) {
						times.add(Double.parseDouble(item));
					}
				} else if(line.startsWith("dataMethods")) {
					String[] content2 = content[1].split(",");
					for(String item : content2) {
						dataMethods.add(item);
					}
				} else if(line.startsWith("checkAbs")) {
					checkAbs = Boolean.parseBoolean(content[1]);
				} else if(line.startsWith("checkFilt")) {
					checkFilt = Boolean.parseBoolean(content[1]);
				} else if(line.startsWith("checkHL")) {
					checkHL = Boolean.parseBoolean(content[1]);
				} else if(line.startsWith("checkNorm")) {
					checkNorm = Boolean.parseBoolean(content[1]);
				} else if(line.startsWith("checkPQS")) {
					checkPQS = Boolean.parseBoolean(content[1]);
				} else if(line.startsWith("checkPQSMin")) {
					checkPQSMin = Boolean.parseBoolean(content[1]);
				} else if(line.startsWith("checkThresh")) {
					checkThresh = Boolean.parseBoolean(content[1]);
				} else if(line.startsWith("replicates")) {
					String[] content2 = content[1].split(",");
					for(String item : content2) {
						replicates.add(Integer.parseInt(item));
					}
				} else if(line.startsWith("replicate:")) {
					replicate = Integer.parseInt(content[1]);
				} else if(line.startsWith("labelingTime")) {
					labelingTime = Double.parseDouble(content[1]);
				} else if(line.startsWith("pathR")) {
					StringBuffer temp = new StringBuffer("");
					for(int i=1; i<content.length; i++) {
						temp.append(content[i]+":");
					}
					pathR = temp.substring(0, temp.length()-1);
				} else if(line.startsWith("attributes")) {
					String original = line.substring(11);
					String[] content2 = original.split(",");
					for(String item : content2) {
						attributeFiles.add(item);
					}
			    } else if(line.startsWith("attributeLabels")) {
			    	String[] lists = content[1].split(",");
			    	for(String item : lists) {
			    		attributes.add(item);
			    	}
				} else if(line.startsWith("attrDescr")) { 
			    	attrDescr = content[1].split(",");
				} else if(line.startsWith("fasta")) {
					StringBuffer temp = new StringBuffer("");
						for(int i=1; i<content.length; i++) {
							temp.append(content[i]+":");
						}
					//if data file address is URL, download file
					if(temp.toString().startsWith("http:")) {

                                               System.out.println("Downloading sequence file.");
						String name = Config.CONFIGDIR+File.separator+"sequences.txt";
						File tempdata = new File(name);
						tempdata=downloadFile(tempdata, temp.toString());
                                                fasta=name;
                                                
                                                if(tempdata==null) {
                                                        DisplayWarnings.warning(
                                                                "Sequence file could not be downloaded to your local file system.\n " +
                                                                "Please download the sequence file manually and replace the web address\n" +
                                                                "in the session file by the local address of the downloaded file. ", new Frame());
                                                }
						
					//else generate file
					} else {
						
						fasta = temp.substring(0, Math.max(temp.length()-1, 0));
                                                File fastaFile = new File(fasta);
                                                if(!fastaFile.exists()) {
                                                        DisplayWarnings.warning("You may have moved or deleted your multiple fasta file; " +
                                                        "unable to load.", new Frame());
                                                }
                                    }
					
				} else if(line.startsWith("column:")) {
					column = Integer.parseInt(content[1]);
				} else if(line.startsWith("alpha:")) { 
					if(content[1].contains("=")){
						String[] param = content[1].split("=");
						alpha          = param[0];
						ccl            = Double.parseDouble(param[1]);
					} else {
						alpha = content[1].trim();
					}
				} else if(!line.startsWith("#")){
					DisplayWarnings.warning("The file you tried to load is no HALO sessions file!", new Frame());
                                        
					return false;
				}
			}
			ArrayList<String> attributesInFile = new ArrayList<String>();
			
//			if(attrDescr != null) {
//				//create list of attributes that are saved in original data file
//				if(attributeFiles.size() < attrDescr.length) {
//					for(int i=0; i<(attrDescr.length-attributeFiles.size()); i++) {
//						attributesInFile.add(attrDescr[i]);
//					}
//				}
//			}
			
			//load data
			if(dataFile != null) {
				guiData = new GuiFilterData();
				if(!spotid.isEmpty()) {
					guiData.setSpotid(spotid);
				}
				guiData.setFile(dataFile);
				guiData.setLog(log);
				guiData.setLabelsNew(columnsNew);
				guiData.setLabelsPre(columnsPre);
				guiData.setLabelsTot(columnsTotal);
				//load attributes that are in the original data file
				if(attributesInFile.size() > 0) {
					guiData.setAttributes(attributesInFile);
				}
				guiData.loadData();
				guiData.setGenename(geneLabel);
				guiData.getData().setReplicate(replicate);
			
			//load multiple fasta file
			if(fasta.length() > 0) {
				File fastaFile = new File(fasta);
				if(fastaFile.exists()) {
					guiData.addFasta(new File(fasta), column);
				} else {
					DisplayWarnings.warning("You may have moved or deleted your multiple fasta file; " +
							"unable to load.", new Frame());
                                        guiData.addFasta(null, column);
				}
				
			}
                       
			if(orgLabelsCalls.size() > 0) {
				guiData.addOriginalAttr((ArrayList<String>)orgLabelsCalls, true);
			}
			if(orgLabelsOther.size() > 0) {
				guiData.addOriginalAttr((ArrayList<String>) orgLabelsOther, false);
			}
			ArrayList<String> pqsMethods = new ArrayList<String>();
			//use filtering procedures
			if(dataMethods.size() > 0) {
				guiData.setCombined(true);
				ArrayList<String> temporary = new ArrayList<String>();
				temporary.addAll(dataMethods);
				for(String method : dataMethods) {
					if(method.startsWith(GuiFilterData.PQS) || method.startsWith(GuiFilterData.PQSMIN)) {
						pqsMethods.add(method);
						temporary.remove(method);
					}
				}
				dataMethods = temporary;
				guiData.setMethods(dataMethods);
				guiData.prepareData();
				guiData.setCombined(false);
			}
			//load attributes from additional files
			for(int i=0; i<attributeFiles.size(); i++) {
				File attrFile = new File(attributeFiles.get(i));
				if(attrFile.exists()) {
					guiData.addAttr(attrFile, attributes);
				} else {
					DisplayWarnings.warning("You may have moved or deleted the attribute file " +
							file+"; unable to load.", new Frame());
				}

//				attributes = guiData.getAttributesList();
//				guiData.addAttributesList(attributes, attrDescr);
			}
			data = guiData.getData();
			guiNorm = new GuiNormal();
			guiNorm.setData(data);
			guiHL = new GuiHalfLife();
			//perform normalization
			if(normMethod.length() > 0) {
				guiNorm.setReplicate(replicate);
				guiNorm.setMethod(normMethod);
				guiNorm.calcNormalization();
				CorrectionFactors corr = guiNorm.getCF();
				guiHL.setCorr(corr);
				guiData.setNorm(guiNorm.getNormalization());
				if(pqsMethods.size() > 0) {
					guiData.setMethods(pqsMethods);
					guiData.setCombined(true);
					guiData.prepareData();
					guiData.setCombined(false);
				}
			}
			dataMethods.addAll(pqsMethods);
			guiData.setMethods(dataMethods);
			guiHL.setData(data);
			Alpha alp;
			if(alpha.equals(ALPHACELLDIV)) {
				alp = new AlphaCellDivision(ccl);
			} else {
				alp = new AlphaConstant();
			}
			guiHL.setAlpha(alp);
			
			//perform half-life calculations
			if(hlMethods.size() > 0) {
				if(replicates.size() == 0 && replicate != Normalization.UNDEF) {
					for(int i=0; i<hlMethods.size(); i++) {
						replicates.add(replicate);
					}
				}
				guiHL.setMethods(hlMethods);
				guiHL.setTimes(times);
				guiHL.setReplicates(replicates);
				guiHL.calcHL();
			}
			}
                         
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Downloads a file from a given address
	 * @param output The output file
	 * @param address The URL address
	 * @return The downloaded file
	 */
	@SuppressWarnings("deprecation")
	public File downloadFile(File output, String address) {
		try {
		    URL url             = new URL(address.substring(0, address.length()-1));
		    InputStream is      = url.openStream();  
		    DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
		    BufferedWriter datafile = new BufferedWriter(new FileWriter(output));
		    String write;
		    
		    while ((write = dis.readLine()) != null) {
		        datafile.write(write+"\n");
		    }
		    datafile.flush();
		    datafile.close();
		    is.close();
		} catch (MalformedURLException mue) {
                     mue.printStackTrace();
		     return null;
		} catch (IOException ioe) {
                     ioe.printStackTrace();
                     return null;
		    
		}
		return output;
	}
	
	/**
	 * Sets the name of the used alpha function
	 * @param alpha The name of the used alpha function
	 */
	public void setAlpha(String alpha) {
		if(alpha.equals(ALPHACELLDIV) || alpha.equals(ALPHACONSTANT)) {
			this.alpha = alpha;
		}
	}
	
	/**
	 * Sets the value of the parameter ccl
	 * @param ccl The parameter ccl
	 */
	public void setCcl(double ccl) {
		this.ccl = ccl;
	}
	
	/**
	 * Sets the descriptor for the attributes
	 * @param attrDescr The list of names of the attributes 
	 * of the data
	 */
	public void setAttrDescr(String[] attrDescr) {
		this.attrDescr = attrDescr;
	}
	
	/**
	 * Sets the (column) number of the used replicate
	 * @param replicate Number of the replicate used for calculation
	 */
	public void setReplicates(List<Integer> replicate) {
		this.replicates = replicate;
	}
	
	/**
	 * Sets the list of attribute labels
	 * @param attributes The list of attributes
	 */
	public void setAttributes(ArrayList<String> attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * Sets the list of attributes from the original file (present calls)
	 * @param originalLabels List of attributes from original file
	 */
	public void setOriginalLabelsCalls(List<String> originalLabels) {
		this.orgLabelsCalls = originalLabels;
	}
	
	/**
	 * Sets the list of attributes from the original file (no present calls)
	 * @param originalLabels List of attributes from original file
	 */
	public void setOriginalLabelsOther(List<String> originalLabels) {
		this.orgLabelsOther = originalLabels;
	}
	
	/**
	 * Sets the list of attribute files
	 * @param attributeFiles The list of attribute files
	 */
	public void setAttributeFiles(List<String> attributeFiles) {
		this.attributeFiles = attributeFiles;
	}
	
	//TODO: attribute labels zum jeweiligen file
	public void setAttributeLabels(ArrayList<ArrayList<String>> attributeLabels) {
		this.attributeLabels = attributeLabels;
	}
	
	/**
	 * Sets the probeset id
	 * @param spotid The probeset id
	 */
//	public void setSpotid(String spotid) {
//		this.spotid = spotid;
//	}

	/**
	 * Sets the value holding information on the
	 * checked status of the filtering method 'absence'
	 * @param checkAbs True, if filtering method 'absence' is checked
	 */
	public void setCheckAbs(boolean checkAbs) {
		this.checkAbs = checkAbs;
	}
	
	/**
	 * Sets the value holding information on the 
	 * checked status of the checkbox that expands the
	 * filtering menu
	 * @param checkFilt True, if filtering is checked
	 */
	public void setCheckFilt(boolean checkFilt) {
		this.checkFilt = checkFilt;
	}
	
	/**
	 * Sets the value holding information on the 
	 * checked status of the checkbox that expands the
	 * half-life calculation
	 * @param checkHL True, if half-life is checked
	 */
	public void setCheckHL(boolean checkHL) {
		this.checkHL = checkHL;
	}
	
	/**
	 * Sets the value holding information on the
	 * checked status of the checkbox that expands the
	 * linear regression
	 * @param checkNorm True, if normalization is checked
	 */
	public void setCheckNorm(boolean checkNorm) {
		this.checkNorm = checkNorm;
	}
	
	
	/**
	 * Sets status of quality plotting
	 * @param plotQuality TRUE if quality plot has been performed
	 */
	public void setPlotQuality(boolean plotQuality) {
		this.plotQuality = plotQuality;
	}
	
	/**
	 * Sets the value holding information on the 
	 * checked status of the filtering method 'pqs'
	 * @param checkPQS True, if filtering method 'pqs' is checked
	 */
	public void setCheckPQS(boolean checkPQS) {
		this.checkPQS = checkPQS;
	}
	 
	/**
	 * Sets the value holding information on the
	 * checked status of the filtering method 'pqs min'
	 * @param checkPQSMin True, if filtering method 'pqs min' is checked
	 */
	public void setCheckPQSMin(boolean checkPQSMin) {
		this.checkPQSMin = checkPQSMin;
	}
	
	/**
	 * Sets the value holding information on the
	 * checked status of the filtering method 'threshold'
	 * @param checkThresh True, if filtering method 'threshold' is checked
	 */
	public void setCheckThresh(boolean checkThresh) {
		this.checkThresh = checkThresh;
	}

	/**
	 * Sets the x position of the window
	 * @param x The x position of the window
	 */
	public void setPosX(int x) {
		posX = x;
	}

	/**
	 * Sets the y position of the window
	 * @param y The y position of the window
	 */
	public void setPosY(int y) {
		posY = y;
	}
	
	/**
	 * Sets the ratio calculation method
	 * @param ratioMethod The ratio calculation method
	 */
	public void setRatioMethod(int ratioMethod) {
		this.ratioMethod = ratioMethod;
	}

	/**
	 * Sets the attribute containing information if the
	 * window is maximized or not
	 * @param m True if the window is maximized
	 */
	public void setMaxim(int m) {
		if(m == 1) {
			maxim = true;
		} else if(m == 0) {
			maxim = false;
		}
	}

	/**
	 * Sets the list of column labels for newly transcribed RNA,
	 * only those that are extracted
	 * @param columnsNew The list of column labels for newly transcribed RNA
	 */
	public void setColumnsNew(List<String> columnsNew) {
		this.columnsNew = columnsNew;
	}


	/**
	 * Sets the list of column labels for pre-existing RNA,
	 * only those that are extracted
	 * @param columnsPre The list of column labels for pre-existing RNA
	 */
	public void setColumnsPre(List<String> columnsPre) {
		this.columnsPre = columnsPre;
	}
	
	
	/**
	 * Sets the list of column labels for total RNA,
	 * only those that are extracted
	 * @param columnsTotal The list of column labels for total RNA
	 */
	public void setColumnsTotal(List<String> columnsTotal) {
		this.columnsTotal = columnsTotal;
	}
	
	
	/**
	 * Sets the list of methods for normalization
	 * @param normMethod The list of methods for normalization
	 */
	public void setNormMethod(String normMethod) {
		this.normMethod = normMethod;
	}
	
	/**
	 * Sets the data object needed for normalization
	 * @param data The data object needed for normalization
	 */
	public void setData(Data data) {
		this.data = data;
	}
	
	/**
	 * Sets the file containing the data
	 * @param dataFile The file containing the data
	 */
	public void setDataFile(File dataFile) {
		this.dataFile = dataFile;
	}
	
	/**
	 * Sets the description of all used half-life 
	 * calculation methods
	 * @param display The description of all half-life 
	 * calculation methods and time points
	 */
	public void setDisplay(StringBuffer display) {
		this.display = display;
	}
	
	/**
	 * Sets the interface between GUI and data handling
	 * @param guiData The GuiFilterData object connecting GUI
	 * and data handling
	 */
	public void setGuiData(GuiFilterData guiData) {
		this.guiData = guiData;
	}
	
	/**
	 * Sets the interface between GUI and half-life calculation
	 * @param guiHL The GuiHalfLife object connecting GUI
	 * and half-life calculation
	 */
	public void setGuiHL(GuiHalfLife guiHL) {
		this.guiHL = guiHL;
	}
	
	/**
	 * Sets the interface between GUI and normalization
	 * @param guiNorm The GuiLinReg object connecting GUI
	 * and normalization
	 */
	public void setGuiNorm(GuiNormal guiNorm) {
		this.guiNorm = guiNorm;
	}
	
	/**
	 * Sets the list of all labels from the data file
	 * @param labels The list of labels from the data file
	 */
	public void setLabels(ArrayList<String> labels) {
		this.labels = labels;
	}
	
	/**
	 * Sets the list of descriptions of half-life calculation
	 * methods
	 * @param hlMethods The list of description of half-life methods
	 */
	public void setHlMethods(List<String> hlMethods) {
		this.hlMethods = hlMethods;
	}
	
	/**
	 * Sets the list of time points for half-life calculation
	 * @param times The list of time points for half-life calculation
	 */
	public void setTimes(List<Double> times) {
		this.times = times;
	}
	
	/**
	 * Sets the file into which the session will be saved
	 * @param file The file into which the session will be saved
	 */
	public void setFile(File file) {
		this.file = file;
	}
	
	/**
	 * Sets the constant labeling time
	 * @param labelingTime A constant labeling time
	 */
	public void setLabelingTime(double labelingTime) {
		this.labelingTime = labelingTime;
	}
	
	/**
	 * Sets the constant path to R
	 * @param pathR The path to the R bin folder
	 */
	public void setPathR(String pathR) {
		this.pathR = pathR;
	}
	
	/**
	 * Sets a constant replicate
	 * @param replicate The constant replicate
	 */
	public void setReplicate(int replicate) {
		this.replicate = replicate;
	}
	
	/**
	 * Sets the file which contains the sequences for the data
	 * @param file The multiple fasta file for the data
	 */
	public void setFasta(File file) {
		this.fasta = file.getAbsolutePath();
	}
	
	/**
	 * Sets the gene name label
	 * @param geneLabel The gene name label
	 */
	public void setGeneLabel(String geneLabel) {
		this.geneLabel = geneLabel;
	}
	
	/**
	 * Sets the column of the multiple fasta file header which
	 * holds the gene name
	 * @param column The column which holds the gene name
	 */
	public void setColumn(int column) {
		this.column = column;
	}
	
	/**
	 * Sets status of attributes from original file
	 * @param calls TRUE if attributes from original file are present calls
	 */
	public void setCalls(boolean calls) {
		this.calls = calls;
	}
	
	/**
	 * Sets the list of filtering methods 
	 * @param dataMethods The list of filtering methods
	 */
	public void setDataMethods(List<String> dataMethods) {
		this.dataMethods = dataMethods;
	}
	
	/**
	 * Sets scale of data (log or linear)
	 * @param log TRUE if data is in log scale
	 */
	public void setLog(boolean log) {
		this.log = log;
	}
	
	/**
	 * Returns the name of the used alpha function
	 * @return The name of the used alpha function
	 */
	public String getAlpha() {
		return alpha;
	}
	
	/**
	 * Returns the value of the parameter ccl
	 * @return The parameter ccl
	 */
	public double getCcl() {
		return ccl;
	}
	
	/**
	 * Returns the replicate number used for calculation
	 * @return The replicate number used
	 */
	public List<Integer> getReplicates() {
		return replicates;
	}
	
	/**
	 * Returns the list of attributes from the original file (present calls)
	 * @return The list of attributes from the original file
	 */
	public List<String> getOriginalLabelsCalls() {
		return orgLabelsCalls;
	}
	
	/**
	 * Returns the list of attributes from the original file (no present calls)
	 * @return The list of attributes from the original file
	 */
	public List<String> getOriginalLabelsOther() {
		return orgLabelsOther;
	}
	
	/**
	 * Returns the labeling time
	 * @return The labeling time
	 */
	public double getLabelingTime() {
		return labelingTime;
	}
	
	/**
	 * Returns the path to R
	 * @return The path to R
	 */
	public String getPathR() {
		return pathR;
	}
	
	/**
	 * Returns the constant replicate
	 * @return The constant replicate
	 */
	public int getReplicate() {
		return replicate;
	}
	
	/**
	 * Returns the list of column labels for newly transcribed RNA,
	 * only the extracted ones
	 * @return The list of column labels for newly transcribed RNA
	 */
	public List<String> getColumnsNew() {
		return columnsNew;
	}
	
	
	/**
	 * Returns the list of column labels for pre-existing RNA,
	 * only the extracted ones
	 * @return The list of column labels for pre-existing RNA
	 */
	public List<String> getColumnsPre() {
		return columnsPre;
	}
	
	
	/**
	 * Returns the list of column labels for total RNA,
	 * only the extracted ones
	 * @return The list of column labels for total RNA
	 */
	public List<String> getColumnsTotal() {
		return columnsTotal;
	}
	
	
	/**
	 * Returns the data object
	 * @return The data object
	 */
	public Data getData() {
		return data;
	}
	
	/**
	 * Returns the file from which the data was extracted
	 * @return The file containing the expression data
	 */
	public File getDataFile() {
		return dataFile;
	}
	
	/**
	 * Returns the list of filtering methods
	 * @return The list of filtering methods
	 */
	public List<String> getDataMethods() {
		return dataMethods;
	}
	
	/**
	 * Returns the description of half-life calculation 
	 * methods and time points
	 * @return The description of half-life calculation
	 */
	public StringBuffer getDisplay() {
		return display;
	}
	
	/**
	 * Returns the interface connecting GUI and filtering of the data
	 * @return The GuiFilterData object
	 */
	public GuiFilterData getGuiData() {
		return guiData;
	}
	
	/**
	 * Returns the interface connecting GUI and half-life calculation
	 * @return The GuiHalfLife object
	 */
	public GuiHalfLife getGuiHL() {
		return guiHL;
	}
	
	/**
	 * Returns the interface connecting GUI and linear regression
	 * @return The GuiNormal object
	 */
	public GuiNormal getGuiNorm() {
		return guiNorm;
	}
	
	/**
	 * Returns the list of methods used for half-life calculation
	 * @return The list of half-life calculation methods
	 */
	public List<String> getHlMethods() {
		return hlMethods;
	}
	
	/**
	 * Returns all the labels available in the data file
	 * @return All labels from the data file
	 */
	public ArrayList<String> getLabels() {
		return labels;
	}
	
	/**
	 * Returns the name of the normalization method
	 * @return The name of the normalization method
	 */
	public String getNormMethod() {
		return normMethod;
	}
	
	/**
	 * Returns the ratio calculation method
	 * @return The ratio calculation method
	 */
	public int getRatioMethod() {
		return ratioMethod;
	}
	
	/**
	 * Returns the list of time points used for half-life calculation
	 * @return The list of time points for half-life calculation
	 */
	public List<Double> getTimes() {
		return times;
	}
	
	/**
	 * Returns the names of the attributes for the data
	 * @return The names of the data attributes
	 */
	public String[] getAttrDescr() {
		return attrDescr;
	}
	
	/**
	 * Returns the list of all attributes
	 * @return The list of all attributes
	 */
	public ArrayList<String> getAttributes() {
		return attributes;
	}
	
	/**
	 * Returns the list of attribute file names
	 * @return The list of attribute file names
	 */
	public List<String> getAttributeFiles() {
		return attributeFiles;
	}
	
	/**
	 * Returns the saved gene label
	 * @return Gene name label
	 */
	public String getGeneLabel() {
		return geneLabel;
	}
	
	/**
	 * Returns the list of attribute label names
	 * @return The list of attribute label names
	 */
	public ArrayList<ArrayList<String>> getAttributeLabels() {
		return attributeLabels;
	}
	
	/**
	 * Returns status of attributes from original file: present calls?
	 * @return TRUE if attributes from original file are present calls
	 */
	public boolean isCalls() {
		return calls;
	}
	
	/**
	 * Returns information on the checked status
	 * of the checkbox for the filtering method 'absence'
	 * @return True, if 'absence' was checked
	 */
	public boolean isCheckAbs() {
		return checkAbs;
	}
	
	/**
	 * Returns information on the checked status
	 * of the checkbox for half-life calculation
	 * @return True, if half-life menu was expanded
	 */
	public boolean isCheckHL() {
		return checkHL;
	}
	
	/**
	 * Returns information on the checked status
	 * of the checkbox for filtering
	 * @return True, if filtering menu was expanded
	 */
	public boolean isCheckFilt() {
		return checkFilt;
	}
	
	/**
	 * Returns information on the checked status
	 * of the checkbox for normalization
	 * @return True, if normalization menu was expanded
	 */
	public boolean isCheckNorm() {
		return checkNorm;
	}
	
	/**
	 * Returns information on the checked status
	 * of the checkbox for the filtering method 'pqs'
	 * @return True, if 'pqs' was checked
	 */
	public boolean isCheckPQS() {
		return checkPQS;
	}
	
	/**
	 * Returns information on the checked status
	 * of the checkbox for the filtering method 'pqs min'
	 * @return True, if 'pqs min' was checked
	 */
	public boolean isCheckPQSMin() {
		return checkPQSMin;
	}
	
	/**
	 * Returns information on the checked status
	 * of the checkbox for the filtering method 'threshold'
	 * @return True, if 'threshold' was checked
	 */
	public boolean isCheckThresh() {
		return checkThresh;
	}

	/**
	 * Returns x position of the window
	 * @return X position of the window
	 */
	public int getPosX() {
		return posX;
	}
	
	/**
	 * Returns y position of the window
	 * @return Y position of the window
	 */
	public int getPosY() {
		return posY;
	}

	/**
	 * Returns maximized status
	 * @return True, if window was maximized
	 */
	public boolean isMaxim() {
		return maxim;
	}
	
	/**
	 * Returns status of quality plotting
	 * @return TRUE if quality should be plotted
	 */
	public boolean isPlotQuality() {
		return plotQuality;
	}
}
