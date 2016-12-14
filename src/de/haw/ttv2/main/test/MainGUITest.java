package de.haw.ttv2.main.test;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
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
import de.haw.ttv2.main.BroadcastLog;
import de.haw.ttv2.main.PlayerStatusEnum;
import de.haw.ttv2.main.BroadcastLog.BroadcastMsg;
import de.haw.ttv2.main.GameState;
import de.uniba.wiai.lspi.chord.data.ID;

public class MainGUITest extends Application {

	private static final double WINDOW_WIDTH = 1200;
	private static final double WINDOW_HEIGHT = 600;
	private static final double RIGHT_WINDOW_SIZE = 200;

	private static final double FRAME_DURATION = 100;

	private ID[] playerIDs = { ID.valueOf(((BigInteger.valueOf(2).pow(160)).subtract(BigInteger.valueOf(1000)))),
			ID.valueOf(((BigInteger.valueOf(2).pow(160)).subtract(BigInteger.valueOf(9900)))),
			ID.valueOf(((BigInteger.valueOf(2).pow(160)).subtract(BigInteger.valueOf(95100)))) };

	private TilePane tilePane;

	private void init(Stage primaryStage) {
		Group root = new Group();
		primaryStage.setResizable(false);
		primaryStage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
		BorderPane borderPane = new BorderPane();
		// Menu
		VBox rightBox = new VBox();
		rightBox.setMinWidth(RIGHT_WINDOW_SIZE);
		rightBox.getChildren().add(new Button("Test"));
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
		TextArea outputTextArea = new TextArea();
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
		centerBox.setMinWidth(310);
		centerBox.setMinHeight(100);
		centerBox.setMaxWidth(310);
		centerBox.setMaxHeight(100);
		Text idText = new Text(player.toString());
		Label idLabel = new Label("Player ID", idText);
		idLabel.setContentDisplay(ContentDisplay.BOTTOM);
		centerBox.getChildren().add(idLabel);
		ProgressBar playerProgress = new ProgressBar();
		playerProgress.setProgress(progress);
		playerProgress.setMinWidth(300);
		Label prLabel = new Label("Player hitted ships", playerProgress);
		prLabel.setContentDisplay(ContentDisplay.BOTTOM);
		centerBox.getChildren().add(prLabel);
		borderPane.setCenter(centerBox);
		borderPane.setBorder(new Border(new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, new CornerRadii(15),
				BorderStroke.THIN)));
		return borderPane;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		init(primaryStage);
		Timeline animation = new Timeline();
		animation.getKeyFrames().add(new KeyFrame(Duration.millis(FRAME_DURATION), new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				int randInt = ThreadLocalRandom.current().nextInt(0, 2 + 1);
				boolean randHit = ThreadLocalRandom.current().nextBoolean();
				ID randID = ID.valueOf(((BigInteger.valueOf(2).pow(160)).subtract(BigInteger.valueOf(ThreadLocalRandom
						.current().nextInt(1, 500 + 1)))));
				BroadcastLog.getInstance().addBroadcast(playerIDs[randInt], randID, randHit);
				Map<ID, List<BroadcastMsg>> bclMap = BroadcastLog.getInstance().getHittingMap();
				tilePane.getChildren().clear();
				for (ID id : bclMap.keySet()) {
					tilePane.getChildren().add(
							createItem(id, bclMap.get(id).size() * (GameState.SHIP_COUNT / 100),
									getPlayerStatus(GameState.SHIP_COUNT - bclMap.get(id).size()).getColor()));
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

	public static void main(String[] args) {
		launch(args);
	}

}
