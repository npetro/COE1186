/**
 * Author: Jennifer Patterson
 * Course: CoE 1186 - Software Engineering
 * Group: HashSlinging Slashers
 * Date Created: 10/3/17
 * Date Modified: 11/26/17
 */

package Modules.TrainModel;
import java.awt.Color;
import java.awt.EventQueue;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.CRC32;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;

import Modules.TrackModel.Block;
import Modules.TrackModel.Station;
import Modules.TrackModel.TrackModel;
import Shared.SimTime;

/**
 * Class that implements functionality separately for each train that is active on the track
 * Trains become instantiated through the CTC when they are dispatched from the yard.
 * 
 * @author Jennifer
 *
 */
public class Train {
	public final double TRAIN_WEIGHT = 81800; 	// currently in lbs
	public final int TRAIN_CAPACITY = 222; 	// per 1 car
	public final double TRAIN_LENGTH = 32.2; 	// in meters currently
	public final double TRAIN_WIDTH = 2.65; 	// m currently
	public final double TRAIN_HEIGHT = 3.42; 	// m currently
	public final double TRAIN_MAX_ACCELERATION = 0.5; 	// m/s^2
	public final double TRAIN_MAX_ACCELERATION_SERVICE_BRAKE = -1.2; 	// m/s^2
	public final double TRAIN_MAX_ACCELERATION_E_BRAKE = -2.73; 	// m/s^2
	public final double TRAIN_MAX_SPEED = 70; 	// km/h currently
	public final double TRAIN_MAX_GRADE = 60;	// percent
	public final int TRAIN_NUM_WHEELS = 6;
	public final double G = 9.8; 	// m/s^2
	public final double FRICTION_COEFFICIENT = 0.16;
	public final double AVE_PASSENGER_WEIGHT = 142.97; //in lbs
	
	//following variables are for back-end conversions
	//public final double M_PER_SEC_TO_MPH = 2.23694;
	public final double SECONDS_PER_HOUR = 3600;      
    public final double METERS_PER_MILE = 1609.34;    
    public final double FEET_PER_METER = 3.28;         
    public final double KG_PER_POUND = 0.454; 
    public final String DEGREE = "\u00b0";
    public final double MS_TO_MPH = 2.23694;
    public final double KPH_TO_MPH = 0.62137119;
    
    public final int APPROACHING = 0;
    public final int ARRIVING = 1;
    public final int DEPARTING = 2;
    public final int EN_ROUTE = 3;
    
    // Declaring a variables for the GUI
    public TrainModelGUI trainModelGUI;
    public JFrame trainModelFrame;
    public TrainModel trnMdl;
    public TrackModel trkMdl;
    public ArrayList<Block> track;
    public Position position;
    
    // Declaring variables in order of sections they appear in on the GUI
    
    // Train Specs
    private String trainID;
    private double trainHeight;
    private double trainWeight;
    private double trainLength;
    private double trainWidth;
    private int trainCars;
    private int trainCapacity;
    private int trainWheels;
    
    // Track Information
    private int prevBlock;
    private int currentBlock;
    private int nextBlock;
    private double currentX;
    private double currentY;
    private String lineColor;
    private double grade = 0;
    private int currentSpeedLimit;
    private boolean GPSAntenna;
    private boolean MBOAntenna;
    
    // Failure Modes Activation
    private boolean engineFailureActive;
    private boolean signalFailureActive;
    private boolean brakeFailureActive;
    
    // Station Control
    //private Station nextStation;
    private double timeOfArrival;
    //private Station nextStation;
    private int arrivalStatus;
    private int numPassengers;
    private int crew;
    private String station;
    private int beacon;
    
    // Speed/Authority
    private double currentSpeed;
    private double CTCSpeed;
    private double CTCAuthority;
    private double powerIn;
    
    // Train Operations
    private boolean leftDoorIsOpen;
    private boolean rightDoorIsOpen;
    private boolean lightsAreOn;
    private int temperature;
    private boolean serviceBrake;
    private boolean emerBrake;
    private boolean driverEmerBrake;
    
