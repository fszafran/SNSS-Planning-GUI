package application;

import Calculations.AlmanacModule;
import Calculations.SatelliteCalculations;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class WelcomeSceneController implements Initializable {
    @FXML
        private TextField phiDeg;
    @FXML
        private TextField phiDeg1;
    @FXML
        private TextField phiDeg2;
    @FXML
        private TextField lamDeg;
    @FXML
        private TextField lamDeg1;
    @FXML
        private TextField lamDeg2;
    @FXML
        private Spinner<Integer> heightSpinner;
    @FXML
        private Spinner<Integer> maskSpinner;
    @FXML
        private DatePicker dateStart;
    @FXML
        private ComboBox<Integer> hourVariants;
        private final Integer[] hrs = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,24};
    @FXML
        private ComboBox<Integer> minuteVariants;
        private final Integer[] mnt = {10,15,20,30};
    @FXML
        private ComboBox<Integer> startHour;
    @FXML
        private ComboBox<Integer> startMinute;
        private final Integer[] mts = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60};


    @FXML
        private ComboBox<Integer> startSecond;
    @FXML
            private Button selectButton;

    int phi;
    int lam;
    int height;
    double mask;
    int hour;
    int minute;
    int second;
    int year;
    int month;
    int day;
    int hourInterval;
    int minuteInterval;
    private Stage stage;
    private Scene scene;
    private Parent root;
    public static SatelliteCalculations satelliteData;
    public static List<List<Double>> nav;

//    static {
//        try {
//            nav = AlmanacModule.readAlmanac("src/main/resources/Almanac2024053.alm");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static Map<Double, List<Double>> elevationMap;

    public WelcomeSceneController() throws IOException {
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        SpinnerValueFactory<Integer> heightFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,8500);
        SpinnerValueFactory<Integer> maskFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,20);
        heightFactory.setValue(100);
        maskFactory.setValue(10);
        heightSpinner.setValueFactory(heightFactory);
        maskSpinner.setValueFactory(maskFactory);
        hourVariants.getItems().addAll(hrs);
        minuteVariants.getItems().addAll(mnt);
        startHour.getItems().addAll(hrs);
        startMinute.getItems().addAll(mts);
        startSecond.getItems().addAll(mts);


    }
    public void submition(ActionEvent event) throws IOException {
        try{//POMYSL NAD DOUBLEM
            phi = Integer.parseInt(phiDeg.getText()) + Integer.parseInt(phiDeg1.getText()) / 60 + Integer.parseInt(phiDeg2.getText()) / 3600;
            lam = Integer.parseInt(lamDeg.getText()) + Integer.parseInt(lamDeg1.getText()) / 60 + Integer.parseInt(lamDeg2.getText()) / 3600;
            height = heightSpinner.getValue();
            mask = maskSpinner.getValue();
            mask = Math.toRadians(mask);
            LocalDate startDate = dateStart.getValue();
            year = startDate.getYear();
            month = startDate.getMonthValue();
            day = startDate.getDayOfMonth();
            hourInterval = hourVariants.getValue();
            minuteInterval = minuteVariants.getValue();
            hour = startHour.getValue();
            minute = startMinute.getValue();
            second = startSecond.getValue();
            if (nav == null){
                nav = AlmanacModule.readAlmanac("src/main/resources/Almanac2024053.alm");
            }


            satelliteData = new SatelliteCalculations(phi, lam, height, mask, year, month, day, hourInterval, minuteInterval, hour, minute, second);
            //MOŻE LEPIEJ ZROBIĆ W CONTROLLERZE
            elevationMap = satelliteData.getElevationTime(nav);


            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("VisualisationMenu.fxml")));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene=new Scene(root);
            stage.setScene(scene);
            stage.show();
            //satelliteData.display_elements();
        }
        catch (NullPointerException e){
            showAlert("Puste parametry","Uzupełnij wszystkie pola");
        }
        catch (NumberFormatException e) {
            showAlert("Niepoprawny format wejściowy", "Wprowadź poprawne wartości liczbowe");
        }
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public void selectAlmanach(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ALM Files","*.alm"));
        File file = fileChooser.showOpenDialog(stage);
        if(file!=null){
            nav=AlmanacModule.readAlmanac(file.getAbsolutePath());
            selectButton.setText(file.getName());
        }
    }
}
