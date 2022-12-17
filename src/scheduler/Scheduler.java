package scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import common_classes.Helper;
import common_classes.Subsystem;
import elevator_subsystem.MotorState;
import elevator_subsystem.Status;
import messages.ElevatorMessage;
import messages.FloorRequest;
import messages.Message;
import messages.Register;
import messages.RequestListMessage;
import messages.Response;
import messages.Terminate;
import views.SchedulerView;

/**
 * The scheduler to act as the communication channel between the floor and
 * elevator subsystem
 *
 * Two threads, one thread receives messages only and sends them to the second
 * thread. the second thread stores the floor requests, interprets the elevator
 * state and does logic (passenger and floors) then sends an instruction to the
 * elevator and sends an empty message to the floor. The thread receiver and
 * thread sender have defined ports.
 *
 * @author Ryan Godfrey
 *
 */
public class Scheduler extends Subsystem {


	// Tracks if floor subsystem is finished
	private boolean running = true;

	// The message receiver
	private SchedulerMessageReceiver schedulerMessageReceiver = new SchedulerMessageReceiver();

	// Maintains the elevator state
	private Map<Integer, ElevatorMessage> elevators = new HashMap<>();

	// Maintains the elevator ports
	private Map<Integer, Integer> elevatorPorts = new HashMap<>();

	// Maintains the the floor requests of each elevator to pick passengers up
	private Map<Integer, List<FloorRequest>> elevatorFloorRequestsAssigned = new HashMap<>();

	// Maintains the the floor requests of each elevator to pick passengers up
	private Map<Integer, List<FloorRequest>> elevatorFloorRequestsInService = new HashMap<>();
	
	// The floor requests that have not been sent to an elevator
	private List<FloorRequest> waitlist = new ArrayList<>();

	// The passenger limit for an elevator
	private int maxCapacity = 5;

	// The starting time of the scheduler
	private long startingTime;
	
	// The finishing time of the scheduler
	private long finishingTime;
	
	// A flag indicating that an elevator has been added
	private boolean elevatorAdded = false;

	// A map to hold the real start time of all the requests
	private Map<FloorRequest, Long> requestStartTimes = new HashMap<>();

	// Tracks the average time for one request
	private double averageTimeForRequest = 0;
			
	// Tracks the number of requests that have finished
	private int numRequestsFinished = 0;
	
	// Saves the durations of the requests 
	private StringBuilder requestDurations = new StringBuilder();
	
	// The view of the scheduler
	private SchedulerView schedulerView;


	
	/**
	 * Initializes the Scheduler with a view
	 *
	 * @param sv the view to which the scheduler will display attributes
	 */
	public Scheduler(SchedulerView sv) {
		schedulerView = sv;
		this.startTimer();
	}


	/**
	 * Starts the scheduler message receiver
	 */
	public void startMessageReceiver() {
		Thread thread = new Thread(schedulerMessageReceiver);
		thread.start();
	}

	/**
	 * Update the elevator states given an elevator state
	 *
	 * @param e the elevator state
	 */
	public void updateElevatorState(ElevatorMessage e) {
		int id = e.getId();

		// update elevator state
		elevators.put(id, e);

		if (e.hasStopped()) {
			// passengers get off
			Iterator<FloorRequest> iter1 = elevatorFloorRequestsInService.get(id).iterator();
			while (iter1.hasNext()) {
				FloorRequest fr = iter1.next();
				if (fr.getDestinationFloor() == e.getFloor()) {
					long duration = System.currentTimeMillis() - requestStartTimes.remove(fr);
					requestDurations.append(fr.getTimestamp()).append(" - ").append(fr).append(" : ").append(duration/1000).append(" seconds\n");
					
					
					averageTimeForRequest = (averageTimeForRequest*numRequestsFinished + duration)/(numRequestsFinished+1);
					numRequestsFinished++;
					
					iter1.remove();
				}
			}
			
			// passengers get on
			Iterator<FloorRequest> iter2 = elevatorFloorRequestsAssigned.get(id).iterator();
			while (iter2.hasNext()) {
				FloorRequest fr = iter2.next();
				if (fr.getSourceFloor() == e.getFloor()) {	
					elevatorFloorRequestsInService.get(id).add(fr);
					iter2.remove();
				}
			}
		}
	}

	
	/* Helper methods for checking if elevator is a candidate for a floor request */
	private boolean isElevatorBelowFloor(ElevatorMessage e, int floor) {
		return e.getFloor() <= floor;
	}

	private boolean isElevatorAboveFloor(ElevatorMessage e, int floor) {
		return e.getFloor() >= floor;
	}
	
