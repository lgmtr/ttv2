package de.haw.ttv2.main;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class Main {

	private static final String PROTOCOL = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
	private static final String SERVER_IP = "141.22.86.241";
	private static final String SERVER_PORT = "8585";
	private static final String CLIENT_IP = "192.168.15.211";
	private static final String CLIENT_PORT = "8585";

	/**
	 * The Class InputThread.
	 */
	private class InputThread implements Runnable {

		/** The running. */
		boolean running = true;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			Scanner scan = new Scanner(System.in);
			while (running) {
				if (scan.hasNext()) {
					input = scan.next();
				}
			}
			scan.close();
		}

		/**
		 * Stop.
		 */
		public void stop() {
			running = false;
		}
	}
	
	private String input = "";
	private InputThread in;
	private Thread inputListener;
	
	private ChordImpl chordImpl;

	private GameState gameState;

	public static void main(String[] args) {
		Main game = new Main();
		game.start();
	}

	private void start() {
		PropertiesLoader.loadPropertyFile();
		chordImpl = new ChordImpl();
		gameState = new GameState(chordImpl);
		chordImpl.setCallback(gameState);
		in = new InputThread();
		inputListener = new Thread(in);
		inputListener.start();
		createServer();
		//createClient();
		Set<Node> fingerSet = new HashSet<Node>(chordImpl.getFingerTable());
		int playerCount = fingerSet.size();
		System.out.print("Joined Player Count: " + playerCount);
		while (!input.equals("q")) {
			fingerSet = new HashSet<Node>(chordImpl.getFingerTable());
			if (playerCount != fingerSet.size()) {
				System.out.print(" : " + fingerSet.size());
				playerCount = fingerSet.size();
			}
			if (input.equals("b")) {
				for(Node n : fingerSet)
					chordImpl.broadcast(n.getNodeID(), false);
				input = "";
				break;
			}
			if (input.equals("s")) {
				break;
			}
			waitTime(500);
		}
		in.stop();
		inputListener.interrupt();
	}

	private void createClient() {

		URL localURL = null;
		try {
			localURL = new URL(PROTOCOL + "://" + CLIENT_IP + ":" + CLIENT_PORT + "/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		URL serverURL = null;
		try {
			serverURL = new URL(PROTOCOL + "://" + SERVER_IP + ":" + SERVER_PORT + "/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		try {
			chordImpl.join(localURL, serverURL);
		} catch (ServiceException e) {
			throw new RuntimeException("Could not join DHT!", e);
		}

		System.out.println("Joined Server: " + serverURL);
	}

	private void createServer() {
		URL localURL = null;
		try {
			localURL = new URL(PROTOCOL + "://" + SERVER_IP + ":" + SERVER_PORT + "/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		try {
			chordImpl.create(localURL);
		} catch (ServiceException e) {
			throw new RuntimeException("Could not create DHT!", e);
		}

		System.out.println("Chord listens on: " + localURL);

	}
	
	private void waitTime(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
