package de.haw.ttv2.main;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.uniba.wiai.lspi.chord.com.Broadcast;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class GameState implements NotifyCallback {

	private ChordImpl chordImpl;

	public static final ID MAXID = ID.valueOf(((BigInteger.valueOf(2).pow(160)).subtract(BigInteger.valueOf(1))));

	public static final int SECTOR_COUNT = 100;

	public static final int SHIP_COUNT = 10;

	private List<Player> playerList;

	private Player ownPlayer;

	// TestCounter
	private int testCounter = 0;

	public GameState(ChordImpl chordImpl) {
		this.chordImpl = chordImpl;
	}

	public void createGameField(ID ownID) {
		Set<Node> fingerSet = new HashSet<>(chordImpl.getFingerTable());
		List<ID> playerIDList = new ArrayList<>();
		for (Node node : fingerSet)
			playerIDList.add(node.getNodeID());
		playerIDList.add(chordImpl.getID());
		Collections.sort(playerIDList);
		playerList = new ArrayList<>();
		for (int i = 0; i < playerIDList.size(); i++) {
			Player newPlayer;
			if (i == 0)
				newPlayer = new Player(playerIDList.get(i), GameState.SECTOR_COUNT, GameState.SHIP_COUNT, playerIDList.get(playerIDList.size() - 1),
						playerIDList.get(i));
			else
				newPlayer = new Player(playerIDList.get(i), GameState.SECTOR_COUNT, GameState.SHIP_COUNT, playerIDList.get(i - 1), playerIDList.get(i));
			if (newPlayer.getPlayerID().equals(ownID))
				ownPlayer = newPlayer;
			playerList.add(newPlayer);
		}
	}

	@Override
	public void retrieved(ID target) {
		System.out.println("retrieved");
		if (playerList.size() < 1) {
			createGameField(chordImpl.getID());
			Collections.sort(playerList);
			playerList.remove(ownPlayer);
		}
		GUIMessageQueue.getInstance().addMessage("ID: " + target + "\n");
		if (testCounter < 10) {
			shoot();
			testCounter++;
			System.out.println(testCounter);
		}
	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		GUIMessageQueue.getInstance().addMessage("Broadcast from: " + source.toString() + "\nto: " + target.toString() + "\nhit: " + hit.toString() + "\n");
	}

	@Override
	public void broadcast(Broadcast bc) {
		// MainGUI.getInstance().outputTextArea.appendText("Broadcast from: " +
		// bc.getSource().toString() + "\nto: " + bc.getTarget().toString() +
		// "\nhit: "
		// + bc.getHit().toString() + "\n");
	}

	// Test-Method
	protected List<Player> getPlayerList() {
		return playerList;
	}

	public void startGame() {
		createGameField(chordImpl.getID());
		Collections.sort(playerList);
		if (playerList.get(playerList.size() - 1).compareTo(ownPlayer) == 0) {
			shoot();
		}
		playerList.remove(ownPlayer);
	}

	private void shoot() {
		if (playerList.size() > 0) {
			Player target = playerList.get(randBetween(0, playerList.size()));
			if(target.getPlayerID().compareTo(chordImpl.getID()) == 0)
				System.out.println("Falsch");
			Sector targetSector = target.getPlayerFields()[randBetween(0, target.getPlayerFields().length)];
			ShootingThread st = new ShootingThread(chordImpl, targetSector.getMiddle());
			st.start();
		}
	}

	private int randBetween(int min, int max) {
		Random rand = new Random();
		return rand.nextInt(max - (min + 1)) + min;
	}

}
