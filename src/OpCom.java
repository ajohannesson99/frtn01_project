import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import graphics.ProgressBox;
import se.lth.control.*;
import se.lth.control.plot.*;

/** Class that creates and maintains a GUI for the Ball and Beam process.
 U two PlotterPanels for the plotters */
		public class OpCom {

			private static final double eps = 0.000001;

			private Regul regul;
			private PIDParameters innerPar;
			private PIDParameters outerPar;
			private int priority;

			// Monitors
			private ModeMonitor modeMon;

			// Declarartion of main frame.
			private JFrame frame;

			// Declarartion of panels.
			private BoxPanel guiPanel, plotterPanel, innerParPanel, outerParPanel, parPanel, rightPanel;
			private JPanel innerParLabelPanel, innerParFieldPanel, outerParLabelPanel, outerParFieldPanel, buttonPanel, somePanel, leftPanel, sizePanel;
			private PlotterPanel measPanel, ctrlPanel;

			// Declaration of components.
			private DoubleField innerParKField = new DoubleField(5,3);
			private DoubleField innerParTiField = new DoubleField(5,3);
			private DoubleField innerParTrField = new DoubleField(5,3);
			private DoubleField innerParTdField = new DoubleField(5,3);
			private DoubleField innerParBetaField = new DoubleField(5,3);
			private DoubleField innerParHField = new DoubleField(5,3);
			private JButton innerApplyButton;

			private DoubleField outerParKField = new DoubleField(5,3);
			private DoubleField outerParTiField = new DoubleField(5,3);
			private DoubleField outerParTdField = new DoubleField(5,3);
			private DoubleField outerParTrField = new DoubleField(5,3);
			private DoubleField outerParNField = new DoubleField(5,3);
			private DoubleField outerParBetaField = new DoubleField(5,3);
			private DoubleField outerParHField = new DoubleField(5,3);
			private JButton outerApplyButton;

			private JRadioButton offModeButton;
			private JRadioButton beamModeButton;
			private JRadioButton ballModeButton;

			private JRadioButton alignModeButton;

			private JRadioButton pushBallModeButton;
			private JButton stopButton;

			private JRadioButton weighBallModeButton;
			private JRadioButton startModeButton;

			//To show active size on the ball
			private JLabel small, med, big;

			private ProgressBox startState, alignState, fireState, weighState, smallBallState, medBallState, bigBallState, finishState;

			private boolean hChanged = false;
			private boolean isInitialized = false;

			/** Constructor. */
			public OpCom(int plotterPriority, ModeMonitor modeMon) {
				priority = plotterPriority;

				this.modeMon = modeMon;
			}

			/** Starts the threads. */
			public void start() {
				measPanel.start();
				ctrlPanel.start();
			}

			/** Set regul in main thread */
			public void setRegul(Regul regul) {
				this.regul = regul;
			}

			/** Creates the GUI. Called from Main. */
			public void initializeGUI() {
				// Create main frame.
				frame = new JFrame("Ball and Beam GUI");

				// Create a panel for the two plotters.
				plotterPanel = new BoxPanel(BoxPanel.VERTICAL);
				// Create PlotterPanels.
				measPanel = new PlotterPanel(2, priority);
				measPanel.setYAxis(20.0, -10.0, 2, 2);
				measPanel.setXAxis(10, 5, 5);
				measPanel.setUpdateFreq(10);
				ctrlPanel = new PlotterPanel(1, priority);
				ctrlPanel.setYAxis(20.0, -10.0, 2, 2);
				ctrlPanel.setXAxis(10, 5, 5);
				ctrlPanel.setUpdateFreq(10);

				plotterPanel.add(measPanel);
				plotterPanel.addFixed(10);
				plotterPanel.add(ctrlPanel);

				// Get initial parameters from Regul
				innerPar = regul.getInnerParameters();
				outerPar = regul.getOuterParameters();

				// Create panels for the parameter fields and labels, add labels and fields
				innerParPanel = new BoxPanel(BoxPanel.HORIZONTAL);
				innerParLabelPanel = new JPanel();
				innerParLabelPanel.setLayout(new GridLayout(0,1));
				innerParLabelPanel.add(new JLabel("K: "));
				innerParLabelPanel.add(new JLabel("Ti: "));
				innerParLabelPanel.add(new JLabel("Tr: "));
				innerParLabelPanel.add(new JLabel("Td"));
				innerParLabelPanel.add(new JLabel("Beta: "));
				innerParLabelPanel.add(new JLabel("h: "));
				innerParFieldPanel = new JPanel();
				innerParFieldPanel.setLayout(new GridLayout(0,1));
				innerParFieldPanel.add(innerParKField);
				innerParFieldPanel.add(innerParTiField);
				innerParFieldPanel.add(innerParTrField);
				innerParFieldPanel.add(innerParTdField);
				innerParFieldPanel.add(innerParBetaField);
				innerParFieldPanel.add(innerParHField);

				// Set initial parameter values of the fields
				innerParKField.setValue(innerPar.K);
				innerParTiField.setValue(innerPar.Ti);
				innerParTiField.setMinimum(-eps);
				innerParTrField.setValue(innerPar.Tr);
				innerParTdField.setValue(innerPar.Td);
				innerParTrField.setMinimum(-eps);
				innerParBetaField.setValue(innerPar.Beta);
				innerParBetaField.setMinimum(-eps);
				innerParHField.setValue(innerPar.H);
				innerParHField.setMinimum(-eps);

				// Add action listeners to the fields
				innerParKField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						innerPar.K = innerParKField.getValue();
						innerApplyButton.setEnabled(true);
					}
				});
				innerParTiField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						innerPar.Ti = innerParTiField.getValue();
						if (innerPar.Ti < eps) {
							innerPar.integratorOn = false;
						}
						else {
							innerPar.integratorOn = true;
						}
						innerApplyButton.setEnabled(true);
					}
				});
				innerParTrField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						innerPar.Tr = innerParTrField.getValue();
						innerApplyButton.setEnabled(true);
					}
				});

				innerParTdField.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						innerPar.Td = innerParTdField.getValue();
						innerApplyButton.setEnabled(true);
					}
				});
				innerParBetaField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						innerPar.Beta = innerParBetaField.getValue();
						innerApplyButton.setEnabled(true);
					}
				});
				innerParHField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						innerPar.H = innerParHField.getValue();
						outerPar.H = innerPar.H;
						outerParHField.setValue(innerPar.H);
						innerApplyButton.setEnabled(true);
						hChanged = true;
					}
				});

				// Add label and field panels to parameter panel
				innerParPanel.add(innerParLabelPanel);
				innerParPanel.addGlue();
				innerParPanel.add(innerParFieldPanel);
				innerParPanel.addFixed(10);

				// Create apply button and action listener.
				innerApplyButton = new JButton("Apply");
				innerApplyButton.setEnabled(false);
				innerApplyButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						regul.setInnerParameters(innerPar);
						if (hChanged) {
							regul.setOuterParameters(outerPar);
						}
						hChanged = false;
						innerApplyButton.setEnabled(false);
					}
				});

				// Create panel with border to hold apply button and parameter panel
				BoxPanel innerParButtonPanel = new BoxPanel(BoxPanel.VERTICAL);
				innerParButtonPanel.setBorder(BorderFactory.createTitledBorder("Inner Parameters"));
				innerParButtonPanel.addFixed(10);
				innerParButtonPanel.add(innerParPanel);
				innerParButtonPanel.addFixed(10);
				innerParButtonPanel.add(innerApplyButton);

				// The same as above for the outer parameters
				outerParPanel = new BoxPanel(BoxPanel.HORIZONTAL);
				outerParLabelPanel = new JPanel();
				outerParLabelPanel.setLayout(new GridLayout(0,1));
				outerParLabelPanel.add(new JLabel("K: "));
				outerParLabelPanel.add(new JLabel("Ti: "));
				outerParLabelPanel.add(new JLabel("Td: "));
				outerParLabelPanel.add(new JLabel("N: "));
				outerParLabelPanel.add(new JLabel("Tr: "));
				outerParLabelPanel.add(new JLabel("Beta: "));
				outerParLabelPanel.add(new JLabel("h: "));

				outerParFieldPanel = new JPanel();
				outerParFieldPanel.setLayout(new GridLayout(0,1));
				outerParFieldPanel.add(outerParKField);
				outerParFieldPanel.add(outerParTiField);
				outerParFieldPanel.add(outerParTdField);
				outerParFieldPanel.add(outerParNField);
				outerParFieldPanel.add(outerParTrField);
				outerParFieldPanel.add(outerParBetaField);
				outerParFieldPanel.add(outerParHField);
				outerParKField.setValue(outerPar.K);
				outerParTiField.setValue(outerPar.Ti);
				outerParTiField.setMinimum(-eps);
				outerParTdField.setValue(outerPar.Td);
				outerParTdField.setMinimum(-eps);
				outerParNField.setValue(outerPar.N);
				outerParTrField.setValue(outerPar.Tr);
				outerParBetaField.setValue(outerPar.Beta);
				outerParBetaField.setMinimum(-eps);
				outerParHField.setValue(outerPar.H);
				outerParHField.setMinimum(-eps);
				outerParKField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						outerPar.K = outerParKField.getValue();
						outerApplyButton.setEnabled(true);
					}
				});
				outerParTiField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						outerPar.Ti = outerParTiField.getValue();
						if (outerPar.Ti < eps) {
							outerPar.integratorOn = false;
						}
						else {
							outerPar.integratorOn = true;
						}
						outerApplyButton.setEnabled(true);
					}
				});
				outerParTdField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						outerPar.Td = outerParTdField.getValue();
						outerApplyButton.setEnabled(true);
					}
				});
				outerParNField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						outerPar.N = outerParNField.getValue();
						outerApplyButton.setEnabled(true);
					}
				});
				outerParTrField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						outerPar.Tr = outerParTrField.getValue();
						outerApplyButton.setEnabled(true);
					}
				});
				outerParBetaField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						outerPar.Beta = outerParBetaField.getValue();
						outerApplyButton.setEnabled(true);
					}
				});
				outerParHField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						outerPar.H = outerParHField.getValue();
						innerPar.H = outerPar.H;
						innerParHField.setValue(outerPar.H);
						outerApplyButton.setEnabled(true);
						hChanged = true;
					}
				});

				outerParPanel.add(outerParLabelPanel);
				outerParPanel.addGlue();
				outerParPanel.add(outerParFieldPanel);
				outerParPanel.addFixed(10);

				outerApplyButton = new JButton("Apply");
				outerApplyButton.setEnabled(false);
				outerApplyButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						regul.setOuterParameters(outerPar);
						if (hChanged) {
							regul.setInnerParameters(innerPar);
						}
						hChanged = false;
						outerApplyButton.setEnabled(false);
					}
				});

				BoxPanel outerParButtonPanel = new BoxPanel(BoxPanel.VERTICAL);
				outerParButtonPanel.setBorder(BorderFactory.createTitledBorder("Outer Parameters"));
				outerParButtonPanel.addFixed(10);
				outerParButtonPanel.add(outerParPanel);
				outerParButtonPanel.addFixed(10);
				outerParButtonPanel.add(outerApplyButton);

				// Create panel for parameter fields, labels and apply buttons
				parPanel = new BoxPanel(BoxPanel.HORIZONTAL);
				parPanel.add(innerParButtonPanel);
				parPanel.addGlue();
				parPanel.add(outerParButtonPanel);

				// Create panel for the radio buttons.
				buttonPanel = new JPanel();
				buttonPanel.setLayout(new FlowLayout());
				buttonPanel.setBorder(BorderFactory.createEtchedBorder());
				// Create the buttons.
				offModeButton = new JRadioButton("OFF");
				beamModeButton = new JRadioButton("BEAM");
				ballModeButton = new JRadioButton("BALL");
				alignModeButton = new JRadioButton("ALIGN");
				pushBallModeButton = new JRadioButton("PUSH BALL");
				weighBallModeButton = new JRadioButton("WEIGH BALL");
				startModeButton = new JRadioButton("START");
				stopButton = new JButton("STOP");
				// Group the radio buttons.
				ButtonGroup group = new ButtonGroup();
				group.add(offModeButton);
				group.add(beamModeButton);
				group.add(ballModeButton);
				group.add(alignModeButton);
				group.add(pushBallModeButton);
				group.add(weighBallModeButton);
				group.add(startModeButton);
				// Button action listeners.
				offModeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						modeMon.setMode(ModeMonitor.Mode.OFF);
					}
				});
				beamModeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						modeMon.setMode(ModeMonitor.Mode.BEAM);
					}
				});
				ballModeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						modeMon.setMode(ModeMonitor.Mode.BALL);
					}
				});

				alignModeButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						modeMon.setMode(ModeMonitor.Mode.ALIGN);
					}
				});

				pushBallModeButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						modeMon.setMode(ModeMonitor.Mode.PUSH_BALL);
					}
				});

				weighBallModeButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						modeMon.setMode(ModeMonitor.Mode.WEIGH_BALL);
					}
				});

				stopButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						regul.shutDown();
						measPanel.stopThread();
						ctrlPanel.stopThread();
						System.exit(0);
					}
				});

				startModeButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						modeMon.setMode(ModeMonitor.Mode.START);
					}
				});

				// Add buttons to button panel.
				buttonPanel.add(startModeButton, BorderLayout.NORTH);
				buttonPanel.add(offModeButton, BorderLayout.NORTH);
				buttonPanel.add(beamModeButton, BorderLayout.CENTER);
				buttonPanel.add(ballModeButton, BorderLayout.SOUTH);
				buttonPanel.add(alignModeButton, BorderLayout.SOUTH);
				buttonPanel.add(pushBallModeButton, BorderLayout.SOUTH);
				buttonPanel.add(weighBallModeButton, BorderLayout.SOUTH);

				// Panel for parameter panel and radio buttons
				somePanel = new JPanel();
				somePanel.setLayout(new BorderLayout());
				somePanel.add(parPanel, BorderLayout.CENTER);
				somePanel.add(buttonPanel, BorderLayout.SOUTH);

				// Select initial mode based on the mutually exclusive resource ModeMonitor
				switch (modeMon.getMode()) {
					case OFF:
						offModeButton.setSelected(true);
						break;
					case BEAM:
						beamModeButton.setSelected(true);
						break;
					case BALL:
						ballModeButton.setSelected(true);
						break;
					case ALIGN:
						alignModeButton.setSelected(true);
						break;
					case PUSH_BALL:
						pushBallModeButton.setSelected(true);
						break;
					case WEIGH_BALL:
						weighBallModeButton.setSelected(true);
						break;
					case START:
						startModeButton.setSelected(true);
						break;
				}


				// Create panel holding everything but the plotters.
				leftPanel = new JPanel();
				leftPanel.setLayout(new BorderLayout());
				leftPanel.add(somePanel, BorderLayout.CENTER);
				leftPanel.add(stopButton, BorderLayout.SOUTH);






				rightPanel = new BoxPanel(BoxPanel.VERTICAL);

				sizePanel = new JPanel();
				sizePanel.setLayout(new GridLayout(1,3));

				small = new JLabel("  Small");
				med = new JLabel("  Medium");
				big = new JLabel("  Big");

				small.setBackground(Color.RED);
				med.setBackground(Color.RED);
				big.setBackground(Color.RED);

				small.setOpaque(true);
				med.setOpaque(true);
				big.setOpaque(true);

				sizePanel.add(small);
				sizePanel.add(med);
				sizePanel.add(big);

				rightPanel.add(sizePanel);

				JPanel activePanel = new JPanel();
				activePanel.setLayout(new GridLayout(3,1));

				startState = new ProgressBox("start");
				alignState = new ProgressBox("align");
				fireState = new ProgressBox("fire");
				weighState = new ProgressBox("weigh");

				activePanel.add(startState);
				activePanel.add(alignState);
				activePanel.add(fireState);
				activePanel.add(weighState);

				JPanel activePanel2 = new JPanel();
				activePanel2.setLayout(new GridLayout(1,3));

				smallBallState = new ProgressBox("basket");
				medBallState = new ProgressBox("throw");
				bigBallState = new ProgressBox("drop");

				activePanel2.add(smallBallState);
				activePanel2.add(medBallState);
				activePanel2.add(bigBallState);

				finishState = new ProgressBox("finish");

				rightPanel.add(activePanel);
				rightPanel.add(activePanel2);
				rightPanel.add(finishState);


				// Create panel for the entire GUI.
				guiPanel = new BoxPanel(BoxPanel.HORIZONTAL);
				guiPanel.add(leftPanel);
				guiPanel.addGlue();
				guiPanel.add(plotterPanel);
				guiPanel.addFixed(10);
				guiPanel.add(rightPanel);
				guiPanel.addFixed(10);

				// WindowListener that exits the system if the main window is closed.
				frame.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						regul.shutDown();
						measPanel.stopThread();
						ctrlPanel.stopThread();
						System.exit(0);
					}
				});

				// Set guiPanel to be content pane of the frame.
				frame.getContentPane().add(guiPanel, BorderLayout.CENTER);

				// Pack the components of the window.
				frame.pack();

				// Position the main window at the screen center.
				Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
				Dimension fd = frame.getSize();
				frame.setLocation((sd.width-fd.width)/2, (sd.height-fd.height)/2);

				// Make the window visible.
				frame.setVisible(true);

				isInitialized = true;
			}


	/** Called by Regul to plot a control signal data point. */
	public synchronized void putControlData(double t, double u) {
		if (isInitialized) {
			ctrlPanel.putData(t, u);
		} else {
			System.out.println("Note: GUI not yet initialized. Ignoring call to putControlData().");
		}
	}

	/** Called by Regul to plot a measurement data point. */
	public synchronized void putMeasurementData(double t, double yRef, double y) {
		if (isInitialized) {
			measPanel.putData(t, yRef, y);
		} else {
			System.out.println("Note: GUI not yet initialized. Ignoring call to putMeasurementData().");
		}
	}

	public synchronized void changeActiveSize(int size){
		if(isInitialized){
			switch (size){
				case 1:
					small.setBackground(Color.GREEN);
					med.setBackground(Color.RED);
					big.setBackground(Color.RED);
					break;
				case 2:
					small.setBackground(Color.RED);
					med.setBackground(Color.GREEN);
					big.setBackground(Color.RED);
					break;
				case 3:
					small.setBackground(Color.RED);
					med.setBackground(Color.RED);
					big.setBackground(Color.GREEN);
					break;
				default:
					small.setBackground(Color.RED);
					med.setBackground(Color.RED);
					big.setBackground(Color.RED);
					break;

			}

		} else {
			System.out.println("Note: GUI not yet initialized. Ignoring call to changeActiveSize().");
		}
	}

	public synchronized void setProgressStatus(int ball) {
		ModeMonitor.Mode mode = modeMon.getMode();

		if(isInitialized){
			switch (mode) {
				case START: {

					//"START"
					if(ball == 0) {
						startState.activate();
						alignState.deactivate();
						fireState.deactivate();
						weighState.deactivate();
						smallBallState.deactivate();
						medBallState.deactivate();
						bigBallState.deactivate();
						finishState.deactivate();
						//"FINISH"
					} else if (ball == -1) {
						startState.deactivate();
						alignState.deactivate();
						fireState.deactivate();
						weighState.deactivate();
						smallBallState.deactivate();
						medBallState.deactivate();
						bigBallState.deactivate();
						finishState.activate();
					}
					break;
				}

				case ALIGN: {
					startState.deactivate();
					alignState.activate();
					fireState.deactivate();
					weighState.deactivate();
					smallBallState.deactivate();
					medBallState.deactivate();
					bigBallState.deactivate();
					finishState.deactivate();
					break;
				}

				case PUSH_BALL: {
					startState.deactivate();
					alignState.deactivate();
					fireState.activate();
					weighState.deactivate();
					smallBallState.deactivate();
					medBallState.deactivate();
					bigBallState.deactivate();
					finishState.deactivate();
					break;
				}
				case WEIGH_BALL: {
					startState.deactivate();
					alignState.deactivate();
					fireState.deactivate();
					weighState.activate();
					smallBallState.deactivate();
					medBallState.deactivate();
					bigBallState.deactivate();
					finishState.deactivate();
					break;
				}

				case MIDDLE: {
					if(ball == 1) {
						startState.deactivate();
						alignState.deactivate();
						fireState.deactivate();
						weighState.deactivate();
						smallBallState.activate();
						medBallState.deactivate();
						bigBallState.deactivate();
						finishState.deactivate();
					} else if (ball == 2) {
						startState.deactivate();
						alignState.deactivate();
						fireState.deactivate();
						weighState.deactivate();
						smallBallState.deactivate();
						medBallState.activate();
						bigBallState.deactivate();
						finishState.deactivate();
					}
					break;
				}
				case BIG: {
					startState.deactivate();
					alignState.deactivate();
					fireState.deactivate();
					weighState.deactivate();
					smallBallState.deactivate();
					medBallState.deactivate();
					bigBallState.activate();
					finishState.deactivate();

				}

				default:
					startState.deactivate();
					alignState.deactivate();
					fireState.deactivate();
					weighState.deactivate();
					smallBallState.deactivate();
					medBallState.deactivate();
					bigBallState.deactivate();
					finishState.deactivate();
					break;

			}
		} else {
			System.out.println("Note: GUI not yet initialized. Ignoring call to setProgressStatus().");
		}
	}
}
