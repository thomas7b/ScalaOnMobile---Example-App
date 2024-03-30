package com.example.acceltone;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuPresenter {


    @FXML
    private Button accButton;
    @FXML
    private Button sigButton;
    private Stage stage;
    private Parent root;
    private Scene scene;

    @FXML
    public void initialize() {
        accButton.setOnAction(this::switchToAccelerator);
        sigButton.setOnAction(this::switchToSigGen);
    }

    @FXML
    private void switchToAccelerator(ActionEvent event) {
        try {
            root = FXMLLoader.load(getClass().getResource("accelerator.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root, Config.screenWidth, Config.screenHeight);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToSigGen(ActionEvent event) {
        try {
            root = FXMLLoader.load(getClass().getResource("signalgenerator.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root, Config.screenWidth, Config.screenHeight);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

