
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis
import io.fair_acc.dataset.spi.DefaultDataSet
import io.fair_acc.math.spectra.SpectrumTools.computeMagnitudeSpectrum
import io.fair_acc.math.spectra.SpectrumTools
import javafx.application.Platform
import org.jtransforms.fft.DoubleFFT_1D
import java.util
import java.util.Arrays
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.math.*
import scala.util.{Failure, Success}

object SignalGenerator {
  
  var fspectra: DefaultDataSet = new DefaultDataSet("spectrum")
  var xAxisf: DefaultNumericAxis = new DefaultNumericAxis("frequency", "hz")
  var yAxisf: DefaultNumericAxis = new DefaultNumericAxis("amplitude")

  var Anz_Pulse: Int = 1
  var Mittenfrequenz: Int = 500 // Hier die gewünschte Mittenfrequenz eintragen
  var Bandbreite: Int = 200
  var PRI: Double = 3
  var Pulsbreite: Double = 3
  var fSample: Double = 44100
  var Abtastperiodendauer: Double = _
  var anzahlElemente: Double = _
  var anzahlElementePuls: Double = _
  var f0: Double = _
  var k: Double = _
  var t: Double = _

  var cwRB: () => Boolean = _
  var chirpRB: () => Boolean = _
  
  def startSinusWave(): Unit = {
    Abtastperiodendauer = 1 / fSample
    anzahlElemente = PRI * fSample
    anzahlElementePuls = Pulsbreite * fSample
    f0 = Mittenfrequenz - (Bandbreite / 2)
    k = (Mittenfrequenz + (Bandbreite / 2) - f0) / Pulsbreite
  }
  
  def calculateFFT(): Unit = {
    val effectiveSampleCount = if (Anz_Pulse > 1) anzahlElementePuls.toInt else anzahlElemente.toInt

    val xValues = new Array[Double](effectiveSampleCount)
    val yValues = new Array[Double](effectiveSampleCount)

    for (i <- 0 until effectiveSampleCount) {
      xValues(i) = i * Abtastperiodendauer
      val t = i * Abtastperiodendauer
      yValues(i) = if (cwRB()) {
        genCW(t)
      } else if (chirpRB()) {
        genChirpLinear(t)
      } else {
        0.0
      }
    }

    val futureFFT = Future {
      // FFT transformation
      val fastFourierTrafo = new DoubleFFT_1D(yValues.length)
      var fftSpectra: Array[Double] = util.Arrays.copyOf(yValues, yValues.length)
      fastFourierTrafo.realForward(fftSpectra)
      fftSpectra = SpectrumTools.interpolateSpectrum(fftSpectra, 20) // Verwende deine nOverSampling2 Variable
      // Calculation of magnitude spectrum
      val mag: Array[Double] = SpectrumTools.computeMagnitudeSpectrum(fftSpectra, true)
      // Calculation of frequency axis
      val frequency = new Array[Double](fftSpectra.length / 2)
      val scaling = fSample / fftSpectra.length
      for (i <- frequency.indices) {
        frequency(i) = i * scaling
      }
      (frequency, mag)
    }

    futureFFT.onComplete {
      case Success((frequency, mag)) =>
        Platform.runLater(() => {
          // Set maximum frequency to Mittenfrequenz * 2
          val maxIndex = frequency.indexWhere(_ > Mittenfrequenz * 2)
          val frequencyToPlot = if (maxIndex == -1) frequency else frequency.slice(0, maxIndex)
          val magToPlot = if (maxIndex == -1) mag else mag.slice(0, maxIndex)
          fspectra.set(new DefaultDataSet("interpolated FFT", frequencyToPlot, magToPlot, frequencyToPlot.length, true))
        })

      case Failure(e) =>
        e.printStackTrace()
    }
  }
  def setRadioButtonStateFunctions(
                                    cwRadioButton: () => Boolean,
                                    chirpRadioButton: () => Boolean): Unit = {
    cwRB = cwRadioButton
    chirpRB = chirpRadioButton
  }

  def genCW(t: Double): Double = {
    math.cos(2 * math.Pi * Mittenfrequenz * t)

  }

  def genChirpLinear(t: Double): Double = {
    math.cos(2 * math.Pi * (k / 2 * t + f0) * t)
  }
}
