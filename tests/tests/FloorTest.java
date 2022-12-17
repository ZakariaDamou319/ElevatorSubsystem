package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import common_classes.Helper;
import floor_subsystem.FloorSubsystem;
import messages.FloorRequest;

public class FloorTest {
	FloorSubsystem floorSubsystem;

	@BeforeEach
	void setUp() {
		floorSubsystem = new FloorSubsystem("");
	}

	/**
	 * Verifies that the floor reads the file correctly to fill out that passenger
	 * list.
	 */
	@Test
	void floorReadsFileTest() throws IOException {
		List<FloorRequest> actResult = floorSubsystem.getFloorRequests("Resources/passengersTest.txt");
		List<FloorRequest> expResult = new ArrayList<FloorRequest>();
		expResult.add(new FloorRequest(Helper.timeStringToMilliseconds("14:05:15.0"), 2, 4));
		expResult.add(new FloorRequest(Helper.timeStringToMilliseconds("14:05:15.24"), 5, 1));

		assertEquals(expResult, actResult);
	}
	
	
	
}
