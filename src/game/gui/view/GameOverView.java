package game.gui.view;

import game.engine.Game;
import game.engine.monsters.Monster;
import game.gui.controller.GameController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class GameOverView {

    public GameOverView(Stage stage, Game game, Monster winner, GameController controller) {
        buildUI(stage, game, winner, controller);
    }

    private void buildUI(Stage stage, Game game, Monster winner, GameController controller) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: #1a1a2e;");

        Text trophy = new Text("GAME OVER!");
        trophy.setFont(Font.font("Arial", FontWeight.BOLD, 52));
        trophy.setStyle("-fx-fill: #f1c40f;");

        Text winnerTitle = new Text("Winner:");
        winnerTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        winnerTitle.setStyle("-fx-fill: #a8dadc;");

        Text winnerName = new Text(winner.getName());
        winnerName.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        winnerName.setStyle("-fx-fill: #e94560;");

        Text winnerRole = new Text("Role: " + winner.getOriginalRole()
                + (winner.isConfused() ? "  [confused -> " + winner.getRole() + "]" : ""));
        winnerRole.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        winnerRole.setStyle("-fx-fill: " + (winner.getOriginalRole().name().equals("SCARER") ? "#bb86fc" : "#ffb347") + ";");

        // Final energies
        Monster player   = game.getPlayer();
        Monster opponent = game.getOpponent();

        GridPane energyGrid = new GridPane();
        energyGrid.setHgap(30);
        energyGrid.setVgap(8);
        energyGrid.setAlignment(Pos.CENTER);
        energyGrid.setPadding(new Insets(20, 40, 20, 40));
        energyGrid.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        addEnergyRow(energyGrid, 0, "Monster", "Role", "Final Energy", "Position", true);
        addEnergyRow(energyGrid, 1, player.getName(), player.getOriginalRole().toString(),
                     String.valueOf(player.getEnergy()), "Cell " + player.getPosition(), false);
        addEnergyRow(energyGrid, 2, opponent.getName(), opponent.getOriginalRole().toString(),
                     String.valueOf(opponent.getEnergy()), "Cell " + opponent.getPosition(), false);

        Button playAgainBtn = new Button("Play Again");
        playAgainBtn.setStyle(btnStyle("#27ae60"));
        playAgainBtn.setOnMouseEntered(e -> playAgainBtn.setStyle(btnStyle("#1e8449")));
        playAgainBtn.setOnMouseExited(e -> playAgainBtn.setStyle(btnStyle("#27ae60")));
        playAgainBtn.setOnAction(e -> controller.restartGame());

        root.getChildren().addAll(trophy, winnerTitle, winnerName, winnerRole, energyGrid, playAgainBtn);

        Scene scene = new Scene(root, 700, 620);
        stage.setScene(scene);
    }

    private void addEnergyRow(GridPane grid, int row, String name, String role, String energy, String pos, boolean header) {
        String color = header ? "#a8dadc" : "#ffffff";
        int size     = header ? 13 : 14;
        boolean bold = header;

        grid.add(styledText(name,   color, size, bold), 0, row);
        grid.add(styledText(role,   color, size, bold), 1, row);
        grid.add(styledText(energy, color, size, bold), 2, row);
        grid.add(styledText(pos,    color, size, bold), 3, row);
    }

    private Text styledText(String str, String color, int size, boolean bold) {
        Text t = new Text(str);
        t.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        t.setStyle("-fx-fill: " + color + ";");
        return t;
    }

    private String btnStyle(String color) {
        return "-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 16px; "
             + "-fx-font-weight: bold; -fx-padding: 12 36; -fx-background-radius: 6; -fx-cursor: hand;";
    }
}
