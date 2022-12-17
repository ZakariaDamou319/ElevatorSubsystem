package elevator_subsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import common_classes.Config;
import common_classes.Subsystem;
import messages.ElevatorMessage;
import messages.FloorRequest;
import messages.Message;
import messages.Register;
import messages.RequestListMessage;
import messages.Terminate;
import views.ElevatorView;

/**
 * The elevator subsystem consists of the buttons and lamps inside of the
 * elevator used to select floors and indicate the floors selected, and to
 * indicate the location of the elevator itself. The elevator subsystem is also
 * used to operate the motor and to open and close the doors.
 *
 * @author sarahjaber
 * @version 1.0
 */

public class ElevatorSubsystem extends Subsystem implements Runnable {

	// the elevator that is maintained by the subsystem
	private Elevator elevator;

	// the buttons that belong to the elevator
	private Map<Integer, ElevatorButton> buttons = new HashMap<Integer, ElevatorButton>();

	// the flag to check if terminate
	private boolean isRunning = true;

	// the floor requests to pick passengers up
	private List<FloorRequest> floorRequestsInService = new ArrayList<>();

	// the floor requests to pick passengers up
	private List<FloorRequest> floorRequestsAssigned = new ArrayList<>();

	private ElevatorView view;

	/**
	 * Create a new Elevator subsystem
	 */
	public ElevatorSubsystem(int elevatorID, int numFloors, ElevatorView view) {
		super();

		this.view = view;

		// the initial elevator state
		elevator = new Elevator(elevatorID, generateRandomBreakingTime());

		
		for (int i = 1; i <= numFloors; i++) {
			buttons.put(i, new ElevatorButton(i));
		}
	}

	/**
	 * Checks to see if the elevator has requests
	 * @return true if the elevator has requests, false otherwise
	 */
	private boolean hasRequests() {
		return !floorRequestsInService.isEmpty() && !floorRequestsAssigned.isEmpty();
	}
	
	/**
	 * The elevator subsystem thread to receive messages and send instructions to
	 * elevator
	 */
	@Override
	public void run() {
		Thread.currentThread().setName("Elevator Subsystem");
		System.out.println("ELEVATOR SUBSYSTEM " + elevator.getId() + ": STARTED");

		// register with scheduler
		Message message = rpcSendAndReceive(new Register(elevator.getId(), receiveSocket.getLocalPort()));

		// Initialize elevator view
		view.setMotorState(elevator.getMotorState());
		view.setDoorState(elevator.getDoorState());
		view.setFloor(elevator.getFloor(), elevator.getStatus(), buttons);

		while (isRunning || hasRequests()) {
			if (elevator.getMotorState() == MotorState.STOPPED && !hasRequests())
				updateSocketTimeout(0);
			else
				updateSocketTimeout(500);

			if (isRunning)
				message = rpcSendAndReceive(elevator.getState());

			if (message == null) {
				System.out.println("ELEVATOR-" + elevator.getId() + ": IDLING");
			} else if (message instanceof RequestListMessage) {
				for (FloorRequest fr : ((RequestListMessage) message).getRequestList()) {
					updateFloorRequest(fr, elevator.getState());
				}
			} else if (message instanceof Terminate) { // no response from scheduler or told to shutdown
				isRunning = false;
				if (elevator.brokeWhilePerformingAction())
					break;
			} else {
				// unknown message
			}

			ElevatorMessage elevatorState = elevator.getState();
			Instruction instruction = getNextInstruction(elevatorState);

			elevator.handleInstruction(instruction);
			handleRequestUpdate(elevator.getState());

			System.out.printf("%-60s || In service --> %s  Assigned --> %s\n",
					"ELEVATOR-" + elevator.getId() + ": " + elevatorState.getChange(elevator.getState()), floorRequestsInService, floorRequestsAssigned);

			view.setMotorState(elevator.getMotorState());
			view.setDoorState(elevator.getDoorState());
			view.setFloor(elevator.getFloor(), elevator.getStatus(), buttons);
		}
		terminate();
		System.out.println("ELEVATOR-" + elevator.getId() + ": TERMINATED");
		closeSockets();
	}

	/**
	 * Updates the floor requests with the input floor request
	 *
	 * @param fr the floor request
	 */
	public void updateFloorRequest(FloorRequest fr, ElevatorMessage e) {
		if (fr.getSourceFloor() == e.getFloor()) {
			floorRequestsInService.add(fr);
		} else {
			floorRequestsAssigned.add(fr);
		}
		
		System.out.println("ELEVATOR-" + elevator.getId() + ": Received Floor Request: " + fr);
	}

	/**
	 * Handles the passenger movement between elevator and floor
	 */
	public void handleRequestUpdate(ElevatorMessage e) {
		if (e.getMotorState() == MotorState.STOPPED) {
			int floor = e.getFloor();
			
			Iterator<FloorRequest> iter1 = floorRequestsAssigned.iterator();
			while (iter1.hasNext()) {
				FloorRequest fr = iter1.next();
				if (fr.getSourceFloor() == floor) {
					buttons.get(fr.getDestinationFloor()).lightOn(); // turn the destination lights on
					floorRequestsInService.add(fr);
					iter1.remove();
				}
			}
			
			Iterator<FloorRequest> iter2 = floorRequestsInService.iterator();
			while (iter2.hasNext()) {
				FloorRequest fr = iter2.next();
				if (fr.getDestinationFloor() == floor) {
					iter2.remove();
				}
			}

			buttons.get(floor).lightOff();
		}
		// view.updateButtons(buttons);
	}
	


