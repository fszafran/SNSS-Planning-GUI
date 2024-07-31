package application;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;


public class Main extends Application {
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("WelcomeScene.fxml")));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Pomiary SNSS");
        stage.getIcons().add(new Image("/OIG3.jpg"));
        stage.show();
    }


}