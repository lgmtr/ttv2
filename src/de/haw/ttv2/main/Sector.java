package de.haw.ttv2.main;

import java.math.BigInteger;

import de.uniba.wiai.lspi.chord.data.ID;

/**
 * Represents one Sector in a specific range
 * 
 * @author Johann Bronsch
 * @author Sascha Waltz
 */
public class Sector {

	private ID from;
	
	private ID middle;
	
	private ID to;
	
	public Sector(ID from, ID to) {
		this.from = from;
		this.to = to;
		if(from.compareTo(to) > 0)
			middle = ID.valueOf(BigInteger.ONE);
		else
			middle = ID.valueOf(from.toBigInteger().add(to.toBigInteger().subtract(from.toBigInteger()).divide(new BigInteger("2"))));
	}

	public ID getFrom() {
		return from;
	}

	public ID getTo() {
		return to;
	}
	
	@Override
	public String toString(){
		return "Sector From:\t" + from.toHexString() + "\nSector To:\t" + to.toHexString() + "\n";
	}

	public ID getMiddle() {
		return middle;
	}
}
