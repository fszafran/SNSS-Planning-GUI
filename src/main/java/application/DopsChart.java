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
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class DopsChart implements Initializable {
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private Pane chartPane;

    private SatelliteCalculations satelliteData = WelcomeSceneController.satelliteData;
    private int hourInterval = satelliteData.hourInterval;
    private int minuteInterval = satelliteData.minuteInterval;
    private List<List<Double>> nav = WelcomeSceneController.nav;
    int maxRecords = 32;
    List<List<Double>> shortenedNav = nav.subList(0, Math.min(nav.size(), maxRecords));
    private List<List<Double>> dops = satelliteData.getDops(shortenedNav);

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
    public void populateChart(LineChart<Number,Number> lineChart){
        String[] factors ={"GDOP", "PDOP","TDOP","HDOP","VDOP"};
        int currentFactor=0;
        for(List<Double> factor : dops){
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
                        String timeString = String.format("%d hours  %02d minutes", hFormat, mFormat);
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
        lineChart.setLegendVisible(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final NumberAxis xAxis = new NumberAxis(0,hourInterval,1);
        final NumberAxis yAxis = new NumberAxis();
        final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        populateChart(lineChart);
        lineChart.prefWidthProperty().bind(chartPane.widthProperty());
        lineChart.prefHeightProperty().bind(chartPane.heightProperty());
        lineChart.setAnimated(false);
        chartPane.getChildren().add(lineChart);
    }
}