package de.haw.ttv2.main;

import java.util.HashSet;
import java.util.Set;

import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class JoiningThread implements Runnable{
	
	private boolean running = true;
	
	private ChordImpl chord;
	
	private int playerCount = 0;

	public JoiningThread(ChordImpl chord) {
		this.chord = chord;
	}

	@Override
	public void run() {
		while(running){
			Set<Node> fingerSet = new HashSet<>(chord.getFingerTable());
			if(playerCount != fingerSet.size()){
				playerCount = fingerSet.size();
				System.out.println(playerCount);
//				MainGUI.getInstance().outputTextArea.appendText("New Player Joined. New playercount is " + playerCount + "\n");
			}
			waitTime(500);
		}
		
	}
	
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