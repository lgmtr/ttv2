package de.haw.ttv2.main;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class GameState implements NotifyCallback {

	private ChordImpl chordImpl;

	public static final ID MAXID = ID.valueOf(((BigInteger.valueOf(2).pow(160)).subtract(BigInteger.valueOf(1))));

	private static final String WIN_LOSE_SEPERATOR = "\n==============================================================================\n";

	public static final int SECTOR_COUNT = 100;

	public static final int SHIP_COUNT = 10;

	private List<Player> playerList;

	private Player ownPlayer;

	private boolean someoneLose = false;

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
		final Boolean handleHit = handleHit(target);
		chordImpl.broadcast(target, handleHit);
		if (handleHit)
			GUIMessageQueue.getInstance().addMessage("The Shoot on the ID: " + target + " was a hit!");
		if (ownPlayer.getRemainingShips() > 0) {
			shoot();
		} else {
			GUIMessageQueue.getInstance().addMessage(WIN_LOSE_SEPERATOR + "I lose!!! Game Over!!!" + WIN_LOSE_SEPERATOR);
			someoneLose = true;
		}
	}

	private Boolean handleHit(ID target) {
		return ownPlayer.shipHitted(target);
	}

	@Override
	public void broadcast(ID source, ID target, Boolean hit) {
		Player shootedPlayer = null;
		findPlayer: for (Player player : playerList) {
			if (player.getPlayerID().compareTo(source) == 0) {
				shootedPlayer = player;
				break findPlayer;
			}
		}
		if (shootedPlayer != null) {
			int hittedSector = shootedPlayer.shootInIntervalOfPlayer(target);
			shootedPlayer.setHittedSector(hittedSector, hit);
			if (hit) {
				if (source.compareTo(chordImpl.getID()) == 0)
					GUIMessageQueue.getInstance().addMessage("I got a hit");
				else
					GUIMessageQueue.getInstance().addMessage("Player with ID: " + source.toString() + " got a hit");
			}
			if (shootedPlayer.getRemainingShips() < 1) {
				GUIMessageQueue.getInstance().addMessage(WIN_LOSE_SEPERATOR + "Player with ID: " + source.toString() + " lose!!" + WIN_LOSE_SEPERATOR);
				someoneLose = true;
			}
		} else
			GUIMessageQueue.getInstance().addMessage("Something went wrong with incomming Broadcast!");
	}

	public void startGame() {
		if (chordImpl.getPredecessorID().compareTo(chordImpl.getID()) > 0) {
			GUIMessageQueue.getInstance().addMessage("I Start!");
			shoot();
		}
	}

	private void shoot() {
		if (playerList.size() > 0) {
			if (!someoneLose) {
				Player target = null;
				int remainingShips = SHIP_COUNT + 1;
				for (Player player : playerList) {
					if (player.getRemainingShips() < remainingShips) {
						target = player;
						remainingShips = player.getRemainingShips();
					}
				}
				Sector targetSector = target.findFreeSector();
				ShootingThread st = new ShootingThread(chordImpl, targetSector.getMiddle());
				st.start();
			}
		}
	}

	protected static int randBetween(int min, int max) {
		Random rand = new Random();
		return rand.nextInt(max - (min + 1)) + min;
	}

	public void createGamefield() {
		if (playerList == null)
			playerList = new ArrayList<>();
		if (playerList.size() < 1) {
			createGameField(chordImpl.getID());
			Collections.sort(playerList);
			playerList.remove(ownPlayer);
			ownPlayer.setShips();
			GUIMessageQueue.getInstance().addMessage("Field Created!");
		}
	}

	public Player getOwnPlayer() {
		return ownPlayer;
	}

}
