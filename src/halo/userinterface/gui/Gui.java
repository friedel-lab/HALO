/*
 * This file is part of the HALO 1.3 graphical user interface
 */
package halo.userinterface.gui;

import halo.data.Data;
import halo.data.Filter;
import halo.data.biascorrection.RCorrelationCoefficient;
import halo.data.biascorrection.RLoessRegression;
import halo.halflife.HalfLife;
import halo.halflife.HalfLifeWriter;
import halo.halflife.alpha.Alpha;
import halo.halflife.alpha.AlphaCellDivision;
import halo.halflife.alpha.AlphaConstant;
import halo.normalization.CorrectionFactors;
import halo.normalization.Normalization;
import halo.tools.Tools;
import halo.userinterface.gui.graphhandler.GraphHandler;
import halo.userinterface.gui.graphhandler.XYGraphConstructor;
import halo.userinterface.gui.guitools.AboutDialog;
import halo.userinterface.gui.guitools.DisplayWarnings;
import halo.userinterface.gui.guitools.GuiTools;
import halo.userinterface.gui.guitools.HelpButton;
import halo.userinterface.gui.guitools.InputHelpDialog;
import halo.userinterface.gui.guitools.JPEGFilter;
import halo.userinterface.gui.guitools.LabelChooserDialog;
import halo.userinterface.gui.guitools.TxtFilter;
import halo.userinterface.gui.session.Config;
import halo.userinterface.gui.session.Session;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;


/**
 * Graphical User Interface for half-life calculation and additional methods
 * @author Stefanie Kaufmann
 *
 */
public class Gui extends JFrame{

	//TODO: mehr memory beim starten, testen auf max mem?

	private static final long   serialVersionUID = 5557850785265038922L;
	public  static final int    UNDEF            = -1;
	private static final String URAFILENAME      = Config.CONFIGPATH+"uracilNumber.list";
	private static String workingdirectory;
	private static final int    WIDTH            = 600;
	private static final int    HEIGHT           = 250;
	private static final int    MAXHEIGHT        = Math.min(680,
			Toolkit.getDefaultToolkit().getScreenSize().height);

	private Session   session;
	private File      sessName, outputData, outputHL, pqs;
	private File      input = null;
	private JMenuItem menuLoadSess, menuSaveSess, menuClearSess, menuExit, menuSaveData, menuSaveHL, menuBias,
	menuSaveR, menuSaveB, menuEva, menuCalcMed, menuMed, menuHelpH, menuHelpAbout, menuHelpWeb, menuSettGene,
	menuSettAlpha, menuSettTime, menuSettR, menuSettMax, menuSettRep, menuHelpReplicate, menuPQS,menuSaveHLA,
	menuSubSave, menuNorm, menuCalcHL;
	private JMenu menuFilter, menuNormal, menuHalfLife;

	private JRadioButton methThresh, methAbs, methPQS, methPQSMin, none;
	private JPanel       dataPa, filtPa, normPa, hlPa, corrPa, panel;
	private JSeparator   sep1, sep2, sep3;
	private JTextField   filepath;
	private JFileChooser chooser;
	private JButton      calc, plotNorm, plotHalf, startHLButton, saveHLs, add;
	private GridBagConstraints c;

//	private List<String> columnsTotalAll = new ArrayList<String>();
//	private List<String> columnsNewAll   = new ArrayList<String>();
//	private List<String> columnsPreAll   = new ArrayList<String>();
	private List<String> columnsTotal    = new ArrayList<String>();
	private List<String> columnsNew      = new ArrayList<String>();
	private List<String> columnsPre      = new ArrayList<String>();
	private List<String> columnsTotalOut = new ArrayList<String>();
	private List<String> columnsNewOut   = new ArrayList<String>();
	private List<String> columnsPreOut   = new ArrayList<String>();
	private List<String> attributesOut   = new ArrayList<String>();
	private List<String> absenceLabel    = new ArrayList<String>();
	private List<String> attributeFiles  = new ArrayList<String>();
	private List<String> orgLabelsCalls  = new ArrayList<String>();
	private List<String> orgLabelsOther  = new ArrayList<String>();
	private ArrayList<ArrayList<String>> attributeFileLabels = new ArrayList<ArrayList<String>>();
	private List<Integer> replicates     = new ArrayList<Integer>();
	private ArrayList<String> labels     = new ArrayList<String>();
	private ArrayList<String> temporary  = new ArrayList<String>();
	private ArrayList<String> attributesOrg   = new ArrayList<String>(); //labels of attributes from original datafile
	private ArrayList<String> attributeLabels = new ArrayList<String>(); //labels of separate attributes

	private ArrayList<String> attributes = new ArrayList<String>();
	private List<CorrectionFactors>            medCorr    = new ArrayList<CorrectionFactors>();

	private XYGraphConstructor      normGraphConstructor;
	private RCorrelationCoefficient cor;
	private RLoessRegression        loess;

	private StringBuffer display;

	private String pathR = "";
	private String spotid;
	private String normMethod;
	private String uraMeth;
	private String method;

	private int     ratioMethod = Data.RATIOFIRST;
	private int     replicate   = UNDEF;
	private int     column;
	private int     startDisplay;
	private int     which;
	private int     tempIndex   = -1;

	private boolean renormalize = false;
	private boolean unequal     = false;
	private boolean cancel      = false;
	private boolean corrExpanded = false;
	private boolean plotQuality = false;

	private double labelingTime = UNDEF;
	private double medTime;
	private double median;

	private GuiFilterData guiData = new GuiFilterData();
	private GuiNormal     guiNorm = new GuiNormal();
	private GuiHalfLife   guiHL   = new GuiHalfLife();
	private Data          dataObj;
	private Normalization eval;
	private Alpha         alpha = new AlphaConstant();

	/**
	 * Generate GUI object, start initializing
	 */
	public Gui() {
		session = new Session();
		new File(Config.CONFIGPATH).mkdirs();
		Config.loadConfigFile();
		pathR = Config.getPathToR();
		JOptionPane.setDefaultLocale(new Locale("en", "US"));
		initGui();
		Toolkit tk           = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		int screenHeight     = screenSize.height;
		int screenWidth      = screenSize.width;
		int posX             = (int) ((screenWidth/2)-0.5*WIDTH);
		int posY             = (int) ((screenHeight/2)-HEIGHT);
		this.setBounds(posX, Math.max(posY,100), WIDTH, HEIGHT);
		//display JFrame
		this.setVisible(true);
	}