    // Other Variables not associated with GUI outputs
    private boolean trainActive;
    private double force;
    private double normalForce;
    private double downwardForce;
    private double totalForce;
    private double friction;
    private double slope;
    private double finalSpeed;
    private double trainAcceleration;
    private double distTravelled;
    private double metersIn;
    private boolean setExit;
    public boolean embarkingPassengersSet;
    
    public Train(String line, String trainID, TrainModel tm, TrackModel tkmdl) {
    	this.trkMdl = tkmdl;
    	this.lineColor = line;
    	this.track = this.trkMdl.getTrack(line);
    	this.position = new Position(track);
    	this.trnMdl = tm;
    	this.trainActive = true;
    	this.trainID = trainID;
    	// Train Specs
    	this.trainCars = 2;
    	this.trainCapacity = TRAIN_CAPACITY * this.trainCars;
        this.trainHeight = TRAIN_HEIGHT;
        this.trainWeight = (TRAIN_WEIGHT * this.trainCars) + ((crew + numPassengers) * AVE_PASSENGER_WEIGHT);
        this.trainLength = TRAIN_LENGTH * this.trainCars;
        this.trainWidth = TRAIN_WIDTH;
        this.crew = 1; // crew will always be one driver
        this.trainWheels = this.trainCars * TRAIN_NUM_WHEELS;
        
        // Track Information
        this.currentBlock = this.trkMdl.getTrack(line).size()-1;
        this.nextBlock = position.getCurrentBlock();
        
        //this.currentBlock = 0;
        //this.nextBlock = 0;
        //this.prevBlock = 0;
        
        this.grade = 0;
        //this.currentSpeedLimit = 0;
        this.GPSAntenna = true;
        this.MBOAntenna = true;
        
        // Failure Modes Activation
        this.engineFailureActive = false;
        this.signalFailureActive = false;
        this.brakeFailureActive = false;
        
        // Station Control
        //this.nextStation = new Station();
        this.timeOfArrival = 0;
        this.arrivalStatus = EN_ROUTE;
        this.numPassengers = 0;
        this.beacon = 0;
        //this.station = null;
        
        // Speed/Authority
        this.currentSpeed = 0;
        this.CTCSpeed = 0;
        this.CTCAuthority = 0;
        this.powerIn = 0.0;
        
        // Train Operations
        this.leftDoorIsOpen = false;
        this.rightDoorIsOpen = false;
        this.lightsAreOn  =false;
        this.temperature = 69;
        this.serviceBrake = false;
        this.emerBrake = false;
        
        // Other Variables not associated with GUI outputs
        this.force = 0;
        this.normalForce = 0;
        this.downwardForce = 0;
        this.totalForce = 0;
        this.friction = 0;
        this.slope = 0;
        this.finalSpeed = 0;
        this.trainAcceleration = 0;
        this.metersIn = 0.0;
    	this.setExit = false;
    	this.trainModelGUI = null;
    }
    
    /**
     * Method that creates a new GUI for a train model
     * @return
     */
    public TrainModelGUI CreateNewGUI() {
        //Create a GUI object
    	trainModelGUI = new TrainModelGUI(this);
    	setValuesForDisplay();
    	return trainModelGUI;
    }
    
    /**
     * Returns a train's GUI
     * @return
     */
    public TrainModelGUI getTrainGUI() {
    	return this.trainModelGUI;
    }
    
    /**
     * Displays a trains GUI
     */
    public void showTrainGUI() {
        //Make sure to set it visible
        this.getTrainGUI().setTitle(this.getTrainID());
        this.getTrainGUI().setVisible(true);
    }

