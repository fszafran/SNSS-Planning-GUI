package application;
import Calculations.SatelliteCalculations;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
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
import java.util.*;


public class Skyplot implements Initializable {
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private Pane chartPane;
    @FXML
    private Slider hourSlider;
    @FXML
    private CheckBox gpsCheckBox;
    @FXML
    private CheckBox glonassCheckBox;
    @FXML
    private CheckBox galileoCheckBox;
    @FXML
    private CheckBox allCheckBox;
    private final SatelliteCalculations satelliteData = WelcomeScene.satelliteData;
    private final int hourInterval = satelliteData.hourInterval;
    private final List<List<Double>> nav = WelcomeScene.nav;
    private final Map<Double, List<Double>> azimuthElevationMap = satelliteData.getAzimuthElevation(nav);
    private final double mask = Math.toDegrees(satelliteData.mask);
    private Map<Double, List<Double>> currentMap;
    private final int startYear = satelliteData.year;
    private final int startMonth = satelliteData.month;
    private final int startDay = satelliteData.day;
    private final int startHour = satelliteData.hour;
    private final int startMinute = satelliteData.minute;
    private final int startSecond = satelliteData.second;

    public void back(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("VisualisationMenu.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void mainMenu(ActionEvent event) throws IOException {
        root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("WelcomeScene.fxml")));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private XYDataset createDataset(int currentTime, Map<Double, List<Double>> skyMap) {
        XYSeriesCollection result = new XYSeriesCollection();
        for (Map.Entry<Double, List<Double>> entry : skyMap.entrySet()) {
            String satelliteId = getString(entry);
            List<Double> azimuthElevation = entry.getValue();
            int index;
            if (currentTime == 0) {
                index = currentTime;
            } else {
                index = currentTime * 2;
            }
            if (index >= 0 && index < azimuthElevation.size() - 1) {
                if (azimuthElevation.get(index + 1) > this.mask) {
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

    private static String getString(Map.Entry<Double, List<Double>> entry) {
        String satelliteId;
        if (entry.getKey() < 38) {
            String keyWithoutDecimals = SatelliteCalculations.formatValue(entry.getKey().intValue());
            satelliteId = "G"+keyWithoutDecimals;
        } else if (entry.getKey() >= 38 && entry.getKey() < 202) {
            String keyWithoutDecimals = SatelliteCalculations.formatValue(entry.getKey().intValue() - 37);
            satelliteId = "R"+keyWithoutDecimals;
        } else {
            String keyWithoutDecimals = SatelliteCalculations.formatValue(entry.getKey().intValue() - 200);
            satelliteId = "E"+keyWithoutDecimals;
        }
        return satelliteId;
    }

    private JFreeChart configureChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createPolarChart("", dataset, true, false, false
        );
        PolarPlot plot = (PolarPlot) chart.getPlot();
        plot.setRadiusMinorGridlinesVisible(false);
        DefaultPolarItemRenderer renderer = (DefaultPolarItemRenderer) plot.getRenderer();
        renderer.setShapesVisible(true);
        NumberAxis rangeAxis = (NumberAxis) plot.getAxis();
        rangeAxis.setTickUnit(new NumberTickUnit(10.0));
        rangeAxis.setMinorTickMarksVisible(false);
        rangeAxis.setRange(0.0, 90.0);
        rangeAxis.setInverted(true);
        return chart;
    }
    private void updateChart(int currentTime, Map<Double, List<Double>> skyMap) {
        chartPane.getChildren().clear();
        XYDataset dataset = createDataset(currentTime, skyMap);
        JFreeChart chart = configureChart(dataset);
        ChartViewer viewer = new ChartViewer(chart);
        viewer.setPrefWidth(900);
        viewer.setPrefHeight(610);
        chartPane.getChildren().add(viewer);
    }

    public void gps(ActionEvent event) {
        if (gpsCheckBox.isSelected()) {
            glonassCheckBox.setSelected(false);
            galileoCheckBox.setSelected(false);
            allCheckBox.setSelected(false);
            List<List<Double>> shortenedNav = new ArrayList<>();
            for (List<Double> sat : this.nav) {
                if (sat.getFirst() < 38) {
                    shortenedNav.add(sat);
                }
            }
            Map<Double, List<Double>> skyMap = satelliteData.getAzimuthElevation(shortenedNav);
            this.currentMap = skyMap;
            updateChart((int) hourSlider.getValue(),skyMap);
        }
    }
    public void glonass(ActionEvent event){
        if(glonassCheckBox.isSelected()){
            gpsCheckBox.setSelected(false);
            galileoCheckBox.setSelected(false);
            allCheckBox.setSelected(false);
            List<List<Double>> shortenedNav = new ArrayList<>();
            for(List<Double> sat : this.nav){
                if(sat.getFirst()>=38 && sat.getFirst()<202){
                    shortenedNav.add(sat);
                }
            }
            Map<Double, List<Double>> skyMap = satelliteData.getAzimuthElevation(shortenedNav);
            this.currentMap = skyMap;
            updateChart((int) hourSlider.getValue(),skyMap);
        }
    }
    public void galileo(ActionEvent event) {
        if (galileoCheckBox.isSelected()) {
            glonassCheckBox.setSelected(false);
            gpsCheckBox.setSelected(false);
            allCheckBox.setSelected(false);
            List<List<Double>> shortenedNav = new ArrayList<>();
            for (List<Double> sat : this.nav) {
                if (sat.getFirst() >= 202) {
                    shortenedNav.add(sat);
                }
            }
            Map<Double, List<Double>> skyMap = satelliteData.getAzimuthElevation(shortenedNav);
            this.currentMap = skyMap;
            updateChart((int) hourSlider.getValue(), skyMap);
        }
    }
    public void all(ActionEvent event){
        if(allCheckBox.isSelected()){
            glonassCheckBox.setSelected(false);
            gpsCheckBox.setSelected(false);
            galileoCheckBox.setSelected(false);
            this.currentMap = azimuthElevationMap;
            updateChart((int) hourSlider.getValue(), this.currentMap);
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hourSlider.setMin(0);
        hourSlider.setMax(hourInterval);
        hourSlider.setBlockIncrement(1);
        hourSlider.setShowTickMarks(true);
        hourSlider.setShowTickLabels(true);
        hourSlider.setMajorTickUnit(1);
        hourSlider.setMinorTickCount(0);
        hourSlider.setSnapToTicks(true);
        Tooltip tooltip = new Tooltip("Time (in hours) starting from: \n       "+SatelliteCalculations.formatValue(startDay)+"."+SatelliteCalculations.formatValue(startMonth)+"."+ startYear
        +" - "+SatelliteCalculations.formatValue(startHour)+":"+SatelliteCalculations.formatValue(startMinute)+":"+SatelliteCalculations.formatValue(startSecond));
        hourSlider.setTooltip(tooltip);
        hourSlider.valueProperty().addListener((ov, old_val, new_val) -> {
            hourSlider.setValue(new_val.intValue());
            updateChart(new_val.intValue(),currentMap);
        });
        currentMap = azimuthElevationMap;
        updateChart((int) hourSlider.getValue(), currentMap);
        allCheckBox.setSelected(true);
    }

}
