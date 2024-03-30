package com.example.acceltone;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.dataset.spi.DoubleDataSet;

import java.util.List;
import java.util.function.BooleanSupplier;

public class AxisConfigurator {
    private static volatile boolean isZooming = false;

    public static void initializeTouchControls(XYChart chart) {
        chart.getXAxis().setAutoGrowRanging(false);
        chart.getXAxis().setAutoRanging(false);
        chart.getXAxis().setAutoUnitScaling(false);

        double[] lastTouchX = {0};
        boolean[] isPanning = {false};

        chart.setOnTouchPressed(event -> {
            if (!isZooming) {
                chart.getXAxis().setAutoGrowRanging(false);
                chart.getXAxis().setAutoRanging(false);
                chart.getXAxis().setAutoUnitScaling(false);
                lastTouchX[0] = event.getTouchPoint().getX();
                isPanning[0] = true;
            }
            event.consume();
        });

        chart.setOnTouchMoved(event -> {
            if (!isZooming) {
                chart.getXAxis().setAutoGrowRanging(false);
                chart.getXAxis().setAutoRanging(false);
                chart.getXAxis().setAutoUnitScaling(false);
                double deltaX = event.getTouchPoint().getX() - lastTouchX[0];
                double currentMinX = chart.getXAxis().getMin();
                double currentMaxX = chart.getXAxis().getMax();
                double axisRange = currentMaxX - currentMinX;
                double shift = (deltaX / chart.getWidth()) * axisRange;
                chart.getXAxis().setMin(currentMinX - shift);
                chart.getXAxis().setMax(currentMaxX - shift);
                updateYAxisRange(Accelerometer.xAxis, Accelerometer.yAxis,
                        List.of(Accelerometer.accDataX, Accelerometer.accDataY, Accelerometer.accDataZ),
                        List.of(Accelerometer.checkBoxXFunc, Accelerometer.checkBoxYFunc, Accelerometer.checkBoxZFunc));
                lastTouchX[0] = event.getTouchPoint().getX();
            }
            event.consume();
        });

        chart.setOnTouchReleased(event -> {
            if (!isZooming) {
                isPanning[0] = false;
            }
            event.consume();
        });
    }

    public static void initializeZoomControls(XYChart chart) {
        chart.setOnZoomStarted(event -> isZooming = true);
        chart.setOnZoom(event -> {
            chart.getXAxis().setAutoGrowRanging(false);
            chart.getXAxis().setAutoRanging(false);
            chart.getYAxis().setAutoGrowRanging(false);
            chart.getYAxis().setAutoRanging(false);

            double zoomFactor = event.getZoomFactor() < 1 ? 1 / (2 - event.getZoomFactor()) : event.getZoomFactor();

            DefaultNumericAxis xAxis = (DefaultNumericAxis) chart.getXAxis();
            DefaultNumericAxis yAxis = (DefaultNumericAxis) chart.getYAxis();
            double currentMinX = xAxis.getMin();
            double currentMaxX = xAxis.getMax();
            double midPoint = (currentMaxX + currentMinX) / 2;
            double range = (currentMaxX - currentMinX) / 2 / zoomFactor;

            xAxis.setMin(midPoint - range);
            xAxis.setMax(midPoint + range);
            updateYAxisRange(
                    xAxis, yAxis,
                    List.of(Accelerometer.accDataX, Accelerometer.accDataY, Accelerometer.accDataZ),
                    List.of(Accelerometer.checkBoxXFunc, Accelerometer.checkBoxYFunc, Accelerometer.checkBoxZFunc)
            );
            event.consume();
        });
        chart.setOnZoomFinished(event -> isZooming = false);
    }


    public static void updateYAxisRange(
            DefaultNumericAxis xAxis,
            DefaultNumericAxis yAxis,
            List<DoubleDataSet> dataSets,
            List<BooleanSupplier> checkBoxStates) {

        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (int index = 0; index < dataSets.size(); index++) {
            DoubleDataSet dataSet = dataSets.get(index);
            BooleanSupplier checkBoxState = checkBoxStates.get(index);

            if (checkBoxState.getAsBoolean()) {
                int dataCount = dataSet.getDataCount();
                for (int i = 0; i < dataCount; i++) {
                    double xValue = dataSet.getX(i);
                    if (xValue >= xAxis.getMin() && xValue <= xAxis.getMax()) {
                        double yValue = dataSet.getY(i);
                        minY = Math.min(minY, yValue);
                        maxY = Math.max(maxY, yValue);
                    }
                }
            }
        }

        if (minY != Double.POSITIVE_INFINITY && maxY != Double.NEGATIVE_INFINITY) {
            yAxis.setMin(minY);
            yAxis.setMax(maxY);
        }
    }
}
