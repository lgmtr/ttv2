package de.haw.ttv2.main;

import java.util.ArrayList;
import java.util.List;

import de.uniba.wiai.lspi.chord.data.ID;

public class BroadcastLog {

	private static BroadcastLog instance;

	private List<BroadcastMsg> messageLog;
	
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
		}
		return instance;
	}
	
	public void addBroadcast(ID source, ID target, Boolean hit){
		messageLog.add(new BroadcastMsg(source, target, hit));
	}

	public String getLastBroadcast() {
		if (messageLog.size() < 1)
			return null;
		return WIN_LOSE_SEPERATOR +"Source: " + messageLog.get(messageLog.size() - 1).getSource().toString() + 
				"\nTarget: " + messageLog.get(messageLog.size() - 1).getTarget().toString() + 
				"\nHit: " + messageLog.get(messageLog.size() - 1).getHit().toString() + WIN_LOSE_SEPERATOR;
	}

}
