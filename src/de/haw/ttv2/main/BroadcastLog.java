package de.haw.ttv2.main;

import java.util.ArrayList;
import java.util.List;

import de.uniba.wiai.lspi.chord.data.ID;

public class BroadcastLog {

	private static BroadcastLog instance;

	private List<BroadcastMsg> messageLog;

	private List<BroadcastMsg> messageLogOfHits;

	private static final String WIN_LOSE_SEPERATOR = "\n==============================================================================\n";

	private class BroadcastMsg {
		private ID source;
		private ID target;
		private Boolean hit;

		public BroadcastMsg(ID source, ID target, Boolean hit) {
			this.source = source;
			this.target = target;
			this.hit = hit;
		}

		public ID getSource() {
			return source;
		}

		public ID getTarget() {
			return target;
		}

		public Boolean getHit() {
			return hit;
		}
	}

	public static synchronized BroadcastLog getInstance() {
		if (instance == null) {
			instance = new BroadcastLog();
			instance.messageLog = new ArrayList<BroadcastMsg>();
			instance.messageLogOfHits = new ArrayList<BroadcastMsg>();
		}
		return instance;
	}

	public void addBroadcast(ID source, ID target, Boolean hit) {
		messageLog.add(new BroadcastMsg(source, target, hit));
		if (hit)
			messageLogOfHits.add(new BroadcastMsg(source, target, hit));
	}

	public String getMessageWithDiffrentSrc() {
		List<BroadcastMsg> bcmList = new ArrayList<>();
		for (int i = 0; i < messageLogOfHits.size(); i++) {
			for (int j = 0; j < messageLogOfHits.size(); j++) {
				if (i != j && messageLogOfHits.get(i).getSource().compareTo(messageLogOfHits.get(j).getSource()) != 0
						&& messageLogOfHits.get(i).getTarget().compareTo(messageLogOfHits.get(j).getTarget()) == 0) {
					bcmList.add(messageLogOfHits.get(i));
					bcmList.add(messageLogOfHits.get(j));
				}
			}
		}
		String returnString = "";
		for (BroadcastMsg broadcastMsg : bcmList) {
			returnString += getBroadcastMsgFormatted(broadcastMsg);
		}
		return returnString;
	}

	// Only for Tests
	public String getLastBroadcast() {
		if (messageLog.size() < 1)
			return null;
		return getBroadcastMsgFormatted(messageLog.get(messageLog.size() - 1));
	}

	private String getBroadcastMsgFormatted(BroadcastMsg bc) {
		if (messageLog.size() < 1)
			return null;
		return WIN_LOSE_SEPERATOR + "Source: " + bc.getSource().toString() + "\nTarget: " + bc.getTarget().toString() + "\nHit: " + bc.getHit().toString()
				+ WIN_LOSE_SEPERATOR;
	}

}
