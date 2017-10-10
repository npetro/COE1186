import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.JComboBox;

public class ScheduleEditor {
	public Schedule currentSchedule;
	public boolean editing = true;
	public JFrame frame;
	private JTextField nameInput;
	private JTextField authorityInput;
	private JComboBox<String> lineSelect;


	/**
	 * Create the application.
	 */
	public ScheduleEditor() {
		initialize();
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public ScheduleEditor(Schedule schedule) {		
		initialize();
		if(schedule==null) {
			currentSchedule = new Schedule();
		}
		else {
			currentSchedule = schedule;
			nameInput.setText(currentSchedule.name);
			authorityInput.setText(Integer.toString(currentSchedule.authority));
		}		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 600);
		frame.getContentPane().setLayout(null);
		
		JButton btnCreateSchedule = new JButton("Create Schedule");
		btnCreateSchedule.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentSchedule.name = nameInput.getText();
				currentSchedule.line = (String) lineSelect.getSelectedItem();
				currentSchedule.authority = Integer.parseInt(authorityInput.getText());
				
				frame.dispose();
				editing = false;
			}
		});
		btnCreateSchedule.setBounds(111, 421, 171, 41);
		frame.getContentPane().add(btnCreateSchedule);
		
		nameInput = new JTextField();
		nameInput.setBounds(150, 60, 199, 39);
		frame.getContentPane().add(nameInput);
		nameInput.setColumns(10);
		
		JLabel lblTrainName = new JLabel("Train Name:");
		lblTrainName.setBounds(42, 63, 115, 33);
		frame.getContentPane().add(lblTrainName);
		
		authorityInput = new JTextField();
		authorityInput.setColumns(10);
		authorityInput.setBounds(150, 200, 199, 39);
		frame.getContentPane().add(authorityInput);
		
		JLabel lblTrainAuthority = new JLabel("Train Authority:");
		lblTrainAuthority.setBounds(26, 203, 115, 33);
		frame.getContentPane().add(lblTrainAuthority);
		
		lineSelect = new JComboBox<String>();
		lineSelect.setModel(new DefaultComboBoxModel<String>(new String[] {"Red", "Green"}));
		lineSelect.setBounds(150, 127, 199, 39);
		frame.getContentPane().add(lineSelect);
		
		JLabel lblLine = new JLabel("Line");
		lblLine.setBounds(83, 127, 115, 33);
		frame.getContentPane().add(lblLine);
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ScheduleEditor window = new ScheduleEditor(new Schedule());
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
