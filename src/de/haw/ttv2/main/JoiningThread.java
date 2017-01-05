package de.haw.ttv2.main;

import java.util.HashSet;
import java.util.Set;

import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

/**
 * Handles the Set of joined players
 * 
 * @author Johann Bronsch
 * @author Sascha Waltz
 */
public class JoiningThread implements Runnable{
	
	private boolean running = true;
	
	private ChordImpl chord;
	
	private int playerCount = 0;

	public JoiningThread(ChordImpl chord) {
		this.chord = chord;
	}

	/* 
	 * Handles the Set for joining players
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while(running){
			Set<Node> fingerSet = new HashSet<>(chord.getFingerTable());
			if(playerCount != fingerSet.size()){
				playerCount = fingerSet.size();
				GUIMessageQueue.getInstance().addMessage("Joined Player Count: " + playerCount);
			}
			waitTime(500);
		}
		
	}
	
	/**
	 * Put the Thread to sleep for a specified amount of time
	 * 
	 * @param time
	 */
	private void waitTime(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public int getPlayerCount(){
		return playerCount;
	}
	
	public void stop(){
		running = false;
	}
}