	/**
	 * Checks to if the elevator should move down based on requests
	 * @param em the elevator state
	 * @return true if elevator should move down, false to otherwise
	 */
	private boolean shouldElevatorMoveDown(ElevatorMessage em) {
		int floor = em.getFloor();
		for (FloorRequest fr: floorRequestsInService) {
			if (fr.getDestinationFloor() < floor) {
				return true;
			}
		}
		
		for (FloorRequest fr: floorRequestsAssigned) {
			if (fr.getSourceFloor() < floor) {
				return true;
			}
		}
		return false;
	}
	

	/**
	 * Checks to if the elevator should move up based on requests
	 * @param em the elevator state
	 * @return true if elevator should move up, false to otherwise
	 */
	private boolean shouldElevatorMoveUp(ElevatorMessage em) {
		int floor = em.getFloor();
		for (FloorRequest fr: floorRequestsInService) {
			if (fr.getDestinationFloor() > floor) {
				return true;
			}
		}
		
		for (FloorRequest fr: floorRequestsAssigned) {
			if (fr.getSourceFloor() > floor) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks to see if elevator has requests that are going down
	 * @return true if requests are going down, false otherwise
	 */
	private boolean isRequestMovingDown() {
		return ((floorRequestsInService.size() > 0 
				&& !floorRequestsInService.get(0).isGoingUp())
				|| 
				(floorRequestsAssigned.size() > 0 
						&& !floorRequestsAssigned.get(0).isGoingUp()));
	}

	/**
	 * Checks to see if elevator has requests that are going up
	 * @return true if requests are going up, false otherwise
	 */
	private boolean isRequestMovingUp() {
		return ((floorRequestsInService.size() > 0 
				&& floorRequestsInService.get(0).isGoingUp())
				|| 
				(floorRequestsAssigned.size() > 0 
						&& floorRequestsAssigned.get(0).isGoingUp()));
	}
	
	/**
	 * Checks to see if there is a request with the destination on this floor
	 * @param floor 
	 * @return true if there is a request with the destination on this floor, false otherwise
	 */
	private boolean isDestination(int floor) {
		for (FloorRequest fr: floorRequestsInService) {
			if (fr.getDestinationFloor() == floor) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Checks to see if there is a request with the source on this floor
	 * @param floor 
	 * @return true if there is a request with the source on this floor, false otherwise
	 */
	private boolean isSource(int floor) {
		for (FloorRequest fr: floorRequestsAssigned) {
			if (fr.getSourceFloor() == floor) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Decides what instruction to send elevator based on elevator state & requests
	 */
	public Instruction getNextInstruction(ElevatorMessage elevatorState) {
		int floor = elevatorState.getFloor();
		if (elevatorState.hasStopped()) {
			if (shouldElevatorMoveDown(elevatorState)) {
				return Instruction.MOVE_DOWN; // stopped, arrived, doors open, request below
			} else if (shouldElevatorMoveUp(elevatorState)) {
				return Instruction.MOVE_UP; // stopped, arrived, doors open, request above
			}
			return Instruction.IDLE; // stopped, arrived, doors open, no requests
		} 

		if (isDestination(floor)) {
			return Instruction.STOP;
		}
		
		if (isSource(floor)) {
			if (elevatorState.getMotorState() == MotorState.DOWN) { 
				if (isRequestMovingDown()) {
					return Instruction.STOP;
				}
				
				if (!shouldElevatorMoveDown(elevatorState)) {
					return Instruction.STOP;
				}
			}
			
			if (elevatorState.getMotorState() == MotorState.UP) { 
				if (isRequestMovingUp()) {
					return Instruction.STOP;
				}
				
				if (!shouldElevatorMoveUp(elevatorState)) {
					return Instruction.STOP;
				}
			} 
		}
		
		
		// (moving up/down, arrived, doors closed) or (moving up/down, approaching, doors closed, no request on current floor)
		return Instruction.CONTINUE;
	}

	/**
	 * Generates a random time between minimum and maximum
	 * @return a random time
	 */
	private int generateRandomBreakingTime() {
		return (int) (Math.random() * Config.MAXIMUM_ELEVATOR_BREAK_TIME_SUBTRACTING_MINIMUM)
				+ Config.MINIMUM_ELEVATOR_BREAK_TIME;
	}


	/**
	 * Updates the elevator view to display termination
	 */
	private void terminate() {
		if (elevator.getStatus() != Status.BROKEN) // successful completion of duties
			view.terminate(elevator.getFloor());
	}
	
	/* Getters */
	public List<FloorRequest> getFloorRequestsInService() {
		return floorRequestsInService;
	}

	public List<FloorRequest> getFloorRequestsAssigned() {
		return floorRequestsAssigned;
	}
}
