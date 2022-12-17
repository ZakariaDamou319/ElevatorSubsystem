package messages;

import java.nio.ByteBuffer;

import common_classes.Helper;
import elevator_subsystem.DoorState;
import elevator_subsystem.MotorState;
import elevator_subsystem.Status;
/**
 * The elevator message contains the state of the elevator 
 * @author Rahel
 */
public class ElevatorMessage extends Message {

	// the id of the elevator
	private int id;

	// motor of the elevator
	private MotorState motorState;

	// the floor position of the elevator
	private int floor;

	// the state of the elevator doors
	private DoorState doorState;

	// the position of the elevator
	private Status status;

	/**
	 * Make a new elevator message
	 *
	 * @param id         the id of the elevator
	 * @param floor      the current floor of the elevator
	 * @param motorState the motorstate of the elevator
	 * @param doorState  the doorstate of the elevator
	 * @param status     the status of the elevator
	 */
	public ElevatorMessage(int id, int floor, MotorState motorState, DoorState doorState, Status status) {
		super(Helper.ELEVATOR_STATE_MESSAGE);
		this.id = id;
		this.floor = floor;
		this.motorState = motorState;
		this.doorState = doorState;
		this.status = status;
	}



	/* Special getters */

	/**
	 * returns a string representation of the difference between this elevator state
	 * and the passed in elevator state
	 *
	 * @param the current elevator state
	 */
	public String getChange(ElevatorMessage es) {
		if (es.status == Status.BROKEN)
			return "BROKEN at floor " + es.floor;
		
		if (motorState != es.motorState) {
			if (es.status == Status.APPROACHING)
				return "Moving " + es.motorState + " and " + es.status + " floor " + es.floor;

			if (es.motorState == MotorState.STOPPED)
				return es.motorState + " at floor " + es.floor;

			return "Moving " + es.motorState + " and " + es.status + " at floor " + es.floor;
		}

		if (doorState != es.doorState)
			return "Doors are now " + es.doorState;

		if (es.status == Status.APPROACHING) {
			return es.status + " floor " + es.floor;
		}
		return es.status + " at floor " + es.floor;
	}

	/**
	 * checks to see if the elevator is moving or not
	 *
	 * @return true if the elevator has stopped, false otherwise
	 */
	public boolean hasStopped() {
		return motorState == MotorState.STOPPED;
	}

	/**
	 * The string representation of the attributes
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return "id:" + id + " | motor:" + motorState + " | doors:" + doorState + " | " + status + " | floor:" + floor;
	}

	/**
	 * Compares this object to another object
	 *
	 * @return true if the id, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ElevatorMessage))
			return false;
		ElevatorMessage em = (ElevatorMessage) o;
		return em.id == id && em.motorState == motorState && em.doorState == doorState && em.status == status
				&& em.floor == floor;
	}

	/**
	 * Converts the ElevatorMessage to a byte array.
	 *
	 * @return the converted byte array.
	 */
	@Override
	public byte[] getData() {
		ByteBuffer pp = ByteBuffer.allocate(12);
		pp.put(Helper.ELEVATOR_STATE_MESSAGE);
		pp.putInt(id);
		pp.putInt(floor);
		pp.put(motorState.getByte());
		pp.put(doorState.getByte());
		pp.put(status.getByte());

		return pp.array();
	}

	/**
	 * This converts a datagram to a ElevatorMessage
	 *
	 * @param datagram the datagram to be converted.
	 * @return the converted message.
	 */
	public static Message datagramToMessage(byte[] data) {
		ByteBuffer pp = ByteBuffer.wrap(data);
		pp.get(); //header
		int id = pp.getInt();
		int floor = pp.getInt();
		MotorState ms = MotorState.get(pp.get());
		DoorState ds = DoorState.get(pp.get());
		Status s = Status.get(pp.get());

		return new ElevatorMessage(id, floor, ms, ds, s);
	}
	
	/* Getters & Setters */
	public int getId() {
		return id;
	}

	public int getFloor() {
		return floor;
	}


	public DoorState getDoorState() {
		return doorState;
	}

	public MotorState getMotorState() {
		return motorState;
	}


	public Status getStatus() {
		return status;
	}


	public void setStatus(Status status) {
		this.status = status;
	}

}
