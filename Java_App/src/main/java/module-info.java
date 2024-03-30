module com.example.acceltone {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.gluonhq.attach.accelerometer;
    requires com.gluonhq.attach.share;
    requires io.fair_acc.dataset;
    requires io.fair_acc.chartfx;
    requires java.desktop;
    requires com.gluonhq.attach.video;
    requires javafx.media;
    requires io.fair_acc.math;
    requires JTransforms;
    requires com.gluonhq.attach.display;
    requires com.gluonhq.attach.lifecycle;
    requires com.gluonhq.attach.storage;
    requires com.gluonhq.attach.util;


    opens com.example.acceltone to javafx.fxml;
    exports com.example.acceltone;
}