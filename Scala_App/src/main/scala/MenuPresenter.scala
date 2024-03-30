
import javafx.event.ActionEvent
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.Button
import javafx.scene.{Node, Parent, Scene}
import javafx.stage.Stage

import java.io.IOException


class MenuPresenter {
  @FXML var startButton: Button = _
  @FXML var stopButton: Button = _
  var screenHeight: Double = _
  var screenWidth: Double = _
  var stage: Stage = _
  var root: Parent = _
  var scene: Scene = _
  @FXML var accButton: Button = _

  @FXML var sigButton: Button = _


  @FXML
  def initialize(): Unit = {
    accButton.setOnAction((event: ActionEvent) => {
      switchToAccelerator(event)
    })
    sigButton.setOnAction((event: ActionEvent) => {
      switchToSigGen(event)
    })
  }

  @throws[IOException]
  def switchToAccelerator(event: ActionEvent): Unit = {

    printf("Screen resolution: %.0fx%.0f\n", screenWidth, screenHeight)
    root =  FXMLLoader.load(getClass.getResource("accelerator.fxml"))
    stage = event.getSource.asInstanceOf[Node].getScene.getWindow.asInstanceOf[Stage]
    scene = new Scene(root, Config.screenWidth, Config.screenHeight)
    stage.setScene(scene)
    stage.show()
  }

  @throws[IOException]
  def switchToSigGen(event: ActionEvent): Unit = {

    printf("Screen resolution: %.0fx%.0f\n", screenWidth, screenHeight)
    root = FXMLLoader.load(getClass.getResource("signalgenerator.fxml"))
    stage = event.getSource.asInstanceOf[Node].getScene.getWindow.asInstanceOf[Stage]
    scene = new Scene(root, Config.screenWidth, Config.screenHeight)
    stage.setScene(scene)
    stage.show()
  }

}