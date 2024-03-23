package application;
import Calculations.AlmanacModule;
import Calculations.SatelliteCalculations;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;


public class Main extends Application {


//    public static void main(String[] args) throws IOException {
//        List<List<Double>> Nav = AlmanacModule.readAlmanac("src/main/resources/Almanac2024053.alm");
//        List<Double> rowNav = Nav.getFirst();
//        double[]XYZ = (SatelliteCalculations.getSatPos(SatelliteCalculations.weekSecond[1],SatelliteCalculations.weekSecond[0], rowNav));
//        int maxRecords = 32;
//        List<List<Double>> shortenedNav = Nav.subList(0, Math.min(Nav.size(), maxRecords));
//        List<List<Double>> A = SatelliteCalculations.satellitePositionInTime(shortenedNav);
//        System.out.println(A.getFirst());
//        launch(args);
//    }


    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("WelcomeScene.fxml")));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("woot");
        stage.getIcons().add(new Image("icon.jpg"));
        stage.show();


    }

}