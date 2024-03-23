package application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import Calculations.SatelliteCalculations;
import javafx.scene.chart.LineChart;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ElevationChart implements Initializable {
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private Pane chartPane;
    @FXML
    private CheckBox GPSCheckBox;
    @FXML
    private CheckBox GalileoCheckBox;
    @FXML
    private CheckBox GlonassCheckBox;
    private SatelliteCalculations satelliteData = WelcomeSceneController.satelliteData;
    private int hourInterval = satelliteData.hourInterval;
    private int minuteInterval = satelliteData.minuteInterval;
    private Map<Double, List<Double>> elevationMap = WelcomeSceneController.elevationMap;
    private List<List<Double>> nav = WelcomeSceneController.nav;
    private final NumberAxis xAxis = new NumberAxis(0,hourInterval,1);
    private final NumberAxis yAxis = new NumberAxis();
    private final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
    public void populateChart(LineChart<Number,Number> elevationChart,Map<Double, List<Double>>elevationMap){
        boolean isInValidSegment = false;

        for (Map.Entry<Double, List<Double>> entry : elevationMap.entrySet()) {
            Double satelliteId = entry.getKey();
            List<Double> elevations = entry.getValue();
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            double time = 0;

            for (Double elevation : elevations) {
                if (elevation > 10) {
                    final double timeFinal = time; // Create a final copy of the time variable
                    XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(time, elevation);
                    series.getData().add(dataPoint);
                    dataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            Tooltip tooltip = new Tooltip("Satellite ID: " + satelliteId + "\nTime: " + timeFinal + " hours\nElevation: " + elevation);
                            Tooltip.install(newNode, tooltip);
                        }
                    });
                    time += (double) minuteInterval / 60; // Increment time by the minuteInterval converted to hours
                } else {
                    time += (double) minuteInterval / 60;
                }
            }
            elevationChart.getData().add(series);
        }

        elevationChart.setLegendVisible(false);



    }
    @Override
    public void initialize(URL arg0, ResourceBundle arg1){

        xAxis.setLabel("Time (hours)");
        yAxis.setLabel("Elevation (m)");
        xAxis.setMinorTickCount(60/minuteInterval);
        xAxis.setMinorTickVisible(true);
        populateChart(lineChart,this.elevationMap);
        lineChart.prefWidthProperty().bind(chartPane.widthProperty());
        lineChart.prefHeightProperty().bind(chartPane.heightProperty());
        lineChart.setAnimated(false);
        chartPane.getChildren().add(lineChart);
        for(List<Double> sat : this.nav){
            System.out.println("nr"+sat.getFirst());
        }
    }


    public void back(ActionEvent event) throws IOException{
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("VisualisationMenu.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene=new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void mainMenu(ActionEvent event) throws IOException{
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("WelcomeScene.fxml")));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene=new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void GPS(ActionEvent event){
        if(GPSCheckBox.isSelected()){
            List<List<Double>> navGPS = new ArrayList<>();
            for(List<Double> sat : this.nav){
                if(sat.getFirst()<33){
                    navGPS.add(sat);
                }
            }
            Map<Double, List<Double>> elevationGPSMap = satelliteData.getElevationTime(navGPS);
            lineChart.getData().clear();
            populateChart(this.lineChart,elevationGPSMap);
        }
    }
    public void glonass(ActionEvent event){
        if(GlonassCheckBox.isSelected()){
            List<List<Double>> navGlonass = new ArrayList<>();
            for(List<Double> sat : this.nav){
                if(sat.getFirst()>=33 && sat.getFirst()<80 ){
                    navGlonass.add(sat);
                }
            }
            Map<Double, List<Double>> elevationGlonassMap = satelliteData.getElevationTime(navGlonass);
            lineChart.getData().clear();
            populateChart(this.lineChart,elevationGlonassMap);
        }
    }
    public void galileo(ActionEvent event){
        if(GalileoCheckBox.isSelected()){
            List<List<Double>> navGalileo = new ArrayList<>();
            for(List<Double> sat : this.nav){
                if(sat.getFirst()>=100 && sat.getFirst()<200 ){
                    navGalileo.add(sat);
                }
            }
            Map<Double, List<Double>> elevationGalileoMap = satelliteData.getElevationTime(navGalileo);
            lineChart.getData().clear();
            populateChart(this.lineChart,elevationGalileoMap);
        }
    }

}