    /**
     * Check all values for updates and reflect these updates on the GUI per clock tick
     */
    public void setValuesForDisplay() {
    	this.trainModelGUI.tempLabel.setText(Integer.toString(this.temperature) + DEGREE + "F");

        //this.trainCars = this.trainModelGUI.numCars();
        this.trainWheels = this.trainCars * TRAIN_NUM_WHEELS;
        this.trainModelGUI.crewCountLabel.setText(Integer.toString(crew));
        this.trainModelGUI.heightVal.setText(Double.toString(truncateTo(this.trainHeight, 2)));
        this.trainModelGUI.widthVal.setText(Double.toString(truncateTo(this.trainWidth, 2)));
        this.trainModelGUI.lengthVal.setText(Double.toString(truncateTo(this.trainLength, 2)));
        this.trainModelGUI.weightVal.setText(Integer.toString(((int)this.trainWeight)));
        this.trainModelGUI.capacityVal.setText(Integer.toString(this.trainCapacity));
        this.trainModelGUI.powerVal.setText(Double.toString(truncateTo(this.powerIn/1000,2)));
        
        GPSAntenna = this.trainModelGUI.signalFailStatus();
        if(!GPSAntenna) {
        	this.trainModelGUI.gpsAntennaStatusLabel.setText("ON");
        } else {
        	this.trainModelGUI.gpsAntennaStatusLabel.setText("OFF");
        }
        MBOAntenna = this.trainModelGUI.signalFailStatus();
        if(!MBOAntenna) {
        	this.trainModelGUI.mboAntennaStatusLabel.setText("ON");
        } else {
        	this.trainModelGUI.mboAntennaStatusLabel.setText("OFF");
        }
     	
     	this.trainModelGUI.timeVal.setText(trnMdl.currentTime.toString());
     	this.trainModelGUI.stationVal.setText(this.station);
     	
     	if(rightDoorIsOpen == true) {
        	this.trainModelGUI.rightDoorStatusLabel.setText("OPEN");
        } else {
        	this.trainModelGUI.rightDoorStatusLabel.setText("CLOSED");
        }
     	if(leftDoorIsOpen == true) {
        	this.trainModelGUI.leftDoorStatusLabel.setText("OPEN");
        } else {
        	this.trainModelGUI.leftDoorStatusLabel.setText("CLOSED");
        }

     	if(lightsAreOn == true) {
     		this.trainModelGUI.lightStatusLabel.setText("ON");
        } else {
        	this.trainModelGUI.lightStatusLabel.setText("OFF");
        }
     	
     	this.trainModelGUI.numPassengers.setText(Integer.toString(this.numPassengers));
     	this.trainModelGUI.numCarsSpinner.setText(Integer.toString(this.trainCars));
     	this.trainModelGUI.authorityVal.setText(Double.toString(truncateTo(this.CTCAuthority/METERS_PER_MILE,2)));
     	this.trainModelGUI.ctcSpeedLabel.setText(Double.toString(truncateTo(this.CTCSpeed*KPH_TO_MPH,2)));
     	
     	if(serviceBrake) {
     		this.trainModelGUI.serviceLabel.setText("ON");
        } else {
        	this.trainModelGUI.serviceLabel.setText("OFF");
        }
     	if(emerBrake) {
     		this.trainModelGUI.emergencyLabel.setText("ON");
        } else {
        	this.trainModelGUI.emergencyLabel.setText("OFF");
        }
     	
     	if(this.arrivalStatus == ARRIVING) {
     		this.trainModelGUI.arrivalStatusLabel.setText("ARRIVING");
     	} else if (this.arrivalStatus == EN_ROUTE) {
     		this.trainModelGUI.arrivalStatusLabel.setText("EN ROUTE");
     	} else if (this.arrivalStatus == APPROACHING) {
     		this.trainModelGUI.arrivalStatusLabel.setText("APPROACHING");
     	} else {
     		this.trainModelGUI.arrivalStatusLabel.setText("DEPARTING");
            embarkingPassengersSet = false;
     	}

     	this.trainModelGUI.currentSpeedLabel.setText(Double.toString(truncateTo((this.currentSpeed*MS_TO_MPH), 2)));
         
     	if (this.lineColor.equals("GREEN")) {
     		this.trainModelGUI.lblLine.setText(lineColor);
     		this.trainModelGUI.lblLine.setForeground(Color.GREEN);
     	} else {
     		this.trainModelGUI.lblLine.setText("RED");
     		this.trainModelGUI.lblLine.setForeground(Color.RED);
        }
     	
    }
    
