package de.haw.ttv2.main;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import de.haw.ttv2.main.network.NetworkInterfaceInfo;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class MainGUI extends Application {

	private static final String PROTOCOL = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);

	private static final double WINDOW_WIDTH = 1200 - 10;
	private static final double WINDOW_HEIGHT = 800 - 10;

	private ChordImpl chordImpl;
	private GameState gameState;

	private ComboBox<String> cb;
	private String ipTextField;
	private String portTextField;

	private int playerCount = 0;
	private JoiningThread jt;
	private Thread t;

	public TextArea outputTextArea;

	private static MainGUI instance = null;

	public static synchronized MainGUI getInstance() {
		return instance;
	}

	private void init(Stage primaryStage) {
		Group root = new Group();
		primaryStage.setResizable(false);
		primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
		BorderPane borderPane = new BorderPane();
		borderPane.setLayoutY(10);
		borderPane.setLayoutX(10);
		VBox rightBox = new VBox();
		rightBox.setMinWidth(200);
		cb = createNIIAComboBox();
		rightBox.getChildren().add(cb);
		rightBox.getChildren().addAll(createTextFields());
		rightBox.getChildren().addAll(createServerAndClientButtons());
		borderPane.setRight(rightBox);
		VBox centerBox = new VBox();
		outputTextArea = new TextArea();
		outputTextArea.setMinSize(WINDOW_WIDTH - 200, WINDOW_HEIGHT);
		centerBox.getChildren().add(outputTextArea);
		centerBox.setMinWidth(WINDOW_WIDTH - 200);
		borderPane.setCenter(centerBox);
		root.getChildren().add(borderPane);
	}

	private List<Node> createServerAndClientButtons() {
		return Arrays.asList(createButton("Create Server", 190, 40, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				URL localURL = null;
				try {
					localURL = new URL(PROTOCOL + "://" + cb.getValue() + ":" + portTextField + "/");
				} catch (MalformedURLException error) {
					throw new RuntimeException(error);
				}

				try {
					if (cb.getValue() != null) {
						if (!cb.getValue().isEmpty()) {
							chordImpl.create(localURL);
							outputTextArea.appendText("Chord listens on: " + localURL + "\n");
							jt = new JoiningThread();
							t = new Thread(jt);
							t.start();
						}
					}
				} catch (ServiceException error) {
					throw new RuntimeException("Could not create DHT!", error);
				}
			}
		}), createButton("Join a Server", 190, 40, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				URL localURL = null;
				try {
					localURL = new URL(PROTOCOL + "://" + cb.getValue() + ":" + portTextField + "/");
				} catch (MalformedURLException error) {
					throw new RuntimeException(error);
				}

				URL serverURL = null;
				try {
					serverURL = new URL(PROTOCOL + "://" + ipTextField + ":" + portTextField + "/");
				} catch (MalformedURLException error) {
					throw new RuntimeException(error);
				}
				try {
					if (cb.getValue() != null && ipTextField != null) {
						if (!cb.getValue().isEmpty() && !ipTextField.isEmpty()) {
							chordImpl.join(localURL, serverURL);
							outputTextArea.appendText("Joined Server: " + serverURL + "\n");
						}
					}
				} catch (ServiceException error) {
					throw new RuntimeException("Could not join DHT!", error);
				}
			}
		}), createButton("Disconnect from Server", 190, 40, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				chordImpl.leave();
				outputTextArea.appendText("Disconnected\n");
			}
		}), createButton("Close Application", 190, 40, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				chordImpl.leave();
				System.exit(0);
				System.out.println("Application Closed");
			}
		}), createButton("Broadcast", 190, 40, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Set<de.uniba.wiai.lspi.chord.com.Node> fingerSet = new HashSet<de.uniba.wiai.lspi.chord.com.Node>(chordImpl.getFingerTable());
				for(de.uniba.wiai.lspi.chord.com.Node n : fingerSet)
					chordImpl.broadcast(n.getNodeID(), false);
			}
		}));
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		init(primaryStage);
		initChord();
		primaryStage.show();
		instance = this;
	}

	private void initChord() {
		PropertiesLoader.loadPropertyFile();
		chordImpl = new ChordImpl();
		gameState = new GameState(chordImpl);
		chordImpl.setCallback(gameState);
	}

	public static void main(String[] args) {
		launch(args);
	}

	private List<Label> createTextFields() {
		Label ipText = createTextField("Server IP, if Client!", "", new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				ipTextField = newValue;
			}
		});
		Label portText = createTextField("Input Port:", "8585", new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				portTextField = newValue;
			}
		});
		portTextField = "8585";
		return Arrays.asList(ipText, portText);
	}

	private Label createTextField(String labelText, String stdText, ChangeListener<String> changeListener) {
		TextField text = new TextField(stdText);
		text.setMinSize(190, 20);
		text.textProperty().addListener(changeListener);
		Label label = new Label(labelText, text);
		label.setContentDisplay(ContentDisplay.BOTTOM);
		return label;
	}

	private ComboBox<String> createNIIAComboBox() {
		ComboBox<String> cb = new ComboBox<String>();
		cb.setId("cb_NI");
		cb.setPromptText("Choose own IP-Address!");
		cb.setMinSize(190, 20);
		cb.setItems(FXCollections.observableArrayList(NetworkInterfaceInfo.getNIIA()));
		return cb;
	}

	private Button createButton(String text, double xSize, double ySize, EventHandler<ActionEvent> event) {
		Button button = new Button(text);
		button.setMinSize(xSize, ySize);
		button.setOnAction(event);
		return button;
	}

	private class JoiningThread implements Runnable {

		boolean running = true;

		@Override
		public void run() {
			while (running) {
				Set<de.uniba.wiai.lspi.chord.com.Node> fingerSet = new HashSet<de.uniba.wiai.lspi.chord.com.Node>(chordImpl.getFingerTable());
				if (fingerSet.size() > playerCount){
					playerCount = fingerSet.size();
					outputTextArea.appendText("Player Joined!");
				}
			}
		}

		public void stop() {
			running = false;
		}
	}
}
