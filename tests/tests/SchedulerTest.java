package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import elevator_subsystem.DoorState;
import elevator_subsystem.MotorState;
import elevator_subsystem.Status;
import messages.ElevatorMessage;
import messages.FloorRequest;
import messages.Register;
import scheduler.Scheduler;
import views.SchedulerView;

public class SchedulerTest {

	Scheduler scheduler;
	SchedulerView sv;

	@BeforeEach
	void setUp() {
		sv = new SchedulerView();
		scheduler = new Scheduler(sv);
	}

	@Test
	public void TestElevatorRegister() {
		// register an elevator 0 at port 69
		int id = 0, port = 69;
		scheduler.register(new Register(id, port));
		assertEquals(scheduler.getElevatorPorts().get(id), port);
		assertEquals(scheduler.getElevatorFloorRequestsAssigned().get(id).size(), 0);
		assertEquals(scheduler.getElevatorFloorRequestsInService().get(id).size(), 0);
	}

	@Test
	public void TestUpdateElevatorState() {
		int id = 0;

		ElevatorMessage em = new ElevatorMessage(id, 1, MotorState.UP, DoorState.OPEN, Status.ARRIVED);
		scheduler.updateElevatorState(em);

		assertEquals(scheduler.getElevators().get(id), em);
	}

	@Test
	public void TestIsElevatorCandidate() {
		ElevatorMessage em = new ElevatorMessage(1, 4, MotorState.UP, DoorState.CLOSED, Status.ARRIVED);
		scheduler.register(new Register(em.getId(), 0));
		scheduler.getElevatorFloorRequestsAssigned().get(em.getId()).add(new FloorRequest(0, 7, 8));

		
		FloorRequest fr1 = new FloorRequest(0, 1, 3);
		FloorRequest fr2 = new FloorRequest(0, 5, 6);
		assertFalse(scheduler.isElevatorCandidate(fr1, em));
		assertTrue(scheduler.isElevatorCandidate(fr2, em));
	}

	@Test
	public void TestGetBestElevator() {
		ElevatorMessage em1 = new ElevatorMessage(1, 4, MotorState.UP, DoorState.CLOSED, Status.ARRIVED);
		scheduler.register(new Register(em1.getId(), 0));
		scheduler.updateElevatorState(em1);
		scheduler.getElevatorFloorRequestsAssigned().get(em1.getId()).add(new FloorRequest(0, 7, 8));

		ElevatorMessage em2 = new ElevatorMessage(2, 3, MotorState.UP, DoorState.CLOSED, Status.ARRIVED);
		scheduler.register(new Register(em2.getId(), 0));
		scheduler.updateElevatorState(em2);
		scheduler.getElevatorFloorRequestsAssigned().get(em2.getId()).add(new FloorRequest(0, 7, 8));

		// elevator 1 is closer
		FloorRequest fr1 = new FloorRequest(0, 5, 6);
		assertEquals(scheduler.getBestElevator(fr1), 1);

		// no elevator available to service this request
		FloorRequest fr2 = new FloorRequest(0, 2, 1);
		assertEquals(scheduler.getBestElevator(fr2), -1);
	}

	@Test
	public void TestGetFloorRequests() {
		FloorRequest fr1 = new FloorRequest(0, 5, 6);
		FloorRequest fr2 = new FloorRequest(0, 7, 8);
		FloorRequest fr3 = new FloorRequest(0, 2, 1);

		scheduler.getWaitlist().add(fr1);
		scheduler.getWaitlist().add(fr2);
		scheduler.getWaitlist().add(fr3);

		// fr1 and fr2 fit the criteria for this elevator
		ElevatorMessage em = new ElevatorMessage(1, 4, MotorState.UP, DoorState.CLOSED, Status.ARRIVED);
		scheduler.register(new Register(em.getId(), 0));
		scheduler.updateElevatorState(em);
		scheduler.getElevatorFloorRequestsAssigned().get(em.getId()).add(new FloorRequest(0, 7, 8));

		assertEquals(scheduler.getFloorRequests(em).size(), 2);
	}

	@Test
	public void TestHandleBrokenElevator() {
		int id = 1;

		// initialize all structures
		scheduler.register(new Register(id, 1));

		ElevatorMessage em = new ElevatorMessage(id, 4, MotorState.UP, DoorState.CLOSED, Status.ARRIVED);

		// update elevator state
		scheduler.updateElevatorState(em);

		// add an unserviced floor request
		FloorRequest fr = new FloorRequest(0, 2, 1);
		scheduler.getElevatorFloorRequestsAssigned().get(id).add(fr);

		// have scheduler handle the broken elevator
		scheduler.handleBrokenElevator(em);

		assertEquals(scheduler.getWaitlist().size(), 1);
	}

}
