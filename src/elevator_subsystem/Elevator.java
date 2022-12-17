package elevator_subsystem;

import common_classes.Config;
import common_classes.Helper;
import messages.ElevatorMessage;

/**
 * The Elevator consists of buttons, lamps and doors that it operates based on
 * requests it receives from the scheduler
 *
 * @author sarahjaber
 * @version 1.0
 *
 */
public class Elevator {

	// ID of the elevator and subsystem
	private int id;

	// motor of the elevator
	private MotorState motorState = MotorState.STOPPED;

	// the floor position of the elevator
	private int floor = 1;

	// the state of the elevator doors
	private DoorState doorState = DoorState.CLOSED;

	// the position of the elevator
	private Status status = Status.ARRIVED;

	// the time till an error occurs
	private int timeBeforeBreaking;

	/**
	 * Create a new Elevator with its initial state on the ground floor and its
	 * doors closed
	 */

	public Elevator(int id, int timeBeforeBreaking) {
		this.id = id;
		this.timeBeforeBreaking = timeBeforeBreaking;
	}

	/**
	 * Given an instruction, do the corresponding action
	 *
	 * @param instruction
	 */
	public void handleInstruction(Instruction instruction) {
		switch (instruction) {
		case MOVE_UP:
			motorState = MotorState.UP;
			move();
			break;
		case MOVE_DOWN:
			motorState = MotorState.DOWN;
			move();
			break;
		case STOP:
			stop();
			break;
		case CONTINUE:
			continueElevator();
			break;
		case IDLE:
			break;
		}
	}

	/**
	 * @return true if the elevator is on floor n and is approaching the next floor
	 *         at constant velocity, false otherwise
	 */
	public boolean isApproachingNextFloor() {
		return status == Status.ARRIVED && motorState != MotorState.STOPPED;
	}

	/**
	 *
	 * @return true if the elevator is arriving at floor n at constant velocity,
	 *         false otherwise
	 */
	public boolean isArrivingAtFloor() {
		return status == Status.APPROACHING && motorState != MotorState.STOPPED;
	}

	/**
	 * Notify the elevatorSubsystem and scheduler of the current elevator state
	 */
	public ElevatorMessage getState() {
		// sends message to subsystem to update elevator states
		return new ElevatorMessage(id, floor, motorState, doorState, status);
	}

	/**
	 * Adjusts the elevator state for cases where elevator is approaching the next
	 * floor (from prev floor) or arriving at the next floor at constant velocity
	 */
	public void continueElevator() {
		// both these cases are happening at constant velocity
		if (isApproachingNextFloor()) {
			timePassesInElevator(Config.TIME_TO_TRAVEL_BETWEEN_FLOORS);
			if (brokeWhilePerformingAction()) {
				return;
			}
			status = Status.APPROACHING;
			floor += motorState == MotorState.UP ? 1 : -1;
		} else if (isArrivingAtFloor()) {
			timePassesInElevator(Config.TIME_TO_ARRIVE_AT_FLOOR);
			if (brokeWhilePerformingAction()) {
				return;
			}
			status = Status.ARRIVED;
		}
	}

	/**
	 * Decelerates the elevator to a stopped state.
	 *
	 * @return
	 */
	private void stop() {
		if (motorState == MotorState.STOPPED) // already stopped
			return;

		if (status == Status.ARRIVED) // passing floor, stop at next floor
			continueElevator();

		timePassesInElevator(Config.TIME_TO_DECELERATE_TO_STOP);
		if (brokeWhilePerformingAction()) {
			return;
		}
		status = Status.ARRIVED;
		motorState = MotorState.STOPPED;
		openDoors();
	}

	/**
	 * the elevator moves from rest to approach the next floor
	 */
	private void move() {
		closeDoors();
		timePassesInElevator(Config.TIME_TO_APPROACH_FLOOR);
		if (brokeWhilePerformingAction()) {
			return;
		}
		status = Status.APPROACHING;
		floor += motorState == MotorState.UP ? 1 : -1;
	}

	/**
	 * Open the elevator doors
	 */
	public void openDoors() {
		if (doorState == DoorState.OPEN)
			return;
		timePassesInElevator(Config.TIME_TO_OPEN_DOORS);
		if (brokeWhilePerformingAction()) {
			return;
		}
		doorState = DoorState.OPEN;
	}

	/**
	 * Close the elevator doors
	 */
	public void closeDoors() {
		if (doorState == DoorState.CLOSED)
			return;
		timePassesInElevator(Config.TIME_TO_CLOSE_DOORS);
		if (brokeWhilePerformingAction()) {
			return;
		}
		doorState = DoorState.CLOSED;
	}

	/**
	 * Checks if time taken to do action results in breaking
	 *
	 * @param duration the time to do action
	 */
	private void timePassesInElevator(int duration) {
		if (duration >= timeBeforeBreaking) {
			Helper.sleep(timeBeforeBreaking);
			timeBeforeBreaking = 0;
			status = Status.BROKEN;
			motorState = MotorState.STOPPED;
		} else {
			Helper.sleep(duration);
			timeBeforeBreaking -= duration;
		}
	}

	/* GETTERS */
	public MotorState getMotorState() {
		return motorState;
	}

	public int getFloor() {
		return floor;
	}

	public DoorState getDoorState() {
		return doorState;
	}

	public Status getStatus() {
		return status;
	}

	public int getId() {
		return id;
	}

	public boolean brokeWhilePerformingAction() {
		return (status == Status.BROKEN);
	}
}
