package com.example.acceltone;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.renderer.spi.BasicDataSetRenderer;
import io.fair_acc.dataset.DataSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SigGenPresenter {

    public TextField PRI;
    public TextField Mittenfrequenz;
    public TextField Bandbreite;
    public TextField Anz_Pulse;
    public TextField Pulsbreite;
    public Button AudioAbspielen;
    public CheckBox calculateFFT;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Button menuButton;
    @FXML private VBox vBox;
    @FXML private RadioButton cwRadioButton;
    @FXML private RadioButton chirpRadioButton;

    private XYChart chart2;

    private Stage stage;
    private Parent root;
    private Scene scene;
    private volatile boolean isZooming = false;

    @FXML
    public void initialize() {
        SignalGenerator signalGenerator = new SignalGenerator();

        calculateFFT.selectedProperty().addListener((observable, oldValue, isSelected) -> {
            if (isSelected) {
                SignalGenerator.calculateFFT();
                chart2 = createChart(SignalGenerator.fspectra, SignalGenerator.xAxisf, SignalGenerator.yAxisf);
                vBox.getChildren().add(chart2);
            } else {
                vBox.getChildren().remove(chart2);
            }
        });

        vBox.setPrefWidth(Config.screenWidth - 50);

        startButton.setOnAction(event -> {
            getValuesFromTextFields();
            signalGenerator.startSinusWave();
            AudioPlayer.save();
        });

        stopButton.setDisable(true);
        AudioPlayer.isPlayingProperty.addListener((obs, oldVal, newVal) -> {
            AudioAbspielen.setDisable(newVal);
            stopButton.setDisable(!newVal);
        });

        stopButton.setOnAction(event -> AudioPlayer.pause());
        menuButton.setOnAction(this::switchToMenu);
        AudioAbspielen.setOnAction(event -> AudioPlayer.play());

        signalGenerator.setRadioButtonStateFunctions(cwRadioButton::isSelected, chirpRadioButton::isSelected);
    }

    private void getValuesFromTextFields() {
        int anzPulseValue = Integer.parseInt(Anz_Pulse.getText());
        int mittenfrequenzValue = Integer.parseInt(Mittenfrequenz.getText());
        int bandbreiteValue = Integer.parseInt(Bandbreite.getText());
        double priValue = Double.parseDouble(PRI.getText());
        double pulsBreiteValue = Double.parseDouble(Pulsbreite.getText());

        SignalGenerator.Anz_Pulse = anzPulseValue;
        SignalGenerator.Mittenfrequenz = mittenfrequenzValue;
        SignalGenerator.Bandbreite = bandbreiteValue;
        SignalGenerator.PRI = priValue;
        SignalGenerator.Pulsbreite = pulsBreiteValue;
    }

    private XYChart createChart(DataSet dataSet, DefaultNumericAxis xAxis, DefaultNumericAxis yAxis) {
        XYChart chart = new XYChart (xAxis, yAxis);
        chart.setLegendVisible(true);
        chart.setAnimated(false);

        BasicDataSetRenderer dataSetRenderer = new BasicDataSetRenderer();
        dataSetRenderer.getDatasets().add(dataSet);
        chart.getRenderers().add(dataSetRenderer);
        chart.prefWidthProperty().bind(vBox.widthProperty());
        return chart;
    }

    private void switchToMenu(ActionEvent event) {
        try {
            Config.setRunning(false);
            root = FXMLLoader.load(getClass().getResource("main.fxml"));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root, Config.screenWidth, Config.screenHeight);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

