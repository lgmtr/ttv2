package de.haw.ttv2.main;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import de.haw.ttv2.main.network.NetworkInterfaceInfo;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class MainGUI extends Application {

	private static final String PROTOCOL = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);

	private static final double WINDOW_WIDTH = 1200;
	private static final double WINDOW_HEIGHT = 600;
	private static final double RIGHT_WINDOW_SIZE = 200;

	private static final double FRAME_DURATION = 10;

	private ChordImpl chordImpl;
	private GameState gameState;

	private ComboBox<String> cb;
	private String ipTextField;
	private String portTextField;

	private int playerCount = 0;

	private boolean gameStarted = false;

	private Circle statusCircle;

	private VBox vboxMenu = new VBox(10);

	public TextArea outputTextArea;

	private Timeline animation;

	private Button startButton;

	private void init(Stage primaryStage) {
		Group root = new Group();
		primaryStage.setResizable(false);
		primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
		BorderPane borderPane = new BorderPane();
		VBox rightBox = new VBox();
		rightBox.setMinWidth(RIGHT_WINDOW_SIZE);
		cb = createNIIAComboBox();
		vboxMenu.getChildren().add(cb);
		vboxMenu.getChildren().addAll(createTextFields());
		vboxMenu.getChildren().addAll(createServerAndClientButtons());
		startButton.setDisable(true);
		vboxMenu.getChildren().add(createStatusCircle());
		vboxMenu.setAlignment(Pos.CENTER);
		rightBox.getChildren().add(vboxMenu);
		borderPane.setRight(rightBox);
		VBox centerBox = new VBox();
		outputTextArea = new TextArea();
		outputTextArea.setMinSize((WINDOW_WIDTH - RIGHT_WINDOW_SIZE) - 20, WINDOW_HEIGHT);
		outputTextArea.setMaxSize((WINDOW_WIDTH - RIGHT_WINDOW_SIZE) - 20, WINDOW_HEIGHT);
		VBox vboxCenter = new VBox();
		vboxCenter.getChildren().add(outputTextArea);
		vboxCenter.setAlignment(Pos.CENTER);
		centerBox.getChildren().add(vboxCenter);
		centerBox.setMinWidth(WINDOW_WIDTH - RIGHT_WINDOW_SIZE);
		borderPane.setCenter(centerBox);
		root.getChildren().add(borderPane);
	}

	private Circle createStatusCircle() {
		statusCircle = new Circle(70, Color.GREEN);
		return statusCircle;
	}

	private List<Node> createServerAndClientButtons() {
		startButton = createButton("Start Game", 190, 40, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				gameState.startGame();
				gameStarted = true;
			}
		});
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
							GUIMessageQueue.getInstance().addMessage("Chord listens on: " + localURL + "\n");
							Thread thread = new Thread(new JoiningThread(chordImpl));
							thread.start();
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
							GUIMessageQueue.getInstance().addMessage("Joined Server: " + serverURL + "\n");
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
				GUIMessageQueue.getInstance().addMessage("Disconnected\n");
			}
		}), createButton("Close Application", 190, 40, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				chordImpl.leave();
				System.exit(0);
				System.out.println("Application Closed");
			}
		}), createButton("Create Gamefield", 190, 40, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				gameState.createGamefield();
			}
		}), startButton);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		initChord();
		init(primaryStage);
		animation = new Timeline();
		animation.getKeyFrames().add(new KeyFrame(Duration.millis(FRAME_DURATION), new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				String message = GUIMessageQueue.getInstance().getFirstMessage();
				if (message != null)
					outputTextArea.appendText(message + "\n");
				if (BroadcastLog.getInstance().getLastBroadcast() != null)
					outputTextArea.appendText(BroadcastLog.getInstance().getLastBroadcast());
				if (gameState.getOwnPlayer() != null) {
					vboxMenu.getChildren().remove(statusCircle);
					statusCircle = new Circle(70, gameState.getOwnPlayer().getPlayerStatus().getColor());
					vboxMenu.getChildren().add(statusCircle);
				}
				try{
					final int newPlayerCount = new HashSet<>(chordImpl.getFingerTable()).size();
					if (newPlayerCount > playerCount && !gameStarted) {
						playerCount = newPlayerCount;
						gameState.createGamefield();
						if (chordImpl.getPredecessorID().compareTo(chordImpl.getID()) > 0) {
							startButton.setDisable(false);
						}
						if(!startButton.isDisabled() && chordImpl.getPredecessorID().compareTo(chordImpl.getID()) <= 0)
							startButton.setDisable(true);
					}
				} catch(NullPointerException e){
					//if catched, then game not started!
				}
			}
		}));
		animation.setCycleCount(Animation.INDEFINITE);
		animation.play();
		primaryStage.show();
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
}