	private boolean isElevatorWaiting(int id) {
		return elevatorFloorRequestsAssigned.get(id).isEmpty() && elevatorFloorRequestsInService.get(id).isEmpty();
	}


	private boolean isElevatorMovingUp(ElevatorMessage e) {
		int id = e.getId();
		return ((elevatorFloorRequestsInService.get(id).size() > 0 
				&& elevatorFloorRequestsInService.get(id).get(0).isGoingUp())
				|| 
				(elevatorFloorRequestsAssigned.get(id).size() > 0 
						&& elevatorFloorRequestsAssigned.get(id).get(0).isGoingUp()));
	}

	private boolean isElevatorMovingDown(ElevatorMessage e) {
		int id = e.getId();
		
		return ((elevatorFloorRequestsInService.get(id).size() > 0 
				&& !elevatorFloorRequestsInService.get(id).get(0).isGoingUp())
				|| 
				(elevatorFloorRequestsAssigned.get(id).size() > 0 
						&& !elevatorFloorRequestsAssigned.get(id).get(0).isGoingUp()));
	}

	/**
	 * Checks to see if elevator can service the floor request. 
	 * The elevator is a candidate if: 
	 * 	if it is carrying less than max capacity 
	 * 		case 1 
	 * 		- passenger wants to move up 
	 * 		- elevator is below request source floor && has requests to move up 
	 * 		- elevator is waiting to receive requests && has no requests to move down 
	 * 		case 2 
	 * 		- passenger wants to move down 
	 * 		- elevator is above request source floor && has requests to move down 
	 * 		- elevator is waiting to receive requests && has no requests to move up
	 *
	 * @param fr the floor request
	 * @param e  the elevator
	 * @return true if yes, false otherwise
	 */
	public boolean isElevatorCandidate(FloorRequest fr, ElevatorMessage e) {
		int id = e.getId();
		int sourceFloor = fr.getSourceFloor();

		if ((elevatorFloorRequestsAssigned.get(id).size() + elevatorFloorRequestsInService.get(id).size()) > maxCapacity)
			return false;

		if (fr.isGoingUp()) {
			// Elevator is below request source floor && has requests to move up
			if (isElevatorBelowFloor(e, sourceFloor) && isElevatorMovingUp(e))
				return true;

			// Elevator is stopped && has no requests to move down && 
			if (e.hasStopped() && isElevatorWaiting(id))
				return true;
		} else {
			// Elevator is above request source floor && has requests to move down
			if (isElevatorAboveFloor(e, sourceFloor) && isElevatorMovingDown(e))
				return true;

			// Elevator is waiting to receive requests && has no requests to move up
			if (e.hasStopped() && isElevatorWaiting(id))
				return true;
		}
		return false;
	}

	/**
	 * Picks an elevator for the floor request. Currently picks the closest elevator
	 * out of a list of candidate elevators.
	 *
	 * @param f the floor request
	 * @return the elevator to be chosen, -1 if there was no elevator
	 */
	public int getBestElevator(FloorRequest floorRequest) {
		ElevatorMessage closestElevator = elevators.values().stream()
				// get every candidate elevator
				.filter(e -> isElevatorCandidate(floorRequest, e))
				// get elevator closest to source floor of floor request
				.min((e1, e2) -> Math.abs(e1.getFloor() - floorRequest.getSourceFloor())
						- Math.abs(e2.getFloor() - floorRequest.getSourceFloor()))
				.orElse(null);

		if (closestElevator == null) // no elevators available
			return -1;

		return closestElevator.getId();
	}

	/**
	 * Gets the floor requests that can be serviced by an elevator
	 *
	 * @param em
	 * @return the floor requests
	 */
	public List<FloorRequest> getFloorRequests(ElevatorMessage em) {
		List<FloorRequest> floorRequests = new ArrayList<>();
		Iterator<FloorRequest> iter = waitlist.iterator();
		int id = em.getId();
		
		MotorState futureDirection = MotorState.STOPPED; // ensures that elevator will only be sent requests in one direction
		int passengerCount = elevatorFloorRequestsAssigned.get(id).size() + elevatorFloorRequestsInService.get(id).size();

		while (iter.hasNext() && passengerCount < maxCapacity) {
			FloorRequest fr = iter.next();
			MotorState passengerDirection = fr.isGoingUp() ? MotorState.UP : MotorState.DOWN;

			if ((futureDirection  == MotorState.STOPPED || futureDirection  == passengerDirection) && getBestElevator(fr) == em.getId()) {
				futureDirection  = passengerDirection;
				floorRequests.add(fr);
				passengerCount++;
				iter.remove();
			}
		}
		schedulerView.updateWaitlist(waitlist);

		return floorRequests;
	}

