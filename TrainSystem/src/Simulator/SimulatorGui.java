package Simulator;

import java.awt.EventQueue;

import javax.swing.JFrame;

import Modules.Ctc.Line;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JSeparator;

public class SimulatorGui {
	private enum Waysides{
		G1, G2, R1, R2;
	};
	
	protected Simulator simulator;
	
	private JFrame frame;
	private final int GUI_WINDOW_HEIGHT = 950;
	private final int GUI_WINDOW_WIDTH = 800;
	
	private JComboBox<String> cbTrainModelTrains;
	private JComboBox<String> cbTrainControllerTrains;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SimulatorGui window = new SimulatorGui(null);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SimulatorGui(Simulator sim) {
		simulator = sim;
		
		initialize();
		frame.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, GUI_WINDOW_WIDTH, GUI_WINDOW_HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		frame.getContentPane().setLayout(null);
		
		/*
		 * ------------------------------
		 *  CTC
		 * ------------------------------
		 */
		JButton btnCtc = new JButton("CTC");
		btnCtc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openGui(ModuleType.CTC);
			}
		});
		btnCtc.setBounds(537, 226, 171, 41);
		frame.getContentPane().add(btnCtc);
		
		/*
		 * ------------------------------
		 *  TRACK CONTROLLER
		 * ------------------------------
		 */
		JButton btnWaysideR1 = new JButton("R1");
		btnWaysideR1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openGui(ModuleType.TRACKCONTROLLER,Waysides.R1);
			}
		});
		btnWaysideR1.setBounds(568, 362, 65, 41);
		frame.getContentPane().add(btnWaysideR1);
		
		JButton btnWaysideR2 = new JButton("R2");
		btnWaysideR2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openGui(ModuleType.TRACKCONTROLLER,Waysides.R2);
			}
		});
		btnWaysideR2.setBounds(643, 362, 65, 41);
		frame.getContentPane().add(btnWaysideR2);
		
		JButton btnWaysideG1 = new JButton("G1");
		btnWaysideG1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openGui(ModuleType.TRACKCONTROLLER,Waysides.G1);
			}
		});
		btnWaysideG1.setBounds(568, 312, 65, 41);
		frame.getContentPane().add(btnWaysideG1);
		
		JButton btnWaysideG2 = new JButton("G2");
		btnWaysideG2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openGui(ModuleType.TRACKCONTROLLER,Waysides.G2);
			}
		});
		btnWaysideG2.setBounds(643, 312, 65, 41);
		frame.getContentPane().add(btnWaysideG2);
		
		/*
		 * ------------------------------
		 *  TRACK MODEL
		 * ------------------------------
		 */
		JButton btnTrackModel = new JButton("Track Model");
		btnTrackModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openGui(ModuleType.TRACKMODEL);
			}
		});
		btnTrackModel.setBounds(537, 465, 171, 41);
		frame.getContentPane().add(btnTrackModel);
		
		/*
		 * ------------------------------
		 *  TRAIN MODEL
		 * ------------------------------
		 */
		cbTrainModelTrains = new JComboBox<String>();
		cbTrainModelTrains.addItem("...");
		cbTrainModelTrains.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String trainName = (String)cbTrainModelTrains.getSelectedItem();
				if(trainName.equals("")) {
					return;
				}
				
				openGui(ModuleType.TRAINMODEL,trainName);
				cbTrainModelTrains.setSelectedIndex(0);
			}
		});		
		cbTrainModelTrains.setBounds(656, 562, 52, 39);
		frame.getContentPane().add(cbTrainModelTrains);
		
		/*
		 * ------------------------------
		 *  TRAIN CONTROLLER
		 * ------------------------------
		 */
		JButton btnTraincontroller = new JButton("TrainController");
		btnTraincontroller.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openGui(ModuleType.TRAINCONTROLLER);
			}
		});
		btnTraincontroller.setBounds(537, 652, 171, 41);
		frame.getContentPane().add(btnTraincontroller);
		
		cbTrainControllerTrains = new JComboBox<String>();
		cbTrainControllerTrains.addItem("...");
		cbTrainControllerTrains.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String trainName = (String)cbTrainControllerTrains.getSelectedItem();
				if(trainName.equals("")) {
					return;
				}
				
				openGui(ModuleType.TRAINCONTROLLER,trainName);
				cbTrainControllerTrains.setSelectedIndex(0);
			}
		});
		cbTrainControllerTrains.setBounds(469, 653, 52, 39);
		frame.getContentPane().add(cbTrainControllerTrains);
		
		/*
		 * ------------------------------
		 *  MBO
		 * ------------------------------
		 */
		JButton btnMbo = new JButton("MBO");
		btnMbo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openGui(ModuleType.MBO);
			}
		});
		btnMbo.setBounds(537, 774, 171, 41);
		frame.getContentPane().add(btnMbo);
		
		
		int y = 200;
		int dy = 133;
		int width = (int)(0.8 * GUI_WINDOW_WIDTH);
		int xStart = (GUI_WINDOW_WIDTH-width)/2;
		for(int i=0; i<5; i++) {
			JSeparator separator = new JSeparator();
			separator.setBounds(xStart, y, width, 2);
			frame.getContentPane().add(separator);
			y += dy;
		}		
	}

	protected void moduleObjectInitialized(ModuleType module) {
		//We can use this to update the GUI with what module has just been initialized
		switch (module) {
			case CTC:
				//...
				break;
			case TRACKCONTROLLER:
				//...
				break;
			case TRACKMODEL:
				//...
				break;
			case TRAINMODEL:
				//...
				break;
			case TRAINCONTROLLER:
				//...
				break;
			case MBO:
				//...
				break;		
		}
	}

	protected void moduleCommunicationInitialized(ModuleType module) {
		//We can use this to update the GUI with what module has just been initialized
		switch (module) {
			case CTC:
				//...
				break;
			case TRACKCONTROLLER:
				//...
				break;
			case TRACKMODEL:
				//...
				break;
			case TRAINMODEL:
				//...
				break;
			case TRAINCONTROLLER:
				//...
				break;
			case MBO:
				//...
				break;		
		}
	}
	
	private void openGui(ModuleType module) {
		//We can use this to open a respective Module's GUI
		switch (module) {
			case CTC:
				simulator.ctc.showGUI();
				break;
			case TRACKMODEL:
				simulator.trackModel.showGUI();
				break;
			case TRAINCONTROLLER:
				simulator.trainController.showGUI();
				break;
			case MBO:
				simulator.mbo.showGUI();
				break;		
		}
	}
	private void openGui(ModuleType module, Waysides wayside) {
		//We can use this to open a respective Module's GUI
		switch (module) {
			case TRACKCONTROLLER:
				simulator.ctc.launchWaysideGui(wayside.ordinal()); 
				break;
		}
	}
	private void openGui(ModuleType module, String trainName) {
		//We can use this to open a respective Module's GUI
		switch (module) {
			case TRAINMODEL:
				simulator.trainModel.getTrain(trainName).showTrainGUI();
				break;
			case TRAINCONTROLLER:
				// THIS STILL HAS BUGS
				simulator.trainController.getController(trainName).showGUI();
				break;	
		}
	}
	
	protected void repaint() {
		DefaultComboBoxModel<String> currentTrains = (DefaultComboBoxModel<String>)cbTrainControllerTrains.getModel();
		DefaultComboBoxModel<String> newTrains = new DefaultComboBoxModel<String>();
		
		Boolean changed = false;
		
		newTrains.addElement("...");
		for(String key : simulator.ctc.trains.keySet()) {
			if(currentTrains.getIndexOf(key)==-1) {
				changed = true;
			}
			
			newTrains.addElement(key);
		}
		
		if(changed) {
			cbTrainControllerTrains.setModel(newTrains);
			cbTrainModelTrains.setModel(newTrains);			
		}
	}
}