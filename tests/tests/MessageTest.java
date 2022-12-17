package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.DatagramPacket;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import common_classes.Helper;
import elevator_subsystem.DoorState;
import elevator_subsystem.MotorState;
import elevator_subsystem.Status;
import messages.ElevatorMessage;
import messages.FloorRequest;
import messages.Message;
import messages.Register;
import messages.Response;
import messages.Terminate;

/**
 * Tests message classes.
 *
 * Elevator message format is: [MessageType, ElevatorId, CurrentFloor,
 * MotorState, DoorState, Status]
 *
 * Floor Request format is: [MessageType, Timestamp, SourceFloor,
 * DestinationFloor]
 *
 * Register Message format is: [MessageType, PortNumber]
 *
 * @author ryan
 *
 */
public class MessageTest {

	@Test
	public void testElevatorMessageGetData() {
		byte type = Helper.ELEVATOR_STATE_MESSAGE;
		byte id = 1;
		byte floor = 4;
		byte motorState = 2; // Refers to MotorState.DOWN
		byte doorState = 0; // Refers to DoorState.OPEN
		byte status = 2; // Refers to Status.BROKEN

		byte[] expected = new byte[] { type, 0, 0, 0, id, 0, 0, 0, floor, motorState, doorState, status };

		ElevatorMessage em = new ElevatorMessage(id, floor, MotorState.values()[motorState],
				DoorState.values()[doorState], Status.values()[status]);

		assertTrue(Arrays.equals(expected, em.getData()));

	}

	@Test
	public void testElevatorMessageDatagramToMessage() {
		byte type = Helper.ELEVATOR_STATE_MESSAGE;
		byte id = 1;
		byte floor = 4;
		byte motorState = 2; // Refers to MotorState.DOWN
		byte doorState = 0; // Refers to DoorState.OPEN
		byte status = 2; // Refers to Status.BROKEN

		ElevatorMessage expected = new ElevatorMessage(id, floor, MotorState.values()[motorState],
				DoorState.values()[doorState], Status.values()[status]);

		byte[] input = new byte[] { type, 0, 0, 0, id, 0, 0, 0, floor, motorState, doorState, status };

		DatagramPacket datagram = new DatagramPacket(input, input.length, null, 1);

		Message actual = ElevatorMessage.datagramToMessage(datagram.getData());

		assertEquals(ElevatorMessage.class, actual.getClass());
		assertEquals(expected, actual);
	}

	@Test
	public void testFloorRequestGetData() {
		byte type = Helper.FLOOR_REQUEST_MESSAGE;
		byte timestamp = 1;
		byte srcFloor = 4;
		byte destFloor = 2;

		byte[] expected = new byte[] { type, 0, 0, 0, timestamp, srcFloor, destFloor };

		FloorRequest em = new FloorRequest(timestamp, srcFloor, destFloor);

		assertTrue(Arrays.equals(expected, em.getData()));

	}

	@Test
	public void testFloorRequestDatagramToMessage() {
		byte type = Helper.FLOOR_REQUEST_MESSAGE;
		byte timestamp = 1;
		byte srcFloor = 4;
		byte destFloor = 2;

		FloorRequest expected = new FloorRequest(timestamp, srcFloor, destFloor);
		Message actual = FloorRequest.datagramToMessage(expected.getData());

		assertEquals(FloorRequest.class, actual.getClass());
		assertEquals(expected, actual);
	}

	@Test
	public void testRegisterGetData() {
		byte type = Helper.REGISTER_MESSAGE;
		byte id = 4;
		byte port = 1;

		byte[] expected = new byte[] { type, 0, 0, 0, id, 0, 0, 0, port };

		Register em = new Register(id, port);

		assertTrue(Arrays.equals(expected, em.getData()));

	}

	@Test
	public void testRegisterDatagramToMessage() {
		byte type = Helper.REGISTER_MESSAGE;
		byte id = 4;
		byte port = 1;

		Register expected = new Register(id, port);

		byte[] input = new byte[] { type, 0, 0, 0, id, 0, 0, 0, port };

		DatagramPacket datagram = new DatagramPacket(input, input.length, null, 1);

		Message actual = Register.datagramToMessage(datagram.getData());

		assertEquals(Register.class, actual.getClass());
		assertEquals(expected, actual);
	}

	@Test
	public void testResponseGetData() {
		byte type = Helper.EMPTY_REPLY_MESSAGE;

		byte[] expected = new byte[] { type };

		Response em = new Response();

		assertTrue(Arrays.equals(expected, em.getData()));
	}

	@Test
	public void testResponseDatagramToMessage() {
		byte type = Helper.EMPTY_REPLY_MESSAGE;

		byte[] input = new byte[] { type };

		DatagramPacket datagram = new DatagramPacket(input, input.length, null, 1);

		Message actual = Response.datagramToMessage(datagram.getData());

		assertEquals(Response.class, actual.getClass());
		// Dont need to test equivalence because empty constructor and no fields
	}



	@Test
	public void testTerminateGetData() {
		byte type = Helper.TERMINATE;

		byte[] expected = new byte[] { type };

		Terminate em = new Terminate();

		assertTrue(Arrays.equals(expected, em.getData()));
	}

	@Test
	public void testTerminateDatagramToMessage() {
		byte type = Helper.TERMINATE;

		byte[] input = new byte[] { type };

		DatagramPacket datagram = new DatagramPacket(input, input.length, null, 1);

		Message actual = Terminate.datagramToMessage(datagram.getData());

		assertEquals(Terminate.class, actual.getClass());
		// Dont need to test equivalence because empty constructor and no fields
	}
}
