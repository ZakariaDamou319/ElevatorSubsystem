package common_classes;

/**
 * This class contains some of the timing configuration constants used 
 * throughout the project. 
 *
 */
public class Config {
	public static int TIME_TO_ACCELERATE = 1000; // 1s
	public static int TIME_TO_DECELERATE_TO_STOP = 1000; // 1s

	// constant speed
	public static int TIME_TO_ARRIVE_AT_FLOOR = 250; // 0.25s
	public static int TIME_TO_TRAVEL_BETWEEN_FLOORS = 4750; // 4.75s*0.8s=3.8
	public static int TIME_TO_APPROACH_FLOOR = 5000; // 1s for accel + 4s for constant speed

	public static int TIME_TO_OPEN_DOORS = 2000; // 2s
	public static int TIME_TO_CLOSE_DOORS = 2000; // 2s

	public static int MINIMUM_ELEVATOR_BREAK_TIME = 80000;
	public static int MAXIMUM_ELEVATOR_BREAK_TIME_SUBTRACTING_MINIMUM = 190000;

}
