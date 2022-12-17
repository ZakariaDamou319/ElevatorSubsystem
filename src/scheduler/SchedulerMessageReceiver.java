package scheduler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import common_classes.Helper;
import common_classes.Subsystem;
import messages.Message;

public class SchedulerMessageReceiver extends Subsystem implements Runnable {
	public SchedulerMessageReceiver() {
		super(Helper.SCHEDULER_PORT);
	}

	@Override
	public void run() {
		while (true) {
			Message message;
			try {
				message = receive();
				if (message != null)
					putMessage(message);
			} catch (IOException e) {
				break;
			}
		}
		System.out.println("SCHEDULER RECEIVER: Terminated.");
	}

	/* Handles communication with Scheduler */
	private List<Message> messages = new LinkedList<Message>();

	public synchronized void putMessage(Message message) {
		messages.add(message);
		notifyAll();
	}
	
	public synchronized List<Message> getMessages() {
		while (messages.isEmpty()) {
			try {
					wait();
			} catch (InterruptedException e) {
				return null;
			}
		}

		List<Message> messages = new LinkedList<>(this.messages);
		this.messages.clear();
		return messages;
	}
}
