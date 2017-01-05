package de.haw.ttv2.main;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.haw.ttv2.main.BroadcastLog.BroadcastMsg;
import de.uniba.wiai.lspi.chord.com.Broadcast;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.NotifyCallback;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

/**
 * Contains the mechanics of the game, GameField, shoot and handle of hits
 * 
 * @author Johann Bronsch
 * @author Sascha Waltz
 */
public class GameState implements NotifyCallback {

	private ChordImpl chordImpl;

	public static final ID MAXID = ID.valueOf(((BigInteger.valueOf(2).pow(160)).subtract(BigInteger.valueOf(1))));

	private static final String WIN_LOSE_SEPERATOR = "\n==============================================================================\n";

	private static final String MOD_WIN_LOSE_SEPERATOR = "==============================================================================\n";

	public static final int SECTOR_COUNT = 100;

	public static final int SHIP_COUNT = 10;

	private List<Player> playerList;

	private Player ownPlayer;

	private boolean someoneLose = false;

	private ID lastTarget;

	private boolean cheatMode = false;

	public GameState(ChordImpl chordImpl) {
		this.chordImpl = chordImpl;
	}

	/*
	 * Creates a GameField for a player
	 */
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
				newPlayer = new Player(playerIDList.get(i), GameState.SECTOR_COUNT, GameState.SHIP_COUNT,
						playerIDList.get(playerIDList.size() - 1), playerIDList.get(i));
			else
				newPlayer = new Player(playerIDList.get(i), GameState.SECTOR_COUNT, GameState.SHIP_COUNT,
						playerIDList.get(i - 1), playerIDList.get(i));
			if (newPlayer.getPlayerID().equals(ownID))
				ownPlayer = newPlayer;
			playerList.add(newPlayer);
		}
	}

	/*
	 * Recalculates the GameField if a unknown player joins the game
	 */
	private void recalculateGameField(ID newID) {
		Set<Node> fingerSet = new HashSet<>(chordImpl.getFingerTable());
		List<ID> playerIDList = new ArrayList<>();
		for (Node node : fingerSet)
			playerIDList.add(node.getNodeID());
		playerIDList.add(chordImpl.getID());
		if (!playerIDList.contains(newID))
			playerIDList.add(newID);
		Collections.sort(playerIDList);
		List<Player> newPlayerList = new ArrayList<>();
		for (int i = 0; i < playerIDList.size(); i++) {
			Player newPlayer;
			if (playerIDList.get(i).compareTo(chordImpl.getID()) == 0) {
				newPlayer = new Player(playerIDList.get(i), GameState.SECTOR_COUNT, ownPlayer.getRemainingShips(),
						i == 0 ? playerIDList.get(playerIDList.size() - 1) : playerIDList.get(i - 1),
						playerIDList.get(i), ownPlayer.getAttackedFields(), ownPlayer.getShipInField());
				ownPlayer = newPlayer;
			} else if (containsInOldList(playerIDList.get(i)) >= 0) {
				Player dummyPlayer = playerList.get(containsInOldList(playerIDList.get(i)));
				newPlayer = new Player(playerIDList.get(i), GameState.SECTOR_COUNT, dummyPlayer.getRemainingShips(),
						i == 0 ? playerIDList.get(playerIDList.size() - 1) : playerIDList.get(i - 1),
						playerIDList.get(i), dummyPlayer.getAttackedFields(), dummyPlayer.getShipInField());
			} else {
				newPlayer = new Player(playerIDList.get(i), GameState.SECTOR_COUNT, GameState.SHIP_COUNT,
						i == 0 ? playerIDList.get(playerIDList.size() - 1) : playerIDList.get(i - 1),
						playerIDList.get(i));
			}
			newPlayerList.add(newPlayer);
		}
		playerList.clear();
		playerList.addAll(newPlayerList);
		playerList.remove(ownPlayer);
		GUIMessageQueue.getInstance().addMessage("Unknown Player added!");
	}

	private int containsInOldList(ID id) {
		for (Player player : playerList)
			if (player.getPlayerID().compareTo(id) == 0)
				return playerList.indexOf(player);
		return -1;
	}

	/*
	 * Handles a retrieved hit on the player
	 * 
	 * (non-Javadoc)
	 * @see de.uniba.wiai.lspi.chord.service.NotifyCallback#retrieved(de.uniba.wiai.lspi.chord.data.ID)
	 */
	@Override
	public void retrieved(ID target) {
		final Boolean handleHit = handleHit(target, chordImpl.getLastReceivedTransactionID() + 1);
		chordImpl.broadcast(target, handleHit);
		if (handleHit) {
			GUIMessageQueue.getInstance().addMessage("The Shoot on the ID: " + target + " was a hit!");
		}
		if (ownPlayer.getRemainingShips() > 0) {
			shoot();
		} else {
			GUIMessageQueue.getInstance()
					.addMessage(WIN_LOSE_SEPERATOR + "I lose!!! Game Over!!!" + WIN_LOSE_SEPERATOR);
		}
	}

	/*
	 * Returns if a ship was hit
	 */
	private Boolean handleHit(ID target, int actualTransactionID) {
		if (cheatMode)
			return ownPlayer.cheatedShipHitted(target, actualTransactionID);
		return ownPlayer.shipHitted(target, actualTransactionID);
	}

	/**
	 * Handles an incoming broadcast, checks if a player has lost and recalculates the gamefield if an unknown player joins the game
	 * 
	 * @param source
	 * @param target
	 * @param hit
	 * @param transaction
	 */
	public void broadcast(ID source, ID target, Boolean hit, Integer transaction) {
		BroadcastLog.getInstance().addBroadcast(source, target, hit, transaction);
		BroadcastLog bcLog = BroadcastLog.getInstance();
		ID hasSomeLose = bcLog.hasSomeoneLost();
		BroadcastMsg lastBroadcastFromLooser = bcLog.getLastBroadcast(hasSomeLose);
		if (hasSomeLose != null && lastTarget.compareTo(lastBroadcastFromLooser.getTarget()) == 0) {
			GUIMessageQueue.getInstance().addMessage(
					WIN_LOSE_SEPERATOR + "Player with ID: " + hasSomeLose + " in Round: "
							+ lastBroadcastFromLooser.getTransaction() + " lost!!!");
			GUIMessageQueue.getInstance().addMessage("Last shot was from me!!!");
			someoneLose = true;
			GUIMessageQueue.getInstance().addMessage(MOD_WIN_LOSE_SEPERATOR);
		} else {
			Player shootedPlayer = null;
			findPlayer: for (Player player : playerList) {
				if (player.getPlayerID().compareTo(source) == 0) {
					shootedPlayer = player;
					break findPlayer;
				}
			}
			if (shootedPlayer != null) {
				handleShoot(source, target, hit, shootedPlayer);
			} else {
				recalculateGameField(source);
				findPlayer: for (Player player : playerList) {
					if (player.getPlayerID().compareTo(source) == 0) {
						shootedPlayer = player;
						break findPlayer;
					}
				}
				handleShoot(source, target, hit, shootedPlayer);
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.uniba.wiai.lspi.chord.service.NotifyCallback#broadcast(de.uniba.wiai.lspi.chord.com.Broadcast)
	 */
	@Override
	public void broadcast(Broadcast bc) {
		broadcast(bc.getSource(), bc.getTarget(), bc.getHit(), bc.getTransaction());
	}

	/**
	 * @param source
	 * @param target
	 * @param hit
	 * @param shootedPlayer
	 */
	private void handleShoot(ID source, ID target, Boolean hit, Player shootedPlayer) {
		int hittedSector = shootedPlayer.shootInIntervalOfPlayer(target);
		shootedPlayer.setHittedSector(hittedSector, hit);
		if (hit) {
			if (source.compareTo(chordImpl.getID()) == 0)
				GUIMessageQueue.getInstance().addMessage("I got a hit");
			else {
				GUIMessageQueue.getInstance().addMessage("Player with ID: " + source.toString() + " got a hit");
			}
		}
	}

	/**
	 * 
	 */
	public void startGame() {
		if (chordImpl.getPredecessorID().compareTo(chordImpl.getID()) > 0) {
			GUIMessageQueue.getInstance().addMessage("I Start!");
			shoot();
		}
	}

	/**
	 * 
	 */
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
				lastTarget = targetSector.getMiddle();
				ShootingThread st = new ShootingThread(chordImpl, targetSector.getMiddle());
				st.start();
			}
		}
	}

	/**
	 * @param min
	 * @param max
	 * @return
	 */
	protected static int randBetween(int min, int max) {
		Random rand = new Random();
		return rand.nextInt(max - (min + 1)) + min;
	}

	/**
	 * 
	 */
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

	public List<Player> getPlayerList() {
		return playerList;
	}

	public boolean isCheatMode() {
		return cheatMode;
	}

	public void setCheatMode(boolean cheatMode) {
		this.cheatMode = cheatMode;
	}

}
