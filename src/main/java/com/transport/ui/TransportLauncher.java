package com.transport.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class TransportLauncher extends Application {

    @Override
    public void start(Stage stage) {
        new LoginView(stage).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