    /**
     * Same concecpt behind this method as the setValuesForDisplay() method, except this method is purely calculations
     * surrounding the train's velocity, which is it's core functionality
     */
    public void updateVelocity() {
    	// Step 1: input power and convert the power to a force based on the starting velocity
    	setWeight();
    	double trainMass = trainWeight*KG_PER_POUND;
    	
    	// this is ensuring that we never get a negative speed
    	if (this.currentSpeed == 0) {
    		this.force = (this.powerIn)/1;
    	} else {
    		this.force = (this.powerIn)/this.currentSpeed;
    	}
    	setGrade();
    	
    	// Step 2: Calculate the slope of the train's current angle (Degrees = Tan-1 (Slope Percent/100))
    	this.slope = Math.atan2(this.grade,100);
    	double angle = Math.toDegrees(this.slope);
    	
    	// Step 3: Calculate the forces acting on the train using the coefficient of friction
    	// and the train's weight in lbs converted to kg divided over the wheels (where the force is technically
    	// being applied times gravity (G)
    	this.normalForce = (trainMass/this.trainWheels) * G * Math.sin((angle*Math.PI)/180);	// divide by 12 for the number of wheels
    	this.downwardForce = (trainMass/this.trainWheels) * G * Math.cos((angle*Math.PI)/180);	// divide by 12 for the number of wheels

    	// compute friction force
    	this.friction = (FRICTION_COEFFICIENT * this.downwardForce) + this.normalForce;
    	
    	// sum of the forces
    	this.totalForce = this.force - this.friction;
    	    	
    	this.force = this.totalForce;
    	
    	// Step 4: Calculate acceleration using the F = ma equation, where m = the mass of the body moving
    	this.trainAcceleration = this.force/trainMass;
    	
    	// and have to check to make sure this acceleration does not exceed our max.
    	if (this.trainAcceleration >= TRAIN_MAX_ACCELERATION * 1) {	// time elapsed (one second)
    		// set the acceleration as the max acceleration because we cannot exceed that
    		this.trainAcceleration = TRAIN_MAX_ACCELERATION * 1;	// time elapsed (one second)
    	}
    	
		emerBrake = this.getEBrake();
    	// decelerates the train based on the values given in the spec sheet for the emergency brake
    	if (emerBrake) {
    		this.trainAcceleration += (TRAIN_MAX_ACCELERATION_E_BRAKE*1);
    	}
    	
    	// decelerates the train based onthe values given in the spec sheet for the service brake
    	if(serviceBrake) {
    		this.trainAcceleration += (TRAIN_MAX_ACCELERATION_SERVICE_BRAKE*1);
    	}
    	
    	// Step 5: Calculate the final speed by adding the old speed with the acceleration x the time elapsed (one second)
    	this.finalSpeed = this.currentSpeed + (this.trainAcceleration * 1);
    	
    	// NO NEGATIVE SPEEDS YINZ
    	if(this.finalSpeed < 0) {
            this.finalSpeed = 0;
        }
    	
    	// resetting the current speed based on our calculations
    	this.currentSpeed =  this.finalSpeed;
    	this.distTravelled = this.currentSpeed * 1; // speed times the time between clock ticks = distance travelled
    	//System.out.println(finalSpeed);
    	
    	if(!(currentBlock == this.position.getCurrentBlock())) {
    		metersIn = 0;
    	} else {
    		metersIn += this.distTravelled;
    	}
    	this.position.moveTrain(this.distTravelled); // method call to tell the position class how far to move the train
    	
    }
    
    /**
     * This method truncates any floating point numbers passed to it to the number of decimal points specified
     * by passing in an integer. I found this to be the most intuitive solution for trimming numbers for me personally
     * @param unroundedNumber
     * @param decimalPlaces
     * @return
     */
    static double truncateTo(double unroundedNumber, int decimalPlaces){
        int truncatedNumberInt = (int)( unroundedNumber * Math.pow( 10, decimalPlaces ) );
        double truncatedNumber = (double)( truncatedNumberInt / Math.pow( 10, decimalPlaces ) );
        return truncatedNumber;
    }
    
    /**
     * Returns the train ID of the current train object
     * @return
     */
    public String getTrainID() {
    	return this.trainID;
    }
    
	/**
	 * Sets the status of an engine failure
	 */
    public void engineFailureStatus() {
    	this.engineFailureActive = this.trainModelGUI.engineFailStatus();
    }
    
	/**
	 * Sets the status of an signal failure
	 */
    public void signalFailureStatus() {
    	this.signalFailureActive = this.trainModelGUI.signalFailStatus();
    	if(this.signalFailureActive) {
        	this.setGPSAntenna(true);
        	this.setMBOAntenna(true);
    	} else {
    		this.setGPSAntenna(false);
        	this.setMBOAntenna(false);
    	}

    	
    }
    
