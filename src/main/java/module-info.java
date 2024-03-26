module org.example.navsat {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires commons.math3;
    requires org.jfree.jfreechart;
    requires org.jfree.chart.fx;

    opens application to javafx.fxml;
    exports application;
}