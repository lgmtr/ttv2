package de.haw.ttv2.main;

import java.net.MalformedURLException;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class Main {

	private static final String PROTOCOL = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
	private static final String SERVER_IP = "192.168.15.211";
	private static final String SERVER_PORT = "8585";
	private static final String CLIENT_IP = "192.168.15.211";
	private static final String CLIENT_PORT = "8585";

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
		createServer();
		//createClient();
		System.out.println(chordImpl.getID().toString());
		chordImpl.broadcast(chordImpl.getID(), true);
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

}
