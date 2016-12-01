package de.haw.ttv2.main;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class ShootingThread implements Runnable{
	
	private ChordImpl chord;
	private ID target;
	
	public ShootingThread(ChordImpl chord, ID target) {
		this.chord = chord;
		this.target = target;
	}

	@Override
	public void run() {
		chord.retrieve(target);
		
	}
}