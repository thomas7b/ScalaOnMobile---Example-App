package com.example.acceltone;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.plugins.EditAxis;
import io.fair_acc.chartfx.renderer.spi.BasicDataSetRenderer;
import io.fair_acc.dataset.spi.DoubleDataSet;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AccPresenter {
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button menuButton;
    @FXML
    private Button exportButton;
    @FXML
    private Button exportCSVButton;
    @FXML
    private VBox VBox;
    @FXML
    private CheckBox filterGravButton;
    @FXML
    private Slider timeSlider;
    @FXML
    private RadioButton liveRButton;
    @FXML
    private RadioButton timeRButton;
    @FXML
    private CheckBox CheckBoxX;
    @FXML
    private CheckBox CheckBoxY;
    @FXML
    private CheckBox CheckBoxZ;

    private Stage Stage;
    private Parent root;
    private Scene scene;
    private BasicDataSetRenderer XRenderer = new BasicDataSetRenderer();
    private BasicDataSetRenderer YRenderer = new BasicDataSetRenderer();
    private BasicDataSetRenderer ZRenderer = new BasicDataSetRenderer();

    @FXML
    public void initialize() {
        XYChart chart = createChart(Accelerometer.accDataX, Accelerometer.accDataY, Accelerometer.accDataZ, Accelerometer.xAxis, Accelerometer.yAxis);
        VBox.getChildren().add(chart);

        VBox.setPrefWidth(Config.screenWidth - 50);
        VBox.setPrefHeight(350);

        Accelerometer.setCheckboxStateFunctions(
                () -> CheckBoxX.isSelected(),
                () -> CheckBoxY.isSelected(),
                () -> CheckBoxZ.isSelected()
        );

        startButton.setOnAction(event -> {
            Config.setRunning(true);

            Accelerometer.setFilterGravity(filterGravButton.isSelected());
            Accelerometer.clearData();
            if (liveRButton.isSelected()) {
                Accelerometer.startAccelerator(500);
            } else if (timeRButton.isSelected()) {
                double sliderValue = timeSlider.getValue();
                Accelerometer.startAccelerator(sliderValue);
            }
            chart.getXAxis().setAutoGrowRanging(true);
            chart.getXAxis().setAutoRanging(true);
        });

        menuButton.setOnAction(this::switchToMenu);
        exportCSVButton.setOnAction(event -> Accelerometer.exportDataToCSV(Stage));

        exportButton.setOnAction(event -> {
            VBox.setPrefWidth(1200);
            VBox.setPrefHeight(600);
            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(e -> {
                Accelerometer.exportScreenshot();
                VBox.setPrefWidth(Config.screenWidth - 50);
                VBox.setPrefHeight(350);
            });
            pause.play();
        });

        Config.isRunningProperty.addListener((observable, oldValue, newValue) -> {
            startButton.setDisable(newValue);
            stopButton.setDisable(!newValue);
            filterGravButton.setDisable(newValue);
        });

        stopButton.setOnAction(event -> {
            startButton.setDisable(false);
            stopButton.setDisable(true);
            Accelerometer.stopAccelerator();
            Config.setRunning(false);
        });
        AxisConfigurator.initializeTouchControls(chart);
        AxisConfigurator.initializeZoomControls(chart);

        timeRButton.selectedProperty().addListener((observable, oldValue, isSelected) -> {
            timeSlider.setDisable(!isSelected);
        });

        CheckBoxX.selectedProperty().addListener((observable, oldValue, isSelected) -> updateChartVisibility(chart, XRenderer, isSelected));
        CheckBoxY.selectedProperty().addListener((observable, oldValue, isSelected) -> updateChartVisibility(chart, YRenderer, isSelected));
        CheckBoxZ.selectedProperty().addListener((observable, oldValue, isSelected) -> updateChartVisibility(chart, ZRenderer, isSelected));
    }

    private void updateChartVisibility(XYChart chart, BasicDataSetRenderer renderer, boolean isSelected) {
        if (isSelected) {
            chart.getRenderers().add(renderer);
        } else {
            chart.getRenderers().remove(renderer);
        }
    }

    private XYChart createChart(DoubleDataSet dataSetX, DoubleDataSet dataSetY, DoubleDataSet dataSetZ, DefaultNumericAxis xAxis, DefaultNumericAxis yAxis) {
        XYChart chart = new XYChart(xAxis, yAxis);
        chart.legendVisibleProperty().set(true);
        chart.setAnimated(false);

        BasicDataSetRenderer lineRenderer = new BasicDataSetRenderer();
        lineRenderer.getDatasets().addAll(dataSetX, dataSetY, dataSetZ);
        XRenderer.getDatasets().add(dataSetX);
        YRenderer.getDatasets().add(dataSetY);
        ZRenderer.getDatasets().add(dataSetZ);

        chart.getRenderers().add(XRenderer);
        chart.getRenderers().add(YRenderer);
        chart.getRenderers().add(ZRenderer);

        EditAxis editAxisPlugin = new EditAxis();
        chart.getPlugins().add(editAxisPlugin);
        chart.getPlugins().add(Accelerometer.screenshot);
        chart.getToolBar().setDisable(true);
        chart.getToolBar().setOpacity(0);
        chart.prefWidthProperty().bind(VBox.widthProperty());
        chart.prefHeightProperty().bind(VBox.heightProperty());

        return chart;
    }

    private void switchToMenu(ActionEvent event) {
        try {
            Config.setRunning(false);
            root = FXMLLoader.load(getClass().getResource("main.fxml"));
            Stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root, Config.screenWidth, Config.screenHeight);
            Stage.setScene(scene);
            Stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