	/**
	 * Sends the requests to an elevator
	 *
	 * @param floorRequests
	 * @param id
	 */
	public void sendRequestsToElevator(List<FloorRequest> floorRequests, int id) {
		for (FloorRequest fr : floorRequests) {
			if (elevators.get(id).getFloor() == fr.getSourceFloor()) {
				elevatorFloorRequestsInService.get(id).add(fr);
			} else {
				elevatorFloorRequestsAssigned.get(id).add(fr);
			}
		}

		send(new RequestListMessage(floorRequests), elevatorPorts.get(id));
	}

	/**
	 * Sets the running to false
	 */
	private void terminate() {
		running = false;
	}

	/**
	 * Registers an elevator with the scheduler
	 *
	 * @param message
	 */
	public void register(Register message) {
		elevatorPorts.put(message.getID(), message.getPort());
		elevatorFloorRequestsInService.put(message.getID(), new ArrayList<>());
		elevatorFloorRequestsAssigned.put(message.getID(), new ArrayList<>());
	}

	/**
	 * Checks to see if all requests have been finished
	 *
	 * @return true if yes, false otherwise
	 */
	public boolean isFinished() {
		for (Integer id : elevatorFloorRequestsInService.keySet()) {
			if (elevatorFloorRequestsInService.get(id).size() > 0)
				return false;
		}
		
		for (Integer id : elevatorFloorRequestsAssigned.keySet()) {
			if (elevatorFloorRequestsAssigned.get(id).size() > 0)
				return false;
		}
		return waitlist.isEmpty();
	}

