package messages;

import java.nio.ByteBuffer;

import common_classes.Helper;

public class Register extends Message {
	private int portNumber;
	private int id;

	/**
	 * The register message holds a port and an integer
	 * @param id the id of the elevator
	 * @param port the port that the elevator is hosted on
	 */
	public Register(int id, int port) {
		super(Helper.REGISTER_MESSAGE);
		this.id = id;
		this.portNumber = port;
	}

	/**
	 * Gets the port
	 * @return portNumber
	 */
	public int getPort() {
		return portNumber;
	}

	/**
	 * Converts the Register Message to a byte array.
	 *
	 * @return the converted byte array.
	 */
	@Override
	public byte[] getData() {
		ByteBuffer bb = ByteBuffer.allocate(9);
		bb.put(Helper.REGISTER_MESSAGE);
		bb.putInt(id);
		bb.putInt(portNumber);
		return bb.array();
	}

	/**
	 * This converts a datagram to a Register Message
	 *
	 * @param datagram the datagram to be converted.
	 * @return the converted message.
	 */
	public static Message datagramToMessage(byte[] data) {
		ByteBuffer bb = ByteBuffer.wrap(data);

		bb.get();
		int id = bb.getInt();
		int port = bb.getInt();

		return new Register(id, port);
	}

	/**
	 * Gets the id
	 * @return id
	 */
	public int getID() {
		return id;
	}

	/**
	 * Checks to see if the objects are equal
	 *
	 * @return true if the objects are equal, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Register))
			return false;

		Register r = (Register) obj;
		return r.id == id && r.portNumber == portNumber;
	}

	@Override
	public String toString() {
		return "REGISTER: " + id + " " + portNumber;

	}

}
