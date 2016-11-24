package de.haw.ttv2.main;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class GameState implements NotifyCallback {

	private ChordImpl chordImpl;

	public GameState(ChordImpl chordImpl) {
		this.chordImpl = chordImpl;
	}

	@Override
	public void retrieved(ID target) {
		System.out.println("ID: " + target);
		//chordImpl.broadcast(target, true);
		//chordImpl.retrieve(target);
	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		System.out.println("Broadcast from: " + source.toString() + "\nto: " + target.toString() + "\nhit: "
				+ hit.toString());
	}

}
