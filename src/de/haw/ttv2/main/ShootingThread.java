package de.haw.ttv2.main;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;

public class ShootingThread extends Thread {
	
	private Chord chord;
	private ID target;
	
	public ShootingThread(Chord chord, ID target) {
		this.chord = chord;
		this.target = target;
	}

	@Override
	public void run() {
		synchronized (this) {
            try {
                this.wait(300);
                chord.retrieve(target);
            } catch (ServiceException | InterruptedException e) {
                e.printStackTrace();
            }
        }
		
	}
}