package de.haw.ttv2.main;

import de.uniba.wiai.lspi.chord.com.Broadcast;
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
		GUIMessageQueue.getInstance().addMessage("ID: " + target + "\n");
	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		GUIMessageQueue.getInstance().addMessage("Broadcast from: " + source.toString() + "\nto: " + target.toString() + "\nhit: "
				+ hit.toString() + "\n");
	}

	@Override
	public void broadcast(Broadcast bc) {
//		MainGUI.getInstance().outputTextArea.appendText("Broadcast from: " + bc.getSource().toString() + "\nto: " + bc.getTarget().toString() + "\nhit: "
//				+ bc.getHit().toString() + "\n");
	}

}
