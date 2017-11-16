package Modules.TrainModel;

import Shared.Module;

import Shared.SimTime;

import java.util.HashMap;

import Modules.TrackModel.*;

/**
 * Class for the overarching class of the TrainModel (which acts as the interface of the train model between other
 * objects within the train system)
 * The primary data structure implemented as the backing structure is a hash map of train IDs to train objects
 * 
 * @author Jennifer
 *
 */
public class TrainModel implements Module{
	public TrackModel trackModel;
	public static HashMap<Integer, Train> trainList;
	public SimTime currentTime = new SimTime("00:00:00");
	double powSum = 0.0;
	private String line = "GREEN";
	//Instantiate a GUI for this train

	/**
	 * Constructor of the Train Model class
	 * This calss is backed by a HashMap that stores trains as objects with keys as the
	 * built in java hash of the Train ID strings
	 */
	public TrainModel(){
		trainList = new HashMap<Integer, Train>();
		//Train train = new Train(line, "Train 1", this);
		dispatchTrain("Train 1", line);
		//instantiateGUI(train);
        this.getTrain("Train 1").showTrainGUI();
        dispatchTrain("Train 2", line);
        dispatchTrain("Train 3", line);
        dispatchTrain("Train 4", line);
        dispatchTrain("Train 5", line);
	}

	/**
	 * updateTime is the shared clock between all submodules
	 */
	@Override
	public boolean updateTime(SimTime time) {
		currentTime = time;
		//setPower("Train 1", pow+10);
		powSum += 10;
		setPower("Train 1", powSum);
		setPower("Train 2", powSum-20);
		setPower("Train 3", powSum*2);
		setPower("Train 4", powSum+20);
		setPower("Train 5", powSum+50);
		for(Train t : trainList.values()) {
			t.updateVelocity();
	        t.setValuesForDisplay();
		}
        
		return true;
	}
	
	/**
	 * Dispatch a train to a specific block (will add that functionality later)
	 * @param trainID
	 * @param train
	 */
	public void dispatchTrain(String trainID, String line) {
		Train newTrain = new Train(line, trainID, this);
		trainList.put(trainID.hashCode(), newTrain);
		instantiateGUI(newTrain);
	}
	
	/**
	 * Instantiates a specified train Gui when passed a train object
	 * This was, when every train is dispatched, its GUi is already available to be displayed if the user
	 * selects a train from the list.
	 * 
	 * @param train
	 */
	private void instantiateGUI(Train train) {
		TrainModelGUI trainModelGUI = train.CreateNewGUI();
		for(Train t : trainList.values()) {
			// adds all the active trains to the new train's GUI
			if (train == t) {
				continue;
			} else {
				trainModelGUI.addTraintoGUIList(t);
			}
			// adds this new train to all the other train's GUI lists
			if(trainList.size() > 1) {
				TrainModelGUI otherGUI = t.getTrainGUI();
				otherGUI.addTraintoGUIList(train);	
			}
		}
		//trainModelGUI.addTraintoGUIList(train);
		//return trainModelGUI;
	}
	
	public Train getTrain(String ID) {
		return trainList.get(ID.hashCode());
	}
	
	/*private int calcDeltaTime(SimTime start, SimTime end) {
		return start-end; // TODO: Ask Kevin how we would be doing this
	}*/
	
	public Train getTrainAtBlock(Block block) {
		// TODO: Should I iterate through every single entry in the hashmap to find the
		// train at the specified block?
		for(Train t : trainList.values()) {
			if(t.getBlock() == block) {
				return t;
			}
		}
		return null;
	}
	
	public void setBeacon(String trainID, int beaconVal) {
		
	}
	
	public double transmitCtcAuthority(String trainID, double authority) {
		return authority;
	}
	
	public double suggestSetpointSpeed(String trainID, double setpoint) {
		return setpoint;
	}
	
	/**
	 * Set the Emergency Brake status based on the train controller, so he will
	 * call this setter if he sets the emergency brake. Then the passenger button
	 * on the train itself will not be "pushable".
	 * 
	 * @param trainID
	 * @param eBrake
	 */
	public void setDriverEmergencyBrake(String trainID, boolean eBrake) {
		
	}
	
	/**
	 * Set the Emergency Brake status based on the passengers. In this case, the driver
	 * will be allowed to override a request for the emergency brake, but only once the 
	 * train has come to a complete stop can he deactivate the e brake
	 * @param trainID
	 * @param eBrake
	 */
	public void setPassengerEmergencyBrake(String trainID, boolean eBrake) {
		
	}
	
	/**
	 * Set by the driver only
	 * 
	 * @param trainID
	 * @param sBrake
	 */
	public void setServiceBrake(String trainID, boolean sBrake) {
		
	}
	
	/*public String[] getPosition(String trainID) {
		return ;
	}*/
	
	public void setPosition(String trainID, double[] coordinates) {
		
	}
	
	public void setPower(String trainID, double powerCommand) {
		this.getTrain(trainID).setPower(powerCommand);
	}
	
	public void setDoors(String trainID, boolean left, boolean right) {
		
	}
	
	public void setLights(String trainID, boolean lights) {
		
	}
	
	public void setTemp(String trainID, int temp) {
		
	}
	
	// TODO: I don't think I need this??
	public void setActualTrainSpeed(String trainID, double actualSpeed) {
		
	}
	
	
}
