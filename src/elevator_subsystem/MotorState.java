/**
 *
 */
package elevator_subsystem;

/**
 * The three states of the motor
 *
 * @author sarahjaber
 *
 */

public enum MotorState {
	UP, DOWN, STOPPED;

	/**
	 * Gets the motorstate as a byte
	 * @return
	 */
	public byte getByte() {
		return (byte) this.ordinal();
	}
	
	/**
	 * gets the motorstate associated with a byte
	 * @param b the byte
	 * @return motorstate
	 */
	public static MotorState get(byte b) {
		return MotorState.values()[b];
	}
}
