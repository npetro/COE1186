//Michael Kotcher

package Modules.TrainController;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import javax.swing.ImageIcon;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Font;
import java.awt.Image;
import java.text.DecimalFormat;

// UI STYLING
import java.awt.GraphicsEnvironment;
import java.awt.EventQueue;
import java.awt.event.*;
import java.io.*;
import javax.swing.border.*;
import java.awt.FontFormatException;
import javax.swing.JComboBox;
import javax.swing.UIManager;
import javax.swing.SwingConstants;
import java.awt.Color;

public class TrnControllerGUI {

	private JFrame frame;

	private JPanel contentPane;
	
	private JTextField newSpeedField;
	private JTextField tempField;
	
	private JButton speedSet;
	private JButton tempSet;
	private JButton modeBtn;
	private JButton serviceBtn;
	private JButton emergencyBtn;
	private JButton rightBtn;
	private JButton leftBtn;
	private JButton lightBtn;
	
	private JLabel speedValue;
	private JLabel setpointValue;
	private JLabel powerValue;
	private JLabel authorityValue;
		
	private int mode;	
	private String trainID;
	private double speed;		//miles per hour
	private double setpoint;	//miles per hour
	private double authority;	//miles
	private double power;		//watts
	private int temperature;	//F
	private int driveMode;		//0 is auto, 1 is manual
	private boolean left;
	private boolean right;
	private boolean service;
	private boolean emergency;
	private boolean lights;
	private int suggestedDoor;	//1 for right, -1 for left, 0 for neither
	private double speedLimit;
	
	private TrnController controller;
	
	public final double SPEEDCONVERSION = 2.23694;			//1 m/s = 2.23694 mph
	public final double DISTANCECONVERSION = 0.000621371;	//1 m = 0.000621371 miles
	public final double POWERCONVERSION = 1000;				//1 kW = 1000 W
	
	DecimalFormat df = new DecimalFormat("#.####");
	
	/*-------------------------------------------------------------------*/
	/**
	 * <COMMON AESTHETICS>
	 * ALLOWABLE FONTS
	 */
	
	Font font_14_bold = new Font("Roboto Condensed", Font.BOLD, 16);
	Font font_16_bold = new Font("Roboto Condensed", Font.BOLD, 20);
	Font font_20_bold = new Font("Roboto Condensed Bold", Font.BOLD, 30);
	Font font_24_bold = new Font("Roboto Condensed", Font.BOLD, 38);

	/**
	 * <COMMON AESTHETICS>
	 * SET LOOK AND FEEL - CALL THIS FIRST!!!
	 */
	public void setLookAndFeel(){
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}

