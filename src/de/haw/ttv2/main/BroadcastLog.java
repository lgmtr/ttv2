package de.haw.ttv2.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uniba.wiai.lspi.chord.data.ID;

public class BroadcastLog {

	private static BroadcastLog instance;

	private List<BroadcastMsg> messageLog;

	private List<BroadcastMsg> messageLogOfHits;

	private static final String WIN_LOSE_SEPERATOR = "\n==============================================================================\n";

	public class BroadcastMsg {
		private ID source;
		private ID target;
		private Boolean hit;
		private Integer transaction;

		public BroadcastMsg(ID source, ID target, Boolean hit, Integer transaction) {
			this.source = source;
			this.target = target;
			this.hit = hit;
			this.transaction = transaction;
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

		public Integer getTransaction() {
			return transaction;
		}

		public void setTransaction(Integer transaction) {
			this.transaction = transaction;
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

	public void addBroadcast(ID source, ID target, Boolean hit, Integer transaction) {
		messageLog.add(new BroadcastMsg(source, target, hit, transaction));
		if (hit)
			messageLogOfHits.add(new BroadcastMsg(source, target, hit, transaction));
	}

	public Map<ID, List<BroadcastMsg>> getHittingMap() {
		Map<ID, List<BroadcastMsg>> hittingMap = new HashMap<>();
		for (BroadcastMsg hittingListItem : messageLogOfHits) {
			if (hittingMap.containsKey(hittingListItem.getSource())) {
				List<BroadcastMsg> bcmList = hittingMap.get(hittingListItem.getSource());
				bcmList.add(hittingListItem);
				hittingMap.replace(hittingListItem.getSource(), bcmList);
			} else {
				List<BroadcastMsg> bcmList = new ArrayList<>();
				bcmList.add(hittingListItem);
				hittingMap.put(hittingListItem.getSource(), bcmList);
			}
		}
		return hittingMap;
	}

	public Map<ID, List<BroadcastMsg>> getLogMap() {
		Map<ID, List<BroadcastMsg>> logMap = new HashMap<>();
		List<BroadcastMsg> messageLogCopy = new ArrayList<>(messageLog);
		for (BroadcastMsg logListItem : messageLogCopy) {
			if (logMap.containsKey(logListItem.getSource())) {
				List<BroadcastMsg> bcmList = logMap.get(logListItem.getSource());
				bcmList.add(logListItem);
				logMap.replace(logListItem.getSource(), bcmList);
			} else {
				List<BroadcastMsg> bcmList = new ArrayList<>();
				bcmList.add(logListItem);
				logMap.put(logListItem.getSource(), bcmList);
			}
		}
		return logMap;
	}

	public ID hasSomeoneLost() {
		Map<ID, List<BroadcastMsg>> hittingMap = getHittingMap();
		for (ID id : hittingMap.keySet()) {
			if (hittingMap.get(id).size() >= GameState.SHIP_COUNT)
				return id;
		}
		return null;
	}

	// Only for Tests
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
	public BroadcastMsg getLastBroadcast() {
		if (messageLog.size() < 1)
			return null;
		return messageLog.get(messageLog.size() - 1);
	}

	// Only for Tests
	private String getBroadcastMsgFormatted(BroadcastMsg bc) {
		if (messageLog.size() < 1)
			return null;
		return WIN_LOSE_SEPERATOR + "Source: " + bc.getSource().toString() + "\nTarget: " + bc.getTarget().toString()
				+ "\nHit: " + bc.getHit().toString() + WIN_LOSE_SEPERATOR;
	}

	public BroadcastMsg getLastBroadcast(ID hasSomeLose) {
		List<BroadcastMsg> dummyList = new ArrayList<>();
		for (BroadcastMsg broadcastMsg : messageLogOfHits) {
			if (broadcastMsg.getSource().compareTo(hasSomeLose) == 0)
				dummyList.add(broadcastMsg);
		}
		if (dummyList.size() > 0)
			return dummyList.get(dummyList.size() - 1);
		return null;
	}

}
