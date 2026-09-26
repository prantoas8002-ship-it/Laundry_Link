package org.example;

import database.DatabaseInitializer;
import database.DatabaseSeeder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Initialize database tables
        DatabaseInitializer.initializeDatabase();

        // Insert initial/default data
        DatabaseSeeder.seedDatabase();

        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/fxml/login.fxml"));

        Scene scene = new Scene(loader.load(), 1000, 650);

        stage.setTitle("LaundryLink");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}