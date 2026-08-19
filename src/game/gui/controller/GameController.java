package game.gui.controller;

import game.engine.*;
import game.engine.cards.*;
import game.engine.exceptions.*;
import game.engine.monsters.*;
import game.gui.view.GameOverView;
import game.gui.view.GameView;
import game.gui.view.StartView;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class GameController {

    private Game game;
    private final Stage stage;
    private GameView gameView;
    private boolean powerupUsedThisTurn = false;
    private boolean rolledThisTurn = false;
    private int turnNumber = 1;

    public GameController(Stage stage, Role playerRole) {
        this.stage = stage;
        try {
            game = new Game(playerRole);
        } catch (IOException e) {
            showError("Failed to load game data:\n" + e.getMessage());
            return;
        }
        gameView = new GameView(this, game);
        gameView.show(stage);
        gameView.updateAll("Game started! Good luck!\n--- " + game.getCurrent().getName() + "'s Turn ---");
    }

    public void handleUsePowerup() {
        if (rolledThisTurn) {
            showError("You already rolled the dice.\nYou cannot use your powerup after rolling.");
            return;
        }
        if (powerupUsedThisTurn) {
            showError("You already used your powerup this turn.");
            return;
        }

        Monster current = game.getCurrent();
        int energyBefore = current.getEnergy();

        try {
            game.usePowerup();
            powerupUsedThisTurn = true;

            String effect;
            if (current instanceof Dasher)
                effect = "Momentum Rush activated! 3x speed for 3 turns.";
            else if (current instanceof Dynamo)
                effect = "Opponent will be FROZEN next turn!";
            else if (current instanceof MultiTasker)
                effect = "Focus Mode activated! Normal speed for 2 turns.";
            else if (current instanceof Schemer)
                effect = "Chain Attack! Stole energy from all enemies.";
            else
                effect = "Powerup activated!";

            String msg = current.getName() + " used powerup! Energy: " + energyBefore
                    + " -> " + current.getEnergy() + " (-" + Constants.POWERUP_COST + ")\n" + effect;

            gameView.updateAll(msg);
            gameView.setButtonStates(false, true);

        } catch (OutOfEnergyException e) {
            showError("Cannot use powerup!\n" + e.getMessage()
                    + "\nCurrent energy: " + current.getEnergy()
                    + " / Required: " + Constants.POWERUP_COST);
        }
    }

    public void handleRollDice() {
        if (rolledThisTurn) {
            showError("You already rolled the dice this turn!");
            return;
        }

        Monster currentBefore = game.getCurrent();
        Monster opponentBefore = (currentBefore == game.getPlayer()) ? game.getOpponent() : game.getPlayer();

        boolean wasFrozen = currentBefore.isFrozen();

        // Snapshot
        int posCurrentBefore     = currentBefore.getPosition();
        int energyCurrentBefore  = currentBefore.getEnergy();
        int posOpponentBefore    = opponentBefore.getPosition();
        int energyOpponentBefore = opponentBefore.getEnergy();
        Role roleCurrentBefore   = currentBefore.getRole();
        Role roleOpponentBefore  = opponentBefore.getRole();
        boolean opponentFrozenBefore = opponentBefore.isFrozen();

        currentBefore.shieldBlockedLastHit  = false;
        opponentBefore.shieldBlockedLastHit = false;
        Board.setLastDrawnCard(null);

        try {
            game.playTurn();
        } catch (InvalidMoveException e) {
            showError("Invalid Move!\n" + e.getMessage()
                    + "\n" + currentBefore.getName() + " stays at cell " + currentBefore.getPosition() + ".\nRoll again.");
            gameView.updateAll("Invalid move for " + currentBefore.getName() + " - staying at cell " + currentBefore.getPosition() + ". Roll again.");
            return;
        }

        rolledThisTurn = true;
        StringBuilder log = new StringBuilder();

        if (wasFrozen) {
            log.append("*** FROZEN! *** ").append(currentBefore.getName())
               .append(" was frozen and their turn was SKIPPED!\n");
        } else {
            int roll = game.getLastDiceRoll();
            log.append(currentBefore.getName()).append(" rolled a ").append(roll).append("!\n");

            if (currentBefore.getPosition() != posCurrentBefore)
                log.append("  Moved: Cell ").append(posCurrentBefore).append(" -> Cell ").append(currentBefore.getPosition()).append("\n");

            if (opponentBefore.getPosition() != posOpponentBefore)
                log.append("  ").append(opponentBefore.getName()).append(" moved: Cell ").append(posOpponentBefore)
                   .append(" -> Cell ").append(opponentBefore.getPosition()).append("\n");

            if (currentBefore.shieldBlockedLastHit)
                log.append("  SHIELD blocked damage for ").append(currentBefore.getName()).append("!\n");
            if (opponentBefore.shieldBlockedLastHit)
                log.append("  SHIELD blocked damage for ").append(opponentBefore.getName()).append("!\n");

            if (currentBefore.getEnergy() != energyCurrentBefore) {
                int diff = currentBefore.getEnergy() - energyCurrentBefore;
                log.append("  ").append(currentBefore.getName()).append(" energy: ")
                   .append(energyCurrentBefore).append(" -> ").append(currentBefore.getEnergy())
                   .append(" (").append(diff > 0 ? "+" : "").append(diff).append(")\n");
            }
            if (opponentBefore.getEnergy() != energyOpponentBefore) {
                int diff = opponentBefore.getEnergy() - energyOpponentBefore;
                log.append("  ").append(opponentBefore.getName()).append(" energy: ")
                   .append(energyOpponentBefore).append(" -> ").append(opponentBefore.getEnergy())
                   .append(" (").append(diff > 0 ? "+" : "").append(diff).append(")\n");
            }

            Card card = Board.getLastDrawnCard();
            if (card != null) {
                gameView.showCardDrawn(card);
                log.append("  [CARD] ").append(card.getName()).append(": ").append(card.getDescription()).append("\n");
            } else {
                gameView.clearCardDrawn();
            }

            if (currentBefore.getRole() != roleCurrentBefore || opponentBefore.getRole() != roleOpponentBefore)
                log.append("  CONFUSION! Roles swapped!\n");

            if (opponentBefore.isFrozen() && !opponentFrozenBefore)
                log.append("  *** ").append(opponentBefore.getName()).append(" is now FROZEN! ***\n");
        }

        Monster winner = game.getWinner();
        if (winner != null) {
            gameView.updateAll(log.toString().trim());
            new GameOverView(stage, game, winner, this);
            return;
        }

        powerupUsedThisTurn = false;
        rolledThisTurn = false;
        turnNumber++;

        log.append("--- Turn ").append(turnNumber).append(": ").append(game.getCurrent().getName()).append("'s Turn ---");
        gameView.updateAll(log.toString().trim());
        gameView.setButtonStates(true, true);
    }

    public void restartGame() {
        new StartView(stage);
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public boolean isPowerupUsedThisTurn() {
        return powerupUsedThisTurn;
    }

    public Game getGame() {
        return game;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Action Not Allowed");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(stage);
        alert.showAndWait();
    }
}
