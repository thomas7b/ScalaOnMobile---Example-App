package com.example.acceltone;

import com.gluonhq.attach.video.VideoService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class AudioPlayer {

    private static Stage stage;
    public static BooleanProperty isPlayingProperty = new SimpleBooleanProperty(false);

    private static Optional<VideoService> videoService;

    static {
        if (!Config.isDesktop) {
            videoService = VideoService.create();
        }
    }

    public static boolean isPlaying() {
        return isPlayingProperty.get();
    }

    public static void setIsPlaying(boolean isPlaying) {
        isPlayingProperty.set(isPlaying);
    }

    public static void pause() {
        videoService.ifPresent(VideoService::stop);
    }

    public static void save() {
        byte[] audioData = generateAudioSignal();
        saveWavFile(audioData, (float) SignalGenerator.fSample, new File(String.valueOf(Config.AudioFilePath)));
    }

    public static void play() {
        if (Config.isDesktop) {
            try {
                URL soundUrl = new File(String.valueOf(Config.AudioFilePath)).toURI().toURL();
                Media sound = new Media(soundUrl.toExternalForm());
                MediaPlayer mediaPlayer = new MediaPlayer(sound);
                mediaPlayer.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            videoService.ifPresent(service -> {
                service.setControlsVisible(true);
                service.getPlaylist().clear();
                service.getPlaylist().add(String.valueOf(Config.AudioFilePath));
                service.statusProperty().addListener((observable, oldValue, newValue) -> {
                    setIsPlaying("PLAYING".equals(newValue.toString()));
                    System.out.println("VideoService Status changed: " + newValue + ", isPlaying: " + isPlaying());
                });
                service.play();
            });
        }
    }

    public static byte[] generateAudioSignal() {
        byte[] audioData = new byte[SignalGenerator.anzahlElemente * 2 * SignalGenerator.Anz_Pulse];
        double t;

        for (int pulse = 0; pulse < SignalGenerator.Anz_Pulse; pulse++) {
            for (int frame = 0; frame < SignalGenerator.anzahlElemente; frame++) {
                t = frame * SignalGenerator.Abtastperiodendauer;
                short value = 0;
                if (frame < SignalGenerator.anzahlElementePuls) {
                    if (SignalGenerator.cwRB.getAsBoolean()) {
                        value = (short) (SignalGenerator.genCW(t) * Short.MAX_VALUE);
                    } else if (SignalGenerator.chirpRB.getAsBoolean()) {
                        value = (short) (SignalGenerator.genChirpLinear(t) * Short.MAX_VALUE);
                    }
                    int index = (pulse * SignalGenerator.anzahlElemente + frame) * 2;
                    audioData[index] = (byte) (value & 0xFF);
                    audioData[index + 1] = (byte) ((value >> 8) & 0xFF);
                }
            }
        }
        return audioData;
    }

    public static void saveWavFile(byte[] audioData, float sampleRate, File filePath) {
        try {
            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(audioData), format, audioData.length / format.getFrameSize());
            Path targetPath = Paths.get(filePath.toString());

            if (Files.exists(targetPath)) {
                Files.delete(targetPath);
            }

            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

