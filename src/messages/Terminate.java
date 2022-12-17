package messages;

import java.io.ByteArrayOutputStream;

import common_classes.Helper;

public class Terminate extends Message {

	/**
	 * Default constructor
	 */
	public Terminate() {
		super(Helper.TERMINATE);
	}


	/**
	 * Converts the Response Message to a byte array.
	 *
	 * @return the converted byte array.
	 */
	@Override
	public byte[] getData() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(Helper.TERMINATE);
		return outputStream.toByteArray();
	}

	/**
	 * This converts a datagram to a Response Message
	 *
	 * @param datagram the datagram to be converted.
	 * @return the converted message.
	 */
	public static Message datagramToMessage(byte[] data) {
		return new Terminate();
	}
	
	/**
	 * Checks to see if the objects are equal
	 *
	 * @return true if the objects are equal, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		return true;
	}

}
