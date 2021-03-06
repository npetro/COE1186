//Kevin Gilboy
//This program mimics the workings of a Train System CTC

package Modules.Ctc;

import Simulator.Simulator;
import Shared.Module;
import Shared.SimTime;

import Modules.TrackController.TrackController;
import Modules.TrackModel.TrackModel;
import Modules.TrainModel.TrainModel;
import Modules.TrainController.TrainController;
import Modules.TrackModel.TrackCsvParser;
import Modules.TrackModel.TrackIterator;
import Modules.TrackModel.Block;
import Modules.TrackModel.Switch;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Ctc implements Module,TimeControl {
	public Ctc ctc;
	
	public CtcGui gui;
	public SimTime startTime;
	public SimTime currentTime;
	public int speedup = 1;
	
	public HashMap<String,Train> trains = new LinkedHashMap <>();
	public ArrayList<Train> trainsToRemove = new ArrayList<Train>();
	public HashMap<String,Schedule> schedules = new LinkedHashMap <>();
	public Queue<Schedule> scheduleQueueToDispatch = new LinkedList<>();

	public TrackCsvParser trackParser = new TrackCsvParser();
	
	public int runningTicketSales;
	public double throughput;

	public Simulator simulator = null;
	public TrackController[] trackControllers;
	public TrackModel trackModel = null;
	public TrainModel trainModel = null;
	public TrainController trainController = null;
	
	private boolean isMovingBlockMode = false;
	
	private String[] bidirectionalReservationGreen = new String[] {"","-1","-1"};
	private String[] bidirectionalReservationRed = new String[] {"","-1","-1"};
	
	boolean lockReservation = false;
	int testTrainNum = 0;

	/**
	 * Called by the SimulatorGUI class to show the GUI when this module is selected
	 */
	public void showGUI(){
		gui.showGUI();
	}
	
	public Ctc() {
		ctc = this;

		initializeBlocks();
		startGui();
		while(gui==null) {
			
		}
		return;
	}
	
	/*
	 * ------------------------------
	 *  GUI
	 * ------------------------------
	 */
	public void startGui(){
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					try {
						gui = new CtcGui(ctc);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				//... silence awt exceptions
			}
	    });
	}
	
	/*
	 * ------------------------------
	 *  BLOCKS
	 * ------------------------------
	 */
	
	/**
	 * Load in blocks from csv
	 */
	public void initializeBlocks() {
		for(Line line : Line.values()) {
			ArrayList<Block> blocks = new TrackCsvParser().parse("Modules/Ctc/"+line.toString()+"LineFinal.csv");
			line.blocksAL = blocks;
			line.blocks = blocks.toArray(new Block[blocks.size()]);
		}
	}
	
	/**
	 * 
	 */
	protected void enableMovingBlockMode(Boolean isMovingBlockMode) {
		this.isMovingBlockMode = isMovingBlockMode;
		for(TrackController wayside : trackControllers) {
			wayside.isMovingBlockMode = isMovingBlockMode;
		}
		simulator.transmitEnableMovingBlockMode(isMovingBlockMode);
	}
	
	/**
	 * Gets the next block ID given a current and previous block
	 */
	public static int getNextBlockId(Line line, int currBlock, int prevBlock) {
		TrackIterator ti = new TrackIterator(line.blocksAL, currBlock, prevBlock);
		return ti.nextBlock();
	}
	
	/**
	 * Gets the current wayside of a block
	 */
	public TrackController getWaysideOfBlock(Line line, int blockNum) {
		if(line==Line.GREEN) {
			if( (0 <= blockNum && blockNum < 53) || (127 <= blockNum && blockNum < 150) ) {
				return trackControllers[0];
			}
			else if( (53 <= blockNum && blockNum < 127) || (150 <= blockNum && blockNum < 152) ){
				return trackControllers[1];
			}
		}
		else if(line==Line.RED) {
			if( (0 <= blockNum && blockNum < 33) || (76 == blockNum) ) {
				return trackControllers[2];
			}
			else if(33 <= blockNum && blockNum < 76){
				return trackControllers[3];	
			}
		}
		
		return null;
	}
	
	/**
	 * Updates the local copy of a block from the Wayside
	 */
	protected void updateLocalBlockFromWayside(Line line, int blockNum){
		Boolean trackModelOccupied = getTrackCircuit(line, blockNum);
		Boolean broken = false;
		Boolean occupied = false;
		if(trackModelOccupied) {
			occupied = true;
			broken = true; //assume broken until proven not broken
			for(Train train : trains.values()) {
				//If it is a train on the block, it is occupied and not broken
				if(train.line == line && train.currLocation == blockNum) {
					broken = false;
				}
			}
			
		}
		line.blocks[blockNum].setRailStatus(!broken);
		line.blocks[blockNum].setOccupancy(occupied);
		
		//Get switch state via wayside
		Boolean switchState = getSwitchState(line,blockNum);
		if(switchState!=null) {
			line.blocks[blockNum].getSwitch().setState(switchState);
		}
	}
	
	/**
	 * Checks the TrackModel block occupancy via the TrackController
	 */
	public Boolean getTrackCircuit(Line line, int blockNum) {
		TrackController wayside = getWaysideOfBlock(line, blockNum);
		Block block = wayside.receiveBlockInfoForCtc(blockNum);
		Boolean occupied = block.getOccupied();
		return occupied;
	}
	
	/**
	 * Checks the TrackModel switch state via the TrackController
	 */
	public Boolean getSwitchState(Line line, int blockNum) {
		TrackController wayside = getWaysideOfBlock(line, blockNum);
		Block block = wayside.receiveBlockInfoForCtc(blockNum);
		Switch sw = block.getSwitch();
		if(sw!=null) {
			return(sw.getState());
		}
		return null;
	}
	
	/**
	 * Sets the TrackModel block to repaired via the TrackController
	 */
	protected void repairBlock(Line line, int blockNum) {
		TrackController wayside = getWaysideOfBlock(line, blockNum);
		wayside.transmitBlockMaintenance(blockNum, false);
		//Block block = wayside.receiveBlockInfoForCtc(line.toString(), blockNum);
		//block.setMaintenance(false);
	}

	/**
	 * Sets the TrackModel block to maintenance mode via the TrackController
	 */
	protected void setBlockMaintenance(Line line, int blockNum) {
		TrackController wayside = getWaysideOfBlock(line, blockNum);
		wayside.transmitBlockMaintenance(blockNum, true);
		//Block block = wayside.receiveBlockInfoForCtc(line.toString(), blockNum);
		//block.setMaintenance(true);
	}
	
	/**
	 * Sets the TrackModel block to maintenance mode via the TrackController
	 */
	protected boolean setSwitchState(Line line, int blockNum, Boolean state) {
		TrackController wayside = getWaysideOfBlock(line, blockNum);
		boolean success = wayside.transmitCtcSwitchState(blockNum, state);
		return success;
	}
	
	public void launchWaysideGui(int i) {
		trackControllers[i].tcgui.showTrackControllerGUI();
	}
	
	public Train getEarliestTrainAtBlock(Line line, int blockNum) {
		Train earliestTrain = null;
		for(Train train : trains.values()) {
			if(train.currLocation==blockNum) {
				if(earliestTrain==null) {
					earliestTrain = train;
					continue;
				}
				
				if(SimTime.hoursBetween(earliestTrain.arrivalAtCurrLocation, train.arrivalAtCurrLocation)<0)
					earliestTrain = train;
			}
		}
		return earliestTrain;
	}
	
	
	/*
	 * ------------------------------
	 *  SCHEDULES
	 * ------------------------------
	 */
	protected Schedule getScheduleByName(String name){
		Schedule schedule = schedules.get(name);
		return schedule;
	}
	
	protected void addSchedule(String name, Schedule schedule) {
		schedules.put(name, schedule);
	}
	
	protected Schedule removeScheduleByName(String name) {
		Schedule removedSchedule = schedules.remove(name);
		return removedSchedule;
	}
	
	/*
	 * ------------------------------
	 *  TRAINS
	 * ------------------------------
	 */
	/**
	 * Creates a sample train to dispatch
	 */
	public void testDispatch(Line line) {
		if(!simulator.simulationRunning) {
			play();
			gui.btnPlay.setEnabled(false);
			gui.stylizeButton_Disabled(gui.btnPlay);
			gui.btnPause.setEnabled(true);
			gui.stylizeButton(gui.btnPause);		
		}
		String testName = "Train"+testTrainNum++;
		if(line==Line.GREEN) {
			Schedule schedule = new Schedule(Line.GREEN);
			schedule.departureTime = new SimTime("11:11:11");
			schedule.name = testName;
			schedule.addStop(0, 104, new SimTime("00:00:30"));
			schedule.addStop(1, 113, new SimTime("00:02:00"));
			schedule.addStop(2, 1, new SimTime("00:02:00"));
			addSchedule(testName,schedule);
			dispatchTrain(testName);
		}
		else if(line==Line.RED) {
			Schedule schedule = new Schedule(Line.RED);
			schedule.departureTime = new SimTime("11:11:11");
			schedule.name = testName;
			schedule.addStop(0, 44, new SimTime("00:02:00"));
			schedule.addStop(1, 59, new SimTime("00:02:00"));
			schedule.addStop(2, 74, new SimTime("00:00:30"));
			addSchedule(testName,schedule);
			dispatchTrain(testName);
		}
	}
	
	/**
	 * Returns a train given a name
	 */
	protected Train getTrainByName(String name){
		Train train = trains.get(name);
		return train;
	}
	
	/**
	 * Dispatch a train and call respective methods in TrainModel and TrainController
	 */
	public void dispatchTrain(String name) {
		Schedule schedule = removeScheduleByName(name);
		
		//If the first block is not occupied, dispatch
		//Else add it to a queue to be subsequently dispatched
		if(checkDispatchability(schedule)) {
			dispatchTrain(schedule);
		}
		else {
			scheduleQueueToDispatch.add(schedule);
		}
	}
	private void dispatchTrain(Schedule schedule) {
		Train train = new Train(schedule);
		schedule.train = train;
		trains.put(schedule.name, train);
		//train.line.blocks[train.line.yardOut].setOccupancy(true);
		
		trainModel.dispatchTrain(schedule.name, train.line.toString().toUpperCase());
		trainController.dispatchTrain(schedule.name, train.line.toString().toUpperCase()); 
	}
	
	/**
	 * Check if the yardOut is occupied
	 */
	protected boolean checkDispatchability(Schedule schedule) {
		boolean yardOutOccupied = false;
		if(schedule.line.blocks[schedule.line.yardOut].getOccupied()) {
			yardOutOccupied = true;
		}
		else{
			for(Train train : trains.values()) {
				if(train.line == schedule.line && train.currLocation==train.line.yardOut) {
					yardOutOccupied = true;
					break;
				}
			}
		}
		TrackController wayside = getWaysideOfBlock(schedule.line, schedule.line.yardOut);
		int pb = -1;
		int cb = schedule.line.yardOut;
		int nb = getNextBlockId(schedule.line,cb,pb);
		int nnb = getNextBlockId(schedule.line,nb,cb);
		
		if(wayside.tcplc.canProceedPath(new int[] {pb,cb,nb,nnb}) && !yardOutOccupied) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * TrainModel uses this method to add passengers to the train
	 */
	public void addPassengers(String trainName, int number) {
		Train train = getTrainByName(trainName);
		train.passengers += number;
	}
	
	/**
	 * TrainModel uses this method to remove passengers from a train
	 */
	public void removePassengers(String trainName, int number) {
		Train train = getTrainByName(trainName);
		train.passengers -= number;
	}
	
	/**
	 * Calculate authority via a BFS
	 */
	public ArrayList<Integer> calculateAuthorityPath(Train train) {
		Queue<ArrayList<Integer>> q = new LinkedList<ArrayList<Integer>>();
		ArrayList<Integer> path = new ArrayList<Integer>();
		int currBlockId = train.currLocation;
		int selfLocation = currBlockId;
		int prevBlockId = train.prevLocation;
		int stopBlockId = train.schedule.getNextStop();
		
		String[] bidirectionalReservation = train.line==Line.GREEN?bidirectionalReservationGreen:bidirectionalReservationRed;
		
		//If no stops, send blank authority
		if(stopBlockId == -1) {
			return path;
		}
		//System.out.println("    ");

		q.add(new ArrayList<Integer>(Arrays.asList(prevBlockId,currBlockId)));
		
		while(!q.isEmpty()) {
			//-------------------
			// Pop next path from queue
			//-------------------
			path = q.remove();
			currBlockId = path.get(path.size()-1);
			prevBlockId = path.get(path.size()-2);

			if(currBlockId>train.line.yardOut ||currBlockId<0) continue;
			
			//-------------------
			// If approaching the yard, ditch the path
			//-------------------
			if(prevBlockId==train.line.yardIn && currBlockId==-1) {
				continue;
			}
			
			if(path.size()>=4 && !bidirectionalReservation[0].equals(train.name) && 
					(Integer.parseInt(bidirectionalReservation[1])==currBlockId || Integer.parseInt(bidirectionalReservation[2])==currBlockId)){
				int locationStart = path.indexOf(Integer.parseInt(bidirectionalReservation[1]));
				int locationEnd = path.indexOf(Integer.parseInt(bidirectionalReservation[2]));
				
				if(locationStart==0) {
					locationStart = 1+path.subList(1,path.size()).indexOf(Integer.parseInt(bidirectionalReservation[1]));
				}
				if(locationEnd==0) {
					List<Integer> sub = path.subList(1,path.size());
					locationEnd = 1+sub.indexOf(Integer.parseInt(bidirectionalReservation[2]));
				}
				if(locationStart==-1 && locationEnd!=-1) {
					path = new ArrayList<Integer>(path.subList(0, locationEnd-2));
				}
				else if(locationEnd==-1 && locationStart!=-1) {
					path = new ArrayList<Integer>(path.subList(0, locationStart-2));
				}
				else if(locationStart<locationEnd){
					int modifier = locationStart>=2?2:locationStart;
					path = new ArrayList<Integer>(path.subList(0, locationStart-modifier));
				}
				else if(locationEnd<=locationStart) {
					int modifier = locationEnd>=2?2:locationEnd;
					path = new ArrayList<Integer>(path.subList(0, locationEnd-modifier));
				}
				continue;
			}
			
			//-------------------
			// Fixed block: If block is occupied, ditch the path
			// Moving block: If block is broken, ditch the path
			//-------------------
			if(!isMovingBlockMode && path.get(0)!=-1 && train.line.blocks[currBlockId].getOccupied() && currBlockId != selfLocation) {
				path.remove(path.size()-1);
				continue;
			}
			else if(isMovingBlockMode && !train.line.blocks[currBlockId].getStatus()) {
				path.remove(path.size()-1);
				continue;
			}
			
			//-------------------
			// If at the stop, path is the optimal route so exit the while
			//-------------------
			if(currBlockId == stopBlockId) {
				break;
			}
			
			//-------------------
			// Fixed block mode: If block is on bidirectional track which is occupied, ditch the path
			//-------------------
			int nbId = getNextBlockId(train.line, currBlockId, prevBlockId);
			if((nbId<=train.line.yardIn && nbId>=0) && train.line.blocks[nbId].getDirection()==0) {
				if(bidirectionalStretchOccupied(train.line,nbId,currBlockId,selfLocation)) {
					path.remove(path.size()-1);
					continue;
				}
			}
			
			//-------------------
			// Otherwise add adj blocks to the queue
			//-------------------
			Switch swCurr= train.line.blocks[currBlockId].getSwitch();
			Switch swPrev;
			if(prevBlockId==-1) {
				swPrev = null;
			}
			else{
				swPrev = train.line.blocks[prevBlockId].getSwitch();
			}
			//Entering a switch
			if(swCurr!=null && swPrev==null){
				// CASE: Entering a head from a non-switch, pursue both ports
				if(swCurr.getEdge()==Switch.EDGE_TYPE_HEAD && swPrev==null) {
					int normId = swCurr.getPortNormal();
					int altId = swCurr.getPortAlternate();
					
					//Follow both paths if valid
					if(train.line.blocks[normId].getDirection() == train.line.blocks[altId].getDirection()) {
						int indexToFollow = (currBlockId+1==normId) ? normId : altId;
						ArrayList<Integer> normPath = cloneAndAppendAL(path,indexToFollow);
						q.add(normPath);
						if(altId==train.line.yardIn || train.line.blocks[normId].getDirection() == 0) {
							ArrayList<Integer> altPath = cloneAndAppendAL(path,altId);
							q.add(altPath);
						}
					}
					else {
						if(train.line.blocks[normId].getDirection()>=train.line.blocks[currBlockId].getDirection()) {
							ArrayList<Integer> newPath = cloneAndAppendAL(path,normId);
							q.add(newPath);
						}
						if(train.line.blocks[altId].getDirection()>=train.line.blocks[currBlockId].getDirection()) {
							ArrayList<Integer> newPath = cloneAndAppendAL(path,altId);
							q.add(newPath);
						}
					}
				}
				// CASE: Entering a tail from a non-switch, pursue the normal port 
				else if(swCurr.getEdge()==Switch.EDGE_TYPE_TAIL && swPrev==null) {
					int nextBlockId = swCurr.getPortNormal();
					ArrayList<Integer> newPath = cloneAndAppendAL(path,nextBlockId);
					q.add(newPath);
				}
			}
			// CASE : Not a switch or about to leave a switch so just use a vanilla TrackIterator to pursue the next block
			else {
				int nextBlockId = getNextBlockId(train.line, currBlockId, prevBlockId);
				ArrayList<Integer> newPath = cloneAndAppendAL(path,nextBlockId);
				q.add(newPath);
			}
		} //while q not empty
		
		//If about to enter bidirectional and there is no existing reservation...
		//Else it is not on bidirectional but it has a reservation so retract the reservation
		if(path.size()>=4 && train.line.blocks[path.get(3)].getDirection()==0 && bidirectionalReservation[0].equals("")) {
			//In moving block mode, we must account for two trains on a block
			//Here we check to see the earliest train that arrived at the block
			//The earliest train is granted the reservation
			boolean yieldToOtherTrainsAtLocation = false;
			for(Train otherTrain : trains.values()) {
				if(otherTrain==train) continue;
				if(otherTrain.currLocation==train.currLocation && SimTime.hoursBetween(train.arrivalAtCurrLocation,otherTrain.arrivalAtCurrLocation)<0) {
					yieldToOtherTrainsAtLocation = true;
				}
				
				int nextBlock = Ctc.getNextBlockId(train.line, train.currLocation, train.prevLocation);
				if(otherTrain.currLocation==nextBlock)
					yieldToOtherTrainsAtLocation = true;
			}		
			
			//If the train is the earliest to the block, grant it the reservation
			if(!yieldToOtherTrainsAtLocation) {
				//Make a reservation
				int prevIndex = 2;
				int currIndex = 3;
				int startBlock = path.get(currIndex);
				int nb = Ctc.getNextBlockId(train.line, startBlock, path.get(prevIndex));
				int endBlock;
				do {
					endBlock = nb;
					try {
						nb = Ctc.getNextBlockId(train.line, nb, path.get(prevIndex++));
					}
					catch(IndexOutOfBoundsException e) {
						break;
					}
					if(nb==-1) {
						endBlock = train.line.yardIn;
						break;
					}
				}while(train.line.blocks[nb].getDirection()==0);
				
				//Alert the wayside that the block is reserved to change light state
				TrackController waysideStart = getWaysideOfBlock(train.line, startBlock);
				waysideStart.transmitCtcReservation(startBlock, true);
				TrackController waysideEnd = getWaysideOfBlock(train.line, startBlock);
				waysideEnd.transmitCtcReservation(endBlock, true);
				
				if(train.line==Line.GREEN) {
					bidirectionalReservationGreen = new String[] {train.name,Integer.toString(startBlock),Integer.toString(endBlock)};
				}
				else if(train.line==Line.RED) {
					bidirectionalReservationRed = new String[] {train.name,Integer.toString(startBlock),Integer.toString(endBlock)};
				}
			}
		}
		else if(bidirectionalReservation[0].equals(train.name) && path.size()>=4 && train.line.blocks[path.get(3)].getDirection()!=0){
			//Retract a reservation
			int startBlock = Integer.parseInt(bidirectionalReservation[1]);
			int endBlock = Integer.parseInt(bidirectionalReservation[2]);
			
			//Alert the wayside that the block is no longer reserved to change light state
			TrackController waysideStart = getWaysideOfBlock(train.line, startBlock);
			waysideStart.transmitCtcReservation(startBlock, false);
			TrackController waysideEnd = getWaysideOfBlock(train.line, endBlock);
			waysideEnd.transmitCtcReservation(endBlock, false);
			
			if(train.line==Line.GREEN) {
				bidirectionalReservationGreen = new String[] {"","-1","-1"};
			}
			else if(train.line==Line.RED) {
				bidirectionalReservationRed = new String[] {"","-1","-1"};
			}
		}
		
		//Remove the first only if not coming from the yard
		if(path.size()>0 && path.get(0)!=-1) {
			path.remove(0);
		}

		//Calculate the distance of the authority
		double dist = 0;
		for(int blockId : path) {
			if(blockId>=0) {
				dist += train.line.blocks[blockId].getLength();
			}
		}
		train.authority = dist; //km
		
		//-------------------
		// Return the found path as the authority
		//-------------------
		/*for(int j=1; j<path.size(); j++) {
			int i = path.get(j);
			System.out.print(train.line.blocks[i].getSection()+Integer.toString(i+1)+", ");
		}
		System.out.println("");*/
		return path;
	}
	
	/**
	 * Calculate speed for a train taking into account
	 * the mode that it is in
	 */
	public void calculateSuggestedSpeed(Train train) {
		//Send 0 if dwelling
		//Else send manual if in manual
		//Else send speed limit
		if(train.dwelling) {
			train.suggestedSpeed = 0;
		}
		else if(train.manualSpeedMode && train.manualSpeed>=0) {
			train.suggestedSpeed = train.manualSpeed;
		}
		else {
			train.suggestedSpeed = train.line.blocks[train.currLocation].getSpeedLimit();
		}
	}
	
	/**
	 * Check a bidirectional stretch of blocks to see if it is occupied
	 */
	private boolean bidirectionalStretchOccupied(Line line, int currBlockId, int prevBlockId, int selfLocation) {
		Boolean[] visited = new Boolean[line.blocks.length];
		for(int i=0; i<visited.length; i++) {
			visited[i] = false;
		}
		visited[prevBlockId] = true;
		
		do {
			if(visited[currBlockId] == true) break;
			if(currBlockId>line.yardOut || currBlockId<0) break;
			//If block is occupied, treat the bidirectional stretch as occupied
			Boolean currOccupied = getTrackCircuit(line, currBlockId);
			if(currOccupied && currBlockId != selfLocation) {
				return true;
			}
			
			visited[currBlockId] = true;
			
			//Block is not occupied, move to the next block
			int nextBlockId = getNextBlockId(line, currBlockId, prevBlockId);
			prevBlockId = currBlockId;
			currBlockId = nextBlockId;
		} while(currBlockId<=line.yardOut && currBlockId>-0 && line.blocks[currBlockId].getDirection()==0);
		
		//The bidirectional stretch must not be occupied
		return false;
	}
	
	/**
	 * Add element to, and return a new ArrayList
	 */
	public <T> ArrayList<T> cloneAndAppendAL(ArrayList<T> oldAl, T newEl) {
		ArrayList<T> newAl = new ArrayList<T>();
		for(T oldEl : oldAl) {
			newAl.add(oldEl);
		}
		newAl.add(newEl);
		return newAl;
	}
	
	
	/*
	 * ------------------------------
	 *  TICKETS
	 * ------------------------------
	 */
	
	/**
	 * TrackModel uses this method to add ticket sales for throughput
	 */
	public void addTicketSales(int tickets) {
		runningTicketSales+=tickets;
	}
	
	/**
	 * Calculates throughput based on ticket sales
	 */
	protected void calculateThroughput() {
		double timeElapsed = SimTime.hoursBetween(startTime, currentTime);
		throughput = runningTicketSales/timeElapsed;
	}
	
	/*
	 * ------------------------------
	 *  TRANSMITTERS
	 * ------------------------------
	 */
	
	/**
	 * Send suggested speed to wayside
	 */
	protected void transmitSuggestedSpeed(String name, TrackController wayside, double speed, int cb) {
		wayside.transmitSuggestedTrainSetpointSpeed(name,speed, cb);
	}
	
	/**
	 * Send suggested authority to wayside as path of blocks
	 */
	protected void transmitCtcAuthority(String name, TrackController wayside, int[] auth) {
		wayside.transmitCtcAuthority(name,auth);
	}
		
	/*
	 * ------------------------------
	 *  MODULE REQUIREMENTS
	 * ------------------------------
	 */
	
	/**
	 * Called every clock tick
	 */
	@Override
	public boolean updateTime(SimTime time) {
		if(startTime==null) {
			startTime = new SimTime(time);
			gui.rdbtnFixedBlockMode.setEnabled(false);
			gui.rdbtnMovingBlockMode.setEnabled(false);
		}
		currentTime = new SimTime(time);
		
		/*
		 * THROUGHPUT
		 */
		calculateThroughput();
		
		/*
		 * AUTO-DISPATCH
		 */
		ArrayList<String[]> schedulesToIterate = new ArrayList<String[]>();
		for(Schedule schedule : schedules.values()) {
			schedulesToIterate.add(new String[] {schedule.departureTime.toString(), schedule.name});
		}
		for(int i=0; i<schedulesToIterate.size(); i++) {
			String [] currSchedule = schedulesToIterate.get(i);
			if((new SimTime(currSchedule[0])).equals(currentTime)) {
				String name = currSchedule[1];
				dispatchTrain(name);
				gui.autoDispatchFromQueue(name);
			}
		}
		
		//If a train was waiting for yard_out to be unoccupied, check yardout and dispatch if clear
		Schedule scheduleToDispatch = scheduleQueueToDispatch.peek();
		if(scheduleToDispatch!=null && checkDispatchability(scheduleToDispatch)) {
			dispatchTrain(scheduleQueueToDispatch.poll());
		}
		
		/*
		 * UPDATE TRAIN LOCATIONS
		 */
		for(Train train : trains.values()) {
			//Check for dwelled trains that need to move on
			if(train.dwelling && currentTime.equals(train.timeToFinishDwelling)) {
				train.schedule.removeStop(0);
				train.dwelling = false;
				
				if(train.schedule.stops.size()==0) {
					train.schedule.addStop(0, train.schedule.line.yardIn);
				}
				
				//Update selected table
				if(gui.dispatchSelectedTable.schedule!=null && gui.dispatchSelectedTable.schedule.name.equals(train.name)) {
					gui.dispatchSelectedTable.fireScheduleChanged();
				}
			}
			
			//Check if train has moved
			int currentLocation = train.currLocation;
			int nextLocation = getNextBlockId(train.line, train.currLocation, train.prevLocation);
			Boolean currOccupied = getTrackCircuit(train.line, currentLocation);
			Boolean nextOccupied = getTrackCircuit(train.line, nextLocation);
			Train earliestTrain = train;
			if(isMovingBlockMode) {
				int trainsAtCurr = 0;
				for(Train t : trains.values()) {
					if(t.currLocation==currentLocation) trainsAtCurr++;
					//if(t!=train && t.currLocation==nextLocation) nextOccupied=false;
				}
				if(trainsAtCurr>1) currOccupied = false;
				
				
				earliestTrain = getEarliestTrainAtBlock(earliestTrain.line,currentLocation);
			}
			if(!currOccupied && nextOccupied) {				
				//Train has moved on
				earliestTrain.prevLocation = currentLocation;
				earliestTrain.currLocation = nextLocation;
				earliestTrain.arrivalAtCurrLocation = new SimTime(currentTime);
			}
			else {
				Switch currSwitch = earliestTrain.line.blocks[currentLocation].getSwitch();
				if(currSwitch!=null && currSwitch.getEdge()==Switch.EDGE_TYPE_HEAD) {
					int norm = currSwitch.getPortNormal();
					int alt = currSwitch.getPortAlternate();
					Boolean normOccupied = getTrackCircuit(earliestTrain.line,norm);
					Boolean altOccupied = getTrackCircuit(earliestTrain.line,alt);
					if(!currOccupied && normOccupied) {
						//Train has moved on
						earliestTrain.prevLocation = currentLocation;
						earliestTrain.currLocation = norm;
						earliestTrain.arrivalAtCurrLocation = new SimTime(currentTime);
						
					}
					else if(!currOccupied && altOccupied) {
						//Train has moved on
						earliestTrain.prevLocation = currentLocation;
						earliestTrain.currLocation = alt;
						earliestTrain.arrivalAtCurrLocation = new SimTime(currentTime);
						
						//Remove stop if we reach it
						if(earliestTrain.currLocation == earliestTrain.schedule.getNextStop()) {
							earliestTrain.schedule.removeStop(0);
						}
					}
				}
			}
			
			//Make train dwell if at stop
			if(train.currLocation == train.schedule.getNextStop() && train.dwelling==false) {
				train.dwelling = true;
				train.timeToFinishDwelling = currentTime.add(train.schedule.stops.get(0).timeToDwell);
				if(train.currLocation==train.line.yardIn) {
					//Train is in the yard
					trainsToRemove.add(train);
					train.line.blocks[train.line.yardIn].setOccupancy(false);
					if(train.name.equals(bidirectionalReservationRed[0])) {
						bidirectionalReservationRed = new String[] {"","-1","-1"};
					}
					simulator.trainPoofByName(train.line.toString(),train.name);
				}
			}
		}
		while(trainsToRemove.size()>0) {
			Train t = trains.remove(trainsToRemove.remove(0).name);
			if(gui.dispatchSelectedTable.schedule!=null && gui.dispatchSelectedTable.schedule.name.equals(t.name)) {
				gui.dispatchSelectedTable.clear();
			}
			t = null;
		}
		
		/*
		 * CHECK BLOCKS FOR FAILURES AND SWITCH STATES
		 */
		for(Line line : Line.values()) {
			//Iterate through each block
			for(int location=0;location<line.yardOut;location++) {
				//Get block occupancy via wayside				
				updateLocalBlockFromWayside(line,location);
			}
		}
		
		/*
		 * SPPED & AUTHORITY
		 */
		for(Train train : trains.values()) {			
			//Get Wayside of current location
			TrackController wayside = getWaysideOfBlock(train.line,train.currLocation);
			
			/*
			 * AUTHORITY
			 */
			//Calculate authority
			ArrayList<Integer> authorityAl = calculateAuthorityPath(train);
			
			//Translate to int array for Wayside
			int[] authority = new int[authorityAl.size()];
			for(int i=0; i<authorityAl.size(); i++) {
				authority[i] = authorityAl.get(i);
			}
			
			//Transmit authority
			transmitCtcAuthority(train.name, wayside, authority);
			
			/*
			 * SPEED
			 */
			//Calculate speed
			calculateSuggestedSpeed(train);
			transmitSuggestedSpeed(train.name, wayside, train.suggestedSpeed, train.currLocation);
		}
		
		/*
		 * UPDATE TRACKCONTROLLERS
		 */
		for(TrackController tc : trackControllers) {
			//Wait for tc to update before continuing
			while(!tc.updateTime(currentTime)) {};
		}
		
		while(!gui.repaint()) {};
		
		return true;
	}
	
	@Override
	public boolean communicationEstablished() {
		trackControllers = new TrackController[4];
		trackControllers[0] = new TrackController("Green",new String[]{"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","39","40","41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","127","128","129","130","131","132","133","134","135","136","137","138","139","140","141","142","143","144","145","146","147","148","149"},"G1");
		trackControllers[1] = new TrackController("Green",new String[]{"53","54","55","56","57","58","59","60","61","62","63","64","65","66","67","68","69","70","71","72","73","74","75","76","77","78","79","80","81","82","83","84","85","86","87","88","89","90","91","92","93","94","95","96","97","98","99","100","101","102","103","104","105","106","107","108","109","110","111","112","113","114","115","116","117","118","119","120","121","122","123","124","125","126","127","128","129","150","151"},"G2");
		trackControllers[2] = new TrackController("Red",  new String[]{"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","76"},"R1");
		trackControllers[3] = new TrackController("Red",  new String[]{"33","34","35","36","37","38","39","40","41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59","60","61","62","63","64","65","66","67","68","69","70","71","72","73","74","75"},"R2");
		for(TrackController tc : trackControllers) {
			tc.trackModel = trackModel;
			tc.updateTime(currentTime);
		}
		
		gui.updateSelectedBlock(true);
		
		enableMovingBlockMode(false);
		
		return true;
	}
	
	/*
	 * ------------------------------
	 *  TIMECONTROL REQUIREMENTS
	 * ------------------------------
	 */
	@Override
	public void pause() {
	    simulator.pause();
	}
	@Override
	public void play() {
	    simulator.play();
	}
	@Override
	public void setSpeedup(int newSpeed) {
	    simulator.setSpeedup(newSpeed);
	}

}
