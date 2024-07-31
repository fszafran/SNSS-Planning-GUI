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

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class WelcomeScene implements Initializable {
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
        public DatePicker dateStart;
    @FXML
        private ComboBox<Integer> hourVariants;
        private final Integer[] hrs = {0,1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
        private final Integer[] hrsInterval={6,12,24};
    @FXML
        private ComboBox<Integer> minuteVariants;
        private final Integer[] mnt = {5,10,15,20,30};
    @FXML
        public ComboBox<Integer> startHour;
    @FXML
        public ComboBox<Integer> startMinute;
        private final Integer[] mts = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59};
    @FXML
        public ComboBox<Integer> startSecond;
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
    LocalDate selectedDate;
    private Stage stage;
    private Scene scene;
    private Parent root;
    public static SatelliteCalculations satelliteData;
    public static List<List<Double>> nav;
    public static Map<Double, List<Double>> elevationMap;

    public void submition(ActionEvent event) throws IOException {
        try{
            File dataFile = new File(System.getProperty("user.dir") + File.separator + "data.txt");
            if(dataFile.exists()){
                dataFile.delete();
            }
            else{
                dataFile.createNewFile();
            }
            try (PrintWriter writer = new PrintWriter(dataFile)){
                writer.println(phiDeg.getText());
                writer.println(phiDeg1.getText());
                writer.println(phiDeg2.getText());
                writer.println(lamDeg.getText());
                writer.println(lamDeg1.getText());
                writer.println(lamDeg2.getText());
                writer.println(heightSpinner.getValue());
                writer.println(maskSpinner.getValue());
                writer.println(startHour.getValue());
                writer.println(startMinute.getValue());
                writer.println(startSecond.getValue());
                writer.println(hourVariants.getValue());
                writer.println(minuteVariants.getValue());
                writer.println(dateStart.getValue());
            }
            catch(Exception e){
                System.out.println(Arrays.toString(e.getStackTrace()));
            }

            phi = Integer.parseInt(phiDeg.getText()) + Integer.parseInt(phiDeg1.getText()) / 60 + Integer.parseInt(phiDeg2.getText()) / 3600;
            lam = Integer.parseInt(lamDeg.getText()) + Integer.parseInt(lamDeg1.getText()) / 60 + Integer.parseInt(lamDeg2.getText()) / 3600;
            height = heightSpinner.getValue();
            mask = maskSpinner.getValue();
            mask = Math.toRadians(mask);
            selectedDate = dateStart.getValue();
            year = selectedDate.getYear();
            month = selectedDate.getMonthValue();
            day = selectedDate.getDayOfMonth();
            hourInterval = hourVariants.getValue();
            minuteInterval = minuteVariants.getValue();
            hour = startHour.getValue();
            minute = startMinute.getValue();
            second = startSecond.getValue();
            if (nav == null){
                nav = AlmanacModule.readAlmanac("src/main/resources/Almanac2024053.alm");
            }
            satelliteData = new SatelliteCalculations(phi, lam, height, mask, year, month, day, hourInterval, minuteInterval, hour, minute, second);
            elevationMap = satelliteData.getElevationTime(nav);
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("VisualisationMenu.fxml")));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene=new Scene(root);
            stage.setScene(scene);
            stage.show();
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
    private ArrayList<String> loadFromTxt(File file) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        catch (FileNotFoundException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
        return lines;
    }
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        SpinnerValueFactory<Integer> heightFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,8848);
        SpinnerValueFactory<Integer> maskFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0,90);
        heightFactory.setValue(100);
        maskFactory.setValue(10);
        heightSpinner.setValueFactory(heightFactory);
        maskSpinner.setValueFactory(maskFactory);
        hourVariants.getItems().addAll(hrsInterval);
        minuteVariants.getItems().addAll(mnt);
        startHour.getItems().addAll(hrs);
        startMinute.getItems().addAll(mts);
        startSecond.getItems().addAll(mts);
        File file = new File(System.getProperty("user.dir") + File.separator + "data.txt");
        if (file.exists()){
            try {
                ArrayList<String> data = loadFromTxt(file);
                phiDeg.setText(data.get(0));
                phiDeg1.setText(data.get(1));
                phiDeg2.setText(data.get(2));
                lamDeg.setText(data.get(3));
                lamDeg1.setText(data.get(4));
                lamDeg2.setText(data.get(5));
                heightSpinner.getValueFactory().setValue(Integer.valueOf(data.get(6)));
                maskSpinner.getValueFactory().setValue(Integer.valueOf(data.get(7)));
                startHour.setValue(Integer.valueOf(data.get(8)));
                startMinute.setValue(Integer.valueOf(data.get(9)));
                startSecond.setValue(Integer.valueOf(data.get(10)));
                hourVariants.setValue(Integer.valueOf(data.get(11)));
                minuteVariants.setValue(Integer.valueOf(data.get(12)));
                LocalDate date = LocalDate.parse(data.get(13));
                dateStart.setValue(date);
                file.delete();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
