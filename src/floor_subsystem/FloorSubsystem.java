package floor_subsystem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import common_classes.Helper;
import common_classes.Subsystem;
import messages.FloorRequest;
import messages.Terminate;

/**
 * The FloorSubsystem is able to be run by a thread.
 *
 * It is responsible for sending and receiving messages including elevator
 * passenger information.
 *
 * @author Matthew Siu
 *
 */
public class FloorSubsystem extends Subsystem {

	String path;

	/**
	 * Constructor for FloorSubsystem.
	 *
	 * @param scheduler The Scheduler messages will be passed to.
	 */
	public FloorSubsystem(String pathToRequestFile) {
		super(Helper.FLOOR_PORT);

		if (pathToRequestFile.isEmpty())
			path = "Resources/requestDocument.txt";
		else
			path = pathToRequestFile;
	}

	/**
	 * When FloorSubsystem is used to create a thread, starting the thread will
	 * execute this method.
	 *
	 * @throws Exception
	 */
	public void run() {
		Thread.currentThread().setName("FLOOR SUBSYSTEM");
		System.out.println("FLOOR SUBSYSTEM: STARTED");
		ArrayList<FloorRequest> floorReqs = getFloorRequests(path);
		int prevTime = -1;
		for (FloorRequest fr : floorReqs) {
			if (prevTime == -1) {
				prevTime = fr.getTimestamp();
			} else {
				int duration = fr.getTimestamp() - prevTime;
				Helper.sleep(duration);
				prevTime = fr.getTimestamp();
			}

			if (rpcSendAndReceive(fr) == null)
				break;
			System.out.println("FLOOR SUBSYSTEM: sent Floor Request: " + fr);
		}

		rpcSendAndReceive(new Terminate());
		closeSockets();
		System.out.println("FLOOR SUBSYSTEM: TERMINATED");
	}

	/**
	 * Reads formatted passenger information from a text file and returns the
	 * information as a list of Messages.
	 *
	 * @param fileName the filename of the text file.
	 * @return A list of Messages containing passenger information.
	 * @throws IOException is thrown if there is an error reading the file.
	 */
	public ArrayList<FloorRequest> getFloorRequests(String fileName) {
		final int TIME = 0;
		final int FLOOR_NUMBER = 1;
		final int CAR_BUTTON = 3;

		ArrayList<FloorRequest> floorReqs = new ArrayList<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = reader.readLine();
			while (line != null) {
				String[] splitLine = line.split(" ");

				floorReqs.add(new FloorRequest(Helper.timeStringToMilliseconds(splitLine[TIME]),
						Integer.parseInt(splitLine[FLOOR_NUMBER]), Integer.parseInt(splitLine[CAR_BUTTON])));
				line = reader.readLine();
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return floorReqs;
	}

}
