package de.haw.ttv2.main;

import java.math.BigInteger;

import de.uniba.wiai.lspi.chord.data.ID;

/**
 * Player Class, handles the player, his ships and his interaction with the game
 * 
 * @author Johann Bronsch
 * @author Sascha Waltz
 */
public class Player implements Comparable<Player> {

	private ID playerID;

	private Sector[] playerFields;

	private boolean[] attackedFields;

	private boolean[] shipInField;

	private int remainingShips;
	
	private int transactionIdOfHit = -1;

	public Player(ID playerID, int sectorCount, int shipCount, ID idRangeFrom, ID idRangeIdTo) {
		this.playerID = playerID;
		attackedFields = new boolean[sectorCount];
		for (int i = 0; i < attackedFields.length; i++)
			attackedFields[i] = false;
		shipInField = new boolean[sectorCount];
		for (int i = 0; i < shipInField.length; i++)
			shipInField[i] = false;
		remainingShips = shipCount;
		playerFields = calculatePlayerSectors(sectorCount, idRangeFrom, idRangeIdTo);
	}

	public Player(ID playerID, int sectorCount, int shipCount, ID idRangeFrom, ID idRangeIdTo, boolean[] attackedFields, boolean[] shipInField) {
		this.playerID = playerID;
		this.attackedFields = attackedFields;
		this.shipInField = shipInField;
		remainingShips = shipCount;
		playerFields = calculatePlayerSectors(sectorCount, idRangeFrom, idRangeIdTo);
	}

	/**
	 * Calculates the sectors of the Player
	 * 
	 * @param sectorCount
	 * @param idRangeFrom
	 * @param idRangeIdTo
	 * @return
	 */
	private Sector[] calculatePlayerSectors(int sectorCount, ID idRangeFrom, ID idRangeIdTo) {
		Sector[] playerFields = new Sector[sectorCount];
		// Predecessor have a bigger ID
		if (idRangeFrom.compareTo(idRangeIdTo) == 1) {
			final BigInteger fromBIID = idRangeFrom.toBigInteger().add(BigInteger.ONE);
			final BigInteger toBIID = idRangeIdTo.toBigInteger();
			final BigInteger distance = toBIID.add(BigInteger.valueOf(2).pow(160).subtract(BigInteger.valueOf(1)).subtract(fromBIID));
			final BigInteger sectorLength = distance.divide(new BigInteger(String.valueOf(sectorCount)));
			BigInteger sectorCounter = fromBIID;
			for (int i = 0; i < playerFields.length; i++) {
				if (sectorCounter.add(sectorLength.add(BigInteger.ONE)).compareTo(GameState.MAXID.toBigInteger()) >= 0) {
					BigInteger modifiedSectorCount = sectorLength.subtract(GameState.MAXID.toBigInteger().subtract(sectorCounter));
					if (i == (playerFields.length - 1))
						playerFields[i] = new Sector(ID.valueOf(sectorCounter), idRangeIdTo);
					else
						playerFields[i] = new Sector(ID.valueOf(sectorCounter), ID.valueOf(BigInteger.ONE.add(modifiedSectorCount)));
					sectorCounter = BigInteger.ONE.add(modifiedSectorCount.add(BigInteger.ONE));
				} else {
					if (i == (playerFields.length - 1))
						playerFields[i] = new Sector(ID.valueOf(sectorCounter), idRangeIdTo);
					else
						playerFields[i] = new Sector(ID.valueOf(sectorCounter), ID.valueOf(sectorCounter.add(sectorLength)));
					sectorCounter = sectorCounter.add(sectorLength.add(BigInteger.ONE));
				}

			}
			// Successor have a smaller ID
		} else if (idRangeFrom.compareTo(idRangeIdTo) == -1) {
			final BigInteger fromBIID = idRangeFrom.toBigInteger().add(BigInteger.ONE);
			final BigInteger toBIID = idRangeIdTo.toBigInteger();
			final BigInteger distance = toBIID.subtract(fromBIID);
			final BigInteger sectorLength = distance.divide(new BigInteger(String.valueOf(sectorCount)));
			BigInteger sectorCounter = fromBIID;
			for (int i = 0; i < playerFields.length; i++) {
				if (i == (playerFields.length - 1))
					playerFields[i] = new Sector(ID.valueOf(sectorCounter), idRangeIdTo);
				else
					playerFields[i] = new Sector(ID.valueOf(sectorCounter), ID.valueOf(sectorCounter.add(sectorLength)));
				sectorCounter = sectorCounter.add(sectorLength.add(BigInteger.ONE));
			}
		} else
			// Etwas ist schief gegangen!!!
			throw new IllegalArgumentException("ID Range is 0");
		return playerFields;
	}

