package de.haw.ttv2.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uniba.wiai.lspi.chord.data.ID;

/**
 *  
 * 
 * @author Johann Bronsch
 * @author Sascha Waltz
 *
 */
public class BroadcastLog {

	private static BroadcastLog instance;

	private List<BroadcastMsg> messageLog;

	private List<BroadcastMsg> messageLogOfHits;

	private Map<ID, List<BroadcastMsg>> logMap;

	private Map<ID, List<BroadcastMsg>> hittingMap;

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
			instance.hittingMap = new HashMap<>();
			instance.logMap = new HashMap<>();
		}
		return instance;
	}

	/**
	 * Adds a new BroadcastMsg to the messageLog
	 * 
	 * @param source
	 * @param target
	 * @param hit
	 * @param transaction
	 */
	public void addBroadcast(ID source, ID target, Boolean hit, Integer transaction) {
		final BroadcastMsg bcm = new BroadcastMsg(source, target, hit, transaction);
		messageLog.add(bcm);
		if (logMap.containsKey(source)) {
			List<BroadcastMsg> bcmList = logMap.get(source);
			bcmList.add(bcm);
			logMap.replace(source, bcmList);
		} else {
			List<BroadcastMsg> bcmList = new ArrayList<>();
			bcmList.add(bcm);
			logMap.put(source, bcmList);
		}
		if (hit){
			messageLogOfHits.add(new BroadcastMsg(source, target, hit, transaction));
			if (hittingMap.containsKey(source)) {
				List<BroadcastMsg> bcmList = hittingMap.get(source);
				bcmList.add(bcm);
				hittingMap.replace(source, bcmList);
			} else {
				List<BroadcastMsg> bcmList = new ArrayList<>();
				bcmList.add(bcm);
				hittingMap.put(source, bcmList);
			}
		}
	}

	public Map<ID, List<BroadcastMsg>> getHittingMap() {
		return hittingMap;
	}
	
	public Map<ID, List<BroadcastMsg>> getLogMap(){
		return logMap;
	}

	/**
	 * Checks if someone has lost his last ship
	 * 
	 * @return
	 */
	public ID hasSomeoneLost(){
		Map<ID, List<BroadcastMsg>> hittingMap = getHittingMap();
		for (ID id : hittingMap.keySet()) {
			if (hittingMap.get(id).size() >= GameState.SHIP_COUNT)
				return id;
		}
		return null;
	}

	/**
	 * Only for Tests
	 * 
	 * @return
	 */
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

	/**
	 * Only for Tests
	 * 
	 * @return
	 */
	public BroadcastMsg getLastBroadcast() {
		if (messageLog.size() < 1)
			return null;
		return messageLog.get(messageLog.size() - 1);
	}

	/**
	 * Only for Tests
	 * 
	 * @return
	 */
	private String getBroadcastMsgFormatted(BroadcastMsg bc) {
		if (messageLog.size() < 1)
			return null;
		return WIN_LOSE_SEPERATOR + "Source: " + bc.getSource().toString() + "\nTarget: " + bc.getTarget().toString()
				+ "\nHit: " + bc.getHit().toString() + WIN_LOSE_SEPERATOR;
	}

	/**
	 * Returns the last boradcast message if someone has lost
	 * 
	 * @param hasSomeLose
	 * @return
	 */
	public BroadcastMsg getLastBroadcast(ID hasSomeLose) {
		final List<BroadcastMsg> bcList = hittingMap.get(hasSomeLose);
		if (bcList.size() > 0)
			return bcList.get(bcList.size() - 1);
		return null;
	}

}
