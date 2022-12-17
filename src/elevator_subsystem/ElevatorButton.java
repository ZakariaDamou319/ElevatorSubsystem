/**
 * 
 */
package elevator_subsystem;

/**
 * The elevator button inside the elevator associated with a floor number and has a lamp. 
 * @author sarahjaber
 *
 */

public class ElevatorButton {
	
	//true of the lamp of the button is lit and false otherwise 
	private boolean isLit; 
	
	//the floor number the button is associated with 
	private int num; 
	
	/**
	 * Create a new ElevatorButton and set the number the elevator button is associated with 
	 * @param num of the floor the elevator is associated with 
	 */
	public ElevatorButton(int num) {
		this.num=num; 
	}
	
	
	/**
	 * turn the lamp light of the button on
	 */
	public void lightOn() {
		isLit = true; 
	}
	
	/**
	 * turn the lamp light of the button off
	 */
	public void lightOff() {
		isLit=false; 
	}
	
	/**
	 * getter of the button number 
	 * @return number of the button 
	 */
	public int getButtonNumber() {
		return num; 
	}
	
	/**
	 * Check if lamp is lit
	 * @return true if lit, false otherwise
	 */
	public boolean isLit() {
		return isLit; 
	}

}
