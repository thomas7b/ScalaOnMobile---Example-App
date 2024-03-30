
import com.gluonhq.attach.util.Platform.isDesktop
import com.gluonhq.attach.util.Platform
import com.gluonhq.attach.video.VideoService
import javafx.beans.property.{BooleanProperty, SimpleBooleanProperty}
import javafx.scene.media.{Media, MediaPlayer}
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage
import java.io.File
import java.nio.file.{Files, Paths}
import java.util.Optional
import javax.sound.sampled.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


object AudioPlayer {

  var Stage: Stage = _
  var root: Parent = _
  var scene: Scene = _
  var t: Double = _

  val isPlayingProperty: BooleanProperty = new SimpleBooleanProperty(false)
  private def isPlaying: Boolean = isPlayingProperty.get()
  private def isPlaying_=(value: Boolean): Unit = isPlayingProperty.set(value)

  private var audioservice: Optional[VideoService] = _
  if (!isDesktop) {audioservice = VideoService.create()}
  
  def pause(): Unit = {
    audioservice.ifPresent(service => {
      service.stop()
    })
  }

  def save(): Unit = {
    val audioData = generateAudio(SignalGenerator.anzahlElemente.toInt, SignalGenerator.fSample.toInt)
    saveWavFile(audioData, SignalGenerator.fSample.toInt, Config.AudioFilePath)
  }

  def play(): Unit = {
    if (isDesktop) {
      var soundUrl = Config.AudioFilePath.toURI.toURL.toExternalForm
      var sound = new Media(soundUrl)
      var mediaPlayer = new MediaPlayer(sound)
      mediaPlayer.play()
    } else {
      audioservice.ifPresent(service => {
        service.setControlsVisible(true)
        service.getPlaylist.clear()
        service.getPlaylist.add(Config.AudioFilePath.toString)
        service.statusProperty().addListener((observable, oldValue, newValue) => {
          isPlaying = newValue.toString.equals("PLAYING")
          println(s"VideoService Status hat sich geändert: $newValue, isPlaying: $isPlaying")
        })
        service.play()
      })
    }
  }

  private def generateAudio(numFrames: Int, sampleRate: Float): Future[Array[Byte]] = {
    val futures: Seq[Future[Array[Byte]]] = (0 until SignalGenerator.Anz_Pulse).map { pulse =>
      Future {
        val partAudioData = new Array[Byte](SignalGenerator.anzahlElemente.toInt * 2)
        for (frame <- 0 until SignalGenerator.anzahlElemente.toInt) {
          val t = frame * SignalGenerator.Abtastperiodendauer
          if (frame < SignalGenerator.anzahlElementePuls) {
            val value: Short = ((if (SignalGenerator.cwRB()) SignalGenerator.genCW(t)
            else if (SignalGenerator.chirpRB()) SignalGenerator.genChirpLinear(t)
            else 0.0) * Short.MaxValue).toShort
            val index = frame * 2
            partAudioData(index) = (value & 0xFF).toByte
            partAudioData(index + 1) = ((value >>> 8) & 0xFF).toByte
          }
        }
        partAudioData
      }
    }
    Future.sequence(futures).map(_.flatten.toArray)
  }
  
  private def saveWavFile(audioDataFuture: Future[Array[Byte]], sampleRate: Float, filePath: File): Unit = {
    audioDataFuture.foreach { audioData =>
        Try {
          val format = new AudioFormat(sampleRate, 16, 1, true, false)
          val audioInputStream = new AudioInputStream(new java.io.ByteArrayInputStream(audioData), format, audioData.length.toLong / format.getFrameSize)
          val targetPath = Paths.get(filePath.toString)
          if (Files.exists(targetPath)) {
            Files.delete(targetPath)
          }
          AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, filePath)
        } match {
          case Success(_) => println("Audio file saved successfully.")
          case Failure(e) => e.printStackTrace()
        }
    }
  }
}
