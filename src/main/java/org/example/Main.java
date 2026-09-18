package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static javafx.application.Application.launch;

public class Main  extends Application{

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/fxml/login.fxml"));

        Scene scene = new Scene(loader.load() , 1000 , 650);

        stage.setTitle("LaundryLink");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}