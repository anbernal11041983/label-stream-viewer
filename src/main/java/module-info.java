module com.inventorymanagementsystem{
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires javafx.media;
    requires java.base;


    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires jasperreports;
    requires org.burningwave.core;

    opens br.com.automacaowebia to javafx.fxml;
    exports br.com.automacaowebia;
    exports br.com.automacaowebia.entity;
    opens br.com.automacaowebia.entity to javafx.fxml;
    exports br.com.automacaowebia.config;
    opens br.com.automacaowebia.config to javafx.fxml;
    exports br.com.automacaowebia.app;
    opens br.com.automacaowebia.app to javafx.fxml;
}