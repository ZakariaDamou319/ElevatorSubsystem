package messages;

import java.nio.ByteBuffer;

import common_classes.Helper;

public class FloorRequest extends Message implements Comparable {

	private int timestamp;
	private int sourceFloor;
	private int destinationFloor;

	public FloorRequest() {
		super(Helper.FLOOR_REQUEST_MESSAGE);
	}

	/**
	 * Constructs a message with the specified parameters
	 *
	 * @param time the timestamp of the message
	 * @param src  the source floor of the passenger
	 * @param dest the destination floor of the passenger
	 */
	public FloorRequest(int time, int src, int dest) {
		super(Helper.FLOOR_REQUEST_MESSAGE);
		timestamp = time;
		sourceFloor = src;
		destinationFloor = dest;
	}

	/**
	 * The string representation of the attributes
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return sourceFloor + "->" + destinationFloor;
	}

	/**
	 * the button the passenger pressed (up or down request button)
	 *
	 * @return
	 */
	public String buttonPress() {
		String direction = sourceFloor > destinationFloor ? "DOWN" : "UP";
		return direction;
	}

	/**
	 * Checks to see if the objects are equal
	 *
	 * @return true if the objects are equal, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		FloorRequest fr = (FloorRequest) obj;
		return this.destinationFloor == fr.destinationFloor && this.sourceFloor == fr.sourceFloor
				&& this.timestamp == fr.timestamp;
	}

	/**
	 * Converts the FloorRequest to a byte array.
	 *
	 * @return the converted byte array.
	 */
	@Override
	public byte[] getData() {
		ByteBuffer qq = ByteBuffer.allocate(7);
		qq.put(Helper.FLOOR_REQUEST_MESSAGE);
		qq.putInt(timestamp);
		qq.put((byte) sourceFloor);
		qq.put((byte) destinationFloor);

		return qq.array();
	}

	/**
	 * This converts a datagram to a FloorRequest
	 *
	 * @param datagram the datagram to be converted.
	 * @return the converted message.
	 */
	public static Message datagramToMessage(byte[] data) {
		ByteBuffer qq = ByteBuffer.wrap(data);
		qq.get();
		int time = qq.getInt();
		int source = qq.get();
		int dest = qq.get();

		return new FloorRequest(time, source, dest);
	}

	/* Getters & Setters */
	public void setSourceFloor(int sourceFloor) {
		this.sourceFloor = sourceFloor;
	}

	public void setDestinationFloor(int destinationFloor) {
		this.destinationFloor = destinationFloor;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public int getSourceFloor() {
		return sourceFloor;
	}

	public int getDestinationFloor() {
		return destinationFloor;
	}
	
	
	public boolean isGoingUp() {
		return destinationFloor > sourceFloor;
	}

	@Override
	public int compareTo(Object arg0) {
		if (!(arg0 instanceof FloorRequest)) {
			System.out.println("Ruh Roh");
			throw new IllegalArgumentException();
		}

		return timestamp - ((FloorRequest) arg0).getTimestamp();
	}
}
