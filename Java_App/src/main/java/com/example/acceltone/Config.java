package com.example.acceltone;

import com.gluonhq.attach.display.DisplayService;
import com.gluonhq.attach.lifecycle.LifecycleEvent;
import com.gluonhq.attach.lifecycle.LifecycleService;
import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Platform;
import com.gluonhq.attach.util.Services;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Dimension2D;

import java.io.File;
import java.util.Optional;

public class Config {

    public static double screenWidth;
    public static double screenHeight;

    public static File EXT_DIR;
    public static File AudioFilePath;

    public static final boolean isDesktop = Platform.isDesktop();

    public static final BooleanProperty isRunningProperty = new SimpleBooleanProperty(false);


    static {
        if (isDesktop) {
            screenHeight = 400;
            screenWidth = 600;
        } else {
            Optional<DisplayService> displayServiceOptional = Services.get(DisplayService.class);
            displayServiceOptional.ifPresent(service -> {
                Dimension2D dimensions = service.getDefaultDimensions();
                screenHeight = dimensions.getHeight();
                screenWidth = dimensions.getWidth();
            });
        }

        EXT_DIR = Services.get(StorageService.class)
                .flatMap(service -> service.getPublicStorage("Gluon"))
                .orElseThrow(() -> new RuntimeException("Error retrieving public storage"));
        EXT_DIR.mkdir();

        AudioFilePath = Services.get(StorageService.class)
                .flatMap(service -> service.getPublicStorage("Gluon/audiofile.wav"))
                .orElseThrow(() -> new RuntimeException("Error retrieving public storage"));

        Services.get(LifecycleService.class).ifPresent(service -> {
            service.addListener(LifecycleEvent.PAUSE, () -> {
                isRunningProperty.set(false);
                System.out.println("Application is paused.");
            });
            service.addListener(LifecycleEvent.RESUME, () -> {
                System.out.println("Application is resumed.");
            });
        });
    }

    public static boolean isRunning() {
        return isRunningProperty.get();
    }

    public static void setRunning(boolean running) {
        isRunningProperty.set(running);
    }
}

