package common_classes;

public class Helper {
	
	public final static byte REQUEST_LIST_MESSAGE = 1;
	public final static byte ELEVATOR_STATE_MESSAGE = 2;
	public final static byte FLOOR_REQUEST_MESSAGE = 3;
	public final static byte EMPTY_REPLY_MESSAGE = 4;
	public final static byte REGISTER_MESSAGE = 5;
	public final static byte TERMINATE = 6;
	
	public final static int LENGTH_OF_HEADER = 1;

	//the ports that the scheduler and floor is hosted on
	public final static int SCHEDULER_PORT = 50;
	public final static int FLOOR_PORT = 60;

	/**
	 * Helper method for converting the timestamp String into an integer value
	 *
	 * @param timestamp the String to be converted.
	 * @return The timestamp converted to milliseconds.
	 */
	public static int timeStringToMilliseconds(String timestamp) {
		final int HOURS = 0;
		final int MINUTES = 1;
		final int SECONDS_AND_MILLISECONDS = 2;
		final int HOURS_TO_MILLISECONDS = 3600000;
		final int MINUTES_TO_MILLISECONDS = 60000;
		final int SECONDS_TO_MILLISECONDS = 1000;

		String[] time = timestamp.split(":");
		int hoursInMilliseconds = Integer.parseInt(time[HOURS]) * HOURS_TO_MILLISECONDS;
		int minutesInMilliseconds = Integer.parseInt(time[MINUTES]) * MINUTES_TO_MILLISECONDS;
		int secondsAndMilliseconds = (int) (Float.parseFloat(time[SECONDS_AND_MILLISECONDS]) * SECONDS_TO_MILLISECONDS);

		return hoursInMilliseconds + minutesInMilliseconds + secondsAndMilliseconds;
	}

	/**
	 * Simulates waiting a specified amount of time
	 *
	 * @param duration the time to sleep for
	 */
	public static void sleep(int duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