		try {
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("Shared/fonts/RobotoCondensed-Bold.ttf")));
		    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("Shared/fonts/RobotoCondensed-BoldItalic.ttf")));
		    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("Shared/fonts/RobotoCondensed-Italic.ttf")));
		    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("Shared/fonts/RobotoCondensed-Regular.ttf")));

		    System.out.println("Loaded custom fonts!");
		} catch (IOException|FontFormatException e) {
		    System.out.println("HssVisualizer Error: Cannot load custom font.");
		}
	}

	/**
	 * <COMMON AESTHETICS>
	 * STANDARD BUTTON, COMBOBOX, LABEL WRAPPERS
	 */
	public void stylizeButton(JButton b){
		b.setFocusPainted(false);
		b.setFont(font_14_bold);
		b.setForeground(Color.WHITE);
		b.setBackground(new Color(102, 0, 153)); // Purple
	}

	public void stylizeComboBox(JComboBox c){
		c.setFont(font_14_bold);
		((JLabel)c.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
		c.setForeground(Color.BLACK);
		c.setBackground(Color.WHITE);
	}

	public void stylizeTextField(JTextField t){
		t.setFont(font_14_bold);
		t.setForeground(Color.BLACK);
		t.setBackground(Color.WHITE);
		t.setHorizontalAlignment(JTextField.CENTER);
	}

	public void stylizeHeadingLabel(JLabel l){
		l.setFont(font_20_bold);
		l.setForeground(Color.WHITE);
		l.setHorizontalAlignment(SwingConstants.LEFT);
	}

	public void stylizeInfoLabel(JLabel l){
		l.setHorizontalAlignment(SwingConstants.LEFT);
		l.setForeground(UIManager.getColor("Button.disabledToolBarBorderBackground"));
		l.setFont(font_16_bold);
	}

	public void stylizeInfoLabel_Bold(JLabel l){
		l.setHorizontalAlignment(SwingConstants.LEFT);
		l.setForeground(new Color(234, 201, 87));
		l.setFont(font_16_bold);
	}

	public void stylizeInfoLabel_Small(JLabel l){
		l.setHorizontalAlignment(SwingConstants.LEFT);
		l.setForeground(UIManager.getColor("Button.disabledToolBarBorderBackground"));
		l.setFont(font_14_bold);
	}
	/*-------------------------------------------------------------------*/
	
	public TrnControllerGUI(TrnController c, String s) {
		trainID = s;
		driveMode = 0;
		speed = 0;
		temperature = 70;
		setpoint = 0;
		authority = 0;
		power = 0;
		left = false;
		right = false;
		service = false;
		emergency = false;
		lights = false;
		controller = c;
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setBounds(600, 500, 600, 480);
		frame.setTitle(trainID);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(35, 35, 35));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		frame.setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel idenLabel = new JLabel(s + "");
		stylizeHeadingLabel(idenLabel);
		idenLabel.setBounds(20, 20, 400, 43);
		contentPane.add(idenLabel);
		
		JLabel speedLabel = new JLabel("CURRENT SPEED:");
		stylizeInfoLabel(speedLabel);
		speedLabel.setBounds(20, 75, 127, 26);
		contentPane.add(speedLabel);
		
		speedValue = new JLabel(speed + "mi/h");
		stylizeInfoLabel_Bold(speedValue);
		speedValue.setBounds(183, 75, 139, 26);
		contentPane.add(speedValue);
		
		JLabel newSpeedLabel = new JLabel("NEW SPEED:");
		stylizeInfoLabel(newSpeedLabel);
		newSpeedLabel.setBounds(20, 114, 109, 26);
		contentPane.add(newSpeedLabel);
		
		newSpeedField = new JTextField();
		stylizeTextField(newSpeedField);
		newSpeedField.setBounds(183, 111, 80, 32);
		contentPane.add(newSpeedField);
		newSpeedField.setColumns(10);
		newSpeedField.setText("");
		
		JLabel newSpeedUnitLabel = new JLabel("mi/h");
		stylizeInfoLabel(newSpeedUnitLabel);
		newSpeedUnitLabel.setBounds(290, 113, 61, 26);
		contentPane.add(newSpeedUnitLabel);
		
		speedSet = new JButton("SET NEW SPEED");
		stylizeButton(speedSet);
		speedSet.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (driveMode == 1) {
					try {
						double d;
						d = Double.parseDouble(newSpeedField.getText());
						if (d > speedLimit) {
							d = speedLimit;
						}
						setpointValue.setText(newSpeedField.getText() + " mi/hr");
						newSpeedField.setText("");
						setpoint = d;
						controller.setSetpointSpeed(setpoint / SPEEDCONVERSION);
					} 
					catch (NumberFormatException E) {
						newSpeedField.setText("");
					}
				}
			}
		});
		speedSet.setBounds(15, 152, 180, 37);
		contentPane.add(speedSet);
		
		JLabel setpointLabel = new JLabel("SETPOINT SPEED:");
		stylizeInfoLabel(setpointLabel);
		setpointLabel.setBounds(20, 201, 127, 26);
		contentPane.add(setpointLabel);
		
		setpointValue = new JLabel(setpoint + " mi/h");
		stylizeInfoLabel_Bold(setpointValue);
		setpointValue.setBounds(183, 204, 139, 22);
		contentPane.add(setpointValue);
		
		JLabel powerLabel = new JLabel("POWER OUTPUT:");
		stylizeInfoLabel(powerLabel);
		powerLabel.setBounds(20, 239, 127, 26);
		contentPane.add(powerLabel);
		
		powerValue = new JLabel(power + " kW");
		stylizeInfoLabel_Bold(powerValue);
		powerValue.setBounds(183, 242, 151, 22);
		contentPane.add(powerValue);
		
		JLabel authorityLabel = new JLabel("CURRENT AUTHORITY:");
		stylizeInfoLabel(authorityLabel);
		authorityLabel.setBounds(20, 277, 151, 26);
		contentPane.add(authorityLabel);
		
		authorityValue = new JLabel(authority + " mi");
		stylizeInfoLabel_Bold(authorityValue);
		authorityValue.setBounds(183, 279, 151, 22);
		contentPane.add(authorityValue);
		
		JLabel temperatureLabel = new JLabel("TEMPERATURE:");
		stylizeInfoLabel(temperatureLabel);
		temperatureLabel.setBounds(20, 315, 112, 21);
		contentPane.add(temperatureLabel);
		
		tempField = new JTextField();
		stylizeTextField(tempField);
		tempField.setBounds(183, 308, 80, 32);
		contentPane.add(tempField);
		tempField.setColumns(10);
		tempField.setText(temperature + "");
		
		JLabel tempUnitLabel = new JLabel("F");
		stylizeInfoLabel(tempUnitLabel);
		tempUnitLabel.setBounds(288, 316, 80, 16);
		contentPane.add(tempUnitLabel);
		
		tempSet = new JButton("SET");
		stylizeButton(tempSet);
		tempSet.setBounds(15, 348, 180, 37);
		tempSet.setEnabled(false);
		contentPane.add(tempSet);
		
		tempField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (driveMode == 1) {
					tempSet.setEnabled(true);
				}
			}
		});
		tempSet.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (driveMode == 1) {
					try {
						int t = Integer.parseInt(tempField.getText());
						temperature = t;
						controller.setTemperature(temperature);
					} 
					catch (NumberFormatException E) {
						tempField.setText(temperature + "");
					}
					tempSet.setEnabled(false);
				}
			}
		});
		
		JLabel sBrakesLabel = new JLabel("SERVICE BREAKS");
		stylizeInfoLabel(sBrakesLabel);
		sBrakesLabel.setBounds(376, 89, 200, 37);
		contentPane.add(sBrakesLabel);
		
		serviceBtn = new JButton();
		serviceBtn.setText("OFF");
		stylizeButton(serviceBtn);
		serviceBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (driveMode == 1) {
					if (serviceBtn.getText().equals("OFF")) {
						serviceBtn.setText("ON");
						service = true;
						controller.sBrakesOn();
					}
					else {
						serviceBtn.setText("OFF");
						service = false;
						controller.sBrakesOff();
					}
				}
			}
		});
		serviceBtn.setBounds(374, 120, 185, 37);
		contentPane.add(serviceBtn);
		
		JLabel eBrakesLabel = new JLabel("EMERGENCY BRAKES");
		stylizeInfoLabel(eBrakesLabel);
		eBrakesLabel.setBounds(376, 153, 200, 37);
		contentPane.add(eBrakesLabel);
		
		emergencyBtn = new JButton();
		emergencyBtn.setText("OFF");
		stylizeButton(emergencyBtn);
		emergencyBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (driveMode == 1) {
					if (emergencyBtn.getText().equals("OFF")) {
						emergencyBtn.setText("ON");
						emergency = true;
						controller.eBrakesOn();
					}
					else {
						emergencyBtn.setText("OFF");
						emergency = false;
						controller.eBrakesOff();
					}
				}
			}
		});
		emergencyBtn.setBounds(374, 184, 185, 37);
		contentPane.add(emergencyBtn);
		
		JLabel rightDoorLabel = new JLabel("RIGHT DOORS");
		stylizeInfoLabel(rightDoorLabel);
		rightDoorLabel.setBounds(376, 216, 200, 37);
		contentPane.add(rightDoorLabel);
		
		rightBtn = new JButton();
		rightBtn.setText("CLOSED");
		stylizeButton(rightBtn);
		rightBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (driveMode == 1) {
					if (rightBtn.getText().equals("CLOSED") && suggestedDoor == 1) {
						rightBtn.setText("OPEN");
						right = true;
						controller.openRight();
					}
					else if (rightBtn.getText().equals("CLOSED") && suggestedDoor != 1) {
						//do nothing
					}
					else {
						rightBtn.setText("CLOSED");
						right = false;
						controller.closeRight();
					}
				}
			}
		});
		rightBtn.setBounds(374, 246, 185, 37);
		contentPane.add(rightBtn);
		
		JLabel leftDoorLabel = new JLabel("LEFT DOORS");
		stylizeInfoLabel(leftDoorLabel);
		leftDoorLabel.setBounds(376, 277, 200, 37);
		contentPane.add(leftDoorLabel);
		
		leftBtn = new JButton();
		leftBtn.setText("CLOSED");
		stylizeButton(leftBtn);
		leftBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (driveMode == 1) {
					if (leftBtn.getText().equals("CLOSED") && suggestedDoor == -1) {
						leftBtn.setText("OPEN");
						left = true;
						controller.openLeft();
					}
					else if (leftBtn.getText().equals("CLOSED") && suggestedDoor != -1) {
						//do nothing
					}
					else {
						leftBtn.setText("CLOSED");
						left = false;
						controller.closeLeft();
					}
				}
			}
		});
		leftBtn.setBounds(374, 309, 185, 37);
		contentPane.add(leftBtn);
		
		JLabel lightLabel = new JLabel("LIGHTS");
		stylizeInfoLabel(lightLabel);
		lightLabel.setBounds(376, 341, 200, 37);
		contentPane.add(lightLabel);
		
		lightBtn = new JButton();
		lightBtn.setText("OFF");
		stylizeButton(lightBtn);
		lightBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (driveMode == 1) {
					if (lightBtn.getText().equals("OFF")) {
						lightBtn.setText("ON");
						lights = true;
						controller.lightsOn();
					}
					else {
						lightBtn.setText("OFF");
						lights = false;
						controller.lightsOff();
					}
				}
			}
		});
		lightBtn.setBounds(374, 374, 185, 37);
		contentPane.add(lightBtn);
		
		JLabel modeLabel = new JLabel("DRIVING MODE");
		stylizeInfoLabel_Bold(modeLabel);
		modeLabel.setBounds(376, 20, 200, 37);
		contentPane.add(modeLabel);
		
		modeBtn = new JButton();
		modeBtn.setText("AUTO");
		stylizeButton(modeBtn);
		modeBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (modeBtn.getText().equals("MANUAL")) {
					modeBtn.setText("AUTO");
					driveMode = 0;
					controller.setDriveMode(driveMode);
				}
				else {
					modeBtn.setText("MANUAL");
					driveMode = 1;
					controller.setDriveMode(driveMode);
				}
			}
		});
		modeBtn.setBounds(374, 53, 185, 37);
		contentPane.add(modeBtn);
		
		speedSet.setEnabled(false);
		tempSet.setEnabled(false);
		serviceBtn.setEnabled(false);
		emergencyBtn.setEnabled(false);
		rightBtn.setEnabled(false);
		leftBtn.setEnabled(false);
		lightBtn.setEnabled(false);
		tempField.setEnabled(false);
		newSpeedField.setEnabled(false);
		
		frame.setVisible(false);
	}
	
	public boolean guiUpdate() {
		speedValue.setText(df.format(speed) + " mi/h");
		setpointValue.setText(df.format(setpoint) + " mi/h");
		authorityValue.setText(df.format(authority) + " mi");
		powerValue.setText(df.format(power) + " kW");
		if (left) {
			leftBtn.setText("OPEN");
		}
		else {
			leftBtn.setText("CLOSED");
		}
		if (right) {
			rightBtn.setText("OPEN");
		}
		else {
			rightBtn.setText("CLOSED");
		}
		if (service) {
			serviceBtn.setText("ON");
		}
		else {
			serviceBtn.setText("OFF");
		}
		if (emergency) {
			emergencyBtn.setText("ON");
		}
		else {
			emergencyBtn.setText("OFF");
		}
		if (lights) {
			lightBtn.setText("ON");
		}
		else {
			lightBtn.setText("OFF");
		}
		if (driveMode == 1) {
			speedSet.setEnabled(true);
			tempSet.setEnabled(true);
			serviceBtn.setEnabled(true);
			emergencyBtn.setEnabled(true);
			rightBtn.setEnabled(true);
			leftBtn.setEnabled(true);
			lightBtn.setEnabled(true);
			tempField.setEnabled(true);
			newSpeedField.setEnabled(true);
		}
		else {
			speedSet.setEnabled(false);
			tempSet.setEnabled(false);
			serviceBtn.setEnabled(false);
			emergencyBtn.setEnabled(false);
			rightBtn.setEnabled(false);
			leftBtn.setEnabled(false);
			lightBtn.setEnabled(false);
			tempField.setEnabled(false);
			newSpeedField.setEnabled(false);
		}
		frame.repaint();
		return true;
	}
	
	public void setSpeed(double s) {
		speed = (s * SPEEDCONVERSION);
	}
	
	public void setSpeedLimit(double s) {	//input is in m/s
		speedLimit = (s * SPEEDCONVERSION);
	}
	
	public void setSetpoint(double s) {
		setpoint = (s * SPEEDCONVERSION);
	}
	
	public void setAuth(double a) {
		authority = (a * DISTANCECONVERSION);
	}
	
	public void setPower(double p) {
		power = (p / POWERCONVERSION);
	}
	
	public void setTemp(int t) {
		temperature = t;
	}
	
	public void setLeft(boolean b) {
		left = b;
	}
	
	public void setRight(boolean b) {
		right = b;
	}
	
	public void setService(boolean b) {
		service = b;
	}
	
	public void setEmergency(boolean b) {
		emergency = b;
	}
	
	public void setLights(boolean b) {
		lights = b;
	}
	
	public void setSuggestedDoor(int d) {
		suggestedDoor = d;
	}
	
	public void setVisible(boolean b) {
		frame.setVisible(b);
	}
	
	public String getId() {
		return trainID;
	}
}