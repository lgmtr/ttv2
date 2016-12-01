package de.haw.ttv2.main;

import java.math.BigInteger;

import de.uniba.wiai.lspi.chord.com.Broadcast;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class GameState implements NotifyCallback {

	private ChordImpl chordImpl;
	
	public static final ID MAXID = ID.valueOf(((BigInteger.valueOf(2).pow(160)).subtract(BigInteger.valueOf(1))));
	
	public static final int SECTOR_COUNT = 100;
	
	public static final int SHIP_COUNT = 10;
	
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
