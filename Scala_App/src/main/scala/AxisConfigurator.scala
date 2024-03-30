import io.fair_acc.chartfx.XYChart
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis
import io.fair_acc.dataset.spi.DoubleDataSet
import javafx.scene.input.{TouchEvent, ZoomEvent}

object AxisConfigurator {
  @volatile private var isZooming: Boolean = false

  def initializeTouchControls(chart: XYChart): Unit = {
    chart.getXAxis.setAutoGrowRanging(false)
    chart.getXAxis.setAutoRanging(false)
    chart.getXAxis.setAutoUnitScaling(false)

    var lastTouchX: Double = 0
    var isPanning: Boolean = false


    chart.setOnTouchPressed((event: TouchEvent) => {
      if (!isZooming) {
        chart.getXAxis.setAutoGrowRanging(false)
        chart.getXAxis.setAutoRanging(false)
        chart.getXAxis.setAutoUnitScaling(false)
        println("moving...")
        lastTouchX = event.getTouchPoint.getX
        isPanning = true
      }
      event.consume()
    })

    chart.setOnTouchMoved((event: TouchEvent) => {
      if (!isZooming) {
        chart.getXAxis.setAutoGrowRanging(false)
        chart.getXAxis.setAutoRanging(false)
        chart.getXAxis.setAutoUnitScaling(false)
        val deltaX = event.getTouchPoint.getX - lastTouchX
        // Passe die x-Achse basierend auf der Bewegung an
        val currentMinX = chart.getXAxis.getMin
        val currentMaxX = chart.getXAxis.getMax
        val axisRange = currentMaxX - currentMinX
        val shift = (deltaX / chart.getWidth) * axisRange
        chart.getXAxis.setMin(currentMinX - shift)
        chart.getXAxis.setMax(currentMaxX - shift)
        updateYAxisRange(
          Accelerometer.xAxis,
          Accelerometer.yAxis,
          Seq(Accelerometer.accDataX, Accelerometer.accDataY, Accelerometer.accDataZ),
          Seq(Accelerometer.checkBoxXFunc, Accelerometer.checkBoxYFunc, Accelerometer.checkBoxZFunc)
        )
        lastTouchX = event.getTouchPoint.getX
      }
      event.consume()
    })

    chart.setOnTouchReleased((event: TouchEvent) => {
      if (!isZooming) {
        isPanning = false
      }
      event.consume()
    })
  }

  def initializeZoomControls(chart: XYChart): Unit = {
    chart.setOnZoomStarted(_ => isZooming = true)
    chart.setOnZoom((event: ZoomEvent) => {
      chart.getXAxis.setAutoGrowRanging(false)
      chart.getXAxis.setAutoRanging(false)
      chart.getYAxis.setAutoGrowRanging(false)
      chart.getYAxis.setAutoRanging(false)

      val zoomFactor = if (event.getZoomFactor < 1) 1 / (2 - event.getZoomFactor) else event.getZoomFactor

      val xAxis = chart.getXAxis
      val currentMinX = xAxis.getMin
      val currentMaxX = xAxis.getMax
      val midPoint = (currentMaxX + currentMinX) / 2

      val range = (currentMaxX - currentMinX) / 2 / zoomFactor

      xAxis.setMin(midPoint - range)
      xAxis.setMax(midPoint + range)
      AxisConfigurator.updateYAxisRange(
        Accelerometer.xAxis,
        Accelerometer.yAxis,
        Seq(Accelerometer.accDataX, Accelerometer.accDataY, Accelerometer.accDataZ),
        Seq(Accelerometer.checkBoxXFunc, Accelerometer.checkBoxYFunc, Accelerometer.checkBoxZFunc)
      )
      event.consume()
    })
    chart.setOnZoomFinished(_ => isZooming = false)
  }

  def updateYAxisRange(
                        xAxis: DefaultNumericAxis,
                        yAxis: DefaultNumericAxis,
                        dataSets: Seq[DoubleDataSet],
                        checkBoxStates: Seq[() => Boolean]): Unit = {

    var minY = Double.MaxValue
    var maxY = Double.MinValue

    dataSets.zip(checkBoxStates).foreach { case (dataSet, checkBoxState) =>
      if (checkBoxState()) {
        val dataCount = dataSet.getDataCount
        for (i <- 0 until dataCount) {
          val xValue = dataSet.getX(i)
          if (xValue >= xAxis.getMin && xValue <= xAxis.getMax) {
            val yValue = dataSet.getY(i)
            minY = Math.min(minY, yValue)
            maxY = Math.max(maxY, yValue)
          }
        }
      }
    }

    if (minY != Double.MaxValue && maxY != Double.MinValue) {
      yAxis.setMin(minY)
      yAxis.setMax(maxY)
    }
  }

}
