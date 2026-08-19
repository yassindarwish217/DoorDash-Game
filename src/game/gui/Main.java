package game.gui;

import game.gui.view.StartView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DoorDasH: Scare vs Laugh Touchdown");
        primaryStage.setResizable(false);
        new StartView(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
