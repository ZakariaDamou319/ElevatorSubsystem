/**
 *
 */
package elevator_subsystem;

/**
 * The two states of the elevator door
 *
 * @author sarahjaber, Matthew Siu
 *
 */
public enum DoorState {

	OPEN, CLOSED;

	/**
	 * Gets the door state as a byte
	 * @return the byte
	 */
	public byte getByte() {
		return (byte) this.ordinal();
	}
	
	/**
	 * converts a byte into a door state
	 * @param b the byte
	 * @return the door state
	 */
	public static DoorState get(byte b) {
		return DoorState.values()[b];
	}
}
