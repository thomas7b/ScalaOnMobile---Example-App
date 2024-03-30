import com.gluonhq.attach.share.ShareService
import io.fair_acc.chartfx.XYChart
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis
import io.fair_acc.chartfx.plugins.EditAxis
import io.fair_acc.chartfx.renderer.spi.BasicDataSetRenderer
import io.fair_acc.dataset.spi.DoubleDataSet
import io.fair_acc.dataset.utils.DataSetStyleBuilder
import javafx.animation.PauseTransition
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.ActionEvent
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.scene.{Node, Parent, Scene}
import javafx.stage.Stage
import javafx.util.Duration

class AccPresenter {
  @FXML var startButton: Button = _
  @FXML var stopButton: Button = _
  @FXML var menuButton: Button = _
  @FXML var exportButton: Button = _
  @FXML var exportCSVButton: Button = _
  @FXML var VBox: VBox = _
  @FXML var filterGravButton: CheckBox = _
  @FXML var timeSlider: Slider = _
  @FXML var liveRButton: RadioButton = _
  @FXML var timeRButton: RadioButton = _
  @FXML var CheckBoxX: CheckBox = _
  @FXML var CheckBoxY: CheckBox = _
  @FXML var CheckBoxZ: CheckBox = _

  var Stage: Stage = _
  var root: Parent = _
  var scene: Scene = _
  var service: ShareService = _
  private val XRenderer = new BasicDataSetRenderer()
  private val YRenderer = new BasicDataSetRenderer()
  private val ZRenderer = new BasicDataSetRenderer()


  @FXML
    def initialize(): Unit = {

      val chart = createChart(Accelerometer.accDataX, Accelerometer.accDataY, Accelerometer.accDataZ, Accelerometer.xAxis, Accelerometer.yAxis)
      VBox.getChildren.add(chart)

      VBox.setPrefWidth(Config.screenWidth - 50)
      VBox.setPrefHeight(350)

      def isXSelected: Boolean = CheckBoxX.isSelected

      def isYSelected: Boolean = CheckBoxY.isSelected

      def isZSelected: Boolean = CheckBoxZ.isSelected

      Accelerometer.setCheckboxStateFunctions(
      () => isXSelected,
      () => isYSelected,
      () => isZSelected
    )

      startButton.setOnAction((event: ActionEvent) => {
        Config.isRunning = true

        Accelerometer.setFilterGravity(filterGravButton.isSelected)
        Accelerometer.clearData()
        if (liveRButton.isSelected) {
          Accelerometer.startAccelerator(500)
        } else if (timeRButton.isSelected) {
          val sliderValue = timeSlider.getValue 
          Accelerometer.startAccelerator(sliderValue)
        }
        chart.getXAxis.setAutoGrowRanging(true)
        chart.getXAxis.setAutoRanging(true)
      })

      menuButton.setOnAction((event: ActionEvent) => switchToMenu(event))
      exportCSVButton.setOnAction((event: ActionEvent) => Accelerometer.exportDataToCSV(primaryStage = Stage))

      exportButton.setOnAction((event: ActionEvent) => {
        VBox.setPrefWidth(1200)
        VBox.setPrefHeight(600)
        val pause = new PauseTransition(Duration.millis(100))
        pause.setOnFinished(_ => {
          Accelerometer.exportScreenshot()
          VBox.setPrefWidth(Config.screenWidth - 50)
          VBox.setPrefHeight(350)
        })
        pause.play()
      })

      Config.isRunningProperty.addListener((_, _, newValue) => {
        startButton.setDisable(newValue)
        stopButton.setDisable(!newValue)
        filterGravButton.setDisable(newValue)
      })

      stopButton.setOnAction((event: ActionEvent) => {
        startButton.setDisable(false)
        stopButton.setDisable(true)
        Accelerometer.stopAccelerator()
        Config.isRunning = false
      })
      AxisConfigurator.initializeTouchControls(chart)
      AxisConfigurator.initializeZoomControls(chart)

      timeRButton.selectedProperty().addListener((_, _, isSelected) => {
        timeSlider.setDisable(!isSelected)
      })


      def addRendererListener(checkBox: CheckBox, renderer: BasicDataSetRenderer): Unit = {
        val listener = new ChangeListener[java.lang.Boolean] {
          override def changed(
                                observable: ObservableValue[_ <: java.lang.Boolean],
                                oldValue: java.lang.Boolean,
                                isSelected: java.lang.Boolean
                              ): Unit = {
            if (isSelected) {
              chart.getRenderers.add(renderer)
            } else {
              chart.getRenderers.remove(renderer)
            }
            AxisConfigurator.updateYAxisRange(
              Accelerometer.xAxis,
              Accelerometer.yAxis,
              Seq(Accelerometer.accDataX, Accelerometer.accDataY, Accelerometer.accDataZ),
              Seq(Accelerometer.checkBoxXFunc, Accelerometer.checkBoxYFunc, Accelerometer.checkBoxZFunc)
            )
          }
        }
        checkBox.selectedProperty().addListener(listener)
      }
      addRendererListener(CheckBoxX, XRenderer)
      addRendererListener(CheckBoxY, YRenderer)
      addRendererListener(CheckBoxZ, ZRenderer)
    }

  private def createChart(dataSetX: DoubleDataSet, dataSetY: DoubleDataSet, dataSetZ: DoubleDataSet, xAxis: DefaultNumericAxis, yAxis: DefaultNumericAxis): XYChart = {
    val chart: XYChart = new XYChart(xAxis, yAxis)
    chart.legendVisibleProperty.set(true)
    chart.setAnimated(false)

    val style1 = DataSetStyleBuilder.instance()
      .setMarkerSize(0)
      .setDatasetColor("red")
      .build()
    val style2 = DataSetStyleBuilder.instance()
      .setMarkerSize(0)
      .setDatasetColor("green")
      .build()
    val style3 = DataSetStyleBuilder.instance()
      .setMarkerSize(0)
      .setDatasetColor("blue")
      .build()

    dataSetX.setStyle(style1)
    dataSetY.setStyle(style2)
    dataSetZ.setStyle(style3)

    val lineRenderer = new BasicDataSetRenderer()
    lineRenderer.getDatasets.addAll(dataSetX, dataSetY, dataSetZ)
    XRenderer.getDatasets.add(dataSetX)
    YRenderer.getDatasets.add(dataSetY)
    ZRenderer.getDatasets.add(dataSetZ)

    chart.getRenderers.add(XRenderer)
    chart.getRenderers.add(YRenderer)
    chart.getRenderers.add(ZRenderer)
    
    val editAxisPlugin = new EditAxis()
    chart.getPlugins.add(editAxisPlugin)
    chart.getPlugins.add(Accelerometer.screenshot)
    chart.getToolBar.setDisable(true)
    chart.getToolBar.setOpacity(0)
    chart.prefWidthProperty().bind(VBox.widthProperty())
    chart.prefHeightProperty().bind(VBox.heightProperty())
    chart
  }
  private def switchToMenu(event: ActionEvent): Unit = {
  
    Config.isRunning = false
    root =  FXMLLoader.load(getClass.getResource("menu.fxml"))
    Stage = event.getSource.asInstanceOf[Node].getScene.getWindow.asInstanceOf[Stage]
    scene = new Scene(root, Config.screenWidth, Config.screenHeight)
    Stage.setScene(scene)
    Stage.show()
  }


}