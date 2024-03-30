
import com.gluonhq.attach.display.DisplayService
import com.gluonhq.attach.lifecycle.{LifecycleEvent, LifecycleService}
import com.gluonhq.attach.storage.StorageService
import com.gluonhq.attach.util.{Platform, Services}
import javafx.beans.property.{BooleanProperty, SimpleBooleanProperty}
import javafx.geometry.Dimension2D
import java.io.File


object Config {

  var screenWidth: Double = _
  var screenHeight: Double = _

  var EXT_DIR: File = _
  var AudioFilePath: File = _


  val isDesktop: Boolean = Platform.isDesktop

  val isRunningProperty: BooleanProperty = new SimpleBooleanProperty(false)

  def isRunning: Boolean = isRunningProperty.get()

  def isRunning_=(value: Boolean): Unit = isRunningProperty.set(value)
  
  if(isDesktop){
    screenHeight = 400
    screenWidth = 600
  }else {
    DisplayService.create().ifPresent(service => {
      val dimensions: Dimension2D = service.getDefaultDimensions

      screenHeight = dimensions.getHeight
      screenWidth = dimensions.getWidth
    })
  }

  EXT_DIR = Services.get(classOf[StorageService])
    .flatMap(_.getPublicStorage("Gluon"))
    .orElseThrow(() => new RuntimeException("Error retrieving public storage"))
    EXT_DIR.mkdir()

  AudioFilePath = Services.get(classOf[StorageService])
    .flatMap(_.getPublicStorage("Gluon/audiofile.wav"))
    .orElseThrow(() => new RuntimeException("Error retrieving public storage"))

  Services.get(classOf[LifecycleService]).ifPresent(service => {
    service.addListener(LifecycleEvent.PAUSE, () => {
      isRunning = false
      System.out.println("Application is paused.")
    })
    service.addListener(LifecycleEvent.RESUME, () => {
      System.out.println("Application is resumed.")
    })
  })

}
