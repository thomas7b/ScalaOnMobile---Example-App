package com.example.acceltone;

import com.gluonhq.attach.accelerometer.Acceleration;
import com.gluonhq.attach.accelerometer.AccelerometerService;
import com.gluonhq.attach.accelerometer.Parameters;
import com.gluonhq.attach.share.ShareService;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.plugins.Screenshot;
import io.fair_acc.dataset.spi.DefaultDataSet;
import javafx.application.Platform;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Accelerometer {

    public static javafx.scene.Scene scene;
    public static AccelerometerService service;
    public static File file;
    public static ShareService shareService;
    private static Acceleration acceleration;
    private static double xv, yv, zv;
    private static int i;
    private static final int BUFFER_CAPACITY = 5;
    private static int DATA_BUFFER_CAPACITY;
    public static DefaultDataSet accDataX = new DefaultDataSet("x-axis");
    public static DefaultDataSet accDataY = new DefaultDataSet("y-axis");
    public static DefaultDataSet accDataZ = new DefaultDataSet("z-axis");
    public static DefaultNumericAxis xAxis = new DefaultNumericAxis("time", "s");
    public static DefaultNumericAxis yAxis = new DefaultNumericAxis("acceleration", "m/s²");
    static final Screenshot screenshot = new Screenshot();
    private static long startTime;
    private static ScheduledExecutorService executorService;

    public static boolean filterGravity = true;

    public static BooleanSupplier checkBoxXFunc;
    public static BooleanSupplier checkBoxYFunc;
    public static BooleanSupplier checkBoxZFunc;

    // Method to set the filtering state
    public static void setFilterGravity(boolean filter) {
        filterGravity = filter;
    }

    public static void generateAccelerometerData(double xv, double yv, double zv) {
        double now = (System.currentTimeMillis() - startTime) / 1000.0;
        accDataX.add(now, xv);
        accDataY.add(now, yv);
        accDataZ.add(now, zv);

        double visibleTimeRange = BUFFER_CAPACITY;

        if (now > visibleTimeRange) {
            xAxis.setMin(now - visibleTimeRange);
            xAxis.setMax(now);
            xAxis.setAutoRanging(false);
            yAxis.setAutoRanging(false);
            AxisConfigurator.updateYAxisRange(
                    xAxis,
                    yAxis,
                    List.of(accDataX, accDataY, accDataZ),
                    List.of(Accelerometer.checkBoxXFunc, Accelerometer.checkBoxYFunc, Accelerometer.checkBoxZFunc)
            );
        } else {
            xAxis.setAutoRanging(true);
            yAxis.setAutoRanging(true);
        }
    }

    public static void setCheckboxStateFunctions(
            BooleanSupplier xFunc,
            BooleanSupplier yFunc,
            BooleanSupplier zFunc) {
        checkBoxXFunc = xFunc;
        checkBoxYFunc = yFunc;
        checkBoxZFunc = zFunc;
    }

    public static void clearData() {
        accDataX.clearData();
        accDataY.clearData();
        accDataZ.clearData();
    }

    public static void startAccelerator(double sliderValue) {
        DATA_BUFFER_CAPACITY = (int) (sliderValue * 20);
        startTime = System.currentTimeMillis();
        try {
            Parameters parameters = new Parameters(100.0, filterGravity);
            service = AccelerometerService.create().get();
            System.out.println("Got accelerometer service.");
            service.start(parameters);
            executorService = Executors.newScheduledThreadPool(1);
            executorService.scheduleAtFixedRate(() -> {
                try {
                    acceleration = service.getCurrentAcceleration();
                    xv = acceleration.getX();
                    yv = acceleration.getY();
                    zv = acceleration.getZ();

                    Platform.runLater(() -> generateAccelerometerData(xv, yv, zv));

                    if (accDataX.getDataCount() >= DATA_BUFFER_CAPACITY || !Config.isRunning()) {
                        stopAccelerator();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Timer not found but sensor found");
                }
            }, 0, 50, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            System.out.println("No accelerometer service.");
            executorService = Executors.newScheduledThreadPool(1);

            executorService.scheduleAtFixedRate(() -> {
                try {
                    i++;
                    System.out.println(i);
                    double xv = sin(0.1 * i);
                    double yv = cos(0.1 * i);
                    double zv = sin(0.1 * i) + cos(0.1 * i);

                    Platform.runLater(() -> generateAccelerometerData(xv, yv, zv));

                    if (accDataX.getDataCount() >= DATA_BUFFER_CAPACITY || !Config.isRunning()) {
                        stopAccelerator();
                    }
                } catch (Exception f) {
                    f.printStackTrace();
                    System.out.println("Timer not found");
                }
            }, 0, 50, TimeUnit.MILLISECONDS);
        }
    }

    public static void stopAccelerator() {
        try {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdownNow();
            }
            Config.isRunningProperty.set(false);
            if (service != null) {
                service.stop();
                System.out.println("Accelerometer service stopped.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error stopping the accelerometer thread or service.");
        }
    }

    public static void exportScreenshot() {
        screenshot.setDirectory(Config.EXT_DIR.toString());
        screenshot.setPattern("screenshot.png");
        if (Config.isDesktop) {
            screenshot.screenshotToFile(true);
        } else {
            screenshot.screenshotToFile(false);
            File file = new File(Config.EXT_DIR, "screenshot.png");
            shareService = ShareService.create().get();
            System.out.println("Got accelerometer service.");
            shareService.share("image/png", file);
        }
    }

    public static void exportDataToCSV(javafx.stage.Stage primaryStage) {
        File file;
        if (Config.isDesktop) {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("CSV-Datei speichern");
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("CSV-Dateien", "*.csv"),
                    new javafx.stage.FileChooser.ExtensionFilter("Alle Dateien", "*.*")
            );
            fileChooser.setInitialFileName("data.csv");
            File bufferfile = fileChooser.showSaveDialog(primaryStage);
            file = bufferfile.getPath().toLowerCase().endsWith(".csv") ? bufferfile : new File(bufferfile.getPath() + ".csv");
        } else {
            file = new File(Config.EXT_DIR, "data.csv");
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("time,x-axis,y-axis,z-axis");
            int dataSize = accDataX.getDataCount();
            for (int i = 0; i < dataSize; i++) {
                double time = accDataX.getX(i);
                double xValue = accDataX.getY(i);
                double yValue = accDataY.getY(i);
                double zValue = accDataZ.getY(i);
                pw.println(time + "," + xValue + "," + yValue + "," + zValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!Config.isDesktop) {
            shareService = ShareService.create().get();
            System.out.println("Got accelerometer service.");
            shareService.share("data/csv", file);
        }
    }
}