	/**
	 * Sets the status of an brake failure
	 */
    public void brakeFailureStatus() {
    	this.brakeFailureActive = this.trainModelGUI.brakeFailStatus();
    }
    
    /**
     * Sets the arrival status for display
     * 
     * @param status
     */
    public void setArrivalStatus(int status) {
    	this.arrivalStatus = status;
    }
    
    /**
     * Sets the station name for display
     * 
     * @param station
     */
    public void setStation(String station) {
    	this.station = station;
    }
    
    /**
     * sets the current value of the beacon
     * 
     * @param beaconVal
     */
    public void setBeacon(int beaconVal) {
    	this.beacon = beaconVal;
    }
    
    /**
     * Returns the current beacon value
     * 
     * @return
     */
    public int getBeacon() {
    	return this.beacon;
    }
    
    /**
     * Returns the current x and y coordinates of this train in a double array
     */
    public double[] getCoordinates() {
    	double []coords = this.position.getCoordinates();
    	this.currentX = coords[0];
    	this.currentY = coords[1];
    	return coords;
    }
    
    /**
     * Returns the position of the train
     * 
     * @return position
     */
    public Position getPosition(){
        return this.position;
    }
    
    /**
     * sets the current position of the train
     * 
     * @param pos
     */
    public void setPosition(Position pos) {
    	this.position = pos;
    	//setGrade();
    }
    
    /**
     * Returns the current block id as an integer
     * 
     * @return
     */
    public int getBlock(){
        return this.position.getCurrentBlock();
    }

    /**
     * Startings of a method to compute the checksum of the signal sent to the MBO with the
     * Train's current position as X and Y coordinates
     */
    public long checkSum() {
    	CRC32 crc = new CRC32();
    	crc.reset(); // in order to reuse the object for all signals
    	String signal = this.trainID + ":" + Double.toString(this.trainWeight) + ":" + Double.toString(this.currentX) + "," + 
    			Double.toString(this.currentY);
    	crc.update(signal.getBytes()); // signal is a String containing your data
    	long checksum = crc.getValue();
    	return checksum;
    }
    
    /**
     * Set the current x and y positions of the current train
     * 
     * @param pos
     */
    public void setCoordinates(double[] pos) {
    	this.currentX = pos[0];
    	this.currentY = pos[1];
    }
    
    /**
     * Returns the line color of this train
     * 
     * @return
     */
    public String getLine() {
    	return this.lineColor;
    }
    
    /**
     * Sets the grade of the current block/position of the train
     * 
     * @param g
     */
    public void setGrade() {
    	this.grade = trkMdl.getBlock(this.lineColor,this.currentBlock).getGrade();
    	//int currentBlockID = position.getCurrentBlock();
    	//Block current = trackModel.getBlock(curentBlockID);
    	//grade = currentBlock.getGrade();
    }
    
    /**
     * Returns the set point speed for this specific train object
     * 
     * @return
     */
    public double getSetpoint() {
    	return this.CTCSpeed;
    }
    
    /**
     * Sets the setpoint speed of a specific train when this method is called in the train model after
     * the parent method is called by the track model
     */
    public void setSetpoint(double setpoint) {
    	this.CTCSpeed = setpoint;
    }
    
    /**
     * Called by the train model class when it's parent method is called by the train controller to set a 
     * power input command based on the setpoint speed
     */
    public void setPower(double pow) {
    	this.powerIn = pow;
    }
    
    /**
     * Returns the train's current velocity
     * 
     * @return
     */
    public double getVelocity() {
    	return this.currentSpeed;
    }
    
    /**
     * returns the train's current authority
     * 
     * @return
     */
    public double getAuthority() {
    	return this.CTCAuthority;
    }
    
    /**
     * Sets the train's authority
     * 
     * @param authority
     */
    public void setAuthority(double authority) {
    	this.CTCAuthority = authority;
    }
    
