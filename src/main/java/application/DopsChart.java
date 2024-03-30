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
import java.util.*;

public class DopsChart implements Initializable {
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private Pane chartPane;
    @FXML
    private CheckBox gpsCheckBox;
    @FXML
    private CheckBox glonassCheckBox;
    @FXML
    private CheckBox galileoCheckBox;
    @FXML
    private CheckBox allCheckBox;


    private SatelliteCalculations satelliteData = WelcomeSceneController.satelliteData;
    private int hourInterval = satelliteData.hourInterval;
    private int minuteInterval = satelliteData.minuteInterval;
    private List<List<Double>> nav = WelcomeSceneController.nav;
    private int startYear = satelliteData.year;
    private int startMonth = satelliteData.month;
    private int startDay = satelliteData.day;
    final NumberAxis xAxis = new NumberAxis(0,this.hourInterval,1);
    final NumberAxis yAxis = new NumberAxis();
    final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
    private List<List<Double>> dops = satelliteData.getDops(this.nav);
    private int startHour = satelliteData.hour;
    private int startMinute = satelliteData.minute;
    private int startSecond = satelliteData.second;

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
    public void populateChart(LineChart<Number,Number> lineChart,List<List<Double>>dop){
        String[] factors ={"GDOP", "PDOP","TDOP","HDOP","VDOP"};
        int currentFactor=0;
        for(List<Double> factor : dop){
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(factors[currentFactor]);
            double time=0;
            for(Double value : factor){
                final double timeFinal = time;
                final int currentFactorFinal = currentFactor;
                XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(time, value);
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
                        String dopString = String.format("%.2f", value);
                        Tooltip tooltip = new Tooltip("Factor: " + factors[currentFactorFinal] + "\nTime: " + timeString + "\nValue: " + dopString);
                        Tooltip.install(newNode, tooltip);
                    }
                });
                time += (double) minuteInterval / 60;
            }
            currentFactor++;
            lineChart.getData().add(series);
        }

    }
    public void gps(){
        if(gpsCheckBox.isSelected()){
            glonassCheckBox.setSelected(false);
            galileoCheckBox.setSelected(false);
            allCheckBox.setSelected(false);
            List<List<Double>> shortenedNav=new ArrayList<>();
            for (List<Double> sat : nav){
                if(sat.getFirst()<38){
                    shortenedNav.add(sat);
                }
            }
            List<List<Double>> gpsDop = satelliteData.getDops(shortenedNav);
            lineChart.getData().clear();
            populateChart(this.lineChart,gpsDop);
        }
    }
    public void glonass(){
        if(glonassCheckBox.isSelected()){
            gpsCheckBox.setSelected(false);
            galileoCheckBox.setSelected(false);
            allCheckBox.setSelected(false);
            List<List<Double>> shortenedNav=new ArrayList<>();
            for (List<Double> sat : nav){
                if(sat.getFirst()>=38 && sat.getFirst()<202){
                    shortenedNav.add(sat);
                }
            }
            List<List<Double>> glonassDop = satelliteData.getDops(shortenedNav);
            lineChart.getData().clear();
            populateChart(this.lineChart,glonassDop);
        }

    }
    public void galileo(){
        if(galileoCheckBox.isSelected()){
            gpsCheckBox.setSelected(false);
            glonassCheckBox.setSelected(false);
            allCheckBox.setSelected(false);
            List<List<Double>> shortenedNav=new ArrayList<>();
            for (List<Double> sat : nav){
                if(sat.getFirst()>=202){
                    shortenedNav.add(sat);
                }
            }
            List<List<Double>> galileoDop = satelliteData.getDops(shortenedNav);
            lineChart.getData().clear();
            populateChart(this.lineChart,galileoDop);
        }

    }
    public void all(ActionEvent event){
        if(allCheckBox.isSelected()){
            glonassCheckBox.setSelected(false);
            gpsCheckBox.setSelected(false);
            galileoCheckBox.setSelected(false);
            lineChart.getData().clear();
            populateChart(this.lineChart,this.dops);
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.xAxis.setLabel("Time (in hours) starting from: \n       "+SatelliteCalculations.formatValue(startDay)+"."+SatelliteCalculations.formatValue(startMonth)+"."+ startYear
                +" - "+SatelliteCalculations.formatValue(startHour)+":"+SatelliteCalculations.formatValue(startMinute)+":"+SatelliteCalculations.formatValue(startSecond));
        this.yAxis.setLabel("Values");
        populateChart(this.lineChart,this.dops);
        lineChart.prefWidthProperty().bind(chartPane.widthProperty());
        lineChart.prefHeightProperty().bind(chartPane.heightProperty());
        lineChart.setAnimated(false);
        chartPane.getChildren().add(lineChart);
        allCheckBox.setSelected(true);

    }
}
