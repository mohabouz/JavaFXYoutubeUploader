module com.mb.youtubeupload {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    requires google.oauth.client;
    requires google.oauth.client.java6;
    requires google.oauth.client.jetty;
    requires google.api.client;
    requires google.http.client;
    requires google.http.client.jackson2;
    requires google.api.services.youtube.v3.rev204;

    opens com.mb.youtubeupload to javafx.fxml;
    exports com.mb.youtubeupload;
}