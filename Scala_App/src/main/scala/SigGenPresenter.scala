import io.fair_acc.chartfx.XYChart
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis
import io.fair_acc.chartfx.renderer.spi.BasicDataSetRenderer
import io.fair_acc.dataset.DataSet
import io.fair_acc.dataset.utils.DataSetStyleBuilder
import javafx.event.ActionEvent
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.scene.{Node, Parent, Scene}
import javafx.stage.Stage

class SigGenPresenter {
  @FXML var label2: Label = _
  @FXML var startButton: Button = _
  @FXML var stopButton: Button = _
  @FXML var menuButton: Button = _
  @FXML var AudioAbspielen: Button = _

  @FXML var VBox: VBox = _
  @FXML
  private var Anz_Pulse: TextField = _

  @FXML
  private var Mittenfrequenz: TextField = _

  @FXML
  private var Bandbreite: TextField = _

  @FXML
  private var PRI: TextField = _

  @FXML
  private var Pulsbreite: TextField = _

  @FXML
  private var cwRadioButton: RadioButton = _

  @FXML
  private var chirpRadioButton: RadioButton = _

  @FXML
  private var Modulation: TextField = _
  @FXML
  private var calculateFFT: CheckBox = _
  private var chart2: XYChart = _

  var Stage: Stage = _
  var root: Parent = _
  var scene: Scene = _

  @FXML
  def initialize(): Unit = {

    calculateFFT.selectedProperty().addListener((_, _, isSelected) => {
      if (isSelected) {
        SignalGenerator.calculateFFT()
        chart2 = createChart(SignalGenerator.fspectra, SignalGenerator.xAxisf, SignalGenerator.yAxisf)
        VBox.getChildren.add(chart2)
      } else {
        VBox.getChildren.remove(chart2)
      }
    })

    VBox.setPrefWidth(Config.screenWidth-50)

    startButton.setOnAction((event: ActionEvent) =>
      getValuesFromTextFields()
      SignalGenerator.startSinusWave()
      AudioPlayer.save()
    )
    stopButton.setDisable(true)
    AudioPlayer.isPlayingProperty.addListener((_, _, newValue) => {
      AudioAbspielen.setDisable(newValue)
      stopButton.setDisable(!newValue)
    })
      
    stopButton.setOnAction((event: ActionEvent) => AudioPlayer.pause())
    menuButton.setOnAction((event: ActionEvent) => switchToMenu(event))
    AudioAbspielen.setOnAction((event: ActionEvent) => AudioPlayer.play())


    def isCWSelected: Boolean = cwRadioButton.isSelected

    def isChirpSelected: Boolean = chirpRadioButton.isSelected

    SignalGenerator.setRadioButtonStateFunctions(
      () => isCWSelected,
      () => isChirpSelected
    )
  }

  def getValuesFromTextFields(): Unit = {
    var anzPulseValue = Anz_Pulse.getText.toDouble
    var mittenfrequenzValue = Mittenfrequenz.getText.toDouble
    var bandbreiteValue = Bandbreite.getText.toDouble
    var priValue = PRI.getText.toDouble
    var pulsBreiteValue = Pulsbreite.getText.toDouble

    SignalGenerator.Anz_Pulse = anzPulseValue.toInt
    SignalGenerator.Mittenfrequenz = mittenfrequenzValue.toInt
    SignalGenerator.Bandbreite = bandbreiteValue.toInt
    SignalGenerator.PRI = priValue
    SignalGenerator.Pulsbreite = pulsBreiteValue

  }

  def createChart(dataSet: DataSet, xAxis: DefaultNumericAxis, yAxis: DefaultNumericAxis): XYChart = {
    val chart: XYChart = new XYChart(xAxis, yAxis)

    chart.legendVisibleProperty.set(true)
    chart.setAnimated(false)
    val style = DataSetStyleBuilder.instance()
      .setMarkerSize(0)
      .build()
    dataSet.setStyle(style)

    val dataSetRenderer = new BasicDataSetRenderer()
    dataSetRenderer.getDatasets.addAll(dataSet)
    chart.getRenderers.add(dataSetRenderer)
    chart.prefWidthProperty().bind(VBox.widthProperty())
    chart
  }


  def switchToMenu(event: ActionEvent): Unit = {
    root = FXMLLoader.load(getClass.getResource("menu.fxml"))
    Stage = event.getSource.asInstanceOf[Node].getScene.getWindow.asInstanceOf[Stage]
    scene = new Scene(root, Config.screenWidth, Config.screenHeight)
    Stage.setScene(scene)
    Stage.show()
  }
}
