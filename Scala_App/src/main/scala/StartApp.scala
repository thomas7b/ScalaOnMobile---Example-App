
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.layout.AnchorPane
import javafx.scene.Scene
import javafx.stage.Stage



object StartApp {
  def main(args: Array[String]): Unit = Application.launch(classOf[StartApp], args: _*)

}
final class StartApp extends Application {

  var Stage: Stage = _
  var screenWidth: Double = _
  var screenHeight: Double = _

  override def start(stage: Stage): Unit = {
    Stage = stage
    val root: AnchorPane = FXMLLoader.load(getClass.getResource("menu.fxml"))
    val scene: Scene = new Scene(root, Config.screenWidth, Config.screenHeight)
    stage.setScene(scene)
    stage.show()
  }
}
