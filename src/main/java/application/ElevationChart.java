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
    @FXML
    private CheckBox allCheckBox;
    private SatelliteCalculations satelliteData = WelcomeSceneController.satelliteData;
    private double mask = Math.toDegrees(satelliteData.mask);
    private int hourInterval = satelliteData.hourInterval;
    private int minuteInterval = satelliteData.minuteInterval;
    private Map<Double, List<Double>> elevationMap = WelcomeSceneController.elevationMap;
    private List<List<Double>> nav = WelcomeSceneController.nav;
    private final NumberAxis xAxis = new NumberAxis(0,hourInterval,1);
    private final NumberAxis yAxis = new NumberAxis();
    private final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
    private int startYear = satelliteData.year;
    private int startMonth = satelliteData.month;
    private int startDay = satelliteData.day;
    private int startHour = satelliteData.hour;
    private int startMinute = satelliteData.minute;
    private int startSecond = satelliteData.second;

    public void populateChart(LineChart<Number,Number> elevationChart,Map<Double, List<Double>>elevationMap){
        for (Map.Entry<Double, List<Double>> entry : elevationMap.entrySet()) {
            String satelliteId;
            List<Double> elevations = entry.getValue();
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            double time = 0;
            if (entry.getKey() < 38) {
                int keyWithoutDecimals = entry.getKey().intValue();
                satelliteId = String.format("G%d", keyWithoutDecimals);
            }
            else if(entry.getKey() >= 38 && entry.getKey() < 202){
                int keyWithoutDecimals = entry.getKey().intValue()-37;
                satelliteId = String.format("R%d", keyWithoutDecimals);
            }
            else{
                int keyWithoutDecimals = entry.getKey().intValue()-200;
                satelliteId = String.format("E%d", keyWithoutDecimals);
            }

            for (Double elevation : elevations) {
                if (elevation > this.mask-5) {
                    final double timeFinal = time;
                    XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(time, elevation);
                    series.getData().add(dataPoint);
                    dataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            int hFormat = (int) Math.floor(timeFinal); // Extract whole hours
                            int mFormat = (int) Math.round((timeFinal - hFormat) * 60); // Calculate minutes (rounded)
                            if(mFormat>=60){
                                hFormat++;
                                mFormat=0;
                            }
                            String timeString = String.format("+%d hours  %02d minutes", hFormat, mFormat);
                            String elevationString = String.format("%.2f", elevation);
                            Tooltip tooltip = new Tooltip("Satellite: " + satelliteId + "\nTime: " + timeString + "\nElevation: " + elevationString);
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
            GlonassCheckBox.setSelected(false);
            GalileoCheckBox.setSelected(false);
            List<List<Double>> navGPS = new ArrayList<>();
            for(List<Double> sat : this.nav){
                if(sat.getFirst()<38){
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
            GPSCheckBox.setSelected(false);
            GalileoCheckBox.setSelected(false);
            List<List<Double>> navGlonass = new ArrayList<>();
            for(List<Double> sat : this.nav){
                if(sat.getFirst()>=38 && sat.getFirst()<202){
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
            GlonassCheckBox.setSelected(false);
            GPSCheckBox.setSelected(false);
            List<List<Double>> navGalileo = new ArrayList<>();
            for(List<Double> sat : this.nav){
                if(sat.getFirst()>=202){
                    navGalileo.add(sat);
                }
            }
            Map<Double, List<Double>> elevationGalileoMap = satelliteData.getElevationTime(navGalileo);
            lineChart.getData().clear();
            populateChart(this.lineChart,elevationGalileoMap);
        }
    }
    public void all(ActionEvent event){
        if(allCheckBox.isSelected()){
            GlonassCheckBox.setSelected(false);
            GPSCheckBox.setSelected(false);
            GalileoCheckBox.setSelected(false);
            lineChart.getData().clear();
            populateChart(this.lineChart,this.elevationMap);
        }
    }
    @Override
    public void initialize(URL arg0, ResourceBundle arg1){

        xAxis.setLabel("Time (in hours) starting from: \n       "+SatelliteCalculations.formatValue(startDay)+"."+SatelliteCalculations.formatValue(startMonth)+"."+ startYear
                +" - "+SatelliteCalculations.formatValue(startHour)+":"+SatelliteCalculations.formatValue(startMinute)+":"+SatelliteCalculations.formatValue(startSecond));
        yAxis.setLabel("Elevation [degrees]");
        yAxis.setLowerBound(mask);
        yAxis.setUpperBound(90);
        yAxis.setAutoRanging(false);
        xAxis.setMinorTickCount(60/minuteInterval);
        xAxis.setMinorTickVisible(true);
        populateChart(lineChart,this.elevationMap);
        lineChart.prefWidthProperty().bind(chartPane.widthProperty());
        lineChart.prefHeightProperty().bind(chartPane.heightProperty());
        lineChart.setAnimated(false);
        chartPane.getChildren().add(lineChart);
    }

}
