package de.haw.ttv2.main;

import java.math.BigInteger;

import de.uniba.wiai.lspi.chord.data.ID;

public class Player {

	private ID playerID;

	private Sector[] playerFields;

	private boolean[] attackedFields;

	private boolean[] shipInField;

	private int remainingShips;

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

	private Sector[] calculatePlayerSectors(int sectorCount, ID idRangeFrom, ID idRangeIdTo) {
		Sector[] playerFields = new Sector[sectorCount];
		// Vorgänger hat die größere ID
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
			// Vorgänger hat die kleinere ID
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

	public int getRemainingShips() {
		return remainingShips;
	}

	public ID getPlayerID() {
		return playerID;
	}

	private boolean shipCountBetween(int a, int b) {
		if (a <= remainingShips && b > remainingShips)
			return true;
		return false;
	}

	public boolean shootInIntervalOfPlayer(ID target) {
		for (int i = 0; i < playerFields.length; i++)
			if (target.isInInterval(playerFields[i].getFrom(), playerFields[i].getTo()))
				return true;
		return false;
	}

	protected Sector[] getPlayerFields() {
		return playerFields;
	}

}
