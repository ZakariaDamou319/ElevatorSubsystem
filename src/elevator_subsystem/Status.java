package elevator_subsystem;

/**
 * The status of the elevator
 *
 * @author Rahel
 *
 */
public enum Status {
	APPROACHING, // the elevator is approaching a floor
	ARRIVED, // the elevator has arrived at a floor
	BROKEN;

	/**
	 * Gets the status as a byte
	 *
	 * @return the byte
	 */
	public byte getByte() {
		return (byte) this.ordinal();
	}

	/**
	 * converts a byte into a status
	 *
	 * @param b the byte
	 * @return the Status
	 */
	public static Status get(byte b) {
		return Status.values()[b];
	}
}
