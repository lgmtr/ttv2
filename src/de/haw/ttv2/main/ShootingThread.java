package de.haw.ttv2.main;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;

/**
 * Thread for shooting at a target
 * 
 * @author Johann Bronsch
 * @author Sascha Waltz
 */
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
                this.wait(50);
                chord.retrieve(target);
            } catch (ServiceException | InterruptedException e) {
                e.printStackTrace();
            }
        }
		
	}
}