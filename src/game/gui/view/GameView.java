package game.gui.view;

import game.engine.*;
import game.engine.cards.*;
import game.engine.cells.CardCell;
import game.engine.cells.ContaminationSock;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.DoorCell;
import game.engine.cells.MonsterCell;
import game.engine.monsters.*;
import game.gui.controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class GameView {

    private static final int CELL_SIZE = 52;

    private final GameController controller;
    private final Game game;

    // Board cells
    private final StackPane[] cellNodes = new StackPane[100];

    // Monster panel labels  [0=name, 1=type, 2=origRole, 3=currRole, 4=energy, 5=pos, 6=statusHdr, 7=shield, 8=frozen, 9=confusion, 10=momentum, 11=focus, 12=progress, 13=powerupCost]
    private Label[] playerLabels;
    private Label[] opponentLabels;

    // Header
    private Label turnLabel;
    private Label currentTurnLabel;
    private Label diceLabel;

    // Actions
    private Button powerupBtn;
    private Button rollBtn;
    private Label frozenWarningLabel;

    // Card area
    private Label cardNameLabel;
    private Label cardEffectLabel;
    private Label deckLabel;

    // Log
    private TextArea logArea;

    public GameView(GameController controller, Game game) {
        this.controller = controller;
        this.game = game;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    public void show(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");
        root.setTop(buildHeader());
        root.setCenter(buildCenter());
        root.setBottom(buildBottom());
        stage.setScene(new Scene(root, 960, 840));
    }

    public void updateAll(String logMessage) {
        refreshBoard();
        refreshPanel(game.getPlayer(),   playerLabels);
        refreshPanel(game.getOpponent(), opponentLabels);
        updateTurnIndicator();
        deckLabel.setText("Deck: " + Board.getCards().size() + " cards");
        appendLog(logMessage);
    }

    public void setButtonStates(boolean powerupEnabled, boolean rollEnabled) {
        powerupBtn.setDisable(!powerupEnabled);
        rollBtn.setDisable(!rollEnabled);
        powerupBtn.setStyle(actionBtnStyle(powerupEnabled ? "#8e44ad" : "#555555"));
        rollBtn.setStyle(actionBtnStyle(rollEnabled ? "#27ae60" : "#555555"));
        updateFrozenWarning();
    }

    public void updateTurnIndicator() {
        Monster cur = game.getCurrent();
        turnLabel.setText("Turn: " + controller.getTurnNumber());
        currentTurnLabel.setText(cur.getName() + "'s Turn"
                + (cur.getRole() != cur.getOriginalRole() ? "  [CONFUSED]" : ""));
        int roll = game.getLastDiceRoll();
        diceLabel.setText("Dice: " + (roll == 0 ? "-" : String.valueOf(roll)));
        updateFrozenWarning();
    }

    public void showCardDrawn(Card card) {
        cardNameLabel.setText("Last Card: " + card.getName());
        cardEffectLabel.setText("  " + card.getDescription());
    }

    public void clearCardDrawn() {
        cardEffectLabel.setText("");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BUILD — HEADER
    // ─────────────────────────────────────────────────────────────────────────

    private HBox buildHeader() {
        HBox bar = new HBox(16);
        bar.setPadding(new Insets(9, 16, 9, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #0f3460;");

        Text logo = new Text("DoorDasH");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        logo.setFill(Color.web("#e94560"));

        turnLabel       = styledLabel("Turn: 1",      "#a8dadc", 14, true);
        currentTurnLabel = styledLabel("",             "#ffffff", 14, true);
        diceLabel       = styledLabel("Dice: -",      "#f1c40f", 16, true);

        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        bar.getChildren().addAll(logo, gap, turnLabel, pipe(), currentTurnLabel, pipe(), diceLabel);
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BUILD — CENTER (left panel + board column + right panel)
    // ─────────────────────────────────────────────────────────────────────────

    private HBox buildCenter() {
        playerPanel   = buildMonsterPanel("YOUR MONSTER", true);
        opponentPanel = buildMonsterPanel("OPPONENT",     false);

        VBox mid = new VBox(6);
        mid.setAlignment(Pos.TOP_CENTER);
        mid.getChildren().addAll(buildBoardPane(), buildActionPanel(), buildCardPanel());

        HBox center = new HBox(8, playerPanel, mid, opponentPanel);
        center.setPadding(new Insets(8, 10, 4, 10));
        center.setAlignment(Pos.TOP_CENTER);
        return center;
    }

    // Monster panel fields (reused by both panels)
    private VBox playerPanel;
    private VBox opponentPanel;

    private VBox buildMonsterPanel(String title, boolean isPlayer) {
        VBox box = new VBox(5);
        box.setPrefWidth(172);
        box.setMinWidth(172);
        box.setMaxWidth(172);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #16213e; -fx-background-radius: 8; "
                   + "-fx-border-color: #444; -fx-border-radius: 8;");

        Label titleLbl = styledLabel(title, isPlayer ? "#bb86fc" : "#ffb347", 13, true);
        titleLbl.setMaxWidth(Double.MAX_VALUE);
        titleLbl.setAlignment(Pos.CENTER);

        Label[] lbs = new Label[14];
        for (int i = 0; i < 14; i++) lbs[i] = styledLabel("", "#cccccc", 12, false);

        if (isPlayer) playerLabels = lbs;
        else          opponentLabels = lbs;

        box.getChildren().addAll(titleLbl, new Separator());
        for (Label lb : lbs) box.getChildren().add(lb);

        refreshPanel(isPlayer ? game.getPlayer() : game.getOpponent(), lbs);
        return box;
    }

    private void refreshPanel(Monster m, Label[] lbs) {
        boolean confused = m.isConfused();

        setLbl(lbs[0], "Name: " + m.getName(), "#ffffff", 12, true);
        setLbl(lbs[1], "Type: " + monsterTypeName(m), "#a8dadc", 12, false);
        setLbl(lbs[2], "Orig Role: " + m.getOriginalRole(),
               m.getOriginalRole() == Role.SCARER ? "#bb86fc" : "#ffb347", 12, false);

        String currColor = confused ? "#e74c3c" : (m.getRole() == Role.SCARER ? "#bb86fc" : "#ffb347");
        setLbl(lbs[3], "Curr Role: " + m.getRole() + (confused ? "  [CONFUSED]" : ""),
               currColor, 12, confused);

        int energy = m.getEnergy();
        String eColor = energy >= Constants.WINNING_ENERGY ? "#2ecc71"
                      : energy >= 500 ? "#f1c40f"
                      : energy >= 200 ? "#e67e22" : "#e74c3c";
        setLbl(lbs[4], "Energy: " + energy, eColor, 15, true);
        setLbl(lbs[5], "Position: Cell " + m.getPosition(), "#cccccc", 12, false);
        setLbl(lbs[6], "─── Status ───", "#666666", 10, false);

        setLbl(lbs[7],  m.isShielded()  ? "[SHIELD] active"                 : "", "#3498db", 11, false);
        setLbl(lbs[8],  m.isFrozen()    ? "[FROZEN] next turn skipped!"     : "", "#74b9ff", 11, true);
        setLbl(lbs[9],  confused        ? "[CONFUSED] " + m.getConfusionTurns() + " turn(s)" : "", "#e74c3c", 11, false);

        String momentum = "";
        if (m instanceof Dasher && ((Dasher)m).getMomentumTurns() > 0)
            momentum = "[MOMENTUM] " + ((Dasher)m).getMomentumTurns() + " turn(s)";
        setLbl(lbs[10], momentum, "#f1c40f", 11, false);

        String focus = "";
        if (m instanceof MultiTasker && ((MultiTasker)m).getNormalSpeedTurns() > 0)
            focus = "[FOCUS] " + ((MultiTasker)m).getNormalSpeedTurns() + " turn(s)";
        setLbl(lbs[11], focus, "#f1c40f", 11, false);

        boolean atWin = m.getPosition() == Constants.WINNING_POSITION;
        boolean enoughE = energy >= Constants.WINNING_ENERGY;
        setLbl(lbs[12],
               atWin && enoughE ? "*** WINNER! ***"
             : atWin ? "At cell 99! Need " + (Constants.WINNING_ENERGY - energy) + " more energy"
             : enoughE ? "Has 1000+ energy! Reach cell 99!"
             : "", "#2ecc71", 11, true);

        setLbl(lbs[13], "Powerup cost: " + Constants.POWERUP_COST, "#555555", 10, false);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BUILD — BOARD
    // ─────────────────────────────────────────────────────────────────────────

    private GridPane buildBoardPane() {
        GridPane grid = new GridPane();
        grid.setHgap(1);
        grid.setVgap(1);
        grid.setStyle("-fx-background-color: #111111; -fx-border-color: #555; -fx-border-width: 1;");

        for (int i = 0; i < 100; i++) {
            cellNodes[i] = makeCellPane(i);
            int[] vp = toVisual(i);
            grid.add(cellNodes[i], vp[1], vp[0]);
        }
        return grid;
    }

    private void refreshBoard() {
        Monster player   = game.getPlayer();
        Monster opponent = game.getOpponent();
        for (int i = 0; i < 100; i++)
            fillCell(cellNodes[i], i, player, opponent);
    }

    private StackPane makeCellPane(int index) {
        StackPane sp = new StackPane();
        sp.setPrefSize(CELL_SIZE, CELL_SIZE);
        sp.setMinSize(CELL_SIZE, CELL_SIZE);
        sp.setMaxSize(CELL_SIZE, CELL_SIZE);
        fillCell(sp, index, game.getPlayer(), game.getOpponent());
        return sp;
    }

    private void fillCell(StackPane pane, int index, Monster player, Monster opponent) {
        pane.getChildren().clear();
        game.engine.cells.Cell cell = game.getBoard().getCellByIndex(index);

        Rectangle bg = new Rectangle(CELL_SIZE, CELL_SIZE);
        bg.setFill(Color.web(cellBg(cell, index)));
        bg.setStroke(Color.web("#1a1a1a"));
        bg.setStrokeWidth(0.5);
        pane.getChildren().add(bg);

        VBox content = new VBox(0);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(1, 1, 1, 2));
        content.setMouseTransparent(true);

        Text idxTxt = new Text(String.valueOf(index));
        idxTxt.setFont(Font.font("Arial", FontWeight.BOLD, 8));
        idxTxt.setFill(Color.web(index == 99 ? "#ffff00" : "#ffffff99"));
        content.getChildren().add(idxTxt);

        String info = cellInfoText(cell);
        if (!info.isEmpty()) {
            Text infoTxt = new Text(info);
            infoTxt.setFont(Font.font("Arial", 7));
            infoTxt.setFill(Color.WHITE);
            infoTxt.setWrappingWidth(CELL_SIZE - 3);
            content.getChildren().add(infoTxt);
        }
        pane.getChildren().add(content);

        // Player/opponent tokens
        boolean pHere = player.getPosition()   == index;
        boolean oHere = opponent.getPosition() == index;
        if (pHere || oHere) {
            HBox tokens = new HBox(2);
            tokens.setAlignment(Pos.BOTTOM_CENTER);
            StackPane.setAlignment(tokens, Pos.BOTTOM_CENTER);
            tokens.setPadding(new Insets(0, 0, 2, 0));
            if (pHere) tokens.getChildren().add(makeToken(player,   "#2980b9"));
            if (oHere) tokens.getChildren().add(makeToken(opponent, "#c0392b"));
            pane.getChildren().add(tokens);
        }

        // Tooltip
        Tooltip tp = new Tooltip(cellTooltip(cell, index));
        Tooltip.install(pane, tp);
    }

    private StackPane makeToken(Monster m, String color) {
        Circle c = new Circle(9);
        c.setFill(Color.web(color));
        c.setStroke(Color.WHITE);
        c.setStrokeWidth(1.2);
        Text t = new Text(m.getName().substring(0, 1).toUpperCase());
        t.setFont(Font.font("Arial", FontWeight.BOLD, 8));
        t.setFill(Color.WHITE);
        return new StackPane(c, t);
    }

    private String cellBg(game.engine.cells.Cell cell, int index) {
        if (cell instanceof DoorCell) {
            DoorCell d = (DoorCell) cell;
            if (d.getRole() == Role.SCARER)
                return d.isActivated() ? "#4a1060" : "#8e44ad";
            else
                return d.isActivated() ? "#7e3200" : "#d35400";
        }
        if (cell instanceof CardCell)          return "#b7950b";
        if (cell instanceof ConveyorBelt)      return "#148f77";
        if (cell instanceof ContaminationSock) return "#922b21";
        if (cell instanceof MonsterCell)       return "#1e6b3a";
        if (index == 0)                        return "#1a5c0a";
        if (index == 99)                       return "#1a5276";
        return "#2c3e50";
    }

    private String cellInfoText(game.engine.cells.Cell cell) {
        if (cell instanceof DoorCell) {
            DoorCell d = (DoorCell) cell;
            String tag = d.getRole() == Role.SCARER ? "S" : "L";
            return tag + ":" + (d.isActivated() ? "USED" : d.getEnergy());
        }
        if (cell instanceof ConveyorBelt)
            return "+" + ((ConveyorBelt) cell).getEffect();
        if (cell instanceof ContaminationSock)
            return "" + ((ContaminationSock) cell).getEffect();
        if (cell instanceof MonsterCell) {
            String nm = ((MonsterCell) cell).getCellMonster().getName();
            return nm.length() > 8 ? nm.substring(0, 8) : nm;
        }
        if (cell instanceof CardCell) return "CARD";
        return "";
    }

    private String cellTooltip(game.engine.cells.Cell cell, int index) {
        StringBuilder sb = new StringBuilder("Cell " + index + "\n");
        sb.append("Type: ").append(cellTypeName(cell)).append("\n");
        if (cell instanceof DoorCell) {
            DoorCell d = (DoorCell) cell;
            sb.append("Name: ").append(cell.getName()).append("\n");
            sb.append("Role: ").append(d.getRole()).append("\n");
            sb.append("Energy: ").append(d.getEnergy()).append("\n");
            sb.append("Activated: ").append(d.isActivated());
        } else if (cell instanceof ConveyorBelt) {
            sb.append("Name: ").append(cell.getName()).append("\n");
            sb.append("Effect: +").append(((ConveyorBelt) cell).getEffect()).append(" cells");
        } else if (cell instanceof ContaminationSock) {
            sb.append("Name: ").append(cell.getName()).append("\n");
            sb.append("Effect: ").append(((ContaminationSock) cell).getEffect()).append(" cells + -100 energy");
        } else if (cell instanceof MonsterCell) {
            Monster m = ((MonsterCell) cell).getCellMonster();
            sb.append("Monster: ").append(m.getName()).append("\n");
            sb.append("Role: ").append(m.getRole()).append("\n");
            sb.append("Energy: ").append(m.getEnergy());
        }
        return sb.toString();
    }

    private String cellTypeName(game.engine.cells.Cell cell) {
        if (cell instanceof DoorCell)          return ((DoorCell)cell).getRole() + " Door";
        if (cell instanceof CardCell)          return "Card Cell";
        if (cell instanceof ConveyorBelt)      return "Conveyor Belt";
        if (cell instanceof ContaminationSock) return "Contamination Sock";
        if (cell instanceof MonsterCell)       return "Monster Cell";
        return "Normal";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BUILD — ACTION PANEL
    // ─────────────────────────────────────────────────────────────────────────

    private VBox buildActionPanel() {
        frozenWarningLabel = styledLabel("", "#74b9ff", 12, true);
        frozenWarningLabel.setMaxWidth(Double.MAX_VALUE);
        frozenWarningLabel.setAlignment(Pos.CENTER);

        powerupBtn = new Button("Use Powerup  (cost: " + Constants.POWERUP_COST + " energy)");
        powerupBtn.setStyle(actionBtnStyle("#8e44ad"));
        powerupBtn.setOnMouseEntered(e -> { if (!powerupBtn.isDisabled()) powerupBtn.setStyle(actionBtnStyle("#6c3483")); });
        powerupBtn.setOnMouseExited(e  -> { if (!powerupBtn.isDisabled()) powerupBtn.setStyle(actionBtnStyle("#8e44ad")); });
        powerupBtn.setOnAction(e -> controller.handleUsePowerup());

        rollBtn = new Button("Roll Dice  (move monster)");
        rollBtn.setStyle(actionBtnStyle("#27ae60"));
        rollBtn.setOnMouseEntered(e -> { if (!rollBtn.isDisabled()) rollBtn.setStyle(actionBtnStyle("#1e8449")); });
        rollBtn.setOnMouseExited(e  -> { if (!rollBtn.isDisabled()) rollBtn.setStyle(actionBtnStyle("#27ae60")); });
        rollBtn.setOnAction(e -> controller.handleRollDice());

        HBox btnRow = new HBox(16, powerupBtn, rollBtn);
        btnRow.setAlignment(Pos.CENTER);

        VBox panel = new VBox(4, frozenWarningLabel, btnRow);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(4, 0, 2, 0));
        return panel;
    }

    private String actionBtnStyle(String color) {
        return "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 13px; "
             + "-fx-font-weight: bold; -fx-padding: 9 22; -fx-background-radius: 6; -fx-cursor: hand;";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BUILD — CARD PANEL
    // ─────────────────────────────────────────────────────────────────────────

    private HBox buildCardPanel() {
        cardNameLabel   = styledLabel("Last Card: none", "#f1c40f", 12, true);
        cardEffectLabel = styledLabel("", "#cccccc", 11, false);
        deckLabel       = styledLabel("Deck: " + Board.getCards().size() + " cards", "#aaaaaa", 11, false);

        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);

        HBox panel = new HBox(10, cardNameLabel, cardEffectLabel, gap, deckLabel);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(5, 10, 5, 10));
        panel.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 4;");
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BUILD — BOTTOM (log)
    // ─────────────────────────────────────────────────────────────────────────

    private VBox buildBottom() {
        Label logTitle = styledLabel("Game Log", "#a8dadc", 12, true);
        logTitle.setPadding(new Insets(5, 0, 2, 10));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(118);
        logArea.setStyle("-fx-control-inner-background: #0d1117; -fx-text-fill: #cccccc; "
                       + "-fx-font-family: monospace; -fx-font-size: 11px;");

        VBox bottom = new VBox(0, logTitle, logArea);
        bottom.setPadding(new Insets(2, 10, 8, 10));
        return bottom;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void appendLog(String msg) {
        if (msg == null || msg.isEmpty()) return;
        logArea.appendText(msg + "\n");
        logArea.setScrollTop(Double.MAX_VALUE);
    }

    private void updateFrozenWarning() {
        Monster cur = game.getCurrent();
        frozenWarningLabel.setText(cur.isFrozen()
                ? "*** " + cur.getName() + " is FROZEN — rolling will skip their turn! ***"
                : "");
    }

    /** Convert board index to GridPane (row, col) with cell 0 at bottom-left. */
    private int[] toVisual(int index) {
        int engineRow = index / 10;
        int engineCol = (engineRow % 2 == 0) ? (index % 10) : (9 - index % 10);
        return new int[]{9 - engineRow, engineCol};
    }

    private String monsterTypeName(Monster m) {
        if (m instanceof Dasher)      return "Dasher";
        if (m instanceof Dynamo)      return "Dynamo";
        if (m instanceof MultiTasker) return "MultiTasker";
        if (m instanceof Schemer)     return "Schemer";
        return "?";
    }

    private Label styledLabel(String text, String color, int size, boolean bold) {
        Label lb = new Label(text);
        lb.setWrapText(true);
        setLbl(lb, text, color, size, bold);
        return lb;
    }

    private void setLbl(Label lb, String text, String color, int size, boolean bold) {
        lb.setText(text);
        lb.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + size + "px;"
                  + (bold ? " -fx-font-weight: bold;" : ""));
    }

    private Node pipe() {
        Label l = new Label("  |  ");
        l.setStyle("-fx-text-fill: #444444;");
        return l;
    }
}
