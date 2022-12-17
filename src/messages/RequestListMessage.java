package messages;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import common_classes.Helper;

public class RequestListMessage extends Message {
	// List of requests handled by the elevator. The Key is the source floor. The
	// value is the destination floor
	private List<FloorRequest> requestList;

	public RequestListMessage(FloorRequest request) {
		super(Helper.REQUEST_LIST_MESSAGE);
		requestList = new ArrayList<FloorRequest>();
		requestList.add(request);
	}
	
	
	public RequestListMessage(List<FloorRequest> requests) {
		super(Helper.REQUEST_LIST_MESSAGE);
		requestList = requests;
	}

	/**
	 * Converts the FloorRequest to a byte array.
	 *
	 * @return the converted byte array.
	 */
	@Override
	public byte[] getData() {
		ByteBuffer zz = ByteBuffer.allocate(1 + requestList.size() * 2);
		zz.put(Helper.REQUEST_LIST_MESSAGE);
		for(FloorRequest fr : requestList) {
			zz.put((byte) fr.getSourceFloor());
			zz.put((byte) fr.getDestinationFloor());
		}
		return zz.array();
	}

	/**
	 * This converts a datagram to a FloorRequest
	 *
	 * @param datagram the datagram to be converted.
	 * @return the converted message.
	 */
	public static Message datagramToMessage(byte[] data) {
		ByteBuffer zz = ByteBuffer.wrap(data);
		zz.get();

		List<FloorRequest> requests = new ArrayList<FloorRequest>();
		while (zz.hasRemaining()) {
			FloorRequest fr = new FloorRequest(0, zz.get(), zz.get());
			requests.add(fr);
		}
		
		return new RequestListMessage(requests);
	}

	/**
	 * Returns a string representation of the list
	 */
	@Override
	public String toString() {
		return requestList.toString();
	}
	
	/**
	 * Checks to see if the objects are equal
	 *
	 * @return true if the objects are equal, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		RequestListMessage r = (RequestListMessage) obj;
		return r.requestList.equals(requestList);
	}
	
	/* Getters & Setters */
	public List<FloorRequest> getRequestList() {
		return requestList;
	}

	public void setRequestList(List<FloorRequest> requestList) {
		this.requestList = requestList;
	}
}
