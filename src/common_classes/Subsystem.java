package common_classes;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import messages.ElevatorMessage;
import messages.FloorRequest;
import messages.Message;
import messages.Register;
import messages.RequestListMessage;
import messages.Response;
import messages.Terminate;

public abstract class Subsystem {

	protected DatagramSocket receiveSocket;
	private DatagramSocket sendReceiveSocket;

	/**
	 * Default constructor for subsystem not hosted on a particular port
	 */
	public Subsystem() {
		try {
			receiveSocket = new DatagramSocket();
			sendReceiveSocket = receiveSocket;
			receiveSocket.setSoTimeout(5000);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor for subsystem hosted on a port
	 */
	public Subsystem(int port) {
		try {
			receiveSocket = new DatagramSocket(port);
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("Port already in use.");
		}
	}

	/**
	 * Updates the socket time out
	 *
	 * @param timeout
	 */
	public void updateSocketTimeout(int timeout) {
		try {
			receiveSocket.setSoTimeout(timeout);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends the messsage to a port
	 *
	 * @param message
	 * @param port
	 */
	protected void send(Message message, int port) {
		byte[] bytes = message.getData();
		try {
			sendReceiveSocket.send(new DatagramPacket(bytes, bytes.length, InetAddress.getLocalHost(), port));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Receives a message
	 *
	 * @return the message
	 * @throws IOException socket timeout
	 */
	protected Message receive() throws IOException {

		byte[] data = new byte[100];
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);

		receiveSocket.receive(receivePacket);
		return datagramToMessage(receivePacket);
	}

	/**
	 * Sends and receive a message to the scheduler
	 *
	 * @param message
	 * @return the response
	 */
	public Message rpcSendAndReceive(Message message) {
		int tries = 0;
		int maxTries = 3;
		while (tries++ < maxTries) {
			send(message, Helper.SCHEDULER_PORT);
			try {
				return receive();
			} catch (SocketTimeoutException e) {

			} catch (Exception e) {
				System.out.println("Socket has closed.");
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	/**
	 * Converts bytes to message
	 *
	 * @param datagram
	 * @return the message
	 */
	public Message datagramToMessage(DatagramPacket datagram) {
		Message message = null;

		byte[] data = Arrays.copyOf(datagram.getData(), datagram.getLength());

		switch (data[0]) {
		case Helper.ELEVATOR_STATE_MESSAGE:
			message = ElevatorMessage.datagramToMessage(data);
			break;
		case Helper.FLOOR_REQUEST_MESSAGE:
			message = FloorRequest.datagramToMessage(data);
			break;
		case Helper.EMPTY_REPLY_MESSAGE:
			message = Response.datagramToMessage(data);
			break;
		case Helper.REGISTER_MESSAGE:
			message = Register.datagramToMessage(data);
			break;
		case Helper.TERMINATE:
			message = Terminate.datagramToMessage(data);
			break;
		case Helper.REQUEST_LIST_MESSAGE:
			message = RequestListMessage.datagramToMessage(data);
			break;
		}
		return message;
	}

	/**
	 * Close the sockets with a fixed port
	 */
	public void closeSockets() {
		receiveSocket.close();
	}
}
