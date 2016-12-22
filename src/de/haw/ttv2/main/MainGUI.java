package de.haw.ttv2.main;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import de.haw.ttv2.main.BroadcastLog.BroadcastMsg;
import de.haw.ttv2.main.network.NetworkInterfaceInfo;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;

public class MainGUI extends Application {

	private static final String PROTOCOL = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);

	private static final double WINDOW_WIDTH = 1200;
	private static final double WINDOW_HEIGHT = 600;
	private static final double RIGHT_WINDOW_SIZE = 200;

	private static final double BUTTON_WIDTH = 190;
	private static final double BUTTON_HEIGHT = 33;

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

	private TilePane tilePane;

	private PlayerStatusEnum lastPlayerState;

	private Integer lastKnownTransactionID = -1;

	private TextField tf_transactionID;

	private void init(Stage primaryStage) {
		lastPlayerState = PlayerStatusEnum.GREEN;
		Thread t = new Thread(new CoapThread(PlayerStatusEnum.GREEN));
		t.start();
		Group root = new Group();
		primaryStage.setResizable(false);
		primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
		BorderPane borderPane = new BorderPane();
		// Menu
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
		// Infobox
		VBox centerBox = new VBox();
		TabPane tabPane = new TabPane();
		tabPane.setPrefSize((WINDOW_WIDTH - RIGHT_WINDOW_SIZE) - 5, WINDOW_HEIGHT);
		tabPane.setSide(Side.TOP);
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
		Tab tab1 = new Tab();
		tab1.setText("Text Output");
		Tab tab2 = new Tab();
		tab2.setText("Visual Output");
		outputTextArea = new TextArea();
		outputTextArea.setMinSize((WINDOW_WIDTH - RIGHT_WINDOW_SIZE) - 10, WINDOW_HEIGHT - 30);
		outputTextArea.setMaxSize((WINDOW_WIDTH - RIGHT_WINDOW_SIZE) - 10, WINDOW_HEIGHT - 30);
		VBox vboxCenter = new VBox();
		vboxCenter.getChildren().add(outputTextArea);
		vboxCenter.setAlignment(Pos.CENTER);
		centerBox.getChildren().add(vboxCenter);
		centerBox.setMinWidth(WINDOW_WIDTH - RIGHT_WINDOW_SIZE);
		tab1.setContent(centerBox);
		tilePane = new TilePane();
		tab2.setContent(tilePane);
		tabPane.getTabs().addAll(tab1, tab2);
		borderPane.setCenter(tabPane);
		root.getChildren().add(borderPane);
	}

	private BorderPane createItem(ID player, double progress, Color color) {
		BorderPane borderPane = new BorderPane();
		VBox rightBox = new VBox(5);
		rightBox.setMinWidth(70);
		rightBox.setMinHeight(100);
		rightBox.setMaxWidth(70);
		rightBox.setMaxHeight(100);
		rightBox.getChildren().add(new Circle(30, color));
		rightBox.setAlignment(Pos.CENTER);
		borderPane.setRight(rightBox);
		VBox centerBox = new VBox(5);
		centerBox.setMinWidth(350);
		centerBox.setMinHeight(100);
		centerBox.setMaxWidth(350);
		centerBox.setMaxHeight(100);
		Text idText = new Text(player.toString());
		Label idLabel = new Label("Player ID", idText);
		idLabel.setContentDisplay(ContentDisplay.BOTTOM);
		centerBox.getChildren().add(idLabel);
		ProgressBar playerProgress = new ProgressBar();
		playerProgress.setProgress(progress);
		playerProgress.setMinWidth(340);
		Label prLabel = new Label("Player hitted ships", playerProgress);
		prLabel.setContentDisplay(ContentDisplay.BOTTOM);
		centerBox.getChildren().add(prLabel);
		borderPane.setCenter(centerBox);
		borderPane.setBorder(new Border(new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, new CornerRadii(15), BorderStroke.THIN)));
		return borderPane;
	}

	private Circle createStatusCircle() {
		statusCircle = new Circle(70, Color.GREEN);
		return statusCircle;
	}

	private List<Node> createServerAndClientButtons() {
		startButton = createButton("Start Game", BUTTON_WIDTH, BUTTON_HEIGHT, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				gameState.startGame();
				gameStarted = true;
			}
		});
		return Arrays.asList(createButton("Create Server", BUTTON_WIDTH, BUTTON_HEIGHT, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if(Integer.valueOf(tf_transactionID.getText()) == -666){
					gameState.setCheatMode(true);
					GUIMessageQueue.getInstance().addMessage("CheatMode activated!!!\n");
				}
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
		}), createButton("Join a Server", BUTTON_WIDTH, BUTTON_HEIGHT, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if(Integer.valueOf(tf_transactionID.getText()) == -666){
					gameState.setCheatMode(true);
					GUIMessageQueue.getInstance().addMessage("CheatMode activated!!!\n");
				}
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
		}), createButton("Disconnect from Server", BUTTON_WIDTH, BUTTON_HEIGHT, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				chordImpl.leave();
				GUIMessageQueue.getInstance().addMessage("Disconnected\n");
			}
		}), createButton("Close Application", BUTTON_WIDTH, BUTTON_HEIGHT, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				chordImpl.leave();
				System.exit(0);
				System.out.println("Application Closed");
			}
		}), createButton("Create Gamefield", BUTTON_WIDTH, BUTTON_HEIGHT, new EventHandler<ActionEvent>() {
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
				if (BroadcastLog.getInstance().getMessageWithDiffrentSrc() != null)
					if (BroadcastLog.getInstance().getMessageWithDiffrentSrc().length() != 0)
						System.out.println(BroadcastLog.getInstance().getMessageWithDiffrentSrc());
				if (gameState.getOwnPlayer() != null) {
					vboxMenu.getChildren().remove(statusCircle);
					statusCircle = new Circle(70, gameState.getOwnPlayer().getPlayerStatus().getColor());
					if (lastPlayerState.compareTo(gameState.getOwnPlayer().getPlayerStatus()) != 0) {
						Thread t = new Thread(new CoapThread(gameState.getOwnPlayer().getPlayerStatus()));
						t.start();
						lastPlayerState = gameState.getOwnPlayer().getPlayerStatus();
					}
					vboxMenu.getChildren().add(statusCircle);
				}
				try {
					final int newPlayerCount = new HashSet<>(chordImpl.getFingerTable()).size();
					if (newPlayerCount > playerCount && !gameStarted) {
						playerCount = newPlayerCount;
						gameState.createGamefield();
						if (chordImpl.getPredecessorID().compareTo(chordImpl.getID()) > 0) {
							startButton.setDisable(false);
						}
						if (!startButton.isDisabled() && chordImpl.getPredecessorID().compareTo(chordImpl.getID()) <= 0)
							startButton.setDisable(true);
					}
				} catch (NullPointerException e) {
					// if catched, then game not started!
				}
				Map<ID, List<BroadcastMsg>> bclHittingMap = BroadcastLog.getInstance().getHittingMap();
				Map<ID, List<BroadcastMsg>> bclMap = BroadcastLog.getInstance().getLogMap();
				tilePane.getChildren().clear();
				if (gameState.getOwnPlayer() != null)
					tilePane.getChildren().add(
							createItem(chordImpl.getID(), ((double) GameState.SHIP_COUNT - (double) gameState.getOwnPlayer().getRemainingShips())
									* ((double) GameState.SHIP_COUNT / 100d), gameState.getOwnPlayer().getPlayerStatus().getColor()));
				for (ID id : bclMap.keySet()) {
					if (bclHittingMap.get(id) != null) {
						tilePane.getChildren().add(
								createItem(id, (double) bclHittingMap.get(id).size() * ((double) GameState.SHIP_COUNT / 100d),
										getPlayerStatus(GameState.SHIP_COUNT - bclHittingMap.get(id).size()).getColor()));
					} else {
						tilePane.getChildren().add(createItem(id, 0, PlayerStatusEnum.GREEN.getColor()));
					}
				}
				BroadcastMsg lastBroadcast = BroadcastLog.getInstance().getLastBroadcast();
				if (lastBroadcast != null)
					if (lastKnownTransactionID != lastBroadcast.getTransaction()) {
						lastKnownTransactionID = lastBroadcast.getTransaction();
						tf_transactionID.setText(String.valueOf(lastKnownTransactionID));
					}
			}
		}));
		animation.setCycleCount(Animation.INDEFINITE);
		animation.play();
		primaryStage.show();
	}

	private PlayerStatusEnum getPlayerStatus(int remainingShips) {
		if (GameState.SHIP_COUNT == remainingShips) {
			return PlayerStatusEnum.GREEN;
		} else if (shipCountBetween(GameState.SHIP_COUNT / 2, GameState.SHIP_COUNT, remainingShips)) {
			return PlayerStatusEnum.BLUE;
		} else if (shipCountBetween(1, GameState.SHIP_COUNT / 2, remainingShips)) {
			return PlayerStatusEnum.VIOLET;
		} else
			return PlayerStatusEnum.RED;
	}

	private boolean shipCountBetween(int a, int b, int remainingShips) {
		if (a <= remainingShips && b > remainingShips)
			return true;
		return false;
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
		TextField text = new TextField(String.valueOf(lastKnownTransactionID));
		text.setMinSize(190, 20);
		Label label = new Label("Last known TransactionID:", text);
		label.setContentDisplay(ContentDisplay.BOTTOM);
		tf_transactionID = text;
		return Arrays.asList(ipText, portText, label);
	}

	private Label createTextField(String labelText, String stdText, ChangeListener<String> changeListener, TextField reference) {
		TextField text = new TextField(stdText);
		text.setMinSize(190, 20);
		if (changeListener != null)
			text.textProperty().addListener(changeListener);
		Label label = new Label(labelText, text);
		label.setContentDisplay(ContentDisplay.BOTTOM);
		if (reference != null)
			reference = text;
		return label;
	}

	private Label createTextField(String labelText, String stdText, ChangeListener<String> changeListener) {
		return createTextField(labelText, stdText, changeListener, null);
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