	/**
	 * terminates the broken elevator and reallocates its assigned requests
	 *
	 * @param message elevator message of the broken elevator
	 */
	public void handleBrokenElevator(ElevatorMessage e) {
		int id = e.getId();

		// reallocate the requests for the broken elevator
		waitlist.addAll(elevatorFloorRequestsAssigned.get(id));
	
		// update elevator state
		elevators.remove(id);
		
		elevatorFloorRequestsAssigned.remove(id);
		
		elevatorFloorRequestsInService.remove(id);

		schedulerView.updateWaitlist(waitlist);
		
		// sending requests from wait list to idling elevators
		for (ElevatorMessage em: elevators.values()) {
			if (isElevatorWaiting(em.getId())){
				List<FloorRequest> floorRequests = getFloorRequests(em);
				if (!floorRequests.isEmpty()) {
					System.out.println("SCHEDULER: Sending "
							+ floorRequests + " to Elevator-" + em.getId()
							+ " | " + elevatorFloorRequestsAssigned.get(em.getId()) + " | " + elevatorFloorRequestsInService.get(em.getId()));
					sendRequestsToElevator(floorRequests, em.getId());
				}
			}
		}
	}
	
	
	/**
	 * Creates a thread receives messages from the messageReceiver thread and if the
	 * message is a floor request then it saves the floor request and sends back an
	 * empty reply. If it is an elevator state then it will update the passenger
	 * variable, do scheduling logic and send the appropriate request back to the
	 * elevator
	 */
	public void run() {
		System.out.println("SCHEDULER: STARTED");
		startMessageReceiver();
		List<Message> messages;
		while (running || !isFinished()) {
			messages = schedulerMessageReceiver.getMessages();
			for (Message message : messages) {
				if (message.getHeader() == Helper.FLOOR_REQUEST_MESSAGE) {
					FloorRequest fr = (FloorRequest) message;
					System.out.println("SCHEDULER: Received floor request -> " + fr);

					// save the start time of the floor request
					requestStartTimes.put(fr, System.currentTimeMillis());
					
					System.out.println("---- Current elevators ----");
					for (ElevatorMessage em : elevators.values()) {
						System.out.printf("%-60s || Requests in service --> %s || Requests assigned --> %s\n", em, elevatorFloorRequestsInService.get(em.getId()), elevatorFloorRequestsAssigned.get(em.getId()));
					}

					int elevatorId = getBestElevator(fr);
					if (elevatorId == -1) {
						waitlist.add(fr);
						schedulerView.updateWaitlist(waitlist);
						System.out.println("No elevators available. Adding to waitlist");
					} else {
						if (isElevatorWaiting(elevatorId)) {
							List<FloorRequest> requests = new ArrayList<>();
							requests.add(fr);
							sendRequestsToElevator(requests, elevatorId);
							System.out.println("SCHEDULER: Sending " + (requests.isEmpty() ? "no requests" : requests)
									+ " to Elevator-" + elevatorId);
						} else {
							waitlist.add(fr);
							schedulerView.updateWaitlist(waitlist);
							System.out.println("Elevator-" + elevatorId + " not receiving. Adding to waitlist");
						}
					}

					// send an empty reply back to floor
					send(new Response(), Helper.FLOOR_PORT);
					schedulerView.updateElevators(elevatorFloorRequestsInService, elevatorFloorRequestsAssigned);
				} else if (message.getHeader() == Helper.ELEVATOR_STATE_MESSAGE) {
					ElevatorMessage em = (ElevatorMessage) message;
					int id = em.getId();

					System.out.println("SCHEDULER: Received elevator state: " + em);
					// check if the elevator is broken
					if (em.getStatus() == Status.BROKEN) {
						handleBrokenElevator(em);
						System.out.println("SCHEDULER: Current wait list: " + waitlist);
						System.out.println("SCHEDULER: Sending terminate to Elevator-" + id);
						send(new Terminate(), elevatorPorts.get(id));
						elevatorPorts.remove(id);
					} else {
						updateElevatorState(em);
						System.out.println("SCHEDULER: Current wait list: " + waitlist);
						
						List<FloorRequest> floorRequests = getFloorRequests(em);

						if (!floorRequests.isEmpty() || !isElevatorWaiting(id)) {
							System.out.println("SCHEDULER: Sending "
									+ (floorRequests.isEmpty() ? "no requests" : floorRequests) + " to Elevator-" + id
									+ " | " + elevatorFloorRequestsAssigned.get(id) + " | " + elevatorFloorRequestsInService.get(id));
							sendRequestsToElevator(floorRequests, id);
						} else {
							System.out.println("SCHEDULER: Withholding response.");
						}
						schedulerView.updateElevators(elevatorFloorRequestsInService, elevatorFloorRequestsAssigned);
					}

				} else if (message.getHeader() == Helper.REGISTER_MESSAGE) {
					System.out.println("SCHEDULER: Received register.");
					Register rm = (Register) message;
					register(rm);
					System.out.println("SCHEDULER: Registered Elevator-" + rm.getID() + " to port " + rm.getPort());
					send(new Response(), rm.getPort());
					elevatorAdded = true;
				} else if (message.getHeader() == Helper.TERMINATE) {
					System.out.println("SCHEDULER: Received terminate.");

					terminate();
					send(new Response(), Helper.FLOOR_PORT);
					
				} else {
					System.out.println("SCHEDULER: Unknown message, ruh roh");
				}
				System.out.println("-----------------");
			}
			if(checkIfElevatorsHaveAllTerminated()) {
				System.out.println("SCHEDULER: All elevators are finished running.");
				terminate();
				break;
			}
		}
		

		Iterator<Integer> iter = elevatorPorts.keySet().iterator();
		while (iter.hasNext()) {
			int id = iter.next();
			if (isElevatorWaiting(id)) {
				System.out.println("SCHEDULER: Sending terminate to Elevator-" + id);
				send(new Terminate(), elevatorPorts.get(id));
				iter.remove();
			}
		}
		
		System.out.println("SCHEDULER: Terminated.");
		endTimerAndPrint();
		schedulerMessageReceiver.closeSockets();
		closeSockets();
	}
	
	/**
	 * Measures initial time where scheduler is started
	 */
	private void startTimer() {
		startingTime = System.nanoTime();
	}
	
	/**
	 * Measures ending time when scheduler is terminating, as well
	 * as prints out a message with the total elapsed time. 
	 */
	private void endTimerAndPrint() {
		finishingTime = System.nanoTime();
		System.out.println("Total time run: " + (finishingTime - startingTime) + " nanoseconds");
		System.out.println(requestDurations);
		System.out.println("Average time for each request: " + (averageTimeForRequest/1000) + " seconds");
	}
	
	/**
	 * Checks for whether all elevators that were added to the Scheduler
	 * have terminated. 
	 * 
	 * @return true if elevators have been terminated, false otherwise. 
	 */
	private boolean checkIfElevatorsHaveAllTerminated() {
		if(elevatorAdded) return this.elevatorPorts.isEmpty();
		return false;
	}

	/* Getters */
	public Map<Integer, ElevatorMessage> getElevators() {
		return elevators;
	}

	public Map<Integer, Integer> getElevatorPorts() {
		return elevatorPorts;
	}

	public List<FloorRequest> getWaitlist() {
		return waitlist;
	}
	
	public Map<Integer, List<FloorRequest>> getElevatorFloorRequestsAssigned() {
		return elevatorFloorRequestsAssigned;
	}

	public Map<Integer, List<FloorRequest>> getElevatorFloorRequestsInService() {
		return elevatorFloorRequestsInService;
	}

}