	/**
	 * Returns the status of the player to switch his color
	 * 
	 * @return
	 */
	public PlayerStatusEnum getPlayerStatus() {
		if (GameState.SHIP_COUNT == remainingShips) {
			return PlayerStatusEnum.GREEN;
		} else if (shipCountBetween(GameState.SHIP_COUNT / 2, GameState.SHIP_COUNT)) {
			return PlayerStatusEnum.BLUE;
		} else if (shipCountBetween(1, GameState.SHIP_COUNT / 2)) {
			return PlayerStatusEnum.VIOLET;
		} else
			return PlayerStatusEnum.RED;
	}

	/**
	 * Sets the ships for the player random on his field
	 */
	public void setShips() {
		for (int i = 0; i < GameState.SHIP_COUNT; i++) {
			boolean shipSet = false;
			do {
				int placeToSet = GameState.randBetween(0, shipInField.length);
				if (!shipInField[placeToSet]) {
					shipInField[placeToSet] = true;
					shipSet = true;
				}
			} while (!shipSet);
		}
	}

	public int getRemainingShips() {
		return remainingShips;
	}

	public ID getPlayerID() {
		return playerID;
	}

	/*
	 * Returns if the ship count is between two values
	 */
	private boolean shipCountBetween(int a, int b) {
		if (a <= remainingShips && b > remainingShips)
			return true;
		return false;
	}

	/**
	 * Returns the field number of the player shot at
	 * 
	 * @param target
	 * @return
	 */
	public int shootInIntervalOfPlayer(ID target) {
		for (int i = 0; i < playerFields.length; i++)
			if (target.isInInterval(playerFields[i].getFrom(), playerFields[i].getTo()))
				return i;
		return -1;
	}

	protected Sector[] getPlayerFields() {
		return playerFields;
	}

	@Override
	public int compareTo(Player otherPlayer) {
		return playerID.compareTo(otherPlayer.getPlayerID());
	}

	/**
	 * Handles the hit of a ship of the player
	 * 
	 * @param target
	 * @param actualTransactionID
	 * @return
	 */
	public Boolean shipHitted(ID target, int actualTransactionID) {
		final int fieldID = shootInIntervalOfPlayer(target);
		if (fieldID > -1)
			if (shipInField[fieldID]) {
				remainingShips--;
				transactionIdOfHit = actualTransactionID;
				return true;
			}
		return false;
	}

	/**
	 * Sets a Sector to hit in the array of player sectors
	 * 
	 * @param hittedSector
	 * @param hit
	 */
	public void setHittedSector(int hittedSector, Boolean hit) {
		if (!attackedFields[hittedSector]) {
			attackedFields[hittedSector] = true;
			if (hit) {
				shipInField[hittedSector] = true;
				if (remainingShips > 0)
					remainingShips--;
			}
		}
	}

	/**
	 * Returns a new Sector wich wasn't shot at
	 * 
	 * @return
	 */
	public Sector findFreeSector() {
		boolean stop = false;
		int sectorToShoot = -1;
		do {
			sectorToShoot = GameState.randBetween(0, attackedFields.length);
			if (!attackedFields[sectorToShoot])
				stop = true;
		} while (!stop);
		return playerFields[sectorToShoot];
	}

	public boolean[] getAttackedFields() {
		return attackedFields;
	}

	public boolean[] getShipInField() {
		return shipInField;
	}

	/**
	 * !!! CLASSIFIED !!!
	 * !!! TOP SECRET !!!
	 * 
	 * @param target
	 * @param actualTransactionID
	 * @return
	 */
	public Boolean cheatedShipHitted(ID target, int actualTransactionID) {
		final int fieldID = shootInIntervalOfPlayer(target);
		if (fieldID > -1) {
			if (attackedFields[fieldID])
				return false;
			attackedFields[fieldID] = true;
			final int freeFields = freeFields();
			if (remainingShips == freeFields) {
				remainingShips--;
				transactionIdOfHit = actualTransactionID;
				return true;
			}
			boolean wasHit = GameState.randBetween(0, 100) > 95 ? true : false;
			if (wasHit) {
				remainingShips--;
				transactionIdOfHit = actualTransactionID;
				return true;
			}
		}
		return false;
	}

	public int getTransactionIdOfHit() {
		return transactionIdOfHit;
	}
	
	/**
	 * Returns the count of remaining free fields
	 * 
	 * @return
	 */
	private int freeFields() {
		int freeFields = 0;
		for (int i = 0; i < attackedFields.length; i++)
			if (!attackedFields[i])
				freeFields++;
		return freeFields;
	}
}
