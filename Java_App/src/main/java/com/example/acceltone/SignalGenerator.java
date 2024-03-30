package com.example.acceltone;

import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.dataset.spi.DefaultDataSet;
import io.fair_acc.math.spectra.SpectrumTools;
import org.jtransforms.fft.DoubleFFT_1D;
import java.util.Arrays;
import java.util.function.BooleanSupplier;

public class SignalGenerator {
    public static DefaultDataSet sinData = new DefaultDataSet("sinusoidal data");
    public static DefaultDataSet fspectra = new DefaultDataSet("spectrum");
    public static DefaultNumericAxis xAxisf = new DefaultNumericAxis("frequency", "hz");
    public static DefaultNumericAxis yAxisf = new DefaultNumericAxis("amplitude");
    public static int Anz_Pulse = 1;
    public static int Mittenfrequenz = 500; // Mittenfrequenz
    public static int Bandbreite = 200;
    public static double PRI = 3;
    public static double Pulsbreite = 3;
    public double TOA = 0.0;
    public static double fSample = 44100;
    public static double Abtastperiodendauer;
    public static int anzahlElemente;
    public static int anzahlElementePuls;
    private static double f0;
    private static double k;
    private static double[] xValues;
    private static double[] yValues1;
    public static BooleanSupplier cwRB;
    public static BooleanSupplier chirpRB;

    public void startSinusWave() {
        Abtastperiodendauer = 1 / fSample;
        anzahlElemente = (int) (PRI * fSample);
        anzahlElementePuls = (int) (Pulsbreite * fSample);
        f0 = Mittenfrequenz - (Bandbreite / 2.0);
        k = (Mittenfrequenz + (Bandbreite / 2.0) - f0) / Pulsbreite;

    }

    public static void calculateFFT() {
        int samplesToProcess = Anz_Pulse > 1 ? anzahlElementePuls : anzahlElemente;

        xValues = new double[samplesToProcess];
        yValues1 = new double[samplesToProcess];
        for (int i = 0; i < samplesToProcess; i++) {
            double t = i * Abtastperiodendauer;
            double modulationValue;
            if (cwRB.getAsBoolean()) {
                modulationValue = genCW(t);
            } else if (chirpRB.getAsBoolean()) {
                modulationValue = genChirpLinear(t);
            } else {
                modulationValue = 0.0;
            }
            yValues1[i] = modulationValue;
            xValues[i] = t;
        }

        sinData.set(xValues, yValues1);

        // FFT transformation
        DoubleFFT_1D fastFourierTrafo = new DoubleFFT_1D(yValues1.length);
        double[] fftSpectra = Arrays.copyOf(yValues1, yValues1.length);
        fastFourierTrafo.realForward(fftSpectra);

        // Interpolation and magnitude calculation
        fftSpectra = SpectrumTools.interpolateSpectrum(fftSpectra, 20);
        double[] mag = SpectrumTools.computeMagnitudeSpectrum(fftSpectra, true);

        // calculate frequency axis
        double[] frequency3 = new double[fftSpectra.length / 2];
        double scaling3 = fSample / fftSpectra.length;
        for (int i = 0; i < frequency3.length; i++) {
            frequency3[i] = i * scaling3;
        }

        // set maximum frequency to plot
        double maxFrequency = Mittenfrequenz + Bandbreite;
        int maxIndex = -1;
        for (int i = 0; i < frequency3.length; i++) {
            if (frequency3[i] > maxFrequency) {
                maxIndex = i;
                break;
            }
        }

        double[] frequencyToPlot = (maxIndex == -1) ? frequency3 : Arrays.copyOfRange(frequency3, 0, maxIndex);
        double[] magToPlot = (maxIndex == -1) ? mag : Arrays.copyOfRange(mag, 0, maxIndex);

        fspectra.set(new DefaultDataSet("interpolated FFT", frequencyToPlot, magToPlot, frequencyToPlot.length, true));
    }
    public void setRadioButtonStateFunctions(BooleanSupplier cwRadioButton, BooleanSupplier chirpRadioButton) {
        cwRB = cwRadioButton;
        chirpRB = chirpRadioButton;
    }

    public static double genCW(double t) {
        return Math.cos(2 * Math.PI * Mittenfrequenz * t);
    }

    public static double genChirpLinear(double t) {
        return Math.cos(2 * Math.PI * (k / 2 * t + f0) * t);
    }

}
