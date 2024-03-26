package application;
import Calculations.SatelliteCalculations;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.fx.ChartViewer;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.renderer.DefaultPolarItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class Skyplot implements Initializable{
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private Pane chartPane;
    @FXML
    private Slider hourSlider;
    private SatelliteCalculations satelliteData = WelcomeSceneController.satelliteData;
    private int hourInterval = satelliteData.hourInterval;
    private int minuteInterval = satelliteData.minuteInterval;
    private List<List<Double>> nav = WelcomeSceneController.nav;
    int maxRecords = 32;
    List<List<Double>> shortenedNav = nav.subList(0, Math.min(nav.size(), maxRecords));
    private Map<Double, List<Double>> azimuthElevationMap = satelliteData.getAzimuthElevation(shortenedNav);
    private double mask = Math.toDegrees(satelliteData.mask);
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

    private XYDataset createDataset(int currentTime){
        XYSeriesCollection result = new XYSeriesCollection();
        for (Map.Entry<Double, List<Double>> entry : azimuthElevationMap.entrySet()){
            Double satelliteId = entry.getKey();
            List<Double> azimuthElevation = entry.getValue();
            int index;
            if(currentTime==0) {
                index = currentTime;
            }
            else{
                index = currentTime*2;
            }
            //System.out.println("przedif"+ index);
            if(index>=0 && index<azimuthElevation.size()-1) {
                if (azimuthElevation.get(index + 1) > this.mask) {
                    //System.out.println("poif"+index);
                    double az = azimuthElevation.get(index);
                    double el = azimuthElevation.get(index + 1);
                    XYSeries series = new XYSeries("Satellite " + satelliteId);
                    series.add(az, el);
                    result.addSeries(series);
                }
            }
        }
    return result;
    }
    private JFreeChart configureChart(XYDataset dataset){
        JFreeChart chart = ChartFactory.createPolarChart("Skyplot",dataset,true,false,false
        );
        PolarPlot plot = (PolarPlot) chart.getPlot();
//        plot.setBackgroundPaint(Color.WHITE);
//        plot.setRadiusGridlinePaint(Color.DARK_GRAY);
//        plot.setAngleGridlinePaint(Color.BLACK);
        plot.setRadiusMinorGridlinesVisible(false);
        DefaultPolarItemRenderer renderer = (DefaultPolarItemRenderer) plot.getRenderer();
        renderer.setShapesVisible(true);
//        plot.setAngleLabelFont(new Font("Arial", 12));
        NumberAxis rangeAxis = (NumberAxis) plot.getAxis();
        rangeAxis.setTickUnit(new NumberTickUnit(10.0));
        rangeAxis.setMinorTickMarksVisible(false);
        rangeAxis.setRange(0.0, 90.0);
        rangeAxis.setInverted(true);
        return chart;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hourSlider.setMin(0);
        System.out.println(hourInterval);
        hourSlider.setMax(hourInterval);
        hourSlider.setBlockIncrement(1);
        hourSlider.setShowTickMarks(true);
        hourSlider.setShowTickLabels(true);
        hourSlider.setMajorTickUnit(1); // Set this to the desired interval for major ticks
        hourSlider.setMinorTickCount(0); // Ensure there are no minor ticks
        hourSlider.setSnapToTicks(true); // Ensure the slider snaps to the ticks
        hourSlider.valueProperty().addListener(new ChangeListener<Number>()
        {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val)
            {
                hourSlider.setValue(new_val.intValue());
                updateChart(new_val.intValue());
                //System.out.println(new_val.intValue());
            }
        });
        for (Map.Entry<Double, List<Double>> entry : azimuthElevationMap.entrySet()){
            System.out.println(entry.getKey()+ ": "+entry.getValue());
        }

        updateChart((int)hourSlider.getValue());
    }
    private void updateChart(int currentTime){
        chartPane.getChildren().clear();
        XYDataset dataset = createDataset(currentTime);
        JFreeChart chart = configureChart(dataset);
        ChartViewer viewer = new ChartViewer(chart);
        viewer.setPrefWidth(900);
        viewer.setPrefHeight(650);
        chartPane.getChildren().add(viewer);
    }
}
