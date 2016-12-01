package de.haw.ttv2.main;

import de.uniba.wiai.lspi.chord.data.ID;

public class Sector {

	private ID from;
	
	private ID to;
	
	public Sector(ID from, ID to) {
		this.from = from;
		this.to = to;
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
}
