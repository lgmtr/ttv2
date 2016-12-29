package de.haw.ttv2.main;

import java.util.ArrayList;
import java.util.List;

public class GUIMessageQueue {

	private static GUIMessageQueue instance = null;

	private List<String> message;

	public static synchronized GUIMessageQueue getInstance() {
		if (instance == null) {
			instance = new GUIMessageQueue();
			instance.message = new ArrayList<String>();
		}
		return instance;
	}

	/*
	 * Returns the first Message in the queue
	 */
	public String getFirstMessage() {
		String returnString = null;
		if (message.size() > 0) {
			returnString = message.get(0);
			message.remove(0);
		}
		return returnString;
	}
	
	/*
	 * Adds a message to the queue
	 */
	public void addMessage(String message) {
		this.message.add(this.message.size(), message);
	}

}
