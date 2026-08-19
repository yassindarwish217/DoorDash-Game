package game.gui.view;

import game.engine.Role;
import game.gui.controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class StartView {

    private final Stage stage;
    private ToggleGroup roleGroup;

    public StartView(Stage stage) {
        this.stage = stage;
        buildUI();
    }

    private void buildUI() {
        VBox root = new VBox(18);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(35));
        root.setStyle("-fx-background-color: #1a1a2e;");

        Text title = new Text("DoorDasH");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 52));
        title.setStyle("-fx-fill: #e94560;");

        Text subtitle = new Text("Scare vs Laugh Touchdown");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        subtitle.setStyle("-fx-fill: #a8dadc;");

        Label chooseLabel = new Label("Choose Your Side:");
        chooseLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");

        roleGroup = new ToggleGroup();

        RadioButton scarerBtn = new RadioButton("  SCARER  -  Scare the children!");
        scarerBtn.setToggleGroup(roleGroup);
        scarerBtn.setUserData(Role.SCARER);
        scarerBtn.setSelected(true);
        scarerBtn.setStyle("-fx-text-fill: #bb86fc; -fx-font-size: 14px;");

        RadioButton laugherBtn = new RadioButton("  LAUGHER  -  Make them laugh!");
        laugherBtn.setToggleGroup(roleGroup);
        laugherBtn.setUserData(Role.LAUGHER);
        laugherBtn.setStyle("-fx-text-fill: #ffb347; -fx-font-size: 14px;");

        VBox roleBox = new VBox(12, scarerBtn, laugherBtn);
        roleBox.setAlignment(Pos.CENTER_LEFT);
        roleBox.setPadding(new Insets(14, 24, 14, 24));
        roleBox.setStyle("-fx-background-color: #16213e; -fx-background-radius: 8; -fx-border-color: #444; -fx-border-radius: 8;");
        roleBox.setMaxWidth(340);

        TitledPane instructionsPane = buildInstructionsPane();
        instructionsPane.setMaxWidth(640);

        Button startBtn = new Button("  START GAME  ");
        startBtn.setStyle(btnStyle("#e94560"));
        startBtn.setOnMouseEntered(e -> startBtn.setStyle(btnStyle("#c0392b")));
        startBtn.setOnMouseExited(e -> startBtn.setStyle(btnStyle("#e94560")));
        startBtn.setOnAction(e -> startGame());

        root.getChildren().addAll(title, subtitle, chooseLabel, roleBox, instructionsPane, startBtn);

        Scene scene = new Scene(root, 700, 740);
        stage.setScene(scene);
    }

    private String btnStyle(String color) {
        return "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 17px; "
             + "-fx-font-weight: bold; -fx-padding: 12 36; -fx-background-radius: 6; -fx-cursor: hand;";
    }

    private TitledPane buildInstructionsPane() {
        String text =
            "OBJECTIVE: Reach cell 99 with at least 1000 energy to win!\n\n"
          + "GAMEPLAY: Take turns rolling a 6-sided dice to move. Optionally activate your\n"
          + "  powerup (costs 500 energy) before rolling each turn.\n\n"
          + "CELL TYPES:\n"
          + "  Normal (Gray)          - No effect\n"
          + "  SCARER Door (Purple)   - Boosts SCARER team, damages LAUGHER team\n"
          + "  LAUGHER Door (Orange)  - Boosts LAUGHER team, damages SCARER team\n"
          + "  Card Cell (Gold)       - Draw a special card\n"
          + "  Conveyor Belt (Teal)   - Transport forward\n"
          + "  Contamination Sock (Red) - Transport back, lose 100 energy\n"
          + "  Monster Cell (Green)   - Ally: use powerup | Enemy: swap energy\n\n"
          + "MONSTER TYPES:\n"
          + "  Dasher     - Moves 2x dice; Powerup = Momentum Rush (3x for 3 turns)\n"
          + "  Dynamo     - Gains 2x energy; Powerup = Freeze opponent 1 turn\n"
          + "  MultiTasker- Moves 0.5x; +200 energy bonus; Powerup = Focus Mode (normal speed 2 turns)\n"
          + "  Schemer    - +10 energy bonus per gain; Powerup = Chain steal energy from all\n\n"
          + "CARDS:\n"
          + "  Position Swap    - Swap places with opponent (if you're behind)\n"
          + "  Energy Steal     - Steal energy from opponent\n"
          + "  Shield           - Block the next negative effect\n"
          + "  Contamination Code - You return to cell 0\n"
          + "  2319 Alert       - Opponent returns to cell 0\n"
          + "  Mind Scramble    - Both roles swapped for 2 turns\n"
          + "  Total Confusion  - Both roles swapped for 3 turns";

        TextArea ta = new TextArea(text);
        ta.setEditable(false);
        ta.setWrapText(false);
        ta.setPrefHeight(210);
        ta.setStyle("-fx-control-inner-background: #0f3460; -fx-text-fill: #cccccc; -fx-font-size: 12px; -fx-font-family: monospace;");

        TitledPane pane = new TitledPane("Game Instructions  (click to expand)", ta);
        pane.setExpanded(false);
        pane.setStyle("-fx-text-fill: #a8dadc; -fx-font-size: 13px;");
        return pane;
    }

    private void startGame() {
        Role role = (Role) roleGroup.getSelectedToggle().getUserData();
        new GameController(stage, role);
    }
}
