import com.gluonhq.attach.accelerometer.{Acceleration, AccelerometerService, Parameters}
import com.gluonhq.attach.share.ShareService
import com.gluonhq.attach.util.Platform.isDesktop
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis
import io.fair_acc.chartfx.plugins.Screenshot
import io.fair_acc.chartfx.plugins
import io.fair_acc.dataset.spi.DefaultDataSet
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.{Parent, Scene}
import javafx.stage.{FileChooser, Stage}
import java.io.{File, PrintWriter}
import scala.concurrent.{ExecutionContext, Future}
import scala.math.{cos, sin}

object Accelerometer {

  // UI-Komponenten
  var stage: Stage = _
  var root: Parent = _
  var scene: Scene = _
  var service: AccelerometerService = _
  var file: File = _
  var shareService: ShareService = _

  private var acceleration: Acceleration = _
  private var xv, yv, zv: Double = _
  var accDataX: DefaultDataSet = new DefaultDataSet("x-axis")
  var accDataY: DefaultDataSet = new DefaultDataSet("y-axis")
  var accDataZ: DefaultDataSet = new DefaultDataSet("z-axis")

  var xAxis: DefaultNumericAxis = new DefaultNumericAxis("time", "s")
  var yAxis: DefaultNumericAxis = new DefaultNumericAxis("acceleration", "m/s²")

  val screenshot = new Screenshot()

  private var startTime: Long = 0
  private var executorService: ExecutionContext = ExecutionContext.global
  private var BUFFER_CAPACITY: Int = 5
  private var DATA_BUFFER_CAPACITY: Int = _

  var filterGravity: Boolean = true

  var checkBoxXFunc: () => Boolean = _
  var checkBoxYFunc: () => Boolean = _
  var checkBoxZFunc: () => Boolean = _

  def setFilterGravity(filter: Boolean): Unit = {
    filterGravity = filter
  }
  
  def generateAccelerometerData(xv: Double, yv: Double, zv: Double): Unit = {
    val now: Double = (System.currentTimeMillis - startTime) / 1000.0
    accDataX.add(now, xv)
    accDataY.add(now, yv)
    accDataZ.add(now, zv)

    val visibleTimeRange: Double = BUFFER_CAPACITY 

    if (now > visibleTimeRange) {
      xAxis.setMin(now - visibleTimeRange)
      xAxis.setMax(now)
      xAxis.setAutoRanging(false)
      yAxis.setAutoRanging(false)
      AxisConfigurator.updateYAxisRange(
        xAxis,
        yAxis,
        Seq(accDataX, accDataY, accDataZ),
        Seq(checkBoxXFunc, checkBoxYFunc, checkBoxZFunc)
      )
    } else {
      xAxis.setAutoRanging(true)
      yAxis.setAutoRanging(true)
    }
  }
  
  def setCheckboxStateFunctions(
       xFunc: () => Boolean,
       yFunc: () => Boolean,
       zFunc: () => Boolean): Unit = {
    checkBoxXFunc = xFunc
    checkBoxYFunc = yFunc
    checkBoxZFunc = zFunc
  }
  
  def clearData(): Unit = {
    accDataX.clearData()
    accDataY.clearData()
    accDataZ.clearData()
  }

  @FXML
  def startAccelerator(sliderValue: Double): Unit = {
    DATA_BUFFER_CAPACITY = (sliderValue * 20).toInt
    startTime = System.currentTimeMillis
    try {
      val parameters = new Parameters(100.0, filterGravity)
      service = AccelerometerService.create().orElseThrow(() => new Exception("Unable to get accelerometer service"))
      println("Got accelerometer service.")
      service.start(parameters)

      Future {
        while (Config.isRunning && (accDataX.getDataCount <= DATA_BUFFER_CAPACITY)) {
          updateAccelerometerData()
          Thread.sleep(50)
        }
        stopAccelerator()
      }(ExecutionContext.global)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        println("Accelerometer service unavailable, using fallback data generation.")
        generateFallbackData()  // Fallback, if no AccelerometerService is available
    }
  }
  private def updateAccelerometerData(): Unit = {
    try {
      acceleration = service.getCurrentAcceleration
      xv = acceleration.getX
      yv = acceleration.getY
      zv = acceleration.getZ

      Platform.runLater(() => generateAccelerometerData(xv, yv, zv))
    } catch {
      case e: Exception =>
        e.printStackTrace()
        println("Error during acceleration data retrieval.")
    }
  }
  private def generateFallbackData(): Unit = {
    var i = 0
    Future {
      while (Config.isRunning && (accDataX.getDataCount <= DATA_BUFFER_CAPACITY)) {
        val xv = sin(0.1 * i)
        val yv = cos(0.1 * i)
        val zv = sin(0.1 * i) + cos(0.1 * i)
        i += 1
        Platform.runLater(() => generateAccelerometerData(xv, yv, zv))
        Thread.sleep(20)
      }
      stopAccelerator()
    }(ExecutionContext.global)
  }
  def stopAccelerator(): Unit = {
    try {
      if (executorService != null && Config.isRunning) {
        Config.isRunning = false
      }
      if (service != null) {
        service.stop()
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        println("Error stopping the accelerometer thread or service.")
    }
  }

  def exportScreenshot(): Unit = {
    screenshot.setDirectory(Config.EXT_DIR.toString)
    screenshot.setPattern("screenshot.png")
    if (isDesktop) {
      screenshot.screenshotToFile(true)
    }
    else {
      screenshot.screenshotToFile(false)

      val file = new File(Config.EXT_DIR, "screenshot.png")
      shareService = ShareService.create().get
      println("Got accelerometer service.")
      shareService.share("image/png", file)
    }
  }
  def exportDataToCSV(primaryStage: Stage): Unit = {

    if (isDesktop) {
      val fileChooser = new FileChooser()
      fileChooser.setTitle("CSV-Datei speichern")
      fileChooser.getExtensionFilters.addAll(
        new FileChooser.ExtensionFilter("CSV-Dateien", "*.csv"),
        new FileChooser.ExtensionFilter("Alle Dateien", "*.*")
      )
      fileChooser.setInitialFileName("data.csv")
      val bufferfile = fileChooser.showSaveDialog(primaryStage)

      file = if (!bufferfile.getPath.toLowerCase.endsWith(".csv")) new File(bufferfile.getPath + ".csv") else bufferfile
      }
      else {
      file = new File(Config.EXT_DIR, "data.csv")
      }

      val pw = new PrintWriter(file)
      try {
        pw.println("time,x-axis,y-axis,z-axis")
        val dataSize = accDataX.getDataCount
        for (i <- 0 until dataSize) {
          val time = accDataX.getX(i)
          val xValue = accDataX.getY(i)
          val yValue = accDataY.getY(i)
          val zValue = accDataZ.getY(i)
          pw.println(s"$time,$xValue,$yValue,$zValue")
        }
      } finally {
        pw.close()
      }
    if(!isDesktop) {
      shareService = ShareService.create().get
      println("Got accelerometer service.")
      shareService.share("data/csv", file)
    }
  }
}
