package com.example.acceltone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class StartApp extends Application {

    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;

        AnchorPane root = FXMLLoader.load(getClass().getResource("main.fxml"));
        Scene scene = new Scene(root, Config.screenWidth, Config.screenHeight);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

