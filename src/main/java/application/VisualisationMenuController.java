package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Calculations.SatelliteCalculations;

import java.io.IOException;
import java.util.Objects;


public class VisualisationMenuController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    private SatelliteCalculations satelliteData = WelcomeSceneController.satelliteData;
    public void button1Click(ActionEvent event) throws IOException {
        //satelliteData.display_elements();
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ElevationChart.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene=new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
