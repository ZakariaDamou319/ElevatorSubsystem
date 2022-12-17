package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import common_classes.Subsystem;
import elevator_subsystem.DoorState;
import elevator_subsystem.MotorState;
import elevator_subsystem.Status;
import messages.ElevatorMessage;
import messages.FloorRequest;
import messages.Message;
import messages.Register;
import messages.RequestListMessage;
import messages.Response;
import messages.Terminate;

public class SubsystemTest {

	Subsystem s = new Subsystem() {
	};

	public SubsystemTest() {
	}

	@Test
	public void testSocketTimeout() {
		long start = System.currentTimeMillis();
		assertNull(s.rpcSendAndReceive(new Message(0) {
			@Override
			public byte[] getData() {
				return new byte[] { 0, 0, 0, 0 };
			}
		}));
		long end = System.currentTimeMillis();

		assertEquals(3 * 5000, end - start, 500);
	}

	@Test
	public void testUpdateSocketTimeout() {

		int timeout = 2000;

		s.updateSocketTimeout(timeout);

		long start = System.currentTimeMillis();
		assertNull(s.rpcSendAndReceive(new Message(0) {

			@Override
			public byte[] getData() {
				return new byte[] { 0, 0, 0, 0 };
			}
		}));
		long end = System.currentTimeMillis();

		assertEquals(3 * timeout, end - start, 500);
	}

	@Test
	public void testDatagramToElevatorMessage() {

		ElevatorMessage msg = new ElevatorMessage(-1, -1, MotorState.STOPPED, DoorState.OPEN, Status.BROKEN);

		Message result = null;
		try {
			result = s.datagramToMessage(
					new DatagramPacket(msg.getData(), msg.getData().length, InetAddress.getLocalHost(), 42));
		} catch (Exception e) {
			assertTrue(false);
		}

		assertNotNull(result);
		assertEquals(result.getClass(), msg.getClass());
	}

	@Test
	public void testDatagramToFloorRequestMessage() {

		FloorRequest msg = new FloorRequest(-1, -1, -1);

		Message result = null;
		try {
			result = s.datagramToMessage(
					new DatagramPacket(msg.getData(), msg.getData().length, InetAddress.getLocalHost(), 42));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}

		assertNotNull(result);
		assertEquals(result.getClass(), msg.getClass());
	}

	@Test
	public void testDatagramToEmptyReplyMessage() {

		Response msg = new Response();

		Message result = null;
		try {
			result = s.datagramToMessage(
					new DatagramPacket(msg.getData(), msg.getData().length, InetAddress.getLocalHost(), 42));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}

		assertNotNull(result);
		assertEquals(result.getClass(), msg.getClass());
	}

	@Test
	public void testDatagramToRegisterMessage() {

		Register msg = new Register(-1, -1);

		Message result = null;
		try {
			result = s.datagramToMessage(
					new DatagramPacket(msg.getData(), msg.getData().length, InetAddress.getLocalHost(), 42));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}

		assertNotNull(result);
		assertEquals(result.getClass(), msg.getClass());
	}

	@Test
	public void testDatagramToTerminateMessage() {

		Terminate msg = new Terminate();

		Message result = null;
		try {
			result = s.datagramToMessage(
					new DatagramPacket(msg.getData(), msg.getData().length, InetAddress.getLocalHost(), 42));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}

		assertNotNull(result);
		assertEquals(result.getClass(), msg.getClass());
	}

	@Test
	public void testDatagramToRequestListMessage() {

		RequestListMessage msg = new RequestListMessage(new ArrayList<FloorRequest>());

		Message result = null;
		try {
			result = s.datagramToMessage(
					new DatagramPacket(msg.getData(), msg.getData().length, InetAddress.getLocalHost(), 42));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}

		assertNotNull(result);
		assertEquals(result.getClass(), msg.getClass());
	}
}
