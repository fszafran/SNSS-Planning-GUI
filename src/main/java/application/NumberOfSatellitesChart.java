package application;

import Calculations.SatelliteCalculations;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;


public class NumberOfSatellitesChart implements Initializable {
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private Pane chartPane;
    @FXML
    private CheckBox glonassCheckBox;
    @FXML
    private CheckBox gpsCheckBox;
    @FXML
    private CheckBox galileoCheckBox;
    @FXML
    private CheckBox allCheckBox;

    private final SatelliteCalculations satelliteData = WelcomeScene.satelliteData;
    private final int hourInterval = satelliteData.hourInterval;
    private final int minuteInterval = satelliteData.minuteInterval;
    private final List<List<Double>> nav = WelcomeScene.nav;
    private final List<Integer> satellitesNumberForMax = satelliteData.getSatellitesAtTheMoment(nav);
    private final NumberAxis xAxis = new NumberAxis(0,hourInterval,1);
    private final NumberAxis yAxis = new NumberAxis();
    final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
    private final int startYear = satelliteData.year;
    private final int startMonth = satelliteData.month;
    private final int startDay = satelliteData.day;
    private final int startHour = satelliteData.hour;
    private final int startMinute = satelliteData.minute;
    private final int startSecond = satelliteData.second;

    public void back(ActionEvent event) throws IOException {
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
    public void populateChart(LineChart<Number,Number> lineChart,List<List<Double>> navSat){
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        double time = 0;
        List<Integer> satellitesAtTheMoment = satelliteData.getSatellitesAtTheMoment(navSat);
        for(int num : satellitesAtTheMoment){
            final double timeFinal = time;
            XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(time, num);
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
                    Tooltip tooltip = new Tooltip( "Number of satellites "+ num +"\nTime: " + timeString);
                    Tooltip.install(newNode, tooltip);
                }
            });
            time += (double) minuteInterval / 60;
        }
        lineChart.getData().add(series);
        lineChart.setLegendVisible(false);
    }
    public void gps(){
        if(gpsCheckBox.isSelected()){
            glonassCheckBox.setSelected(false);
            galileoCheckBox.setSelected(false);
            allCheckBox.setSelected(false);
            List<List<Double>> shortenedNav = new ArrayList<>();
            for (List<Double> sat : nav){
                if(sat.getFirst()<38){
                    shortenedNav.add(sat);
                }
            }
            lineChart.getData().clear();
            populateChart(this.lineChart, shortenedNav);
        }
    }
    public void glonass(){
        if(glonassCheckBox.isSelected()){
            gpsCheckBox.setSelected(false);
            galileoCheckBox.setSelected(false);
            allCheckBox.setSelected(false);
            List<List<Double>> shortenedNav = new ArrayList<>();
            for (List<Double> sat : nav){
                if(sat.getFirst()>=38 && sat.getFirst()<202){
                    shortenedNav.add(sat);
                }
            }
            lineChart.getData().clear();
            populateChart(this.lineChart, shortenedNav);
        }
    }
    public void galileo(){
        if(galileoCheckBox.isSelected()){
            gpsCheckBox.setSelected(false);
            glonassCheckBox.setSelected(false);
            allCheckBox.setSelected(false);
            List<List<Double>> shortenedNav = new ArrayList<>();
            for (List<Double> sat : nav){
                if(sat.getFirst()>=202){
                    shortenedNav.add(sat);
                }
            }
            lineChart.getData().clear();
            populateChart(this.lineChart, shortenedNav);
            
        }
    }
    public void all(ActionEvent event){
        if(allCheckBox.isSelected()){
            glonassCheckBox.setSelected(false);
            gpsCheckBox.setSelected(false);
            galileoCheckBox.setSelected(false);
            lineChart.getData().clear();
            populateChart(this.lineChart,this.nav);
        }
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        yAxis.setAutoRanging(false);
        int maxSatellites = satellitesNumberForMax.stream().max(Integer::compareTo).get();
        yAxis.setUpperBound(maxSatellites+5);
        xAxis.setLabel("Time (in hours) starting from: \n       "+SatelliteCalculations.formatValue(startDay)+"."+SatelliteCalculations.formatValue(startMonth)+"."+ startYear
                +" - "+SatelliteCalculations.formatValue(startHour)+":"+SatelliteCalculations.formatValue(startMinute)+":"+SatelliteCalculations.formatValue(startSecond));
        yAxis.setLabel("Number of satellites");
        populateChart(lineChart,nav);
        lineChart.prefWidthProperty().bind(chartPane.widthProperty());
        lineChart.prefHeightProperty().bind(chartPane.heightProperty());
        lineChart.setAnimated(false);
        chartPane.getChildren().add(lineChart);
        allCheckBox.setSelected(true);
    }
}
