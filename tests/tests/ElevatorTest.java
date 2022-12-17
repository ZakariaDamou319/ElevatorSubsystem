package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import common_classes.Config;
import common_classes.Helper;
import elevator_subsystem.DoorState;
import elevator_subsystem.Elevator;
import elevator_subsystem.ElevatorSubsystem;
import elevator_subsystem.Instruction;
import elevator_subsystem.MotorState;
import elevator_subsystem.Status;
import messages.ElevatorMessage;
import messages.FloorRequest;
import views.ElevatorView;

/**
 * The JUnit test file that will test the flow of information between the floor
 * and the elevator
 *
 * @author Zakaria Damou
 *
 */
class ElevatorTest {

	ElevatorSubsystem elevatorSubsystem;
	Elevator elevator;
	ElevatorView ev;

	@BeforeEach
	void setUp() {
		int numFloors = 7;
		ev = new ElevatorView(0, numFloors);

		elevatorSubsystem = new ElevatorSubsystem(1, numFloors, ev);
		elevator = new Elevator(0, 1000000);
	}
	

	@Test
	public void elevatorStopsAtLastRequest() {
		ElevatorMessage em =new ElevatorMessage(1, 7, MotorState.STOPPED, DoorState.OPEN, Status.ARRIVED);
		elevatorSubsystem.updateFloorRequest(new FloorRequest(0, 7, 3), em);
		elevatorSubsystem.updateFloorRequest(new FloorRequest(0, 5, 4), em);
		elevatorSubsystem.updateFloorRequest(new FloorRequest(0, 5, 1), em);
		elevatorSubsystem.updateFloorRequest(new FloorRequest(0, 3, 2), em);
		elevatorSubsystem.updateFloorRequest(new FloorRequest(0, 4, 1), em);
		
		ElevatorMessage em1 =new ElevatorMessage(1, 7, MotorState.STOPPED, DoorState.OPEN, Status.ARRIVED);
		elevatorSubsystem.handleRequestUpdate(em1);
		
		
		ElevatorMessage em2 = new ElevatorMessage(1, 5, MotorState.DOWN, DoorState.CLOSED, Status.APPROACHING);
		assertEquals(Instruction.STOP, elevatorSubsystem.getNextInstruction(em2));
	}
	

	/**
	 * Verifies that the elevator receives the correct information from the floor.
	 */
	@Test
	void elevatorReceivesFloorRequest() {
		// sending floor request
		FloorRequest expectedFloorRequest = new FloorRequest(Helper.timeStringToMilliseconds("14:05:15.0"), 2, 4);

		ElevatorMessage em = new ElevatorMessage(1, 2, MotorState.STOPPED, DoorState.CLOSED, Status.ARRIVED);
		
		// update floor req
		elevatorSubsystem.updateFloorRequest(expectedFloorRequest, em);
		assertEquals(elevatorSubsystem.getFloorRequestsInService().size(), 1);

	}

	/**
	 * Testing that instruction MOVE_DOWN is sent to the elevator in response to the
	 * floor request
	 */
	@Test
	public void elevatorSubsystemHandleElevatorMoveDownTest() {
		ElevatorMessage em = new ElevatorMessage(1, 2, MotorState.STOPPED, DoorState.CLOSED, Status.ARRIVED);
		elevatorSubsystem.updateFloorRequest(new FloorRequest(0, 1, 2), em);
		assertEquals(Instruction.MOVE_DOWN, elevatorSubsystem.getNextInstruction(em));
	}

	/**
	 * Testing that instruction MOVE_UP is sent to the elevator in response to the
	 * floor request
	 */
	@Test
	public void elevatorSubsystemHandleElevatorMoveUpTest() {
		ElevatorMessage em = new ElevatorMessage(1, 1, MotorState.STOPPED, DoorState.CLOSED, Status.ARRIVED);
		elevatorSubsystem.updateFloorRequest(new FloorRequest(0, 5, 2), em);
		assertEquals(Instruction.MOVE_UP, elevatorSubsystem.getNextInstruction(em));
	}

	/**
	 * Testing that instruction STOP is sent to the elevator in response to the
	 * floor request
	 */
	@Test
	public void elevatorSubsystemHandleElevatorStopTest() {
		ElevatorMessage em = new ElevatorMessage(1, 2, MotorState.DOWN, DoorState.CLOSED, Status.APPROACHING);
		elevatorSubsystem.updateFloorRequest(new FloorRequest(0, 2, 3), em);
		
		ElevatorMessage em1 = new ElevatorMessage(1, 3, MotorState.UP, DoorState.CLOSED, Status.APPROACHING);
		assertEquals(Instruction.STOP, elevatorSubsystem.getNextInstruction(em1));
	}

	/**
	 * Testing that instruction CONTINUE is sent to the elevator in response to the
	 * floor request
	 */
	@Test
	public void elevatorSubsystemHandleElevatorContinueTest() {
		ElevatorMessage em = new ElevatorMessage(1, 1, MotorState.UP, DoorState.CLOSED, Status.ARRIVED);
		elevatorSubsystem.updateFloorRequest(new FloorRequest(0, 3, 5), em);
		assertEquals(Instruction.CONTINUE, elevatorSubsystem.getNextInstruction(em));
	}

	
	/**
	 * Testing that elevator moves down after it receives MOVE_DOWN Instruction
	 */
	@Test
	public void elevatorMoveDownTest() {
		elevator.handleInstruction(Instruction.MOVE_DOWN);
		assertEquals(MotorState.DOWN, elevator.getMotorState());
	}

	/**
	 * Testing that elevator moves up after it receives MOVE_UP Instruction
	 */
	@Test
	public void elevatorMoveUpTest() {
		elevator.handleInstruction(Instruction.MOVE_UP);
		assertEquals(MotorState.UP, elevator.getMotorState());
	}

	/**
	 * Testing that elevator stop after it receives STOP Instruction
	 */
	@Test
	public void elevatorStopTest() {
		elevator.handleInstruction(Instruction.STOP);
		assertEquals(MotorState.STOPPED, elevator.getMotorState());
	}

	/**
	 * Testing that elevator continues after it receives CONTINUE Instruction
	 */
	@Test
	public void elevatorContinueTest() {
		elevator.handleInstruction(Instruction.STOP);
		elevator.handleInstruction(Instruction.CONTINUE);
		assertEquals(Status.ARRIVED, elevator.getStatus());
	}

	/**
	 * Testing that elevator breaks after the specified breaking time
	 */
	@Test
	public void elevatorBreakTest() {
		elevator = new Elevator(0, Config.TIME_TO_APPROACH_FLOOR);
		elevator.handleInstruction(Instruction.MOVE_UP);
		assertEquals(Status.BROKEN, elevator.getStatus());
	}
	


}
