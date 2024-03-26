package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;


public class VisualisationMenuController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    public void button1Click(ActionEvent event) throws IOException {
        //satelliteData.display_elements();
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ElevationChart.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene=new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void button2Click(ActionEvent event) throws IOException {
        //satelliteData.display_elements();
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("NumberOfSatellitesChart.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene=new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void button3Click(ActionEvent event) throws IOException {
        //satelliteData.display_elements();
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("DopsChart.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene=new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void button4Click(ActionEvent event) throws IOException {
        //satelliteData.display_elements();
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("Skyplot.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene=new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