	/**
	 * Initialize the GUI
	 */
	public void initGui() {
		//set title and generate main menu
		this.setTitle("HALO");
		if(Config.getDisplayHint()) {
			questionMarkInfo();
		}

		sessName             = new File("Halo_temp");
		GridBagConstraints c = new GridBagConstraints();
		panel         = new JPanel(new GridBagLayout());
		JScrollPane scroll   = new JScrollPane(panel);
		this.setJMenuBar(generateMainMenus());

		c.gridx     = 0;
		c.gridy     = 0;
		c.weightx   = 1;
		c.insets    = new Insets(4,4,4,4);
		c.anchor    = GridBagConstraints.WEST;
		c.weighty   = 0;
		c.gridwidth = 1;

		c.anchor = GridBagConstraints.WEST;
		c.fill   = GridBagConstraints.HORIZONTAL;
		//create sub panels
		subMenuData(c);
		subMenuFilter(c);
		subMenuNorm(c);
		corrPa = new JPanel(new GridBagLayout());
		subMenuHL(c);

		guiHL.setAlpha(new AlphaConstant());

		//add hidden sub panels to great panel
		c.gridy++;
		panel.add(dataPa, c);
		c.gridy++;
		panel.add(filtPa, c);
		c.gridy++;
		panel.add(normPa, c);
		c.gridy++;
		panel.add(hlPa, c);
		c.gridy++;
		this.c = c;
		panel.add(corrPa, c);

		dataPa.setVisible(true);

		// close listener
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Component gui = Gui.this;
				if(Config.getDisplayExit()) {
					Object[] message = new Object[2];
					message[0] = "Do you want to save the Session before Program exits?";
					JCheckBox remember = new JCheckBox("Do not show this message again");
					remember.setActionCommand("enable");
					remember.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							JCheckBox check = (JCheckBox) e.getSource();
							if(check.getActionCommand().equals("enable")) {
								check.setActionCommand("disable");
								Config.setDisplayExit(false);
							} else {
								check.setActionCommand("enable");
								Config.setDisplayExit(true);
							}
						}
					});
					message[1] = remember;
					int answer = JOptionPane.showConfirmDialog(gui, message, "Program exits", JOptionPane.YES_NO_OPTION);
					if(answer == JOptionPane.YES_OPTION) {
						JFileChooser choose = new JFileChooser();
						int returnVal = choose.showSaveDialog(Gui.this);
						if(returnVal == JFileChooser.APPROVE_OPTION) {
							if(choose.getSelectedFile().exists()) {
								if(overwriteFile()) {
									sessName = choose.getSelectedFile();
									saveSession();
								}
							} else {
								sessName = choose.getSelectedFile();
								saveSession();
							}
						}
					} 
				}

				actionExit();
			}
		});
		this.setContentPane(scroll);
	}

	/**
	 * Reset size of window, hide all sub panels
	 */
	private void resetWindow() {
		this.setBounds(this.getX(), this.getY(), WIDTH, HEIGHT);
		menuFilter.setEnabled(false);
		filtPa.setVisible(false);
		menuNormal.setEnabled(false);
		normPa.setVisible(false);
		corrPa.setVisible(false);
		menuHalfLife.setEnabled(false);
		hlPa.setVisible(false);
	}

	/**
	 * make window bigger
	 * @param height How much the window will be enlarged
	 */
	private void enlargeWindow(int height) {
		int x = this.getBounds().x;
		int y = this.getBounds().y;
		int w = this.getBounds().width;
		int h = this.getBounds().height;
		this.setBounds(x, y, w, Math.min(h+height,MAXHEIGHT));
	}

	/**
	 * Create sub panel for loading the data
	 * @param c Layout information from great panel
	 * @return sub panel containing instruments for loading of data
	 */
	private JPanel subMenuData(GridBagConstraints c) {
		GridBagConstraints cFilt = new GridBagConstraints();

		cFilt.gridx   = 0;
		cFilt.gridy   = 0;
		cFilt.weightx = c.weightx;
		cFilt.weighty = c.weighty;
		cFilt.insets  = new Insets(4,4,4,4);

		dataPa = new JPanel(new GridBagLayout());
		//create help button for this panel
		HelpButton help = new HelpButton("Choose the file containing your data using the 'Browse' button; to load the data click 'Load'. " +
		"You can still load additional files containing attributes afterwards using the 'Add' button.");
		cFilt.fill   = GridBagConstraints.NONE;
		cFilt.anchor = GridBagConstraints.EAST;
		dataPa.add(help, cFilt);
		cFilt.fill   = GridBagConstraints.HORIZONTAL;
		cFilt.gridx++;
		dataPa.add(new JLabel("Choose input file:  "), cFilt);
		cFilt.gridx++;

		//browse file system
		filepath = new JTextField();
		filepath.setMargin(new Insets(5,5,5,5));
		filepath.setEditable(false);
		JScrollPane logScrollPane = new JScrollPane(filepath);
		dataPa.add(filepath, cFilt);
		dataPa.add(logScrollPane);
		cFilt.gridx++;

		chooser      = new JFileChooser();
		chooser.addChoosableFileFilter(new TxtFilter());
		JButton file = new JButton("Browse");
		file.setMnemonic(KeyEvent.VK_O);
		dataPa.add(file, cFilt);
		cFilt.gridx = 1;

		file.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = chooser.showOpenDialog(Gui.this);

				//check if file has been chosen
				if (returnVal == JFileChooser.APPROVE_OPTION) {
//					columnsNewAll   = new ArrayList<String>();
//					columnsTotalAll = new ArrayList<String>();
//					columnsPreAll   = new ArrayList<String>();
					columnsNew      = new ArrayList<String>();
					columnsPre      = new ArrayList<String>();
					columnsTotal    = new ArrayList<String>();
					input  = chooser.getSelectedFile();
					labels = Tools.getColumnLabels(input);

					//check if input file exists
					if(!input.exists()) {
						filepath.setText("File does not exist.");
						//check if the file format is correct
					} else if(labels == null) { 
						DisplayWarnings.error("Your file is not in the correct " +
								"format or may contain invalid letters", Gui.this);
						//if everything is correct, go on and let user choose labels
					} else {
						filepath.setText("Opening: " + input.getName() + ".\n");

						//set file for calculations
						guiData.setFile(input);

						//choose label descriptions corresponding to newly transcribed, pre-existing, total RNA
						ArrayList<Component> listNew = new ArrayList<Component>();
						for(String item : labels) {
							JCheckBox lab  = new JCheckBox(item);
							listNew.add(lab);
							lab.setActionCommand("enable");

							lab.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									JCheckBox lab = (JCheckBox) e.getSource();
									if(lab.getActionCommand().equals("enable")) {
										lab.setActionCommand("disable");
										columnsNew.add(lab.getText());
									} else {
										lab.setActionCommand("enable");
										if(columnsNew.contains(lab.getText())) {
											columnsNew.remove(lab.getText());
										}
									}
								}
							});
						}
						LabelChooserDialog dialogNewLabels = new LabelChooserDialog(Gui.this, "Define labels", 
								"Please choose all labels defining_newly transcribed RNA", listNew);
						dialogNewLabels.setVisible(true);
						cancel = dialogNewLabels.isCancel();

						//do not continue if user canceled operation
						if(!cancel) {
							ArrayList<Component> listPre = new ArrayList<Component>();
							//display only labels not yet used
							for(String item : labels) {
								JCheckBox lab2 = new JCheckBox(item);
								if(!columnsNew.contains(item)) {
									lab2.setActionCommand("enable");
									listPre.add(lab2);
								}

								lab2.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										JCheckBox lab2 = (JCheckBox) e.getSource();
										if(lab2.getActionCommand().equals("enable")) {
											columnsPre.add(lab2.getText());
											lab2.setActionCommand("disable");
										} else {
											lab2.setActionCommand("enable");
											if(columnsPre.contains(lab2.getText()));
											columnsPre.remove(lab2.getText());
										}
									}
								});

							}
							LabelChooserDialog dialogPreLabels = new LabelChooserDialog(Gui.this, "Define labels", 
									"Please choose all labels defining_pre-existing RNA", listPre);
							dialogPreLabels.setVisible(true);
							cancel = dialogPreLabels.isCancel();

							if(!cancel) {
								ArrayList<Component> listTotal = new ArrayList<Component>();
								//display only labels not yet used
								for(String item : labels) {
									JCheckBox lab3 = new JCheckBox(item);
									if(!columnsNew.contains(item) && !columnsPre.contains(item)) {
										lab3.setActionCommand("enable");
										listTotal.add(lab3);
									}

									lab3.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(ActionEvent e) {
											JCheckBox lab3 = (JCheckBox) e.getSource();
											if(lab3.getActionCommand().equals("enable")) {
												lab3.setActionCommand("disable");
												columnsTotal.add(lab3.getText());
											} else {
												lab3.setActionCommand("enable");
												if(columnsTotal.contains(lab3.getText())) {
													columnsTotal.remove(lab3.getText());
												}
											}
										}
									});
								}
								LabelChooserDialog dialogTotalLabels = new LabelChooserDialog(Gui.this, 
										"Define labels", "Please choose all labels defining_total RNA", listTotal);
								dialogTotalLabels.setVisible(true);
								cancel = dialogTotalLabels.isCancel();

								if(!cancel) {

									//check if any category contains no labels at all; if so repeat displaying possible labels and error message
									while(columnsNew.size() == 0 || columnsPre.size() == 0 || columnsTotal.size() == 0 || 
											(columnsNew.size() != columnsPre.size() || columnsPre.size() != columnsTotal.size())) {
										int answer = JOptionPane.showConfirmDialog(Gui.this, "You chose an unequal number of newly transcribed, pre-existing and total labels.\n" +
												"Are you sure you want to continue?", "Warning", JOptionPane.YES_NO_OPTION);

										if(answer == JOptionPane.YES_OPTION) {
											unequal = true;
											break;
										}
										dialogNewLabels.setVisible(true);
										dialogPreLabels.setVisible(true);
										dialogTotalLabels.setVisible(true);
									}

									ButtonGroup group = new ButtonGroup();
									JRadioButton no2  = new JRadioButton();
									no2.setVisible(false);
									no2.setSelected(true);
									group.add(no2);
									ArrayList<Component> listOther = new ArrayList<Component>();
									//display only labels not yet used
									for(String item : labels) {
										JRadioButton lab4 = new JRadioButton(item);
										group.add(lab4);

										if(!columnsNew.contains(item) && !columnsPre.contains(item) && !columnsTotal.contains(item)) {
											lab4.setActionCommand("enable");
											listOther.add(lab4);
										}

										lab4.addActionListener(new ActionListener() {
											@Override
											public void actionPerformed(ActionEvent e) {
												JRadioButton lab4 = (JRadioButton) e.getSource();
												if(lab4.getActionCommand().equals("enable")) {
													lab4.setActionCommand("disable");
													spotid = lab4.getText();
													guiData.setSpotid(spotid);
												} else {
													lab4.setActionCommand("enable");
													spotid = dataObj.getSpotid();
													guiData.setSpotid(spotid);
												}
											}
										});
									}
									LabelChooserDialog dialogOtherLabels = new LabelChooserDialog(Gui.this, 
											"Define labels", "Please choose the label defining the probeset\\_ids", listOther);
									dialogOtherLabels.setVisible(true);

									if(!dialogOtherLabels.isCancel()) {
										//if no specific labels will be chosen, take all
										guiData.setLabelsNew(columnsNew);
										guiData.setLabelsPre(columnsPre);
										guiData.setLabelsTot(columnsTotal);
//										columnsNew   = columnsNewAll;
//										columnsPre   = columnsPreAll;
//										columnsTotal = columnsTotalAll;

										//ask user if logarithmical or linear scale should be used
										Object[] message = new Object[3];
										message[0]       = new JLabel("Are expression values given in log- or linear scale?");
										JRadioButton log = new JRadioButton("Log");
										JRadioButton lin = new JRadioButton("Linear");
										ButtonGroup group2 = new ButtonGroup();
										group2.add(log);
										group2.add(lin);
										lin.setSelected(true);
										log.addActionListener(new ActionListener() {
											@Override
											public void actionPerformed(ActionEvent e) {
												guiData.setLog(true);
											}

										});
										lin.addActionListener(new ActionListener() {

											@Override
											public void actionPerformed(ActionEvent e) {
												guiData.setLog(false);
											}
										});
										message[1] = log;
										message[2] = lin;
										JOptionPane.showMessageDialog(Gui.this, message, "Which scale are your values in?", 
												JOptionPane.INFORMATION_MESSAGE);

										int rest = labels.size()-columnsTotal.size()-columnsPre.size()-columnsNew.size()-1;

										//if there are labels left (more than the label for spot ids), set attributes
										if(rest>0) {

											int answer = JOptionPane.showConfirmDialog(Gui.this, "Do you want to load other columns as attributes?", 
													"Add attributes", JOptionPane.YES_NO_OPTION);

											if(answer == JOptionPane.YES_OPTION) {
												ArrayList<Component> listAttrs = new ArrayList<Component>();
												guiData.setAttributesInFile(true);
												for(String item : labels) {
													if(!columnsNew.contains(item) && !columnsPre.contains(item) && !columnsTotal.contains(item) 
															&& !item.equals(guiData.getSpotid())) {
														JCheckBox attrLabel = new JCheckBox(item);
														listAttrs.add(attrLabel);
														attrLabel.setActionCommand("enable");
														attrLabel.addActionListener(new ActionListener() {

															@Override
															public void actionPerformed(ActionEvent e) {
																JCheckBox attrLabel = (JCheckBox) e.getSource();
																if(e.getActionCommand().equals("enable")) {
																	attrLabel.setActionCommand("disable");
																	if(!attributesOrg.contains(attrLabel.getText())) {
																		attributesOrg.add(attrLabel.getText());
																	}
																} else {
																	attrLabel.setActionCommand("enable");
																	attributesOrg.remove(attrLabel.getText());
																}

															}
														});
													}
												}

												if(rest == 1) {
													JCheckBox attrLabel = (JCheckBox) listAttrs.get(0);
													if(!attributesOrg.contains(attrLabel.getText())) {
														attributesOrg.add(attrLabel.getText());
													}
												} else {
													LabelChooserDialog dialogAttributes = new LabelChooserDialog(Gui.this, "Add attributes", "Please choose labels defining attributes", listAttrs);
													dialogAttributes.setVisible(true);
													cancel = dialogAttributes.isCancel();
												}
												if(!cancel) {
													if(displayReplicatesDialog()) {
//														DisplayWarnings.warning("Please note that you should not load present call attributes \n" +
//																"with other attributes! You can load seperate attributes from your file \n" +
//																"with 'Add attributes/sequences'.", Gui.this);
														guiData.setTurnReverseMapOff(true);
														guiData.setAttributes(attributesOrg);
														orgLabelsCalls.addAll(attributesOrg);
													} else {
														guiData.setAttributes(attributesOrg);
														orgLabelsOther.addAll(attributesOrg);
													}
													
												}
											}


										}
									}
								}
							}
						}
					}
				} else {
					filepath.setText("Open command cancelled by user.\n");
				}
				filepath.setCaretPosition(filepath.getDocument().getLength());
			}
		});
		cFilt.gridy += 4;
		cFilt.gridx++;

		sep1                  = new JSeparator();
		GridBagConstraints c2 = new GridBagConstraints();

		//Add Button to load the data into the application
		JButton enter = new JButton("Load");
		enter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(input != null && !cancel) {
					//load data when pressed
					normGraphConstructor = null; //reset normalization graph
					guiData.loadData();
					dataObj = guiData.getData();
					spotid  = dataObj.getSpotid();
					guiNorm.setData(dataObj);
					guiHL.setData(dataObj);
					menuFilter.setEnabled(true);
					menuNormal.setEnabled(true);
					add.setEnabled(true);
					menuHelpReplicate.setEnabled(true);
					menuCalcMed.setEnabled(true);
					menuMed.setEnabled(true);
					if(unequal) {
						menuHalfLife.setEnabled(true);
					}
					//display number of probe sets in dialog window
					String message = "Your data has been loaded!\nThere are "+guiData.getSize()+" probe sets loaded.";
					JOptionPane.showMessageDialog(Gui.this, message, "Done loading", JOptionPane.PLAIN_MESSAGE);
				} else if(input == null) {
					DisplayWarnings.error("You didn't select a file!", Gui.this);
				} else {
					DisplayWarnings.error("You canceled the file and label selection!", Gui.this);
				}

			}
		});

		cFilt.anchor = GridBagConstraints.EAST;
		cFilt.gridx++;
		cFilt.fill   = GridBagConstraints.NONE;
		dataPa.add(enter, cFilt);
		cFilt.gridy++;

		add = new JButton("Add attributes/sequences");
		add.setEnabled(false);
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] message = new Object[4];
				message[0]       = "What do you want to add to the data?";
				JCheckBox fastaBox   = new JCheckBox("Multiple fasta file");
				fastaBox.setActionCommand("enable");
				JCheckBox attrBox   = new JCheckBox("File with additional attributes");
				attrBox.setActionCommand("enable");
				JCheckBox originalBox = new JCheckBox("Attributes from the original data file");
				originalBox.setActionCommand("enable");

				//Adds a multiple fasta file with sequences corresponding to spots
				fastaBox.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JCheckBox fastaBox = (JCheckBox) e.getSource();
						if(fastaBox.getActionCommand().equals("enable")) {
							fastaBox.setActionCommand("disable");
							JFileChooser choose = new JFileChooser();
							int returnVal       = choose.showOpenDialog(Gui.this);
							if(returnVal == JFileChooser.APPROVE_OPTION) {
								File file = choose.getSelectedFile();
								boolean correct = false;
								if(!file.exists()) {
									DisplayWarnings.error("Your chosen file does not exist; loading has failed.", Gui.this);
								} else {
									String header   = "";

									//check if the file is in fasta format
									try { 
										BufferedReader buff = new BufferedReader(new FileReader(file));
										String line;

										while((line = buff.readLine())!=null) {
											if(line.startsWith(">")) {
												header  = line.trim();
												correct = true;
												break;
											}
										}
									} catch (IOException ex) {
										ex.printStackTrace();
									}

									if(!correct) {
										DisplayWarnings.error("Your file is not in FASTA format!", Gui.this);
										fastaBox.setActionCommand("enabled");
										fastaBox.setSelected(false);
										//if the format is correct, start parsing the file
									} else {
										//let user choose column with gene name
										String[] parts = header.split("\\|");
										Object[] mess  = new Object[parts.length+1];
										mess[0]        = "Select the column denoting the gene name.\nExample values for the first sequence are shown.";
										int index      = 1;
										column         = 1;
										ButtonGroup group = new ButtonGroup();
										for(String p : parts) {
											JRadioButton item;
											if(p.startsWith(">")) {
												item = new JRadioButton(p.substring(1).trim());
												item.setSelected(true);
											} else {
												item = new JRadioButton(p.trim());
											}
											group.add(item);
											mess[index] = item;
											item.setActionCommand(String.valueOf(index));
											item.addActionListener(new ActionListener() {
												@Override
												public void actionPerformed(ActionEvent e) {
													column = Integer.parseInt(e.getActionCommand());
												}
											});
											index++;
										}

										JOptionPane.showMessageDialog(Gui.this, mess, "Define gehe name", JOptionPane.INFORMATION_MESSAGE);
										if(guiNorm.hasNormalization()) {
											menuEva.setEnabled(true);
											menuBias.setEnabled(true);
										}
										try {
											//load fasta file
											guiData.addFasta(choose.getSelectedFile(), column);
											JOptionPane.showMessageDialog(Gui.this, "Sequences have been loaded.", 
													"Loading successful", JOptionPane.INFORMATION_MESSAGE);
											//											fastaBox.setSelected(false);
											//											fastaBox.setActionCommand("enable");
										} catch (NumberFormatException ex) {
											DisplayWarnings.error("You have to enter a numerical value for the column!", Gui.this);
										}
									}
								}
							}
							if(!guiData.hasGenes()) {
								actionSetGene();
							}
							if(guiData.hasGenes()) {
								menuEva.setEnabled(true);
								menuBias.setEnabled(true);
							} 
						} else {
							fastaBox.setActionCommand("enable");
						}
					}
				});

				//load attribute files with column structure
				attrBox.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JCheckBox attrBox = (JCheckBox) e.getSource();
						if(attrBox.getActionCommand().equals("enable")) {
							attrBox.setActionCommand("disable");
							int answer = JOptionPane.YES_OPTION;
							//load as many attribute files as wanted
							while(answer == JOptionPane.YES_OPTION) {
								JFileChooser choose = new JFileChooser();
								int returnVal       = choose.showOpenDialog(Gui.this);
								if(returnVal == JFileChooser.APPROVE_OPTION) {
									//load attribute file
									File selected = choose.getSelectedFile();
									File file = choose.getSelectedFile();

									//check if file exists
									if(!file.exists()) {
										DisplayWarnings.error("Your chosen file does not exist; loading has failed.", Gui.this);
										attrBox.setActionCommand("enable");
										attrBox.setSelected(false);
										//checks if file is valid and has at least two columns									
									} else if(!Tools.testValidityForFile(file, 2, false)) { 
										DisplayWarnings.error("Your file contains unusual letters or is in the wrong " +
												"format; loading has failed.", Gui.this);
										attrBox.setActionCommand("enable");
										attrBox.setSelected(false);
									} else {

										ArrayList<String> labels = Tools.getColumnLabels(selected);

										if((labels.size()>1 && !labels.contains(dataObj.getSpotid())) 
												|| (labels.size()>2 && labels.contains(dataObj.getSpotid()))) {
											ArrayList<Component> components = new ArrayList<Component>();
											attributeLabels = new ArrayList<String>();

											for(String item : labels) {
												if(!item.equals(dataObj.getSpotid())) {
													JCheckBox box = new JCheckBox(item);
													box.setActionCommand("enable");
													box.addActionListener(new ActionListener() {
														@Override
														public void actionPerformed(ActionEvent e) {
															JCheckBox source = (JCheckBox) e.getSource();
															if(source.getActionCommand().equals("enable")) {
																if(!attributeLabels.contains(source.getText())) {
																	attributeLabels.add(source.getText());
																}
																source.setActionCommand("disable");
															} else {
																source.setActionCommand("enable");
																attributeLabels.remove(source.getText());
															}
														}
													});
													components.add(box);
												}
											}
											LabelChooserDialog dialog = new LabelChooserDialog(Gui.this, "Choose attribute labels", 
													"Please choose the attributes you want to load", components);
											dialog.setVisible(true);
											cancel = dialog.isCancel();
										} else {
											attributeLabels = new ArrayList<String>();
											for(String item : labels) {
												if(!item.equals(dataObj.getSpotid()) && !attributeLabels.contains(item)) {
													attributeLabels.add(item);
												}
											}
										}
										if(cancel) {
											attrBox.setSelected(false);
										}

										if(attributeLabels.size() != 0) {
											attributeFiles.add(selected.getAbsolutePath());
											attributeFileLabels.add(attributeLabels);
											if(displayReplicatesDialog()) {
												guiData.setTurnReverseMapOff(true);
											}
											guiData.addAttr(selected, attributeLabels);
											attributes.addAll(attributeLabels);
//											attributes = guiData.getAttributesList();
											JOptionPane.showMessageDialog(Gui.this, "Additional attributes have been loaded.", 
													"Loading successful", JOptionPane.INFORMATION_MESSAGE);
											//											attrBox.setSelected(false);
											//											attrBox.setActionCommand("enable");
										}
									}
								}
								answer = JOptionPane.showConfirmDialog(Gui.this, "Do you want to load another attribute file?", "Load attributes", JOptionPane.YES_NO_OPTION);
							}
							if(guiData.hasGenes()) {
								menuEva.setEnabled(true);
								menuBias.setEnabled(true);
							}
						} else {
							attrBox.setActionCommand("enable");
						}
					}
				});

				originalBox.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						JCheckBox originalBox = (JCheckBox) e.getSource();
						if(originalBox.getActionCommand().equals("enable")) {
							originalBox.setActionCommand("disable");
							ArrayList<String> labels  = Tools.getColumnLabels(input);
							ArrayList<Component> comp = new ArrayList<Component>();
							temporary                 = new ArrayList<String>();
							int number = labels.size();
							for(String item : labels) {
								if(!columnsNew.contains(item) && !columnsPre.contains(item)
										&& !columnsTotal.contains(item) && !attributesOrg.contains(item)
										&& !attributeLabels.contains(item) && !item.equals(spotid)) {
									JCheckBox box = new JCheckBox(item);
									box.setActionCommand("enable");
									comp.add(box);
									box.addActionListener(new ActionListener() {

										@Override
										public void actionPerformed(ActionEvent e) {
											JCheckBox box = (JCheckBox) e.getSource();
											if(box.getActionCommand().equals("enable")) {
												box.setActionCommand("disable");
												temporary.add(box.getText());
												if(!attributesOrg.contains(box.getText())) {
													attributesOrg.add(box.getText());
												}
											} else {
												box.setActionCommand("enable");
												temporary.remove(box.getText());
												attributesOrg.remove(box.getText());
											}
										}
									});
								} else {
									number--;
								}
							}
							if(number == 0) {
								originalBox.setSelected(false);
								originalBox.setActionCommand("enable");
								DisplayWarnings.warning("There are no attributes left in the original file!", Gui.this);
							} else {
								LabelChooserDialog dialog = new LabelChooserDialog(Gui.this, "Choose your attributes", 
										"Please choose any attributes you want to load.", comp);
								dialog.setVisible(true);
								cancel = dialog.isCancel();
								if(temporary.size() > 0 && !cancel) {
									boolean turnOff = displayReplicatesDialog();
									guiData.addOriginalAttr(temporary, turnOff);
									if(turnOff) {
										orgLabelsCalls.addAll(temporary);
									} else {
										orgLabelsOther.addAll(temporary);
									}
									JOptionPane.showMessageDialog(Gui.this, "Additional attributes have been loaded.",
											"Loading successful", JOptionPane.INFORMATION_MESSAGE);
									//									originalBox.setActionCommand("enable");
									//									originalBox.setSelected(false);
								} else {
									originalBox.setSelected(false);
									originalBox.setActionCommand("enable");
								}
							}

						} else {
							originalBox.setActionCommand("enable");
						}
					}
				});

				message[1] = attrBox;
				message[2] = fastaBox;
				message[3] = originalBox;
				JOptionPane.showMessageDialog(Gui.this, message, "Add information", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		cFilt.gridx = 1;
		cFilt.fill  = GridBagConstraints.HORIZONTAL;
		dataPa.add(new JLabel("Add attribute files:"), cFilt);
		cFilt.gridx++;
		dataPa.add(add, cFilt);
		cFilt.gridy += 2;
		cFilt.gridx--;
		cFilt.gridx  = 0;
		cFilt.anchor = GridBagConstraints.WEST;
		cFilt.fill   = GridBagConstraints.NONE;
		c2.fill      = GridBagConstraints.HORIZONTAL;
		c2.gridx     = 0;
		c2.gridy     = cFilt.gridy;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		dataPa.add(sep1, c2);
		dataPa.setVisible(false);
		return dataPa;
	}

	/**
	 * Open sub panel containing instruments for the filtering of the data
	 * @param c Layout information from the great panel
	 * @return The sub panel for filtering
	 */
	private JPanel subMenuFilter(GridBagConstraints c) {
		filtPa                   = new JPanel(new GridBagLayout());
		GridBagConstraints cFilt = new GridBagConstraints();

		cFilt.gridx   = 0;
		cFilt.gridy   = 0;
		cFilt.weightx = c.weightx;
		cFilt.weighty = c.weighty;
		cFilt.insets  = new Insets(4,4,4,4);
		cFilt.fill    = GridBagConstraints.HORIZONTAL;

		//Display all possible filtering methods
		filtPa.add(new JLabel("Choose filtering method:"), cFilt);
		cFilt.gridx++;
		methThresh = new JRadioButton("Threshold");
		methThresh.setActionCommand("enable");
		methAbs    = new JRadioButton("Present/Absent calls");
		methAbs.setActionCommand("enable");
		methPQS    = new JRadioButton("Probeset quality score - threshold");
		methPQSMin = new JRadioButton("Probeset quality score - optimal probeset");
		none       = new JRadioButton("");
		none.setVisible(false);

		//place filtering methods on panel
		ButtonGroup group = new ButtonGroup();
		group.add(methThresh);
		group.add(methAbs);
		group.add(methPQS);
		group.add(methPQSMin);
		group.add(none);

		//add help buttons to each method
		filtPa.add(new HelpButton("Threshold method:\nHere you can enter a numerical threshold; only probesets for which every value exceeds this " +
		"threshold will be retained."), cFilt);
		cFilt.gridx++;
		filtPa.add(methThresh, cFilt);
		cFilt.gridy++;

		cFilt.gridx = 1;
		filtPa.add(new HelpButton("Present/absent calls method:\nHere you can filter the data according to present/absent calls. You have to load these calls as " +
		"attribute prior to this method."), cFilt);
		cFilt.gridx++;
		filtPa.add(methAbs, cFilt);
		cFilt.gridx = 1;
		cFilt.gridy++;

		filtPa.add(new HelpButton("PQS - threshold method:\nHere you can enter a numerical threshold; every probeset with a quality score lower than this" +
				" threshold will be retained. Please note that normalization will be started for this method, if it has not been performed" +
		" yet."), cFilt);
		cFilt.gridx++;
		filtPa.add(methPQS, cFilt);
		cFilt.gridx = 1;
		cFilt.gridy++;

		filtPa.add(new HelpButton("PQS - optimal probeset method:\nThis method is also based on quality scores; for each gene only the probeset with lowest quality score " +
				"will be retained. Please note that this method can only be used if you have loaded gene names as an attribute; normalization " +
		"will be started if it has not been performed yet."), cFilt);
		cFilt.gridx++;
		filtPa.add(methPQSMin, cFilt);
		cFilt.gridy += 3;
		sep2                  = new JSeparator();
		GridBagConstraints c2 = new GridBagConstraints();
		//Add a start button
		JButton enter = new JButton("Start ");
		enter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<String> meths = guiData.getMethods();
				if(meths.size() > 0) {

					if(methPQS.isSelected() || methPQSMin.isSelected()) {
						menuHalfLife.setEnabled(true);
					}

					//start filtering when pressed
					guiData.prepareData();
					dataObj = guiData.getData();
					guiNorm.setData(dataObj);
					guiHL.setData(dataObj);
					//display information about number of probe sets
					String message = "Your data has been filtered!\nThere are now "+guiData.getSize()+" probe sets.";
					JOptionPane.showMessageDialog(Gui.this, message, "Done filtering", JOptionPane.PLAIN_MESSAGE);

					//reset the filtering panel for subsequent filtering
					methAbs.setSelected(false);
					methThresh.setSelected(false);
					methPQS.setSelected(false);
					methPQSMin.setSelected(false);
					none.setSelected(true);
					methAbs.setActionCommand("enable");
					methThresh.setActionCommand("enable");
					methPQS.setActionCommand("enable");
					methPQSMin.setActionCommand("enable");
				} else {
					DisplayWarnings.warning("You didn't choose a filtering method;\nNo filtering has been performed.", Gui.this);
				}

			}
		});
		cFilt.anchor = GridBagConstraints.EAST;
		cFilt.fill   = GridBagConstraints.NONE;
		HelpButton help = new HelpButton("Choose any number of filtering methods and start the filtering with 'Start'. You can still refine " +
				"the filtering afterwards, any new choices will be used additionally to the previous choices;" +
		" to reset the filtering completely click 'Clear'.");
		filtPa.add(help, cFilt);
		cFilt.gridx++;

		filtPa.add(enter, cFilt);
		//Add clear button
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearFiltering();
			}
		});
		cFilt.gridy++;
		filtPa.add(clear, cFilt);

		//Add save button
		JButton save = new JButton("Save ");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveData();
			}
		});

		cFilt.gridy++;
		filtPa.add(save, cFilt);
		cFilt.gridx = 0;
		cFilt.anchor = GridBagConstraints.WEST;

		cFilt.gridy++;
		c2.fill      = GridBagConstraints.HORIZONTAL;
		c2.gridx     = 0;
		c2.gridy     = cFilt.gridy;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		filtPa.add(sep2, c2);

		//action listener for all filtering methods
		methThresh.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JRadioButton thresh = (JRadioButton) e.getSource();
				if(e.getActionCommand().equals("enable")) {
					//add threshold filtering to list of methods
					thresh.setActionCommand("disable");
					String enter  = "Enter threshold";
					Component gui = Gui.this;
					String input  = JOptionPane.showInputDialog(gui, "Please enter the threshold for filtering the data:\n" +
							"(Minimum in data: "+dataObj.getMinimum()+")\n(Maximum in data: "+dataObj.getMaximum()+")",
							enter, JOptionPane.PLAIN_MESSAGE);
					if(input != null) {
						try {
							input = input.replace(",", ".");
							Double.parseDouble(input);
							guiData.addMethod(GuiFilterData.THRESHOLD, input);
						} catch (NumberFormatException ex) {
							DisplayWarnings.error("You need to specify a numerical threshold!", Gui.this);
							thresh.setActionCommand("enable");
							none.setSelected(true);
						}
					} else {
						thresh.setActionCommand("enable");
						none.setSelected(true);
					}
				} else {
					thresh.setActionCommand("enable");
				}
			}
		});

		methAbs.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if(methAbs.getActionCommand().equals("enable")) {

					if(dataObj.getAttrDescr() != null) {
						absenceLabel = new ArrayList<String>();
						methAbs.setActionCommand("disable");
						ArrayList<String> tempLabels = labels;
						for(String descr : dataObj.getAttrDescr()) {
							if(!tempLabels.contains(descr)) {
								tempLabels.add(descr);
							}
						}

						ArrayList<Component> components = new ArrayList<Component>();

						for(String item : tempLabels) {
							if(!columnsNew.contains(item) && !columnsPre.contains(item) && !columnsTotal.contains(item)) {
								JCheckBox lab = new JCheckBox(item);
								lab.setActionCommand("enable");
								components.add(lab);

								lab.addActionListener(new ActionListener() {

									@Override
									public void actionPerformed(ActionEvent e) {
										JCheckBox lab = (JCheckBox) e.getSource();
										if(lab.getActionCommand().equals("enable")) {
											lab.setActionCommand("disable");
											absenceLabel.add(lab.getText());
										} else {
											lab.setActionCommand("enable");
											if(absenceLabel.contains(lab.getText())) {
												absenceLabel.remove(lab.getText());
											}
										}
									}
								});
							}
						}
						LabelChooserDialog dialogLabels = new LabelChooserDialog(Gui.this, "Define labels", "Please choose all labels describing absence calls:", components);
						dialogLabels.setVisible(true);
						cancel = dialogLabels.isCancel();

						Object[] message2 = new Object[5];
						message2[0]       = "Please enter the call which you want to use:";
						JTextField call   = new JTextField();
						message2[1]       = call;
						message2[2]       = "Please enter the threshold:";
						JTextField thresh = new JTextField();
						thresh.setText("1");
						HelpButton help   = new HelpButton("Only probesets will be " +
						"retained for which the number of times the given call value is observed is below the threshold value.");
						Color color       = Gui.this.getBackground();
						help.setBackground(new Color(color.getBlue(),color.getGreen(),color.getRed()));
						message2[3]       = thresh;
						message2[4]       = help;

						if(absenceLabel.size() > 0 && !cancel) {
							JOptionPane.showMessageDialog(Gui.this, message2, "Define calls", JOptionPane.PLAIN_MESSAGE);
							try {
								Integer.parseInt(thresh.getText().trim());
								if(absenceLabel.size() > 0) {
									StringBuffer buff = new StringBuffer("");
									for(String la : absenceLabel) {
										buff.append(la+";");
									}
									buff.deleteCharAt(buff.length()-1);
									buff.append("|"+call.getText()+"|"+thresh.getText());
									//add absence filtering to list of methods
									guiData.addMethod(GuiFilterData.PRESENT, buff.toString());
								} else {
									methAbs.setActionCommand("enable");
									none.setSelected(true);
								}
							} catch (NumberFormatException ex) {
								DisplayWarnings.error("Your threshold needs to be numerical!", Gui.this);
							}
						} else {
							methAbs.setActionCommand("enable");
							none.setSelected(true);
						}
					} else {
						DisplayWarnings.error("You need to load present/absent call attributes before filtering " +
								"according to these!", Gui.this);
						methAbs.setActionCommand("enable");
						none.setSelected(true);
					} 

				} else {
					methAbs.setActionCommand("enable");
				}
			}

		});

		methPQS.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog(Gui.this, "Please enter the threshold for filtering the quality" +
						" control scores:\n(Minimum in data: "+dataObj.getMinimum()+")\n(Maximum in data: "+
						dataObj.getMaximum()+")", "Enter threshold", JOptionPane.PLAIN_MESSAGE);
				if(input != null) {
					try {
						if(input.isEmpty()) {
							throw new NullPointerException();
						}
						input = input.replace(",", ".");
						Double.parseDouble(input);
						savePQS();

						guiData.setPqsPlot(plotPQS());
						//add method to list of methods
						guiData.addMethod(GuiFilterData.PQS, input);

						//if normalization has not been performed, choose method for normalization for evaluation
						if(!guiNorm.hasNormalization()) {
							Object[] message2 = new Object[2];
							message2[0]       = "Please choose a normalization method for evaluation";
							JRadioButton lrm  = new JRadioButton("Standard method");
							lrm.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									//perform linear regression
									performNormalization();
								}
							});
							//if no other method is checked, perform normalization using standard method
							lrm.setSelected(true);
							performNormalization();
							message2[1] = lrm;
							JOptionPane.showMessageDialog(Gui.this, message2, "Normalization", JOptionPane.PLAIN_MESSAGE);
							subMenuCorr();
							menuCalcMed.setEnabled(true);
							menuMed.setEnabled(true);
							menuPQS.setEnabled(true);
						} else {
							//if normalization has been performed, use this object
							eval = guiNorm.getNormalization();
							guiData.setNorm(eval);
						}
					} catch (NullPointerException ex) {
						DisplayWarnings.error("You need to enter a numerical threshold!", Gui.this);
						methPQS.setActionCommand("enable");
						none.setSelected(true);
					}
				} else {
					methPQS.setActionCommand("enable");
					none.setSelected(true);
				}
			}
		});

		methPQSMin.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if(guiData.getGenename().equals(guiData.getOriginalGene())) {
					actionSetGene();
				}

				int rep = replicate;
				boolean error = false;
				if(replicate == UNDEF) {
					String input = JOptionPane.showInputDialog(Gui.this, "Please enter a replicate (optional):", "Enter replicate", JOptionPane.PLAIN_MESSAGE);
					if(input != null) {
						try {
							if(input.isEmpty()) {
								input = "-1";
							} 
							rep = Integer.parseInt(input);
							guiNorm.setReplicate(rep);
						} catch(NumberFormatException ex) {
							DisplayWarnings.error("You need to enter a numerical threshold!", Gui.this);
							methPQSMin.setActionCommand("enable");
							none.setSelected(true);
						} catch (NullPointerException ex) {
							error = true;
                                                        ex.printStackTrace();
						}
					} else {
						none.setSelected(true);
						error = true;
					}
				} else {
					guiNorm.setReplicate(replicate);
				}
				if(guiData.hasGeneNames() && !error) {
					//if normalization has not yet been performed, start it
					if(!guiNorm.hasNormalization()) {
						Object[] message2 = new Object[2];
						message2[0]       = "Please choose a normalization method for evaluation";
						JRadioButton lrm  = new JRadioButton("Standard method");
						lrm.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								performNormalization();
							}
						});
						lrm.setSelected(true);
						performNormalization();
						message2[1] = lrm;
						JOptionPane.showMessageDialog(Gui.this, message2, "Normalization", JOptionPane.PLAIN_MESSAGE);
						//						normalizationResults();
					} else {
						eval = guiNorm.getNormalization();
						guiData.setNorm(eval);
					}
					savePQS();
					guiData.setPqsPlot(plotPQS());
					guiData.addMethod(GuiFilterData.PQSMIN, String.valueOf(rep));
					menuPQS.setEnabled(true);
				} else if(!error && !none.isSelected()){
					none.setSelected(true);
					DisplayWarnings.error("You need to load gene name attributes\nprior to PQS " +
							"min filtering!\n", Gui.this);
				}
			}

		});

		//hide sub panel 
		filtPa.setVisible(false);
		return filtPa;
	}

	/**
	 * Performs the normalization and sets all necessary values
	 */
	private void performNormalization() {
		guiNorm = new GuiNormal();
		guiNorm.setData(dataObj);
		guiNorm.setMethod(GuiNormal.STANDARD);
		normMethod = GuiNormal.STANDARD;
		guiNorm.calcNormalization();
		eval = guiNorm.getNormalization();
		guiData.setNorm(eval);
		guiHL.setCorr(guiNorm.getCF());
	}

	/**
	 * Prepare saving of quality scores into a file
	 */
	private void savePQS() {
		//Display dialog window to choose, if saving is wanted
		int answer = JOptionPane.showConfirmDialog(Gui.this, "Do you want to save the quality scores into a file?", "Quality scores", JOptionPane.YES_NO_OPTION);

		if(answer == JOptionPane.YES_OPTION) {
			JFileChooser choose = new JFileChooser();
			int returnVal       = choose.showSaveDialog(Gui.this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				if(choose.getSelectedFile().exists()) {
					if(overwriteFile()) {
						pqs = choose.getSelectedFile();
						guiData.setPQSName(pqs.getAbsolutePath());
					}
				} else {
					pqs = choose.getSelectedFile();
					guiData.setPQSName(pqs.getAbsolutePath());
				}

			}
		} else {
			pqs = null;
		}
	}

	/**
	 * Show dialog that offers the possibility to plot the PQS histogram
	 * @return TRUE if PQS histogram should be plotted, FALSE otherwise
	 */
	private boolean plotPQS() {
		int answer = JOptionPane.showConfirmDialog(Gui.this, "Do you want to plot the quality scores?", "Plot Quality scores", JOptionPane.YES_NO_OPTION);

		if(answer == JOptionPane.YES_OPTION) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Open sub panel containing instruments for normalization
	 * @param c Layout information of great panel
	 * @return Sub panel for normalization
	 */
	private JPanel subMenuNorm(GridBagConstraints c) {
		GridBagConstraints cFilt = new GridBagConstraints();

		cFilt.gridx   = 0;
		cFilt.gridy   = 0;
		cFilt.weightx = c.weightx;
		cFilt.weighty = c.weighty;
		cFilt.insets  = new Insets(4,4,4,4);

		normPa = new JPanel(new GridBagLayout());
		cFilt.gridy++;
		cFilt.gridx += 2;
		cFilt.anchor = GridBagConstraints.CENTER;
		normPa.add(new JLabel("Normalization"), cFilt);
		cFilt.anchor = GridBagConstraints.EAST;
		cFilt.fill   = GridBagConstraints.NONE;
		cFilt.gridx = 0;
		cFilt.gridy++;
		normPa.add(new HelpButton("In this section you can perform the normalization. After choosing a method by clicking " +
				"on the 'Normalization method' button, as well as choosing how to calculate the ratios of all replicates, you can " +
				"start the normalization with the 'Start' button.\n\n" +
				"Linear regression: This method performs a simple linear regression for normalization.This method is only available " +
				"if you have an equal amount of replicates for newly transcribed, total and pre-existing RNA.\n\n" +
		"Median half-life based: Here you can normalize your data based on a median value you have to enter."), cFilt);
		cFilt.gridx++;
		cFilt.anchor = GridBagConstraints.WEST;
		normPa.add(new JLabel("Choose method: "), cFilt);
		cFilt.gridx++;
		cFilt.anchor   = GridBagConstraints.CENTER;
		//Add button to choose normalization method
		JButton choose = new JButton("Normalization method");
		choose.setActionCommand("enable");
		choose.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int answer = JOptionPane.YES_OPTION;
				if(guiNorm.hasNormalization()) {
					answer = JOptionPane.showConfirmDialog(Gui.this, "You already performed normalization.\n" +
							"Repeating normalization will result in different values!\nAre you sure you want to continue?", "Warning", JOptionPane.YES_NO_OPTION);
				}

				/*
				 * Adapt this part if you have implemented your own method:
				 */
				if(answer == JOptionPane.YES_OPTION) {
					startHLButton.setEnabled(false);
					calc.setEnabled(true);
					renormalize = true;
					//Display all possible normalization methods (at the moment only one)
					int size           = 2;
					if(!unequal) {
						size = 3;
					}
					Object[] message   = new Object[size];
					message[0]         = "Please choose a method for normalization:";
					int i              = 1;
					ButtonGroup group  = new ButtonGroup();
					if(!unequal) {
						JRadioButton stand = new JRadioButton("Linear regression");
						group.add(stand);
						stand.setSelected(true);
						normMethod = GuiNormal.STANDARD;
						stand.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								//set method to standard
								normMethod = GuiNormal.STANDARD;
							}
						});
						message[i]       = stand;
						i++;
					} 

					JRadioButton med = new JRadioButton("Median half-life based");
					med.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							normMethod = GuiNormal.MEDIAN;
							calc.setEnabled(false);
						}
					});
					group.add(med);
					message[i]       = med;


					JOptionPane.showMessageDialog(Gui.this, message, "Choose method", JOptionPane.PLAIN_MESSAGE);

					if(normMethod.equals(GuiNormal.STANDARD)) {
						Object[] messageRatioMethod = new Object[4];
						messageRatioMethod[0]       = "Please choose a method for ratio calculation:";
						ButtonGroup group2          = new ButtonGroup();
						JRadioButton averageFirst   = new JRadioButton("Average first, then ratio calculation");
						JRadioButton ratioFirst     = new JRadioButton("Ratio first, then average calculation");
						JRadioButton repliButton    = new JRadioButton("Ratio calculation for one replicate");
						group2.add(averageFirst);
						group2.add(ratioFirst);
						group2.add(repliButton);
						ratioFirst.setSelected(true);
						averageFirst.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								ratioMethod = Data.AVERAGEFIRST;
								guiNorm.setRatioMethod(ratioMethod);
								dataObj.setMethod(ratioMethod);
								guiHL.setRatioMethod(ratioMethod);
							}
						});
						ratioFirst.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								ratioMethod = Data.RATIOFIRST;
								guiNorm.setRatioMethod(ratioMethod);
								dataObj.setMethod(ratioMethod);
								guiHL.setRatioMethod(ratioMethod);
							}
						});
						repliButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								if(replicate == UNDEF) {
									String response = JOptionPane.showInputDialog(Gui.this, "Please enter the number of your replicate", "Replicate number", JOptionPane.INFORMATION_MESSAGE);
									try {
										int repli = Integer.parseInt(response)-1;
										guiNorm.setReplicate(repli);
										dataObj.setReplicate(repli);
										ratioMethod = Data.REPLICATE;
										guiNorm.setRatioMethod(ratioMethod);
										dataObj.setMethod(ratioMethod);
										//										guiHL.setRatioMethod(ratioMethod);
									} catch (NumberFormatException ex) {
										DisplayWarnings.error("Your replicate has to be numerical", Gui.this);
									}
								} else {
									ratioMethod = Data.REPLICATE;
									guiNorm.setReplicate(replicate);
									dataObj.setReplicate(replicate);
									guiNorm.setRatioMethod(ratioMethod);
									dataObj.setMethod(ratioMethod);
									//									guiHL.setRatioMethod(ratioMethod);
								}
							}
						});

						messageRatioMethod[1] = averageFirst;
						messageRatioMethod[2] = ratioFirst;
						messageRatioMethod[3] = repliButton;

						JOptionPane.showMessageDialog(Gui.this, messageRatioMethod, "Choose ratio method", JOptionPane.PLAIN_MESSAGE);
					} else if(normMethod.equals(GuiNormal.MEDIAN)){
						startMedianBasedCorrectionFactors(false);
					}

				} else {
					subMenuCorr();
				}

			}
		});

		if(guiNorm.hasNormalization()) {
			normPa.add(new JLabel(String.valueOf(guiNorm.getCF().getC_l())), cFilt);
		} else {
		}

		normPa.add(choose, cFilt);
		cFilt.gridx++;
		cFilt.gridy++;
		cFilt.anchor  = GridBagConstraints.EAST;
		//Add button to start calculations
		JButton enter = new JButton("Start");

		enter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(normMethod != null || renormalize) {
					//start normalization when pressed
					//enable half-life checkbox
					menuHalfLife.setEnabled(true);
					if(!normMethod.equals(GuiNormal.MEDIAN)) {
						guiNorm.setMethod(normMethod);
						guiNorm.calcNormalization();
						plotNorm.setEnabled(true);
						subMenuCorr();
					} else {
						subMenuCorr();
						//						displayMedianBasedCorrectionFactors(false);
					}
					menuPQS.setEnabled(true);

				} else if(guiNorm.hasNormalization()){
					plotNorm.setEnabled(true);
					menuPQS.setEnabled(true);
					subMenuCorr();
				} else {
					DisplayWarnings.error("You have to choose a normalization method first!", Gui.this);
				}
			}
		});
		normPa.add(enter, cFilt);
		cFilt.gridy++;
		plotNorm = new JButton("Plot ");
		plotNorm.setEnabled(false);
		plotNorm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				plotNormalization();
			}
		});
		normPa.add(plotNorm, cFilt);
		cFilt.gridx = 0;
		cFilt.anchor = GridBagConstraints.WEST;
		sep3 = new JSeparator();
		cFilt.gridy++;
		GridBagConstraints c2 = new GridBagConstraints();
		c2.fill               = GridBagConstraints.HORIZONTAL;
		c2.gridx              = 0;
		c2.gridy              = cFilt.gridy;
		c2.gridwidth          = GridBagConstraints.REMAINDER;
		normPa.add(sep3, c2);
		normPa.setVisible(false);
		return normPa;
	}

	private void subMenuCorr() {
		guiHL.setCorr(guiNorm.getCF());
		GridBagConstraints cFilt = new GridBagConstraints();
		if(!corrExpanded) {
			enlargeWindow(110);
		} else {
			corrPa.setVisible(false);
			corrPa = new JPanel(new GridBagLayout());
			panel.add(corrPa, c);
		}

		cFilt.gridx   = 0;
		cFilt.gridy   = 0;
		cFilt.weightx = c.weightx;
		cFilt.weighty = c.weighty;
		cFilt.insets  = new Insets(4,4,4,4);

		if(guiNorm.hasNormalization() && !normMethod.equals(GuiNormal.MEDIAN)) {
			corrPa.add(new JLabel("Your correction factors:"), cFilt);
			cFilt.gridx++;
			cFilt.gridy++;
			double c_l  = guiNorm.getCF().getC_l();
			double c_u  = guiNorm.getCF().getC_u();
			double c_lu = guiNorm.getCF().getC_lu();
			if(c_l != Normalization.UNDEF) {
				cFilt = addCF("c_l:", cFilt, c_l);
			}
			if(c_u != Normalization.UNDEF) {
				cFilt = addCF("c_u:", cFilt, c_u);
			}
			if(c_lu != Normalization.UNDEF) {
				cFilt = addCF("c_lu:", cFilt, c_lu);
			}

		} else if(display.length() > 0 && medCorr != null) {
			corrPa.add(new JLabel("Your correction factors:"), cFilt);
			cFilt.gridx++;
			cFilt.gridy++;
			String[] all = display.toString().split(" # ");
			int i            = 0;

			for(String item : all) {
				String[] split = item.split(", ");
				//Translate short form into more detailed description
				if(split[0].equals(HalfLife.NEWLY)) {
					cFilt = addCF("c_l:", cFilt, medCorr.get(i).getC_l());
				} else if(split[0].equals(HalfLife.PRE)) {
					cFilt = addCF("c_u:", cFilt, medCorr.get(i).getC_u());
				} else if(split[0].equals(HalfLife.NEWPRE)) {
					cFilt = addCF("c_lu:", cFilt, medCorr.get(i).getC_lu());
				}
				i++;
			}
		}
		cFilt.gridy++;
		cFilt.gridy++;
		GridBagConstraints c2 = new GridBagConstraints();
		c2.fill               = GridBagConstraints.HORIZONTAL;
		c2.gridx              = 0;
		c2.gridy              = cFilt.gridy;
		c2.gridwidth          = GridBagConstraints.REMAINDER;
		corrPa.add(new JSeparator(), c2);
		corrPa.setVisible(true);
		corrExpanded = true;
	}

	private GridBagConstraints addCF(String cfdesc, GridBagConstraints c, double cf) {
		if(cf != Normalization.UNDEF) {
			corrPa.add(new JLabel(cfdesc), c);
			c.gridx++;
			JTextField field = new JTextField(String.valueOf(cf));
			field.setEditable(false);
			field.setBorder(null);
			corrPa.add(field, c);
			c.gridx--;
			c.gridy++;
		}
		return c;
	}

	//	/**
	//	 * Displays and sets the results (correction factors) from normalization 
	//	 */
	//	private void normalizationResults() {
	//		CorrectionFactors corr = guiNorm.getCF();
	//		guiHL.setCorr(corr);
	//		displayCorrs(corr);
	//	}

	/**
	 * Creates a plot for the normalization
	 */
	private void plotNormalization() {
		//check if the plot has already been created, if not generate new one
		if(normGraphConstructor == null) {
			normGraphConstructor = GraphHandler.plotNormalization(guiNorm.getNormalization(), dataObj);
		} else {
			normGraphConstructor.generateGraph();
		}
		//ask if graph should be saved
		FileFilter filter = new JPEGFilter();
		File output       = GuiTools.displaySaveDialog(Gui.this, "Do you want to save the normalization graph?", filter);
		if(output != null) {
			normGraphConstructor.saveGraph(output);
		}
		File plotFile = savePlottingFileDialog();
		if(plotFile != null) {
			normGraphConstructor.savePlotFile(plotFile);
		}
	}

	public File savePlottingFileDialog() {
		int response = JOptionPane.showConfirmDialog(Gui.this, "Do you want to save your plotting information?",
				"Save plotting file?", JOptionPane.YES_NO_OPTION);

		if(response == JOptionPane.YES_OPTION) {
			JFileChooser chooser = new JFileChooser();
			int returnVal = chooser.showSaveDialog(Gui.this);

			if(returnVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if((file.exists() && overwriteFile()) || !file.exists()) {
					return file;
				}
			}
		}
		return null;
	}

	//	/**
	//	 * Dialog window that displays a set of correction factors
	//	 * @param corr The correction factors
	//	 */
	//	private void displayCorrs(CorrectionFactors corr) {
	//		//Display resulting correction factors as dialog window
	//		Object[] message = new Object[2];
	//		message[0]       = "The resulting Correction Factors are:";
	//		JTextArea area   = new JTextArea();
	//		area.setBorder(new BevelBorder(BevelBorder.LOWERED));
	//		area.setText("c_l: "+corr.getC_l()+"\nc_u: "+corr.getC_u()+"\nc_lu: "+corr.getC_lu());
	//		area.setEditable(false);
	//		message[1]       = area;
	//		JOptionPane.showMessageDialog(Gui.this,message, "Correction Factors", JOptionPane.PLAIN_MESSAGE);
	//	}

	/**
	 * Open sub panel containing half-life calculation instruments
	 * @param c Layout information from big panel
	 * @return Sub panel with half-life calculation instruments
	 */
	private JPanel subMenuHL(GridBagConstraints c) {
		GridBagConstraints cFilt = new GridBagConstraints();

		cFilt.gridx   = 0;
		cFilt.gridy   = 0;
		cFilt.weightx = c.weightx;
		cFilt.weighty = c.weighty;
		cFilt.insets  = new Insets(4,4,4,4);

		display = new StringBuffer("");
		hlPa    = new JPanel(new GridBagLayout());
		cFilt.gridy++;
		cFilt.gridx += 2;
		cFilt.anchor = GridBagConstraints.CENTER;
		hlPa.add(new JLabel("Half-life calculation"), cFilt);
		cFilt.gridx = 0;
		cFilt.gridy++;
		HelpButton help = new HelpButton("In this section you can perform half life calculations.\n" +
				"When you click on the 'Add calculation' button a dialog window will appear, where you can choose a type " +
				"of half-life calculation, a labeling time t and (optionally) the number of a replicate you want to use. If no replicate " +
				"is defined, the ratio of all replicates will be used (calculation method as defined in section Normalization). You can " +
				"always add more types of calculations through repetitive clicking on the 'Add calculation' button, and get an overview " +
				"of every chosen method via the 'Display choices' button.\n" +
		"\nYou can reset your calculations with the 'Clear' button; in order to start them you have to use the 'Start' button.");
		cFilt.anchor = GridBagConstraints.EAST;
		hlPa.add(help, cFilt);
		cFilt.gridx++;
		cFilt.anchor = GridBagConstraints.WEST;
		hlPa.add(new JLabel("Choose methods:"), cFilt);
		cFilt.anchor = GridBagConstraints.CENTER;
		cFilt.fill   = GridBagConstraints.HORIZONTAL;
		cFilt.gridx++;
		startDisplay = cFilt.gridy+1;

		//Button for adding a half-life calculation
		calc = new JButton("Add calculation");
		calc.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				hlCalculationDialog();
				GridBagConstraints grid = new GridBagConstraints();
				grid.anchor             = GridBagConstraints.SOUTHWEST;
				grid.gridy              = startDisplay;
			}
		});

		hlPa.add(calc, cFilt);
		cFilt.gridx  = 1;
		cFilt.gridy++;
		cFilt.anchor = GridBagConstraints.WEST;
		//Add button to display all methods that have been chosen till now
		hlPa.add(new JLabel("Display methods:"), cFilt);
		cFilt.anchor = GridBagConstraints.CENTER;
		cFilt.gridx++;
		JButton see  = new JButton("Display choices");
		see.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayHalfLifeCalculations(display);
			}
		});

		hlPa.add(see, cFilt);
		cFilt.gridx++;
		cFilt.fill    = GridBagConstraints.NONE;
		//Add start button
		startHLButton = new JButton("Start ");
		startHLButton.setEnabled(false);
		startHLButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				guiHL.setAlpha(alpha);
				if(display.length()>0) {
					//calculate half-lives when pressed
					if(normMethod.equals(GuiNormal.MEDIAN)) {
						guiHL.calcHLonMedian(medCorr);
					} else {
						guiHL.calcHL();
					}
					JOptionPane.showMessageDialog(Gui.this, "Your half-lives have been calculated.", 
							"Half-life calculation done", JOptionPane.PLAIN_MESSAGE);
					plotHalf.setEnabled(true);
					saveHLs.setEnabled(true);
					menuSubSave.setEnabled(true);
					if(guiData.hasGenes()) {
						menuEva.setEnabled(true);
						menuBias.setEnabled(true);
					}
				} else {
					DisplayWarnings.error("You have to choose a half-life calculation method first!", Gui.this);
				}

			}
		});

		cFilt.anchor = GridBagConstraints.EAST;
		hlPa.add(startHLButton, cFilt);
		cFilt.gridy++;

		plotHalf = new JButton(" Plot ");
		plotHalf.setEnabled(false);
		plotHalf.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				plotHL();
			}
		});
		hlPa.add(plotHalf, cFilt);
		cFilt.gridy++;

		//Add clear button to reset all half-lives to null
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				display = new StringBuffer("");
				guiHL.clearHL();
				saveHLs.setEnabled(false);
				plotHalf.setEnabled(false);
				JOptionPane.showMessageDialog(Gui.this, "Your calculations are now reset", "Cleared", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		hlPa.add(clear, cFilt);
		cFilt.gridy++;
		//Add button for saving of half-lives
		saveHLs = new JButton("Save ");
		saveHLs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = JOptionPane.showConfirmDialog(Gui.this, "Do you want to save half-lives with attributes?", 
						"Define values", JOptionPane.YES_NO_OPTION);
				if(returnVal == JOptionPane.YES_OPTION) {
					saveHLAttr();
				} else {
					saveHLPrep();
				}
			}
		});
		saveHLs.setEnabled(false);
		hlPa.add(saveHLs, cFilt);
		sep3 = new JSeparator();
		cFilt.gridy++;
		GridBagConstraints c2 = new GridBagConstraints();
		c2.fill               = GridBagConstraints.HORIZONTAL;
		c2.gridx              = 0;
		c2.gridy              = cFilt.gridy;
		c2.gridwidth          = GridBagConstraints.REMAINDER;
		hlPa.add(sep3, c2);
		hlPa.setVisible(false);
		return hlPa;
	}

	/**
	 * Displays a dialog for the choosing of half-life calculation settings; among these are 
	 * method, time and (optionally) replicate. Only parameters that are not set through the main 
	 * menu are available.
	 */
	private void hlCalculationDialog() {

		int index   = 0;
		int length  = 8;
		double time = UNDEF;

		if(labelingTime != UNDEF) {
			length -= 2;
		} 
		if(replicate != UNDEF) {
			length -= 2;
		}
		Object[] message = new Object[length];
		//Display all possible methods
		message[0]        = "Choose method";
		JRadioButton met1 = new JRadioButton("Newly transcribed/Total based");
		met1.setSelected(true);
		JRadioButton met2 = new JRadioButton("Pre-existing/Total based");
		JRadioButton met3 = new JRadioButton("Newly transcribed/Pre-existing based");
		ButtonGroup group = new ButtonGroup();
		group.add(met1); group.add(met2); group.add(met3);
		method = "new";
		//set method according to action
		met1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				method = "new";
			}
		});
		met2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				method = "pre";
			}
		});
		met3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				method = "new/pre";
			}
		});

		message[1]    = met1;
		message[2]    = met2;
		message[3]    = met3;

		//Display text field for entering time if no constant time is set
		JTextField re = new JTextField();
		JTextField ti = new JTextField();
		index = 4;
		//check if constant time is set
		if(labelingTime == UNDEF) {
			message[4]    = "Enter labeling time";
			message[5]    = ti;
			index         = 6;
		}

		//check if constant replicate is set, otherwise provide possibility to enter replicate
		if(replicate == Normalization.UNDEF) {
			message[index]    = "Enter number of replicate (optional):";
			message[index+1]  = re;
		}

		//Display dialog window containing all options
		JOptionPane.showMessageDialog(Gui.this, message, "Add calculation", JOptionPane.PLAIN_MESSAGE);

		try {
			if(labelingTime == UNDEF) {
				//check if anything has been written into the text field
				if(ti.getText().length()>0) {
					time = Integer.parseInt(ti.getText());
				} 
			} else {
				time = labelingTime;
			}
			if(replicate == UNDEF ) {
				//if so, set replicate
				if(re.getText().length() > 0) {
					replicate = Integer.parseInt(re.getText())-1;
					if(replicate < 0 || replicate >= columnsTotal.size() || replicate >= columnsNew.size() 
							|| replicate >= columnsPre.size()) {
						replicate = UNDEF;
						DisplayWarnings.error("Your replicate number exceeds the number of \nreplicates present in the data! It has to\n" +
								"be between 1 and "+columnsTotal.size()+".", Gui.this);
					} 

				}
			}

			if(time != UNDEF) {
				//add values to half-life calculations
				replicates.add(replicate);
				guiHL.addHL(method, time, replicate);
				display.append(method+", "+time+", "+replicate+" # ");
				startHLButton.setEnabled(true);
			}

			//check if a number has been written into the text field, if not display error message
		} catch(NumberFormatException ex) {
			String what = "time";
			if(re.getText().length() > 0) {
				what    = "replicate";
			}
			DisplayWarnings.error("You need to specify a (numerical) "+what+" for calculation!", Gui.this);
		}

	}

	/**
	 * Check, which values (half-life, ratio or both) should be saved, and call second saving step
	 */
	private void saveHLPrep() {
		//save only if half-lives have been calculated
		if(guiHL.hasHL()) {

			//ask which values should be saved
			Object[] message = new Object[4];
			message[0]       = "Which values do you want to save?";
			JRadioButton hls = new JRadioButton("Half-life values");
			JRadioButton rs  = new JRadioButton("Ratio values");
			JRadioButton bo  = new JRadioButton("Both values");
			which            = HalfLifeWriter.HALFLIFE;
			hls.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					which = HalfLifeWriter.HALFLIFE;
				}
			});
			rs.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					which = HalfLifeWriter.RATIO;
				}
			});
			bo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					which = HalfLifeWriter.BOTH;
				}
			});
			ButtonGroup group = new ButtonGroup();
			group.add(rs); group.add(hls); group.add(bo);
			hls.setSelected(true);

			message[1]		 = hls;
			message[2]       = rs;
			message[3]       = bo;
			//display dialog with radiobuttons
			JOptionPane.showMessageDialog(Gui.this, message, "Choose values", JOptionPane.PLAIN_MESSAGE);

			//continue with saving
			saveHLComp();
			//if no half-lives have been calculated, display error message
		} else {
			DisplayWarnings.error("You need to start half-life calculation first!", Gui.this);
		}


	}

	/**
	 * Save half-life values after definition, which values should be exported
	 */
	private void saveHLComp() {
		//save only if half-lives have been calculated
		if(guiHL.hasHL()) {

			//Afterwards, open browse dialog
			JFileChooser choose = new JFileChooser();
			int returnVal       = choose.showSaveDialog(Gui.this);

			//if file has been chosen, start saving
			if (returnVal == JFileChooser.APPROVE_OPTION) {

				if(choose.getSelectedFile().exists()) {
					if(overwriteFile()) {
						outputHL = choose.getSelectedFile();
						guiHL.setOutput(outputHL.getAbsolutePath(), which);
						guiHL.saveHL();
					}
				} else {
					outputHL = choose.getSelectedFile();
					guiHL.setOutput(outputHL.getAbsolutePath(), which);
					guiHL.saveHL();
				}
			}
			//if no half-lives have been calculated, display error message
		} else {
			DisplayWarnings.error("You need to start half-life calculation first!", Gui.this);
		}

	}

	/**
	 * Prepares the (filtered) data for being saved, calls saving method
	 */
	private void saveData() {
		//check if there is already data loaded
		if(guiData.hasData()) {
			ArrayList<Component> components = new ArrayList<Component>();
			columnsNewOut   = new ArrayList<String>();
			columnsPreOut   = new ArrayList<String>();
			columnsTotalOut = new ArrayList<String>();
			attributesOut   = new ArrayList<String>();

			//Choose from the labels that were loaded those which should be exported
			//display newly transcribed labels first
			for(String item : columnsNew) {
				JCheckBox check = new JCheckBox(item);
				check.setActionCommand("enable");
				components.add(check);
				check.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						JCheckBox check = (JCheckBox)e.getSource();
						if(e.getActionCommand().equals("enable")) {
							check.setActionCommand("disable");
							columnsNewOut.add(check.getText());
						} else {
							check.setActionCommand("enable");
							columnsNewOut.remove(check.getText());
						}
					}
				});
			}
			components.add(new JSeparator());
			//display pre-existing labels
			for(String item : columnsPre) {
				JCheckBox check = new JCheckBox(item);
				check.setActionCommand("enable");
				components.add(check);
				check.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						JCheckBox check = (JCheckBox)e.getSource();
						if(e.getActionCommand().equals("enable")) {
							check.setActionCommand("disable");
							columnsPreOut.add(check.getText());
						} else {
							check.setActionCommand("enable");
							columnsPreOut.remove(check.getText());
						}
					}
				});
			}
			components.add(new JSeparator());
			//display total labels
			for(String item : columnsTotal) {
				JCheckBox check = new JCheckBox(item);
				check.setActionCommand("enable");
				components.add(check);
				check.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						JCheckBox check = (JCheckBox)e.getSource();
						if(e.getActionCommand().equals("enable")) {
							check.setActionCommand("disable");
							columnsTotalOut.add(check.getText());
						} else {
							check.setActionCommand("enable");
							columnsTotalOut.remove(check.getText());
						}
					}
				});
			}
			components.add(new JSeparator());
			if(dataObj.getAttrDescr() != null) {
				//display attribute labels
				for(String item : dataObj.getAttrDescr()) {
					JCheckBox check = new JCheckBox(item);
					check.setActionCommand("enable");
					components.add(check);
					check.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							JCheckBox check = (JCheckBox) e.getSource();
							if(e.getActionCommand().equals("enable")) {
								check.setActionCommand("disable");
								attributesOut.add(check.getText());
							} else {
								check.setActionCommand("enable");
								attributesOut.remove(check.getText());
							}
						}
					});
				}
			}

			//display dialog window with check boxes
			LabelChooserDialog dialog = new LabelChooserDialog(Gui.this, "Please choose the labels of the columns that you want to export:", "Choose labels", components);
			dialog.setVisible(true);

			if((columnsPreOut.size( )> 0 || columnsNewOut.size() > 0 || columnsTotalOut.size() > 0 
					|| attributesOut.size() > 0) && !dialog.isCancel()) {
				//open file browser
				JFileChooser choose = new JFileChooser();
				int returnVal       = choose.showSaveDialog(Gui.this);

				//if file was chosen, set values in data filter connection interface, start saving
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					boolean check = true;

					if(choose.getSelectedFile().exists()) {
						if(!overwriteFile()) {
							check = false;
						}
					}
					if(check) {
						outputData = choose.getSelectedFile();
						guiData.setNewOut(columnsNewOut);
						guiData.setPreOut(columnsPreOut);
						guiData.setTotOut(columnsTotalOut);
						guiData.setAttributesOut(attributesOut);
						guiData.setOutput(outputData);
						guiData.saveData();
					}
				}
			}

			//if no data has been loaded, display error message
		} else {
			DisplayWarnings.error("You need to load the data first!", Gui.this);
		}

	}

	/**
	 * Generates a Dialog window which displays all chosen half-life calculation settings
	 * @param display StringBuffer containing short description of calculation settings
	 */
	private void displayHalfLifeCalculations(StringBuffer display) {
		Object[] message = null;
		if(display.length() > 0) {
			String[] all = display.toString().split(" # ");

			message = new Object[all.length+1];
			message[0]       = "These are your selected half-life calculations:";
			int index        = 1;
			for(String item : all) {
				String[] split = item.split(", ");
				String meth    = "";

				//Translate short form into more detailed description
				if(split[0].equals("new")) {
					meth       = "Newly transcribed/Total based";
				} else if(split[0].equals("pre")) {
					meth       = "Pre-existing/Total based";
				} else if(split[0].equals("new/pre")) {
					meth       = "Newly transcribed/Pre-existing based";
				}
				String rep     = "";
				if(split.length > 2) {
					int repli      = Integer.parseInt(split[2]);
					if(repli != UNDEF) {
						rep        = ", Replicate: "+(repli+1);
					}
				}
				if(split[0].equals("Median half-life based")) {
					message[index] = "Method: Median half-life based";
				} else {
					message[index] = "Method: "+meth+", Labeling time: "+split[1]+rep;
				}
				index++;
			}
		} else {
			message    = new Object[1];
			message[0] = "You did not choose any methods";
		}
		//open dialog window
		JOptionPane.showMessageDialog(Gui.this, message, "Your chosen methods", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Lets user choose one of the list of calculated half-life methods
	 * @return Index of the chosen half-life
	 */
	private int chooseHalfLifeCalculation() {
		ArrayList<Component> list = new ArrayList<Component>();
		temporary                 = new ArrayList<String>();
		tempIndex                 = -1;

		//display pop-up only if there are half-lives calculated
		if(display.length() > 0) {
			String[] all = display.toString().split(" # ");

			ButtonGroup group = new ButtonGroup();
			for(String item : all) {
				String[] split = item.split(", ");
				String meth    = "";

				//Translate short form into more detailed description
				if(split[0].equals("new")) {
					meth       = "Newly transcribed/Total based";
				} else if(split[0].equals("pre")) {
					meth       = "Pre-existing/Total based";
				} else if(split[0].equals("new/pre")) {
					meth       = "Newly transcribed/Pre-existing based";
				}
				String rep     = "";
				if(split.length > 2) {
					int repli      = Integer.parseInt(split[2]);
					if(repli != UNDEF) {
						rep        = ", Replicate: "+(repli+1);
					}
				}
				String line;
				if(split[0].equals("Median half-life based")) {
					line = "Method: Median half-life based";
				} else {
					line = "Method: "+meth+", Labeling time: "+split[1]+rep;
				}

				//generate radio buttons for each method
				JRadioButton button = new JRadioButton(line);
				button.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						JRadioButton button = (JRadioButton) e.getSource();
						tempIndex = temporary.indexOf(button.getText());
					}
				});
				list.add(button);
				group.add(button);
				if(list.size()==1) {
					button.setSelected(true);
					tempIndex = 0;
				}
			}
			//if only one calculation has been performed, use this index
			if(list.size() == 1) {
				return 0;
				//otherwise let user choose
			} else {
				LabelChooserDialog dialog = new LabelChooserDialog(Gui.this, "Which calculation do you want to save?", 
						"Please choose the calculation you want to save:", list);
				dialog.setVisible(true);
				if(dialog.isCancel()) {
					return -1;
				}
				return tempIndex;
			}
		} else {
			DisplayWarnings.error("You can't save half-lives before calculating them!", Gui.this);
			return -1;
		}

	}

	/**
	 * Starts the evaluation procedure based on average number of uracils per sequence and log(e'/n')
	 * @param force TRUE if saving of plotting information should be forced
	 * @param corr The correction values from bias correction, null if not present
	 * @param plot TRUE if evaluation results should be plotted, FALSE otherwise
	 */
	private void startEvaluation(boolean force, HashMap<String, Double> corr, boolean plot) {
		
		plotQuality = true;

		Object[] choose   = new Object[4];
		choose[0]         = "Which ratios do you want to plot against uracil numbers?";
		JRadioButton en   = new JRadioButton(Data.LOGEN);
		JRadioButton un   = new JRadioButton(Data.LOGUN);
		JRadioButton eu   = new JRadioButton(Data.LOGEU);
		ButtonGroup group = new ButtonGroup();
		group.add(en); group.add(un); group.add(eu);
		en.setSelected(true);
		choose[1]       = en;
		choose[2]       = un;
		choose[3]       = eu;

		uraMeth  = Data.LOGEN;

		en.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				uraMeth = Data.LOGEN;
			}
		});

		un.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				uraMeth = Data.LOGUN;
			}
		});

		eu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				uraMeth = Data.LOGEU;
			}
		});

		//ask user if plotting data should be saved
		JOptionPane.showMessageDialog(Gui.this, choose, "Choose ratio", JOptionPane.PLAIN_MESSAGE);
		String printName = "";
		if(!force) {
			int answer = JOptionPane.showConfirmDialog(Gui.this, "Do you want to save the plotting data?", "Save into file?", JOptionPane.YES_NO_OPTION);
			if(answer == JOptionPane.YES_OPTION) {
				JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showSaveDialog(Gui.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					if(chooser.getSelectedFile().exists()) {
						if(overwriteFile()) {
							printName = chooser.getSelectedFile().getAbsolutePath();
						}
					}
					printName = chooser.getSelectedFile().getAbsolutePath();
				}
			} else {
				printName = URAFILENAME;
			}
		} else {
			printName = URAFILENAME;
		}

		JOptionPane.showMessageDialog(Gui.this, "Uracil numbers will be calculated. This may take a while.",
				"Starting uracil number calculation", JOptionPane.INFORMATION_MESSAGE);
                
		//perform the evaluation
		guiData.evaluateFasta(uraMeth, printName, corr, plot);
                
		//Display the results in a text area for copying
		double avgU     = guiData.getAvgU();
		double avgL     = guiData.getAvgL();
		Object[] message = new Object[2];
		message[0]       = "Evaluation results:";
		JTextArea area   = new JTextArea("Average uracil number: "+avgU+"\nAverage log(e'/n'): "+avgL);
		area.setBorder(new BevelBorder(BevelBorder.LOWERED));
		area.setEditable(false);
		message[1]       = area;
		JOptionPane.showMessageDialog(Gui.this, message, "Evaluation results", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Starts the correction of a bias in the data with R
	 */
	private void startBiasCorrection() {
		/*
		 * calculate Correlation coefficient with R
		 */

		int returnVal = JFileChooser.CANCEL_OPTION;
		if(pathR.isEmpty()) {
			//choose path of the R-installation
			JOptionPane.showMessageDialog(Gui.this, "Please choose the bin folder of your R-installation", "Choose R-installation path", JOptionPane.INFORMATION_MESSAGE);

			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			returnVal = chooser.showOpenDialog(Gui.this);

			if(returnVal == JFileChooser.APPROVE_OPTION) {
				File directory = chooser.getSelectedFile();
				if(directory.getAbsolutePath().endsWith("bin")) {
					pathR = directory.getAbsolutePath();
					Config.setPathToR(pathR);
				}
			}
		} else {
			returnVal = JFileChooser.APPROVE_OPTION;
		}

		if(returnVal == JFileChooser.APPROVE_OPTION) {

			//ask for already performed uracil-number calculation in a file, if not performed, do it now
			int answer = JOptionPane.showConfirmDialog(Gui.this, "Have you already calculated uracil numbers and saved the output?", "Previous calculations", JOptionPane.YES_NO_OPTION);
			String ura = URAFILENAME;
			boolean cont = true;

			if(answer == JOptionPane.YES_OPTION) {
				JFileChooser uraFile = new JFileChooser();
				returnVal = uraFile.showOpenDialog(Gui.this);

				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File file = uraFile.getSelectedFile();

					//check if file is in correct format
					if(!Tools.testValidityForFile(file, 2, true)) {
						DisplayWarnings.error("Your file is not in the correct format; " +
								"loading has failed.", Gui.this);
						cont = false;
						//check if file exists
					} else if(file.exists()) {
						ura = file.getAbsolutePath();
					} else {
						DisplayWarnings.error("Your chosen file does not exist; loading has failed.", Gui.this);
						cont = false;
					}
				} else {
					cont = false;
				}
			} else {
				startEvaluation(true, null, true);
			}

			if(cont) {
				//Start the correlation with R
				cor = new RCorrelationCoefficient(pathR, ura);

				//choose method
				Object[] methods = new Object[4];
				methods[0]       = "Please choose a method for the correlation";
				JRadioButton me1 = new JRadioButton("Spearman");
				JRadioButton me2 = new JRadioButton("Pearson");
				JRadioButton me3 = new JRadioButton("Kendal");
				methods[1]       = me1;
				methods[2]       = me2;
				methods[3]       = me3;
				ButtonGroup me   = new ButtonGroup();
				me.add(me1); me.add(me2); me.add(me3);
				me1.setSelected(true);

				me1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cor.setMethod(RCorrelationCoefficient.SPEARMAN);
					}
				});
				me2.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cor.setMethod(RCorrelationCoefficient.PEARSON);
					}
				});
				me3.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cor.setMethod(RCorrelationCoefficient.KENDAL);
					}
				});

				JOptionPane.showMessageDialog(Gui.this, methods, "Choose calculation method", JOptionPane.PLAIN_MESSAGE);			

				//start correlation coefficient calculation
				double coefficient          = cor.calculateCorrelationCoefficient();

				JTextField result = new JTextField();
				result.setBorder(new BevelBorder(BevelBorder.LOWERED));
				result.setEditable(false);
				result.setText(String.valueOf(coefficient));
				Object[] inform = new Object[2];
				inform[0]       = "Your correlation coefficient is";
				inform[1]       = result;
				JOptionPane.showMessageDialog(Gui.this, inform, "Correlation Coefficient", JOptionPane.INFORMATION_MESSAGE);
			}


			/******************************************************************************************/

			/*
			 * Calculate Correction bias with R
			 */
			//perform the uracil-evaulation for all three types of normalized ratios
			String tmp1 = Config.CONFIGPATH+"Ratio-Eluate-Total.tmp";
			String tmp2 = Config.CONFIGPATH+"Ratio-Unlabeled-Total.tmp";
			String tmp3 = Config.CONFIGPATH+"Ratio-Eluate-Unlabeled.tmp";

			JOptionPane.showMessageDialog(Gui.this, "Confirm to start bias correction\n" +
					"(this may take a moment).", "Start bias correction", JOptionPane.INFORMATION_MESSAGE);

			guiData.evaluateFasta(Data.LOGEN, tmp1, null, false);
			//fill mapping and corresponding arrays
			String[] spots1 = dataObj.getSpot();

			guiData.evaluateFasta(Data.LOGUN, tmp2, null, false);
			String[] spots2 = dataObj.getSpot();

			guiData.evaluateFasta(Data.LOGEU, tmp3, null, false);
			String[] spots3 = dataObj.getSpot();


			//Start the loess regression with R
			loess = new RLoessRegression(pathR, tmp1, spots1);
			HashMap<String, Double> lo = loess.calculateLoessRegression();
			dataObj.setCorrNewTot(lo);
			dataObj = Filter.filterCorrectionBias(dataObj, lo);
			loess = new RLoessRegression(pathR, tmp2, spots2);
			lo = loess.calculateLoessRegression();
			dataObj.setCorrPreTot(lo);
			loess = new RLoessRegression(pathR, tmp3, spots3);
			lo = loess.calculateLoessRegression();
			dataObj.setCorrNewPre(lo);

			//replot uracil number with corrected values if wanted
			int replot = JOptionPane.showConfirmDialog(Gui.this, "Do you want to plot the uracil numbers with corrected ratios?", "Replot", JOptionPane.YES_NO_OPTION);

			if(replot == JOptionPane.YES_OPTION) {
				startEvaluation(true, lo, true);
			}

			//recalculate normalization and half-lives if wanted
			int recalculate = JOptionPane.showConfirmDialog(Gui.this, "Do you want to recalculate normalization " +
					"and half-lives with corrected ratios?", "Recalculate", JOptionPane.YES_NO_OPTION);
			if(recalculate == JOptionPane.YES_OPTION) {
				performNormalization();
				//recreate normalization plot only if wanted
				int response = JOptionPane.showConfirmDialog(Gui.this, "Do you want to recreate the normalization plot with " +
						"the new values?", "Replot", JOptionPane.YES_NO_OPTION);
				if(response == JOptionPane.YES_OPTION) {
					plotNormalization();
				}

				if(!guiHL.hasHL()) {
					guiHL.clearHL();
					hlCalculationDialog();
				}

				if(guiHL.getMethods().size() > 0) {
					guiHL.setData(dataObj);
					guiHL.calcHL();
					//recreate half-life plot only if wanted
					response = JOptionPane.showConfirmDialog(Gui.this, "Do you want to recreate the half-life plot with " +
							"the new values?", "Replot", JOptionPane.YES_NO_OPTION);
					if(response == JOptionPane.YES_OPTION) {
						plotHL();
					}
				}

			}
		} else {
			DisplayWarnings.error("You did not choose the bin folder!", Gui.this);
		}
	}

	/**
	 * Plots all calculated half-lives
	 */
	private void plotHL() {
		GraphHandler.plotHalfLifeHisto(guiHL.getHL(), guiHL.getTimes(), Gui.this);
	}

	/**
	 * Calculates the median for all half-life methods
	 */
	private void calculateMedian() {

		boolean error = false;

		if(labelingTime == UNDEF) {
			//display dialog to choose time
			String response = JOptionPane.showInputDialog(Gui.this, "Please enter the labeling time for calculation", "Enter labeling time", JOptionPane.PLAIN_MESSAGE);
			try {
				response = response.replace(",", ".");
				medTime = Double.parseDouble(response);
			} catch (NumberFormatException e) {
				error = true;
				DisplayWarnings.error("Your labeling time needs to be numerical", Gui.this);
			}
		} else {
			medTime = labelingTime;
		}

		if(!error) {
			//calculate median as average over methods
			double median = Tools.calculateMedianForHalfLives(medTime, dataObj);

			//display result
			JTextArea text  = new JTextArea(String.valueOf(median));
			text.setBorder(new BevelBorder(BevelBorder.LOWERED));
			text.setEditable(false);
			Object[] result = new Object[2];
			result[0]       = "Your median is:";
			result[1]       = text;
			JOptionPane.showMessageDialog(Gui.this, result, "Result", JOptionPane.INFORMATION_MESSAGE);
		}
	}


	/**
	 * Start the calculation of correction factors and half-lives based on median
	 * @param menu TRUE if method is called from the main menu, FALSE otherwise
	 */
	private void startMedianBasedCorrectionFactors(boolean menu) {
		boolean error = false;
		//If half-life calculations have not been performed, provide possibility to enter median
		if(!guiHL.hasHL() || renormalize) {
			guiHL.clearHL();
			display = new StringBuffer("");
			Object[] messageMed = new Object[2];
			messageMed[0]       = "Please enter the median half-life:";
			JTextField field    = new JTextField();
			messageMed[1]       = field;
			JOptionPane.showMessageDialog(Gui.this, messageMed, "Enter median", JOptionPane.PLAIN_MESSAGE);
			try {
				String input = field.getText();
				input = input.replace(",", ".");
				median = Double.parseDouble(input);

				int answer = JOptionPane.YES_OPTION;
				while(answer == JOptionPane.YES_OPTION) {
					hlCalculationDialog();
					answer = JOptionPane.showConfirmDialog(Gui.this, "Do you want to include more methods?", "Add more?", JOptionPane.YES_NO_OPTION);
				}
				//generate half-life objects without calculation
				guiHL.generateHL();
			} catch (NumberFormatException e) {
				DisplayWarnings.error("Your median value has to be numerical!", Gui.this);
				error = true;
			}
		}
		else if(guiHL.hasHL()) {
			//Calculate the correction factors based on the median
			median = guiHL.calculateMedian();
		} 

		if(!error) {
			medCorr           = guiHL.getCFonMedian(median);
		}
	}

	//	private void displayMedianBasedCorrectionFactors(boolean menu) {
	//		//Print result into format that is easier readable
	//		StringBuffer buff = new StringBuffer("");
	//		if(display.length() > 0 && medCorr != null) {
	//			String[] all = display.toString().split(" # ");
	//			int index        = 1;
	//			int i            = 0;
	//
	//			for(String item : all) {
	//				String[] split = item.split(", ");
	//				String meth    = "";
	//				String c       = "";
	//				//Translate short form into more detailed description
	//				if(split[0].equals(HalfLife.NEWLY)) {
	//					meth       = "Newly transcribed/Total based,\t";
	//					c          = "c_l: "+medCorr.get(i).getC_l();
	//				} else if(split[0].equals(HalfLife.PRE)) {
	//					meth       = "Pre-existing/Total based,";
	//					c          = "c_u: "+medCorr.get(i).getC_u();
	//				} else if(split[0].equals(HalfLife.NEWPRE)) {
	//					meth       = "Newly transcribed/Pre-existing based,";
	//					c          = "c_lu: "+medCorr.get(i).getC_lu();
	//				}
	//				buff.append("Method: "+meth+"\tLabeling time: "+split[1]+",\t"+c+"\n");
	//				index++;
	//				i++;
	//			}
	//			//Display the results in a text area for copying
	//			Object[] messageResults = new Object[2];
	//			messageResults[0]       = "Your correction factors based on the median:\n\n";
	//			JTextArea area          = new JTextArea(buff.toString());
	//			area.setEditable(false);
	//			area.setBorder(new BevelBorder(BevelBorder.LOWERED));
	//			messageResults[1]       = area;
	//			JOptionPane.showMessageDialog(Gui.this, messageResults, "Results for median CF", JOptionPane.INFORMATION_MESSAGE);
	//
	//			startHLButton.setEnabled(true);
	//
	//			if(menu) {
	//				//if wanted, start calculating half-lives based on median
	//				int answer = JOptionPane.showConfirmDialog(Gui.this, "Do you want to calculate the half-lives\nbased on the median?", 
	//						"Recalculate half-lives", JOptionPane.YES_NO_OPTION);
	//				if(answer == JOptionPane.YES_OPTION) {
	//					guiHL.calcHLonMedian(medCorr);
	//					plotHL();
	//					//if wanted, start procedure for saving of half-live values
	//					int a = JOptionPane.showConfirmDialog(Gui.this, "Do you want to save the half-lives based on median?", "Save", JOptionPane.YES_NO_OPTION);
	//					if(a == JOptionPane.YES_OPTION) {
	//						saveHLPrep();
	//					}
	//
	//				}
	//			}
	//		}
	//	}

	/**
	 * Display the list of replicates and their corresponding column labels
	 */
	private void displayReplicates() {
		ArrayList<Component> components = new ArrayList<Component>();

		//for each replicate display newly transcribed, pre-existing and total labels
		for(int i=0; i<columnsTotal.size(); i++) {
			String description = "Replicate "+(i+1)+":";
			JSeparator sep = new JSeparator();
			JLabel rep = new JLabel(description);
			JLabel ne  = new JLabel("Newly transcribed: "+columnsNew.get(i));
			JLabel pr  = new JLabel("Pre-existing: "+columnsPre.get(i));
			JLabel to  = new JLabel("Total: "+columnsTotal.get(i));
			components.add(sep);
			components.add(rep); 
			components.add(ne);
			components.add(pr);
			components.add(to);
		}
		components.add(new JSeparator());
		LabelChooserDialog dialog = new LabelChooserDialog(Gui.this, "Your replicates", 
				"These are your replicates:", components);
		dialog.setVisible(true);
	}

	private boolean displayReplicatesDialog() {
		int returnVal = JOptionPane.showConfirmDialog(Gui.this, "Are the attributes you want " +
				"to load present calls?", "Present calls?", JOptionPane.YES_NO_OPTION);
		if(returnVal == JOptionPane.YES_OPTION) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Clears the filtering steps and reloads the complete data
	 */
	private void clearFiltering() {
		//clear methods
		guiData.setMethods(new ArrayList<String>());
		guiData.setFirst(false);
		//reload data
		guiData.loadData();
//		guiData.addAttributesList(attributes, dataObj.getAttrDescr());
		for(String file : attributeFiles) {
			guiData.addAttr(new File(file), attributes);
		}
		if(orgLabelsCalls.size() > 0) {
			guiData.addOriginalAttr((ArrayList<String>)orgLabelsCalls, true);
		}
		if(orgLabelsOther.size() > 0) {
			guiData.addOriginalAttr((ArrayList<String>)orgLabelsOther, false);
		}

		JOptionPane.showMessageDialog(Gui.this, "There are now "+guiData.getSize()+" probe sets loaded.", "Data reseted", JOptionPane.INFORMATION_MESSAGE);
		dataObj = guiData.getData();
		normGraphConstructor = null;
		guiNorm.setData(dataObj);
		guiHL.setData(dataObj);
		//clear check boxes
		menuFilter.setEnabled(true);
		menuNormal.setEnabled(true);
		if(unequal) {
			menuHalfLife.setEnabled(true);
		}
		methThresh.setSelected(false);
		methThresh.setActionCommand("enable");
		methAbs.setSelected(false);
		methAbs.setActionCommand("enable");
		none.setSelected(true);
		methPQS.setActionCommand("enable");
		methPQSMin.setActionCommand("enable");
	}

	/**
	 * Generates the Main Menu bar
	 * @return Menu bar
	 */
	private JMenuBar generateMainMenus() {
		JMenuBar menuBar = new JMenuBar();

		// session menu
		JMenu session             = new JMenu("Session");
		session.setMnemonic(KeyEvent.VK_O);
		session.add(menuLoadSess  = new JMenuItem("Load session"));
		menuLoadSess.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK));

		session.add(menuSaveSess  = new JMenuItem("Save session"));
		menuSaveSess.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));

		session.add(menuClearSess = new JMenuItem("Clear session"));
		menuClearSess.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));

		session.add(new JSeparator());
		session.add(menuExit      = new JMenuItem("Exit"));
		menuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK));
		menuBar.add(session);

		//Settings menu
		JMenu settings = new JMenu("Settings");
		settings.setMnemonic(KeyEvent.VK_G);
		settings.add(menuSettAlpha = new JMenuItem("Set alpha function"));
		settings.add(menuSettTime  = new JMenuItem("Set labeling time"));
		settings.add(menuSettR     = new JMenuItem("Set path to R"));
		settings.add(menuSettMax   = new JMenuItem("Set maximum/minimum half-life"));
		settings.add(menuSettRep   = new JMenuItem("Set replicate"));
		settings.add(menuSettGene  = new JMenuItem("Set gene name attribute label"));
		menuBar.add(settings);

		// quality control menu
		JMenu eva        = new JMenu("Quality control");
		eva.setMnemonic(KeyEvent.VK_Q);
		eva.add(menuEva  = new JMenuItem("Calculation of Uracil number"));
		eva.add(menuBias = new JMenuItem("Correction for bias in capture-rates"));
		eva.add(menuPQS  = new JMenuItem("Calculate the probeset quality control scores"));
		menuBar.add(eva);

		//filter data menu
		menuFilter = new JMenu("Filter data");
		menuFilter.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menuFilter);
		menuFilter.setActionCommand("enable");
		menuFilter.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				if(menuFilter.getActionCommand().equals("enable")) {
					if(!filtPa.isVisible()) {
						enlargeWindow(260);
					}
					filtPa.setVisible(true);
					menuFilter.setActionCommand("disable");
				} 
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});

		//normalization menu
		menuNormal = new JMenu("Normalization");
		menuNormal.setMnemonic(KeyEvent.VK_N);
		menuNormal.add(menuNorm = new JMenuItem("Normalization"));
		menuNormal.add(menuMed = new JMenuItem("Correction factors from median half-life"));
		menuNorm.setActionCommand("enable");
		menuNorm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
		menuBar.add(menuNormal);

		//half-life menu
		menuHalfLife = new JMenu("Half-life");
		menuHalfLife.setMnemonic(KeyEvent.VK_H);
		menuHalfLife.add(menuCalcHL  = new JMenuItem("Half-life calculation"));
		menuHalfLife.add(menuCalcMed = new JMenuItem("Calculate median half-life"));
		menuBar.add(menuHalfLife);
		menuCalcHL.setActionCommand("enable");
		menuCalcHL.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));

		// save menu
		JMenu save                 = new JMenu("Save");
		save.setMnemonic(KeyEvent.VK_S);
		save.add(menuSaveData      = new JMenuItem("Save (filtered) data"));
		menuSubSave          = new JMenu("Save calculated values");
		save.add(menuSubSave);
		menuSubSave.add(menuSaveHL  = new JMenuItem("Half-life values"));
		menuSubSave.add(menuSaveR   = new JMenuItem ("Ratio values"));
		menuSubSave.add(menuSaveB   = new JMenuItem("Half-lives and ratios"));
		menuSubSave.add(menuSaveHLA = new JMenuItem("Half-lives with attributes"));
		menuBar.add(save);

		menuSubSave.setEnabled(false);
		menuCalcMed.setEnabled(false);
		menuBias.setEnabled(false);
		menuEva.setEnabled(false);
		menuMed.setEnabled(false);
		menuPQS.setEnabled(false);
		menuHalfLife.setEnabled(false);
		menuNormal.setEnabled(false);
		menuFilter.setEnabled(false);

		// help menu
		JMenu help             = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_E);
		help.add(menuHelpReplicate = new JMenuItem("Show replicates with numbers"));
		help.add(menuHelpH     = new JMenuItem("Help"));
		help.add(new JSeparator());
		help.add(menuHelpAbout = new JMenuItem("About"));
		help.add(menuHelpWeb   = new JMenuItem("Web"));
		menuBar.add(help);

		menuHelpReplicate.setEnabled(false);

		// add all listeners
		ActionListener listener = generateMenuListener();

		menuLoadSess.addActionListener(listener);
		menuSaveSess.addActionListener(listener);
		menuClearSess.addActionListener(listener);
		menuExit.addActionListener(listener);
		menuNorm.addActionListener(listener);
		menuCalcHL.addActionListener(listener);
		menuSaveData.addActionListener(listener);
		menuSaveHL.addActionListener(listener);		
		menuSaveHLA.addActionListener(listener);
		menuSaveR.addActionListener(listener);
		menuSaveB.addActionListener(listener);
		menuEva.addActionListener(listener);
		menuBias.addActionListener(listener);
		menuPQS.addActionListener(listener);
		menuCalcMed.addActionListener(listener);
		menuSettAlpha.addActionListener(listener);
		menuSettTime.addActionListener(listener);
		menuSettR.addActionListener(listener);
		menuSettMax.addActionListener(listener);
		menuSettRep.addActionListener(listener);
		menuSettGene.addActionListener(listener);
		menuMed.addActionListener(listener);
		menuHelpReplicate.addActionListener(listener);
		menuHelpH.addActionListener(listener);
		menuHelpAbout.addActionListener(listener);
		menuHelpWeb.addActionListener(listener);
		return menuBar;

	}

	/**
	 * Contains the ActionListeners for the menu
	 * @return ActionListener for a specific menu item
	 */
	private ActionListener generateMenuListener() {
		final Gui window = this;
		return new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				new Thread() {

					public void run() {
						try {
							Object source = e.getSource();

							//saving the data
							if (source == menuSaveData) {
								saveData();
								//saving half-life values
							} else if (source == menuSaveHL) {
								which = HalfLifeWriter.HALFLIFE;
								saveHLComp();
								//saving ratio values
							} else if (source == menuSaveR) {
								which = HalfLifeWriter.RATIO;
								saveHLComp();
								//saving both values
							} else if (source == menuSaveB) {
								which = HalfLifeWriter.BOTH;
								saveHLComp();
								//closing window
							} else if (source == menuSaveHLA) { 
								saveHLAttr();
							} else if (source == menuExit) {
								actionExit();
								window.dispose();
								//saving session
							} else if (source == menuSaveSess) {
								actionSaveSession();
								//loading session
							} else if (source == menuLoadSess) {
                                                           
								actionLoadSession();
								//clearing/resetting session
							} else if (source == menuClearSess) {
								actionClearSession();
                                                              
							} else if (source == menuCalcHL) {
								if(menuCalcHL.getActionCommand().equals("enable")) {
									menuCalcHL.setActionCommand("disable");
									if(!hlPa.isVisible()) {
										enlargeWindow(220);
									}
									hlPa.setVisible(true);
								} 
							} else if (source == menuNorm) { 
								if(menuNorm.getActionCommand().equals("enable")) {
									menuNorm.setActionCommand("disable");
									if(!normPa.isVisible()) {
										enlargeWindow(120);
									}
									normPa.setVisible(true);
								} 
								//set an alpha function
							} else if (source == menuSettAlpha) {
								actionChooseAlpha();
								//set a constant labeling time
							} else if (source == menuSettTime) {
								actionSetTime();
								//set a constant path to R bin folder
							} else if (source == menuSettR) {
								actionEnterRPath();
								//set a maximum value for half-lives
							} else if (source == menuSettMax) {
								actionSetMax();
								//set a constant replicate
							} else if (source == menuSettRep) {
								actionSetRep();
							} else if(source == menuSettGene) {	
								actionSetGene();
								//display replicates and corresponding labels
							} else if (source == menuHelpReplicate) {
								displayReplicates();
								//display help
							} else if (source == menuHelpH) {
								actionShowHelp();
								//display "About"
							} else if (source == menuHelpAbout) {
								new AboutDialog(Gui.this);
							} else if (source == menuHelpWeb) {
								try {
									GuiTools.showBrowser(new URL(Tools.HOMEPAGE).toURI(), Gui.this);
								} catch (Exception e1) {
                                                                    e1.printStackTrace();
                                                                } // won't happen
							} else if (source == menuEva) {
								startEvaluation(false, null, true);
							} else if (source == menuBias) { 
								startBiasCorrection();
							} else if (source == menuMed) {
								startMedianBasedCorrectionFactors(true);
								//								displayMedianBasedCorrectionFactors(true);
								subMenuCorr();
							} else if (source == menuPQS) {
								startPQS();
							} else if(source == menuCalcMed) {
								calculateMedian();
							}
						} 
                                            catch(OutOfMemoryError e)
                                            {
                                                outOfMemory();
                                            }
                                            catch(Exception e)
                                            {
                                                e.printStackTrace();//							GUICommons.unexpectedError(e);
                                            }
					}


					/**
					 * Starts the calculation of probeset quality scores independent
					 * of filtering
					 */
					private void startPQS() {
						Normalization norm = guiNorm.getNormalization();
						boolean histogram = plotPQS();
						JFileChooser chooser = new JFileChooser();
						int returnVal = chooser.showSaveDialog(Gui.this);

						if(returnVal == JFileChooser.APPROVE_OPTION) {
							double[] pqs = norm.calculateQualityControl(dataObj, histogram);
							File file = chooser.getSelectedFile();
							if(file.exists()) {
								if(overwriteFile()) {
									dataObj.writeToFileArray(file.getAbsolutePath(), pqs);
								}
							} else {
								dataObj.writeToFileArray(file.getAbsolutePath(), pqs);
							}
						}
					}

					/**
					 * Displays a dialog for entering of the replicate number and saves it
					 */
					private void actionSetRep() {
						InputHelpDialog dialog = new InputHelpDialog(Gui.this, "Enter replicate", "Please enter the replicate you want to use for your\n" +
								"calculations. (Please make sure that replicates are \n" +
								"given in the same order for newly transcribed,\n" +
								"pre-existing and total RNA)", "If you want to limit your calculations to one single " +
								"replicate instead of using an average over all replicates, you can set " +
								"this replicate here, and it will be used for all subsequent analyses." +
								"\nReplicate numbers start at 1; if you don't know which number your " +
								"replicate has, check out Help > Show replicates with numbers.", "");
						dialog.setVisible(true);
						String response = dialog.getInputText();
						if(response != null && !dialog.isCancel()) {
							try {
								replicate = Integer.parseInt(response)-1;
								//check if data has been loaded yet
								if(columnsTotal.size() == 0) {
									DisplayWarnings.error("You cannot set a replicate before loading the data!", Gui.this);
									//check if replicate is in allowed range
								} else if(replicate < 0 || replicate >= columnsTotal.size() || replicate >= columnsNew.size() 
										|| replicate >= columnsPre.size()) {
									DisplayWarnings.error("Your replicate number exceeds the number of \nreplicates present in the data! It has to\n" +
											"be between 1 and "+columnsTotal.size()+".", Gui.this);
								} else {
									//set constant replicate
									dataObj.setReplicate(replicate);
									guiNorm.setReplicate(replicate);
								}
							} catch (NumberFormatException ex) {
								DisplayWarnings.error("Your replicate has to be numerical!", Gui.this);
							}
						}
					}

					/**
					 * Displays a dialog window for setting the maximal half-life value
					 */
					private void actionSetMax() {
						int size = 5;
						GridBagConstraints c = new GridBagConstraints();
						c.anchor = GridBagConstraints.WEST;
						c.fill = GridBagConstraints.NONE;
						Object[] message = new Object[size];
						message[0]       = new HelpButton("Since after application of correction factors, " +
								"normalized ratios may be larger than 1 for a few transcripts, a capping " +
								"is applied and the maximum (for pre-existing/total RNA ratios) or minimum " +
								"half-life (for newly transcribed/total RNA ratios) is returned for these " +
						"transcripts.");
						message[1]       = new JLabel("Please enter your maximum half-life:");
						JTextField maxfield = new JTextField();
						JTextField minfield = new JTextField();
						message[2]       = maxfield;
						message[3]       = new JLabel("Please enter your minimum half-life:");
						message[4]       = minfield;


						JOptionPane.showMessageDialog(Gui.this, message, "Set maximum/minimum", JOptionPane.PLAIN_MESSAGE);
						try {
							double maxHWZ;
							double minHWZ;
							if(maxfield.getText().isEmpty()) {
								maxHWZ = HalfLife.getMaximumHalfLife();
							} else {
								String hl1 = maxfield.getText().replace(",", ".");
								maxHWZ = Double.parseDouble(hl1);
							}
							if(minfield.getText().isEmpty()) {
								minHWZ = HalfLife.getMinimumHalfLife();
							} else {
								String hl2 = minfield.getText().replace(",", ".");
								minHWZ = Double.parseDouble(hl2);
							}
							//set new maximum
							guiHL.setMax(maxHWZ);
							//set new minimum
							guiHL.setMin(minHWZ);
						} catch (NumberFormatException e) {
							DisplayWarnings.error("You have to enter numerical half-lives!", Gui.this);
						}
					}

					/**
					 * Displays a dialog window for setting the labeling time
					 */
					private void actionSetTime() {
						InputHelpDialog dialog = new InputHelpDialog(Gui.this, "Labeling time", "Please enter " +
								"your labeling time:", "You need to provide the duration of 4sU labeling " +
								"in a time unit of your choice. The type of unit (e.g. min or h) " +
								"does not have to be given. Half-lives are automatically calculated " +
								"in the same unit.", "Please make sure that maximal and minimal half-life and\n "+
						"labeling time are given in the same time unit.");
						dialog.setVisible(true);
						String response = dialog.getInputText();
						if(response != null && !dialog.isCancel()) {
							try {
								response = response.replace(",", ".");
								double time = Double.parseDouble(response);
								//set labeling time
								labelingTime = time;
							} catch (NumberFormatException e) {
								DisplayWarnings.error("Your labeling time has to be numerical!", Gui.this);
							}
						}
					}

					/**
					 * Displays a file chooser window for setting the path to R
					 */
					private void actionEnterRPath() {
						JFileChooser chooser = new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						int returnVal = chooser.showOpenDialog(Gui.this);

						if(returnVal == JFileChooser.APPROVE_OPTION) {
							File directory = chooser.getSelectedFile();
							//check if chosen directory is a bin folder
							if(directory.getAbsolutePath().endsWith("bin")) {
								//set path to R
								pathR = directory.getAbsolutePath();
								Config.setPathToR(pathR);
							}
						}
					}

					/**
					 * Displays a dialog and lets the user choose a function for alpha calculation
					 */
					private void actionChooseAlpha() {
						//Display Dialog to let user choose alpha
						Object[] message  = new Object[4];
						message[0]        = new HelpButton("For the calculation of half-lives an alpha function " +
								"has to be defined. You can choose between Constant alpha function, which always " +
								"returns 1, and Cell division alpha, which models the cell division. For the last" +
						" function you have to know the parameter ccl (cell cycle length) of this function.");
						message[1]        = "Please choose your alpha function:";
						JRadioButton a1   = new JRadioButton("Constant alpha: 1");
						JRadioButton a2   = new JRadioButton("Cell division alpha");
						ButtonGroup group = new ButtonGroup();
						message[2]        = a1;
						message[3]        = a2;
						group.add(a1); 
						group.add(a2);
						a1.setSelected(true);
						a1.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								//set constant alpha function
								alpha = new AlphaConstant();
							}
						});
						a2.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								//offer dialog to input parameter ccl
								String response = JOptionPane.showInputDialog(Gui.this, "Please enter your parameter\nccl (cell cycle length):", 
										"Enter ccl", JOptionPane.INFORMATION_MESSAGE);
								if(response != null) {
									try {
										response   = response.replace(",", ".");
										double ccl = Double.parseDouble(response);
										alpha      = new AlphaCellDivision(ccl);
										//set cell division modeling alpha function
									} catch (NumberFormatException ex) {
										DisplayWarnings.error("Your parameter has to be numerical!", Gui.this);
									}
								}
							}
						});
						JOptionPane.showMessageDialog(Gui.this, message, "Choose alpha", JOptionPane.PLAIN_MESSAGE);
					}

					/**
					 * Displays a help message
					 */
					private void actionShowHelp() {
						final String docs = "doc/indexgui.html";

						File doc = new File(docs);

						if (!doc.exists() && workingdirectory != null)
							// try something else
							doc = new File(workingdirectory + docs);

						if (!doc.exists())
							// last try
							doc = new File(System.getProperty("user.dir")
									+ System.getProperty("file.separator")
									+ docs);

						// check if it is there finally
						if (!doc.exists()) {
							int answer = JOptionPane.showConfirmDialog(Gui.this, "Could not find " +
									"documentation files in doc/ subdirectory.\n" +
									"You are probably running the webstart version of HALO.\n\n" +
									"Do you want to view the online documentation on the HALO website?",
									"View online documentation?", JOptionPane.YES_NO_OPTION);
							if (answer == JOptionPane.YES_OPTION) {
								try {
									GuiTools.showBrowser(new URL(Tools.HOMEPAGEDOC).toURI(), Gui.this);
								} catch (Exception e) {
                                                                e.printStackTrace();}
							}
						} else {
							// found it, show it
							GuiTools.showBrowser(doc.toURI(), Gui.this);
						}
					}

					/**
					 * Start procedure to load a saved session
					 */
					private void actionLoadSession() {
						//Choose the file containing the session
						JFileChooser choose = new JFileChooser();
                                               
						int returnVal       = choose.showOpenDialog(Gui.this);
						if(returnVal == JFileChooser.APPROVE_OPTION) {
							sessName = choose.getSelectedFile();
							if(!sessName.exists()) {
								DisplayWarnings.error("Your chosen file does not exist; loading has failed.", Gui.this);
							} else {
								loadSession();
							}
						}

					}

					/**
					 * Start procedure to save the current session
					 */
					private void actionSaveSession() {
						//Choose the name/place of the file in which the session will be saved
						JFileChooser choose = new JFileChooser();
						int returnVal       = choose.showSaveDialog(Gui.this);
						if(returnVal == JFileChooser.APPROVE_OPTION) {
							File file = choose.getSelectedFile();
							if(file.exists()) {
								if(overwriteFile()) {
									sessName = file;
									saveSession();
								}
							} else {
								sessName = file;
								saveSession();
							}
						}
					}

					/**
					 * Reset all current values to empty values, reset window size
					 */
					private void actionClearSession() {
						columnsNew      = new ArrayList<String>();
//						columnsNewAll   = new ArrayList<String>();
						columnsPre      = new ArrayList<String>();
//						columnsPreAll   = new ArrayList<String>();
						columnsTotal    = new ArrayList<String>();
//						columnsTotalAll = new ArrayList<String>();
						labels          = new ArrayList<String>();
						attributeLabels = new ArrayList<String>();
						orgLabelsCalls  = new ArrayList<String>();
						orgLabelsOther  = new ArrayList<String>();
						attributesOrg            = new ArrayList<String>();
						attributeFileLabels = new ArrayList<ArrayList<String>>();
						attributeFiles = new ArrayList<String>();
						attributes     = new ArrayList<String>();
						attributesOut  = new ArrayList<String>();
						normGraphConstructor = null;
						unequal  = false;
						dataObj  = null;
						input    = null;
						guiData  = new GuiFilterData();
						display  = new StringBuffer("");
						sessName = new File("Halo_temp");
						guiHL    = new GuiHalfLife();
						guiNorm    = new GuiNormal();
						filepath.setText("");
						menuFilter.setActionCommand("enable");
						menuHalfLife.setActionCommand("enable");
						menuNormal.setActionCommand("enable");
						methThresh.setSelected(false);
						methThresh.setActionCommand("enable");
						methAbs.setSelected(false);
						methAbs.setActionCommand("enable");
						methPQS.setActionCommand("enable");
						methPQSMin.setActionCommand("enable");
						none.setSelected(true);

						//disable all buttons and menu entries that may not be used prior to calculations
						menuBias.setEnabled(false);
						menuCalcMed.setEnabled(false);
						menuEva.setEnabled(false);
						menuMed.setEnabled(false);
						menuPQS.setEnabled(false);
						plotNorm.setEnabled(false);
						plotHalf.setEnabled(false);
						startHLButton.setEnabled(false);
						saveHLs.setEnabled(false);
						add.setEnabled(false);

						//reset all constant values
						labelingTime = UNDEF;
						pathR        = "";

						resetWindow();

					}
				}.start();
			}
		};
	}

	/**
	 * Starts saving of half-lives with attributes
	 */
	private void saveHLAttr() {
		//choose one half-life calculation
		int index = chooseHalfLifeCalculation();
		
		if(index != -1) {
			//choose labels
			ArrayList<Component> components = new ArrayList<Component>();
			ArrayList<String> temp          = new ArrayList<String>();
			temp.addAll(attributeLabels);
			temp.addAll(attributesOrg);
			int answer = JOptionPane.YES_OPTION;
			boolean cancel = false;

			if(temp.size() == 0) {
				answer = JOptionPane.showConfirmDialog(Gui.this, "You have loaded no attributes. Are you sure \n" +
						"you want to continue? This procedure will save \n" +
						"only one half-life calculation!", "No attributes", JOptionPane.YES_NO_OPTION);
			} else {
				temporary = new ArrayList<String>();
				for(String label : temp) {
					JCheckBox box = new JCheckBox(label);
					box.setActionCommand("enable");
					components.add(box);
					box.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							JCheckBox box = (JCheckBox) e.getSource();
							if(box.getActionCommand().equals("enable")) {
								box.setActionCommand("disable");
								if(!temporary.contains(box.getText())) {
									temporary.add(box.getText());
								}
							} else {
								box.setActionCommand("enable");
								temporary.remove(box.getText());
							}
						}
					});
				}
				LabelChooserDialog dialog = new LabelChooserDialog(Gui.this, "Choose attributes", 
						"Please choose the attributes you want to save with half-lives:", components);
				dialog.setVisible(true);
				cancel = dialog.isCancel();
			}
			if(answer == JOptionPane.YES_OPTION && !cancel) {

				//choose file
				JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showSaveDialog(Gui.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					String input = file.getAbsolutePath();

					guiHL.saveHLWithAttributes(index, input, temporary);
				}
			}
		}
	}

	/**
	 * Enables user to change the name of the gene name attribute
	 */
	private void actionSetGene() {
		ArrayList<Component> components = new ArrayList<Component>();
		ArrayList<String> temp          = new ArrayList<String>();
		temp.addAll(attributeLabels);
		temp.addAll(attributesOrg);
		if(dataObj != null) {
			guiData.setGenename(Data.GENEDEFAULT);
		}
		for(String label : temp) {
			JRadioButton button = new JRadioButton(label);
			components.add(button);
			button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JRadioButton button = (JRadioButton) e.getSource();
					guiData.setGenename(button.getText());
				}
			});
		}
		JLabel label     = new JLabel("Other:");
		JTextField field = new JTextField();
		if(components.size() != 0) {
			components.add(label);
		}
		components.add(field);
		components.add(new HelpButton("You can load gene names and genes corresponding to your probesets. " +
				"In order to know which attribute contains the gene names, you need to provide the " +
				"label of these.\nDefault is gene_name and will be used if you click OK without " +
				"choosing any label. Otherwise please choose among the labels of the loaded " +
		"attributes or enter a custom label if gene names are not yet loaded."));
		LabelChooserDialog dialog = new LabelChooserDialog(Gui.this, "Choose gene label name", 
				"Please provide the label for the gene name attribute\n(Default: gene\\_name).", components);
		dialog.setVisible(true);

		if(!field.getText().isEmpty()) {
			guiData.setGenename(field.getText());
		}
	}

	/**
	 * Exits the application
	 */
	private void actionExit() {
		// save settings
		Config.generateConfigFile();
		System.exit(0);
	}

	/**
	 * Saves the Window Position in the session object
	 */
	private void saveWindowPos() {
		session.setPosX(this.getX());
		session.setPosY(this.getY());
		session.setMaxim(this.getExtendedState() == JFrame.MAXIMIZED_BOTH ? 1 : 0);
	}

	/**
	 * Saves the current status of all variables in a Session object
	 */
	private void saveSession() {
		session = new Session(sessName);

		saveWindowPos();
		//defining all parameters for this Session
		session.setColumnsNew(columnsNew);
		session.setColumnsPre(columnsPre);
		session.setColumnsTotal(columnsTotal);
		session.setData(dataObj);
		session.setDataFile(input);
		session.setDataMethods(guiData.getMethods());
		session.setDisplay(display);
		session.setPlotQuality(plotQuality);
		session.setFile(sessName);
		session.setHlMethods(guiHL.getMethods());
		session.setLabels(labels);
		session.setTimes(guiHL.getTimes());
		session.setCheckAbs(methAbs.isSelected());
		session.setCheckFilt(menuFilter.getActionCommand().equals("disable"));
		session.setCheckHL(menuHalfLife.getActionCommand().equals("disable"));
		session.setCheckNorm(menuNormal.getActionCommand().equals("disable"));
		session.setCheckPQS(methPQS.isSelected());
		session.setCheckPQSMin(methPQSMin.isSelected());
		session.setCheckThresh(methThresh.isSelected());
		session.setReplicates(replicates);
		session.setRatioMethod(ratioMethod);
		session.setGeneLabel(guiData.getGenename());
		session.setReplicate(replicate);
		session.setPathR(pathR);
		session.setCalls(guiData.isCalls());
		session.setOriginalLabelsCalls(orgLabelsCalls);
		session.setOriginalLabelsOther(orgLabelsOther);
		session.setLabelingTime(labelingTime);
		String alphaDesc = Session.ALPHACONSTANT;
		if(alpha instanceof AlphaCellDivision) {
			alphaDesc = Session.ALPHACELLDIV;
			AlphaCellDivision cellDiv = (AlphaCellDivision) alpha;
			session.setCcl(cellDiv.getCcl());
		}
		session.setAlpha(alphaDesc);
		if(guiNorm.hasNormalization()) {
			session.setNormMethod(normMethod);
		}
		if(guiData.hasGenes()) {
			session.setFasta(guiData.getFasta());
			session.setColumn(guiData.getColumn());
		}
		if(attributes.size() > 0) {
			session.setAttributeFiles(attributeFiles);
			session.setAttributeLabels(attributeFileLabels);
			session.setAttributes(attributes);
			session.setAttrDescr(guiData.getAttrDescr());
		}
		//saving the session into a file
		session.store();
	}

	/**
	 * Loads a saved session
	 */
	private void loadSession() {
		resetWindow();
		session = new Session(sessName);
		//load session and set all necessary values
		boolean correct = session.load(sessName);

		if(correct) {
			columnsNew      = session.getColumnsNew();
			columnsPre      = session.getColumnsPre();
			columnsTotal    = session.getColumnsTotal();
			dataObj         = session.getData();
			input           = session.getDataFile();
			guiData         = session.getGuiData();
			spotid          = guiData.getSpotid();
			display         = session.getDisplay();
			guiHL           = session.getGuiHL();
			labels          = session.getLabels();
			normMethod      = session.getNormMethod();
			guiNorm         = session.getGuiNorm();
			attributes      = session.getAttributes();
			attributeFiles  = session.getAttributeFiles();
			attributeFileLabels = session.getAttributeLabels();
			replicates      = session.getReplicates();
			ratioMethod     = session.getRatioMethod();
			pathR           = session.getPathR();
			orgLabelsCalls  = session.getOriginalLabelsCalls();
			orgLabelsOther  = session.getOriginalLabelsOther();
			plotQuality     = session.isPlotQuality();

			//check all previously checked checkboxes in the methods section
			filepath.setText("Opening: "+input.getName());
			menuFilter.setActionCommand("disable");
			menuFilter.setEnabled(true);
			menuHalfLife.setActionCommand("disable");
			menuHalfLife.setEnabled(true);
			menuNormal.setActionCommand("disable");
			menuNormal.setEnabled(true);

			//enlarge window that all previously used sub-panels are visible
			filtPa.setVisible(true);
			dataPa.setVisible(true);
			
			this.setLocation(session.getPosX(), session.getPosY());
			if(session.isMaxim()) {
				this.setExtendedState(JFrame.MAXIMIZED_BOTH);
			}
			enlargeWindow(600);

			//check the filtering method checkboxes that were previously checked
			methThresh.setSelected(session.isCheckThresh());
			methAbs.setSelected(session.isCheckAbs());
			methPQS.setSelected(session.isCheckPQS());
			methPQSMin.setSelected(session.isCheckPQSMin());

			//enable every menu point that should be accessible
			menuHelpReplicate.setEnabled(true);
			add.setEnabled(true);

			if(guiData.hasGenes()) {
				menuEva.setEnabled(true);
				menuBias.setEnabled(true);
				menuPQS.setEnabled(true);
			} 			
			if(guiNorm.hasNormalization()) {
				normPa.setVisible(true);
				plotNorm.setEnabled(true);
				menuMed.setEnabled(true);
				menuCalcMed.setEnabled(true);
				subMenuCorr();
				plotNormalization();
			}

			if(guiHL.hasHL()) {
				hlPa.setVisible(true);
				plotHalf.setEnabled(true);
				startHLButton.setEnabled(true);
				saveHLs.setEnabled(true);
				plotHL();
				if(guiData.hasGeneNames() && guiData.hasGenes()) {
					menuBias.setEnabled(true);
					menuEva.setEnabled(true);
				}
			}
			if(session.isPlotQuality() && guiData.getFasta() !=null) {
				startEvaluation(false, null, true);
			}
		} else {
			DisplayWarnings.error("Your session could not be loaded.\n" +
                                "Please check that your session file is correct.", Gui.this);
		}


	}

	/**
	 * Returns an out of memory error on the console and exits
	 */
	private void outOfMemory() {
		System.err.println("Out of memory! Please start the program with a higher amount" +
                                   "of available memory using the -Xmx switch in Java.\n\n" +
		"Will exit now.");
                DisplayWarnings.error("Out of memory! Please start the program with a higher amount\n " +
                        "of available memory using the -Xmx switch in Java.\nWill exit now.", Gui.this);
		//saveSession();
		System.exit(1);
	}

	/**
	 * Displays a greeting window informing the user about the help button
	 */
	private void questionMarkInfo() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(4,4,4,4);

		panel.add(new JLabel("<html>Wherever you see the little question mark shown at the bottom <br>left of " +
		"this window, click it to get context-related help about<br> the HALO GUI.</html>"),c);
		c.gridy++;

		//remember decision: show dialog or not?
		JCheckBox remember = new JCheckBox("Do not show this message again");
		remember.setActionCommand("enable");
		remember.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBox check = (JCheckBox) e.getSource();
				boolean hint = true;
				if(check.getActionCommand().equals("enable")) {
					check.setActionCommand("disable");
					hint = false;
				} else {
					check.setActionCommand("enable");
					hint = true;
				}
				Config.setDisplayHint(hint);
			}
		});
		c.gridy++;
		c.anchor = GridBagConstraints.WEST;
		panel.add(remember, c);
		c.gridy++;
		panel.add(new HelpButton("Yes, this is the button that will show you context-related help."),c);
		JOptionPane.showMessageDialog(Gui.this, panel, "Hint", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Asks user if existing file really should be overwritten
	 * @return TRUE if file can be overwritten, FALSE otherwise
	 */
	private boolean overwriteFile() {
		int response = JOptionPane.showConfirmDialog(Gui.this, "Are you sure you want to overwrite this file?", 
				"Overwrite file?", JOptionPane.YES_NO_OPTION);
		return (response == JOptionPane.YES_OPTION);
	}


	/**
	 * Starting the application
	 * @param args
	 */
	public static void main(String[] args) {


            try
            {
                // Set System L&F
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            }
            catch(UnsupportedLookAndFeelException e)
            {
                e.printStackTrace();
            }
            catch(ClassNotFoundException e)
            {
                 e.printStackTrace();
            }
            catch(InstantiationException e)
            {
                 e.printStackTrace();
            }
            catch(IllegalAccessException e)
            {
                e.printStackTrace();
            }


		// first argument might contain the current path
		workingdirectory = null;
		if (args.length > 0 && args[0].length() > 0) {
			workingdirectory = args[0];
			// should not end with a .
			if (workingdirectory.endsWith("."))
				workingdirectory = workingdirectory.substring(0,workingdirectory.length()-1);
			// should end with a / or \
			if (!workingdirectory.endsWith(File.separator))
				workingdirectory += File.separator;
		}
		try {
			new Gui();
		} catch (OutOfMemoryError e) {
			DisplayWarnings.error("Out of memory!\nPlease start HALO GUI with a higher amount of memory!", null);
			System.exit(1);
		}

	}
}
