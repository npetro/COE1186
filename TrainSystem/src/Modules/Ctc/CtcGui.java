package Modules.Ctc;

import Modules.TrackModel.Block;
import Modules.TrackModel.Light;
import Modules.TrackModel.Crossing;
import Modules.TrackModel.Station;
import Modules.TrackModel.Switch;
import Modules.TrackModel.Beacon;

import java.awt.EventQueue;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
//import java.io.File;
import java.awt.Container;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.Font;
import java.awt.Image;
import java.awt.SystemColor;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class CtcGui {
	private CtcCore ctc;
	
	private JFrame frame;
	
	private JLabel lblThroughputAmt;
	
	/**
	 * Dispatched train tables
	 */
	private Object[] dispatchedTrainsColumnNames = {"Train","Location","Speed","Authority","Passengers"};
	private Object[][] dispatchedTrainsInitialData = new Object[0][dispatchedTrainsColumnNames.length];

	private ScheduleJTable dispatchSelectedTable;
		
	/**
	 * Creator tables
	 */
	private ScheduleJTable trainCreationTable;
	private JTextField trainCreationDepartTime;
	private JComboBox<Line> trainCreationLine;
	private JTextField trainCreationName;
	
	/**
	 * Queue tables
	 */
	private Object[] queueTrainColumnNames = {"Train","Departure"};
	private Object[][] queueTrainInitialData = new Object[0][queueTrainColumnNames.length];
	
	private ScheduleJTable queueSelectedTable;
	private JButton btnRevertQueueSchedule;
	private JButton btnSaveQueueSchedule;
	private JButton btnDeleteQueueSchedule;
	
	private JSpinner blockNumberSpinner;
	private JComboBox<Line> blockLineComboBox;
	private JToggleButton selectedBlockToggle;
	private JButton btnRepairBlock;
	private JButton btnCloseTrack;
	private JLabel selectedBlockOccupiedIndicator;
	private JLabel selectedBlockStatusIndicator;
	
	private JLabel lblSpeedup;
	private JButton btnPause;
	private JButton btnPlay;
	private int currentSpeedupIndex = 0;
	private int[] availableSpeedups = {1,2,4,8,16,32};
	private JButton btnDecSpeed;
	private JButton btnIncSpeed;
	
	/**
	 * Constants
	 */
	private final int GUI_WINDOW_HEIGHT = 800;
	private final int GUI_WINDOW_WIDTH = 1400;

	/**
	 * Real Time
	 */
	private JLabel clockLabel = new JLabel("00:00:00");


	/**
	 * Create the application.
	 */
	public CtcGui(CtcCore ctc) {
		this.ctc = ctc;
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		finally{
			initialize();
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	/**
	 * @wbp.parser.entryPoint
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setTitle("CTC");
		frame.setBounds(100, 100, GUI_WINDOW_WIDTH, GUI_WINDOW_HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(null);
		
		JSeparator horizontalBar = new JSeparator();
		horizontalBar.setBackground(Color.BLACK);
		horizontalBar.setBounds(45, 128, 1300, 2);
		frame.getContentPane().add(horizontalBar);
		
		JSeparator horizontalBar2 = new JSeparator();
		horizontalBar2.setBackground(Color.BLACK);
		horizontalBar2.setBounds(45, 625, 1300, 2);
		frame.getContentPane().add(horizontalBar2);
		
		JSeparator leftVerticalBar = new JSeparator();
		leftVerticalBar.setOrientation(SwingConstants.VERTICAL);
		leftVerticalBar.setBackground(Color.BLACK);
		leftVerticalBar.setBounds(450, 148, 2, 440);
		frame.getContentPane().add(leftVerticalBar);
		
		JSeparator rightVerticalBar = new JSeparator();
		rightVerticalBar.setOrientation(SwingConstants.VERTICAL);
		rightVerticalBar.setBackground(Color.BLACK);
		rightVerticalBar.setBounds(920, 148, 2, 440);
		frame.getContentPane().add(rightVerticalBar);
		
		/**
		 * TOP BAR
		 */

		JLabel hazardIcon = new JLabel();
		hazardIcon.setIcon(new ImageIcon(CtcGui.class.getResource("/javax/swing/plaf/metal/icons/ocean/warning.png")));
		hazardIcon.setBounds(509, 657, 37, 32);
		frame.getContentPane().add(hazardIcon);
		
		clockLabel.setFont(new Font("Courier New",Font.BOLD,28));
		clockLabel.setHorizontalAlignment(SwingConstants.CENTER);
		clockLabel.setBounds(592, -1, 200, 44);
		contentPane.add(clockLabel);
		
		JLabel lblThroughput = new JLabel("Throughput: ");
		lblThroughput.setFont(new Font("SansSerif", Font.PLAIN, 16));
		lblThroughput.setBounds(631, 95, 98, 33);
		contentPane.add(lblThroughput);
		
		lblThroughputAmt = new JLabel("###");
		lblThroughputAmt.setBounds(724, 97, 89, 33);
		contentPane.add(lblThroughputAmt);
		
		/**
		 * LEFT FRAME
		 */
		
		JLabel lblManualTrainCreation = new JLabel("<html><u>Train Creation</u></htm>");
		lblManualTrainCreation.setHorizontalAlignment(SwingConstants.CENTER);
		lblManualTrainCreation.setFont(new Font("SansSerif", Font.BOLD, 20));
		lblManualTrainCreation.setBounds(-14, 142, 468, 33);
		contentPane.add(lblManualTrainCreation);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(18, 403, 221, 210);
		contentPane.add(scrollPane_1);
		
		trainCreationTable = new ScheduleJTable();
		trainCreationTable.addBlankRow();
		trainCreationTable.setFont(new Font("SansSerif", Font.PLAIN, 16));
		scrollPane_1.setViewportView(trainCreationTable);
		
		JLabel lblDepartAt = new JLabel("Depart @");
		lblDepartAt.setFont(new Font("SansSerif", Font.ITALIC, 14));
		lblDepartAt.setBounds(178, 344, 110, 33);
		contentPane.add(lblDepartAt);
		
		trainCreationDepartTime = new JTextField();
		trainCreationDepartTime.setText("N/A");
		trainCreationDepartTime.setBounds(173, 366, 66, 39);
		contentPane.add(trainCreationDepartTime);
		trainCreationDepartTime.setColumns(10);
		
		JLabel lblLine = new JLabel("Line");
		lblLine.setFont(new Font("SansSerif", Font.ITALIC, 14));
		lblLine.setBounds(99, 344, 45, 33);
		contentPane.add(lblLine);
			
		trainCreationLine = new JComboBox<Line>();
		trainCreationLine.setModel(new DefaultComboBoxModel<Line>(Line.values()));
		trainCreationLine.setBounds(92, 366, 52, 39);
		contentPane.add(trainCreationLine);
		
		JButton addToDispatchToQueue = new JButton("<html><center>Add Schedule<br>To Queue \u2192</center></html>");
		addToDispatchToQueue.setFont(new Font("Dialog", Font.PLAIN, 16));
		addToDispatchToQueue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Schedule schedule = trainCreationTable.schedule;
				ctc.addSchedule(schedule.name, schedule);
				updateQueueTable();
				
				//Remove from the creator
				trainCreationTable.clear();
				trainCreationTable.addBlankRow();
			}
		});
		addToDispatchToQueue.setBounds(251, 475, 171, 67);
		contentPane.add(addToDispatchToQueue);
		
		/**
		 * MID FRAME
		 */	
		
		JLabel lblQueue = new JLabel("<html><u>Queue</u></html>");
		lblQueue.setHorizontalAlignment(SwingConstants.CENTER);
		lblQueue.setFont(new Font("SansSerif", Font.BOLD, 20));
		lblQueue.setBounds(450, 142, 472, 33);
		contentPane.add(lblQueue);
		
		JTabbedPane queueTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		queueTabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 16));
		queueTabbedPane.setBounds(489, 174, 406, 187);
		contentPane.add(queueTabbedPane);
		
		for(Line line : Line.values()) {
			JScrollPane scrollPane = new JScrollPane();
			queueTabbedPane.addTab(line.toString(), null, scrollPane, null);
			line.queueData = new DefaultTableModel(queueTrainInitialData,queueTrainColumnNames);
			line.queueTable = new JTable(line.queueData);
			line.queueTable.setFont(new Font("SansSerif", Font.PLAIN, 16));
			line.queueTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int row = line.queueTable.rowAtPoint(e.getPoint());
					String trainName = (String) line.queueData.getValueAt(row, 0);
					Schedule schedule = ctc.getScheduleByName(trainName);
					openScheduleInTable(queueSelectedTable,schedule);
				}
			});
			scrollPane.setViewportView(line.queueTable);
		}
		
		JScrollPane scrollPane_4 = new JScrollPane();
		scrollPane_4.setBounds(476, 403, 221, 210);
		contentPane.add(scrollPane_4);
		
		queueSelectedTable = new ScheduleJTable();
		queueSelectedTable.setFont(new Font("SansSerif", Font.PLAIN, 16));
		scrollPane_4.setViewportView(queueSelectedTable);
		
		btnSaveQueueSchedule = new JButton("Save Changes");
		btnSaveQueueSchedule.setEnabled(false);
		btnSaveQueueSchedule.setFont(new Font("Dialog", Font.PLAIN, 16));
		btnSaveQueueSchedule.setBounds(713, 412, 171, 26);
		contentPane.add(btnSaveQueueSchedule);
		
		btnRevertQueueSchedule = new JButton("Revert Changes");
		btnRevertQueueSchedule.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO revert schedule
			}
		});
		btnRevertQueueSchedule.setEnabled(false);
		btnRevertQueueSchedule.setFont(new Font("Dialog", Font.PLAIN, 16));
		btnRevertQueueSchedule.setBounds(713, 437, 171, 26);
		frame.getContentPane().add(btnRevertQueueSchedule);
		
		btnDeleteQueueSchedule = new JButton("Delete selected");
		btnDeleteQueueSchedule.setEnabled(false);
		btnDeleteQueueSchedule.setFont(new Font("Dialog", Font.PLAIN, 16));
		btnDeleteQueueSchedule.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Line line = getLineByName(queueTabbedPane.getTitleAt(queueTabbedPane.getSelectedIndex()));
				int row = line.queueTable.getSelectedRow();
				String trainName = (String) line.queueData.getValueAt(row, 0);
				
				ctc.removeScheduleByName(trainName);
				updateQueueTable();

				queueSelectedTable.clear();
			}
		});
		btnDeleteQueueSchedule.setBounds(713, 475, 171, 41);
		contentPane.add(btnDeleteQueueSchedule);
		
		JButton btnDispatchQueueSchedule = new JButton("Dispatch Now \u2192");
		btnDispatchQueueSchedule.setFont(new Font("Dialog", Font.PLAIN, 16));
		btnDispatchQueueSchedule.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Line line = getLineByName(queueTabbedPane.getTitleAt(queueTabbedPane.getSelectedIndex()));
				int row = line.queueTable.getSelectedRow();
				String trainName = (String) line.queueData.getValueAt(row, 0);

				Schedule schedule = ctc.removeScheduleByName(trainName);
				updateQueueTable();
				
				ctc.addTrain(trainName,schedule);
				schedule.dispatched = true;
				updateDispatchedTable();
				
				queueSelectedTable.clear();		
			}
		});
		btnDispatchQueueSchedule.setBounds(713, 528, 171, 67);
		contentPane.add(btnDispatchQueueSchedule);
		
		
			
		/**
		 * RIGHT FRAME
		 */
		JLabel lblDispatchedTrains = new JLabel("<html><u>Dispatched Trains</u></html>");
		lblDispatchedTrains.setHorizontalAlignment(SwingConstants.CENTER);
		lblDispatchedTrains.setFont(new Font("SansSerif", Font.BOLD, 20));
		lblDispatchedTrains.setBounds(920, 142, 468, 33);
		contentPane.add(lblDispatchedTrains);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 16));
		tabbedPane.setBounds(946, 174, 425, 187);
		contentPane.add(tabbedPane);
		
		for(Line line : Line.values()) {
			JScrollPane scrollPane = new JScrollPane();
			
			line.dispatchedData = new DefaultTableModel(dispatchedTrainsInitialData,dispatchedTrainsColumnNames);
			line.dispatchedTable = new JTable(line.dispatchedData);
			line.dispatchedTable.setFont(new Font("SansSerif", Font.PLAIN, 16));
			line.dispatchedTable.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int row = line.dispatchedTable.rowAtPoint(e.getPoint());
					String trainName = (String) line.dispatchedData.getValueAt(row, 0);
					Schedule schedule = ctc.getTrainByName(trainName).schedule;
					openScheduleInTable(dispatchSelectedTable,schedule);
				}
			});
			scrollPane.setViewportView(line.dispatchedTable);
			tabbedPane.addTab(line.toString(), null, scrollPane, null);
		}
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(942, 403, 221, 210);
		contentPane.add(scrollPane);
		
		dispatchSelectedTable = new ScheduleJTable();
		dispatchSelectedTable.setFont(new Font("SansSerif", Font.PLAIN, 16));
		scrollPane.setViewportView(dispatchSelectedTable);
		
		JLabel lblSelectedTrainSchedule = new JLabel("Selected Train's Schedule");
		lblSelectedTrainSchedule.setFont(new Font("SansSerif", Font.PLAIN, 18));
		lblSelectedTrainSchedule.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelectedTrainSchedule.setBounds(920, 367, 456, 33);
		contentPane.add(lblSelectedTrainSchedule);
		
		JButton editSelectedDispatchedTrainSchedule = new JButton("Edit Schedule");
		editSelectedDispatchedTrainSchedule.setFont(new Font("Dialog", Font.PLAIN, 16));
		editSelectedDispatchedTrainSchedule.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Line line = getLineByName(tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
				int row = line.dispatchedTable.getSelectedRow();
				if(row<0) return;
				String trainName = (String) line.dispatchedData.getValueAt(row, 0);
				
				Schedule schedule = ctc.getTrainByName(trainName).schedule;
				ctc.addTrain(schedule.name,schedule);
				updateDispatchedTable();			
			}
		});
		editSelectedDispatchedTrainSchedule.setBounds(1179, 403, 171, 41);
		contentPane.add(editSelectedDispatchedTrainSchedule);
		
		JButton btnimportschedule = new JButton("<html><center>Import<br>Schedule</center></html>");
		btnimportschedule.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				FileFilter filter = new FileNameExtensionFilter("CSV file", new String[] {"csv"});
				fc.setFileFilter(filter);
				fc.showSaveDialog(frame);
				//File file = fc.getSelectedFile();
				//TODO import CSV file and add schedule to queue
			}
		});
		btnimportschedule.setFont(new Font("Dialog", Font.PLAIN, 16));
		btnimportschedule.setBounds(144, 221, 163, 64);
		frame.getContentPane().add(btnimportschedule);
		
		JLabel lblSelectedTrainsSchedule = new JLabel("Selected Train's Schedule");
		lblSelectedTrainsSchedule.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelectedTrainsSchedule.setFont(new Font("SansSerif", Font.PLAIN, 18));
		lblSelectedTrainsSchedule.setBounds(464, 367, 456, 33);
		frame.getContentPane().add(lblSelectedTrainsSchedule);
		
		trainCreationName = new JTextField();
		trainCreationName.setText("N/A");
		trainCreationName.setColumns(10);
		trainCreationName.setBounds(18, 366, 52, 39);
		frame.getContentPane().add(trainCreationName);
		
		JLabel lblName = new JLabel("Name");
		lblName.setFont(new Font("SansSerif", Font.ITALIC, 14));
		lblName.setBounds(25, 344, 45, 33);
		frame.getContentPane().add(lblName);
		
		JLabel lblAutomatic = new JLabel("Automatic");
		lblAutomatic.setHorizontalAlignment(SwingConstants.CENTER);
		lblAutomatic.setFont(new Font("SansSerif", Font.PLAIN, 18));
		lblAutomatic.setBounds(-14, 187, 477, 33);
		frame.getContentPane().add(lblAutomatic);
		
		JLabel lblManual = new JLabel("Manual");
		lblManual.setHorizontalAlignment(SwingConstants.CENTER);
		lblManual.setFont(new Font("SansSerif", Font.PLAIN, 18));
		lblManual.setBounds(-14, 297, 477, 33);
		frame.getContentPane().add(lblManual);
		
		JLabel logoPineapple = new JLabel();
		Image img = new ImageIcon(this.getClass().getResource("pineapple_icon.png")).getImage();
		logoPineapple.setIcon(new ImageIcon(img));
		logoPineapple.setBounds(1250,660,138,76);
		contentPane.add(logoPineapple);
		
		JLabel lbltrackStatusAnd = new JLabel("<html><u>Track Status<br> and Maintenance</u></htm>");
		lbltrackStatusAnd.setHorizontalAlignment(SwingConstants.LEFT);
		lbltrackStatusAnd.setFont(new Font("SansSerif", Font.BOLD, 20));
		lbltrackStatusAnd.setBounds(383, 651, 163, 63);
		frame.getContentPane().add(lbltrackStatusAnd);
		
		JLabel selectedBlockLine = new JLabel("Line");
		selectedBlockLine.setFont(new Font("SansSerif", Font.ITALIC, 14));
		selectedBlockLine.setBounds(575, 657, 37, 33);
		frame.getContentPane().add(selectedBlockLine);
		
		JLabel selectedBlockNum = new JLabel("Block");
		selectedBlockNum.setFont(new Font("SansSerif", Font.ITALIC, 14));
		selectedBlockNum.setBounds(631, 657, 37, 33);
		frame.getContentPane().add(selectedBlockNum);
		
		btnCloseTrack = new JButton("Close for Maintenance");
		btnCloseTrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Block block = getSelectedBlock();
				block.setMaintenance(true);
				updateSelectedBlock();
			}
		});
		btnCloseTrack.setFont(new Font("Dialog", Font.PLAIN, 16));
		btnCloseTrack.setBounds(815, 672, 200, 32);
		frame.getContentPane().add(btnCloseTrack);
		
		btnRepairBlock = new JButton("Repair Block");
		btnRepairBlock.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Block block = getSelectedBlock();
				block.setMaintenance(false);
				updateSelectedBlock();
			}
		});
		btnRepairBlock.setFont(new Font("Dialog", Font.PLAIN, 16));
		btnRepairBlock.setBounds(815, 705, 200, 32);
		frame.getContentPane().add(btnRepairBlock);
		
		selectedBlockToggle = new JToggleButton("Toggle Switch");
		selectedBlockToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Block block = getSelectedBlock();
				block.getSwitch().setState(selectedBlockToggle.isSelected());
			}
		});
		selectedBlockToggle.setFont(new Font("Dialog", Font.PLAIN, 16));
		selectedBlockToggle.setBounds(815, 639, 200, 32);
		frame.getContentPane().add(selectedBlockToggle);
		
		JLabel lblOccupied = new JLabel("Occupied");
		lblOccupied.setFont(new Font("SansSerif", Font.PLAIN, 14));
		lblOccupied.setBounds(725, 688, 70, 33);
		frame.getContentPane().add(lblOccupied);
		
		selectedBlockOccupiedIndicator = new JLabel();
		selectedBlockOccupiedIndicator.setBounds(791, 688, 45, 32);
		setIndicator(selectedBlockOccupiedIndicator,"grey");
		frame.getContentPane().add(selectedBlockOccupiedIndicator);
		
		JLabel lblStatus = new JLabel("Status");
		lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 14));
		lblStatus.setBounds(745, 655, 52, 33);
		frame.getContentPane().add(lblStatus);
		
		selectedBlockStatusIndicator = new JLabel();
		selectedBlockStatusIndicator.setBounds(791, 655, 45, 32);
		setIndicator(selectedBlockStatusIndicator,"grey");
		frame.getContentPane().add(selectedBlockStatusIndicator);
		
		blockLineComboBox = new JComboBox<Line>();
		blockLineComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setBlockSpinnerLimits();
			}
		});
		blockLineComboBox.setModel(new DefaultComboBoxModel<Line>(Line.values()));
		blockLineComboBox.setBounds(558, 687, 71, 26);
		frame.getContentPane().add(blockLineComboBox);
		
		blockNumberSpinner = new JSpinner();
		setBlockSpinnerLimits();
		blockNumberSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				updateSelectedBlock();
			}
		});
		blockNumberSpinner.setBounds(631, 686, 70, 28);
		frame.getContentPane().add(blockNumberSpinner);
		
		updateSelectedBlock();
		
		JPanel panel = new JPanel();
		panel.setBackground(SystemColor.inactiveCaption);
		panel.setBounds(709, 638, 325, 101);
		frame.getContentPane().add(panel);
		
		btnPlay = new JButton("<html><center>Play</center></html>");
		btnPlay.setEnabled(false);
		btnPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPause.setEnabled(true);
				btnPlay.setEnabled(false);
				ctc.play();
			}
		});
		btnPlay.setFont(new Font("Dialog", Font.PLAIN, 16));
		btnPlay.setBounds(612, 35, 82, 32);
		frame.getContentPane().add(btnPlay);
		
		btnPause = new JButton("<html><center>Pause</center></html>");
		btnPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPause.setEnabled(false);
				btnPlay.setEnabled(true);
				ctc.pause();
			}
		});
		btnPause.setFont(new Font("Dialog", Font.PLAIN, 16));
		btnPause.setBounds(690, 35, 82, 32);
		frame.getContentPane().add(btnPause);
		
		btnIncSpeed = new JButton("<html><center>&gt&gt</center></html>");
		btnIncSpeed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentSpeedupIndex = Math.min(currentSpeedupIndex+1,availableSpeedups.length-1);
				lblSpeedup.setText(Integer.toString(availableSpeedups[currentSpeedupIndex])+"X");
				ctc.setSpeedup(availableSpeedups[currentSpeedupIndex]);
			}
		});
		btnIncSpeed.setFont(new Font("Dialog", Font.PLAIN, 16));
		btnIncSpeed.setBounds(712, 68, 60, 32);
		frame.getContentPane().add(btnIncSpeed);
		
		btnDecSpeed = new JButton("<html><center>&lt&lt</center></html>");
		btnDecSpeed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentSpeedupIndex = Math.max(currentSpeedupIndex-1,0);
				lblSpeedup.setText(Integer.toString(availableSpeedups[currentSpeedupIndex])+"X");
				ctc.setSpeedup(availableSpeedups[currentSpeedupIndex]);
			}
		});
		btnDecSpeed.setFont(new Font("Dialog", Font.PLAIN, 16));
		btnDecSpeed.setBounds(612, 68, 60, 32);
		frame.getContentPane().add(btnDecSpeed);
		
		lblSpeedup = new JLabel("1X");
		lblSpeedup.setHorizontalAlignment(SwingConstants.CENTER);
		lblSpeedup.setBounds(673, 67, 39, 33);
		frame.getContentPane().add(lblSpeedup);
	}
	
	protected void setBlockSpinnerLimits() {
		Line line = (Line)blockLineComboBox.getSelectedItem();

		blockNumberSpinner.setModel(new SpinnerNumberModel(1, 1, line.blocks.length-1, 1));
		blockNumberSpinner.setValue(1);
	}

	private Block getSelectedBlock() {
		Block block = null;
		int blockNumber = (int) blockNumberSpinner.getValue();
		Line line = (Line)blockLineComboBox.getSelectedItem();
		
		block = line.blocks[blockNumber];

		return block;
	}
	
	private void updateSelectedBlock() {
		Block block = getSelectedBlock();
		
		if(block.STATUS_WORKING==true) {
			//Track is open, check if switch
			if(block.getSwitch() != null) {
				selectedBlockToggle.setEnabled(true);
				selectedBlockToggle.setSelected(block.getSwitch().getState());
			}
			else {
				selectedBlockToggle.setEnabled(false);
			}
			
			btnRepairBlock.setEnabled(false);
			btnCloseTrack.setEnabled(true);
			setIndicator(selectedBlockStatusIndicator,"green");
		}
		else {
			//Track is closed, disable switch toggle regardless
			selectedBlockToggle.setEnabled(false);
			
			btnRepairBlock.setEnabled(true);
			btnCloseTrack.setEnabled(false);
			setIndicator(selectedBlockStatusIndicator,"red");
		}
		
		if(block.getOccupied() == true) {
			setIndicator(selectedBlockOccupiedIndicator,"green");
		}
		else {
			setIndicator(selectedBlockOccupiedIndicator,"grey");
		}
	}

	private void setIndicator(JLabel label, String color) {
		Image imgStatus = new ImageIcon(this.getClass().getResource(color+"StatusIcon.png")).getImage();
		label.setIcon(new ImageIcon(imgStatus));
	}

	private void openScheduleInTable(ScheduleJTable table, Schedule schedule) {
		//data.fireTableDataChanged();
		//table.schedule = schedule;						
	}
	
	private void updateQueueTable(){
		//Clear the red and green tables
		for(Line line : Line.values()) {
			line.queueData.setDataVector(queueTrainInitialData, queueTrainColumnNames);
		}
		
		//Cycle through each dispatched train's schedule
		Schedule schedule;
		for(String trainName:ctc.schedules.keySet()) {
			schedule = ctc.getScheduleByName(trainName);
			Object[] row = {schedule.name,schedule.departureTime.toString()};
			schedule.line.queueData.addRow(row);
		}

		//Update the tables in the GUI
		for(Line line : Line.values()) {
			line.queueData.fireTableDataChanged();
		}
	}

	private Line getLineByName(String s) {
		s = s.toLowerCase();
		Line line = null;
		for(Line l : Line.values()) {
			if(s.equals(l.toString().toLowerCase())){
				line = l;
			}
		}
		return line;
	}
	
	private void updateDispatchedTable(){
		//Clear the red and green tables
		for(Line line : Line.values()) {
			line.dispatchedData.setDataVector(dispatchedTrainsInitialData, dispatchedTrainsColumnNames);
		}

		//Cycle through each dispatched train's schedule
		Train train;
		for(String trainName:ctc.trains.keySet()) {
			train = ctc.getTrainByName(trainName);
			//Object[] row; //build the row here, but for now we fake the functionality below
			Object[] row = {train.name,"C9","0",train.authority+" mi","0"};
			train.line.dispatchedData.addRow(row);
		}

		//Update the tables in the GUI
		for(Line line : Line.values()) {
			line.dispatchedData.fireTableDataChanged();
		}
	}
	
	public void repaint() {
		//Update Throughput Label
		lblThroughputAmt.setText(Double.toString(ctc.throughput));
		
		//Update Time
		clockLabel.setText(ctc.currentTime.toString());
		
		//Update the info of the selected block
		updateSelectedBlock();
		
		//Dispatch trains who are scheduled to be dispatched
		//checkQueueForDisaptches();
		
		//Update the locations of trains
		//updateDispatchedTable();
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new CtcGui(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