    /**
     * Sets the status of the emergency brake by the train controller or the passengers
     * 
     * @param ebrake
     */
    public void setEBrake(boolean ebrake) {
		this.engineFailureStatus();
		this.brakeFailureStatus();
		this.signalFailureStatus();
        if(!this.brakeFailureActive && !this.signalFailureActive && !this.engineFailureActive) {
            this.emerBrake = ebrake;
        } else if(this.brakeFailureActive || this.signalFailureActive || this.engineFailureActive){
			this.emerBrake = true;
		}
    }
    
    /**
     * Sets the status of the service brake as commanded by the train controller
     * 
     * @param sBrake
     */
    public void setServiceBrake(boolean sBrake) {
		this.serviceBrake = sBrake;
		this.brakeFailureStatus();
        if(!this.brakeFailureActive){
            this.serviceBrake = sBrake;
        } else {
			this.serviceBrake = false;
		}
    }
    
    /**
     * Returns the emergency brake status - for the train controller to know if the passengers
     * activated it or not
     * 
     * @return
     */
    public boolean getEBrake() {
    	return this.emerBrake;
    }
    
    /**
     * Sets the status of the right doors of the train
     * 
     * @param right
     */
    public void setRightDoors(boolean right) {
    	this.rightDoorIsOpen = right;
    	if(this.rightDoorIsOpen) {
    		setNumDeparting();
    	}
    }
    
    /**
     * Sets the status of the left doors in the train
     * 
     * @param left
     */
    public void setLeftDoors(boolean left) {
    	this.leftDoorIsOpen = left;
    	if(this.leftDoorIsOpen) {
    		setNumDeparting();
    	}
    }
    
    /**
     * Sets the light status of the current train
     * 
     * @param lights
     */
    public void setLights(boolean lights) {
    	this.lightsAreOn = lights;
    }
    
    /**
     * Sets the cabin temperature of the current train
     * 
     * @param temp
     */
    public void setTemp(int temp) {
    	this.temperature = temp;
    }
    
    public void setNumEmbarking(int num) {

        // Limit the number of embarking passengers so that the 
        // total after boarding does not exceed the capacity
        // for two cars.
        if (num > (2*TRAIN_CAPACITY) - this.numPassengers){
            num = (2*TRAIN_CAPACITY) - this.numPassengers;
        }
        this.numPassengers += num;
        trkMdl.passengersBoarded(trainID, num);
        trkMdl.sendTicketSalesToCtc(num);
        embarkingPassengersSet = true;
    }

    // Return whether the train is stopped
    public boolean isStopped(){
        return (currentSpeed == 0);
    }
    
    /**
     * Sets the number of passengers exiting the train using a random number generator
     * This method should only ever be called when train is STOPPED at a station
     * 
     * @param numPassengers
     * @return
     */
    public void setNumDeparting() {
        if (isStopped() && !embarkingPassengersSet){
        	Random rand = new Random();
        	int  n = rand.nextInt(this.numPassengers+1);
        	if (this.numPassengers - n <= 0) {
        		this.numPassengers = 0;
                n = this.numPassengers;
        	} else {
                this.numPassengers = this.numPassengers - n;
            }
            trkMdl.passengersUnboarded(trainID, n);
        }
    	//return 0;
    }
    
    /**
     * Computes the current weight of the train
     */
    private void setWeight() {
    	this.trainWeight = (this.trainCars*TRAIN_WEIGHT) + (this.crew + this.numPassengers) * AVE_PASSENGER_WEIGHT;
    }
    
    /**
     * Computes the current weight of the train
     */
    public double getWeight() {
    	return this.trainWeight;
    }
    
    /**
     * Sets the current speed limit based on the current position of the train
     * 
     * @param speedLim
     */
    public void setSpeedLimit(int speedLim) {
    	this.currentSpeedLimit = trkMdl.getBlock(this.lineColor,this.currentBlock).getSpeedLimit();
    }
    
    /**
     * Sets the status of the MBO antenna
     * 
     * @param status
     */
    public void setMBOAntenna(boolean status) {
    	this.MBOAntenna = status;
    }
    
    /**
     * Sets the status of the trains GPS
     * 
     * @param status
     */
    public void setGPSAntenna(boolean status) {
    	this.GPSAntenna = status;
    }
    
    /**
     * Exits all active train model GUIs
     * @param yes
     */
    public void setExitAllGuis(boolean yes) {
    	if(yes)
    		trnMdl.exitAll();
    }
}
