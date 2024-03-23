package Calculations;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;

import java.util.*;

public class SatelliteCalculations {
    public int hour;
    public int minute;
    public int second;
    public int year;
    public int month;
    public int day;
    public int phi;

    int lam;
    int height;
    public int hourInterval;
    public int minuteInterval;
    double mask;
    double[] weekSecond;

    public SatelliteCalculations(int phi, int lam, int height, double mask, int year, int month, int day, int hourInterval, int minuteInterval, int hour, int minute, int second) {
        this.phi = phi;
        this.lam = lam;
        this.height = height;
        this.mask = mask;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hourInterval = hourInterval;
        this.minuteInterval = minuteInterval;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.weekSecond = getGPSTime(year, month, day, hour, minute, second);

    }

    public void display_elements() {
        System.out.println("Phi:" + this.phi);
        System.out.println("Lam:" + this.lam);
        System.out.println("Height:" + this.height);
        System.out.println("Mask:" + this.mask);
        System.out.println("Year:" + this.year);
        System.out.println("Month:" + this.month);
        System.out.println("Day:" + this.day);
        System.out.println("Hour Interval:" + this.hourInterval);
        System.out.println("Minute Interval:" + this.minuteInterval);
        System.out.println("Hour:" + this.hour);
        System.out.println("Minute:" + this.minute);
        System.out.println("Second:" + this.second);
        System.out.println("Sow:" + this.weekSecond[1]);

    }
//    List<List<Double>> nav = AlmanacModule.readAlmanac("src/main/resources/Almanac2024053.alm");

    public double[][] rNeu(double phi, double lam) {
        double[][] R = new double[3][3];
        R[0][0] = -Math.sin(phi) * Math.cos(lam);
        R[0][1] = -Math.sin(lam);
        R[0][2] = Math.cos(phi) * Math.cos(lam);

        R[1][0] = -Math.sin(phi) * Math.sin(lam);
        R[1][1] = Math.cos(lam);
        R[1][2] = Math.cos(phi) * Math.sin(lam);

        R[2][0] = Math.cos(phi);
        R[2][1] = 0;
        R[2][2] = Math.sin(phi);

        return R;
    }

    public double julday(int year, int month, int day, int hour) {
        if (month <= 2) {
            year = year - 1;
            month = month + 12;
        }
        return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + (double) hour / 24 - 1537.5;
    }

    public double[] getGPSTime(int year, int month, int day, int hour, int minute, int second) {
        double days = julday(year, month, day, 0) - julday(1980, 1, 6, 0);
        double week = Math.floor(days / 7);
        double CalculatedDay = days % 7;
        double secondOfWeek = CalculatedDay * 86400 + hour * 3600 + minute * 60 + second;
        return new double[]{week, secondOfWeek};
    }


    public double[] getSatPos(double t, double week, List<Double> rowNav) {
        double u = 3.986005e14;
        double WE = 7.2921151467e-5;
        double e = rowNav.get(2);
        double a = Math.pow(rowNav.get(3), 2);
        double omega = Math.toRadians(rowNav.get(4));
        double w = Math.toRadians(rowNav.get(5));
        double M = Math.toRadians(rowNav.get(6));
        double toa = rowNav.get(7);
        double i = Math.toRadians(rowNav.get(8) + 54);
        double omegaTime = Math.toRadians(rowNav.get(9) / 1000);
        double GPSWeek = rowNav.get(12);
        double time = week * 7 * 86400 + t;
        double toaWeek = GPSWeek * 7 * 86400 + toa;
        double tk = time - toaWeek;
        double n = Math.sqrt(u / Math.pow(a, 3));
        double Mk = M + n * tk;
        List<Double> E = new ArrayList<>();
        int j = 0;
        E.add(Mk);
        do {
            E.add(Mk + e * Math.sin(E.get(j)));
            j++;
            //System.out.println("Jotka:" + j);
        } while (!(Math.abs(E.get(j) - E.get(j - 1)) < 1e-12));
        double vk = Math.atan2(Math.sqrt(1 - Math.pow(e, 2)) * Math.sin(E.get(j)), Math.cos(E.get(j)) - e);
        double phik = vk + w;
        double rk = a * (1 - e * Math.cos(E.get(j)));
        double xk = rk * Math.cos(phik);
        double yk = rk * Math.sin(phik);
        double omegaK = omega + (omegaTime - WE) * tk - WE * toa;
        double X = xk * Math.cos(omegaK) - yk * Math.cos(i) * Math.sin(omegaK);
        double Y = xk * Math.sin(omegaK) + yk * Math.cos(i) * Math.cos(omegaK);
        double Z = yk * Math.sin(i);
        return new double[]{X, Y, Z};
    }

    public double[] blh2xyz(double phi, double lam, double height) {
        double a = 6378137;
        double e2 = 0.00669438002290;
        phi = Math.toRadians(phi);
        lam = Math.toRadians(lam);
        double N = a / Math.sqrt(1 - e2 * Math.pow(Math.sin(phi), 2));
        double X = (N + height) * Math.cos(phi) * Math.cos(lam);
        double Y = (N + height) * Math.cos(phi) * Math.sin(lam);
        double Z = (N * (1 - e2) + height) * Math.sin(phi);
        return new double[]{X, Y, Z};
    }

    int iter = 0;

//    public List<List<Double>> satellitePositionInTime(List<List<Double>> nav) {
//
//        double[][] R = rNeu(Math.toRadians(phi), Math.toRadians(lam));
//        double[][] RT = new double[3][3];
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 3; j++) {
//                RT[i][j] = R[j][i];
//            }
//        }
//        double[] XYZr = blh2xyz(phi, lam, height);
//        int start = (int) weekSecond[1];
//        int stop = start + 60 * 60 * hourInterval;
//        List<List<Double>> azimuthElevationCoords = new ArrayList<>();
//        List<List<Double>> A = new ArrayList<>();
//        Map<Double, List<Double>> elevationMap=new TreeMap<>();
//        for (int i = start; i <= stop; i += 60 * minuteInterval) {
//            A.clear();
//            iter++;
//            for (List<Double> sat : nav) {
//                double[] XYZs = getSatPos(i, weekSecond[0], sat);
//                double[] XYZsr = {XYZs[0] - XYZr[0], XYZs[1] - XYZr[1], XYZs[2] - XYZr[2]};
//                double[] neu = new double[RT.length];
//                for (int j = 0; j < RT.length; j++) {
//                    for (int k = 0; k < XYZsr.length; k++) {
//                        neu[j] += RT[j][k] * XYZsr[k];
//                    }
//                }
//
//                double azimuth = Math.atan2(neu[1], neu[0]);
//                if (azimuth < 0) {
//                    azimuth += 2 * Math.PI;
//                }
//
//                double elevation = Math.asin(neu[2] / Math.sqrt(Math.pow(neu[0], 2) + Math.pow(neu[1], 2) + Math.pow(neu[2], 2)));
////                if (iter == 1) {
////                    System.out.println(sat.getFirst() + " elev: " + Math.toDegrees(elevation));
////                }
//
//
//                List<Double> coords = new ArrayList<>();
//                coords.add(sat.getFirst());
//                coords.add((double) i - weekSecond[1]);
//                coords.add(XYZs[0]);
//                coords.add(XYZs[1]);
//                coords.add(XYZs[2]);
//                coords.add(Math.toDegrees(azimuth));
//                coords.add(Math.toDegrees(elevation));
//                azimuthElevationCoords.add(coords);
//
//                double p = Math.sqrt(Math.pow(XYZs[0] - XYZr[0], 2) + Math.pow(XYZs[1] - XYZr[1], 2) + Math.pow(XYZs[2] - XYZr[2], 2));
//
//                if (!elevationMap.containsKey(sat.getFirst())) {
//                    List<Double> elevations = new ArrayList<>();
//                    elevations.add(Math.toDegrees(elevation));
//                    elevationMap.put(sat.get(0), elevations);
//                } else {
//                    List<Double> elevations = elevationMap.get(sat.getFirst());
//                    elevations.add(Math.toDegrees(elevation));
//                    //this.elevationMap.put(sat.get(0), elevations);
//                }
//
//                if (elevation > mask) {
//                    List<Double> wierszA = new ArrayList<>();
//                    wierszA.add(-(XYZs[0] - XYZr[0]) / p);
//                    wierszA.add(-(XYZs[1] - XYZr[1]) / p);
//                    wierszA.add(-(XYZs[2] - XYZr[2]) / p);
//                    wierszA.add(1.0);
//                    A.add(wierszA);
//
//
//                }
//
//            }
//            //tu implementacja
//            double[][] matrixData = new double[A.size()][];
//            for (int it = 0; it < A.size(); it++) {
//                matrixData[it] = A.get(it).stream().mapToDouble(Double::doubleValue).toArray();
//            }
////            for (int test = 0; test < matrixData.length; test++) {
////                for (int test1 = 0; test1 < matrixData[test1].length; test1++) {
////                    System.out.print(matrixData[test][test1] + " ");
////                }
////                System.out.println();
////            }
//            A.clear();
//            RealMatrix AMatrix = new Array2DRowRealMatrix(matrixData);
//            RealMatrix transposeA = AMatrix.transpose();
//            RealMatrix ATA = transposeA.multiply(AMatrix);
//            RealMatrix Q = new LUDecomposition(ATA).getSolver().getInverse();
//            double GDOP = Math.sqrt(Q.getTrace());
//            double PDOP = Math.sqrt(Q.getEntry(0, 0) + Q.getEntry(1, 1) + Q.getEntry(2, 2));
//            double TDOP = Math.sqrt(Q.getEntry(3, 3));
//            RealMatrix Qxyz = Q.getSubMatrix(0, 2, 0, 2);
//            RealMatrix RTR = new Array2DRowRealMatrix(RT);
//            RealMatrix RR = new Array2DRowRealMatrix(R);
//            RealMatrix Qneu = RTR.multiply(Qxyz).multiply(RR);
//            double HDOP = Math.sqrt(Qneu.getEntry(0, 0) + Qneu.getEntry(1, 1));
//            double VDOP = Math.sqrt(Qneu.getEntry(2, 2));
//            double PDOPneu = Math.sqrt(Qneu.getEntry(0, 0) + Qneu.getEntry(1, 1) + Qneu.getEntry(2, 2));
//            if (Math.abs(PDOP - PDOPneu) > 1e-9) {
//                System.out.println("PDOP != PDOPneu");
//            }
//
//
//        }
//        for (Map.Entry<Double, List<Double>> entry : elevationMap.entrySet()) {
//            Double key = entry.getKey();
//            List<Double> values = entry.getValue();
//            System.out.println("Key: " + key + " Values: " + values);
//        }
//
//        return azimuthElevationCoords;
//    }

    public Map<Double, List<Double>> getElevationTime(List<List<Double>> nav) {
        double[][] R = rNeu(Math.toRadians(phi), Math.toRadians(lam));
        double[][] RT = new double[3][3];
        Map<Double, List<Double>> elevationMap=new TreeMap<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                RT[i][j] = R[j][i];
            }
        }
        double[] XYZr = blh2xyz(phi, lam, height);
        int start = (int) weekSecond[1];
        int stop = start + 60 * 60 * hourInterval;
        for (int i = start; i <= stop; i += 60 * minuteInterval) {
            iter++;
            for (List<Double> sat : nav) {
                double[] XYZs = getSatPos(i, weekSecond[0], sat);
                double[] XYZsr = {XYZs[0] - XYZr[0], XYZs[1] - XYZr[1], XYZs[2] - XYZr[2]};
                double[] neu = new double[RT.length];
                for (int j = 0; j < RT.length; j++) {
                    for (int k = 0; k < XYZsr.length; k++) {
                        neu[j] += RT[j][k] * XYZsr[k];
                    }
                }
                double elevation = Math.asin(neu[2] / Math.sqrt(Math.pow(neu[0], 2) + Math.pow(neu[1], 2) + Math.pow(neu[2], 2)));
                if (!elevationMap.containsKey(sat.getFirst())) {
                    List<Double> elevations = new ArrayList<>();
                    elevations.add(Math.toDegrees(elevation));
                    elevationMap.put(sat.getFirst(), elevations);
                } else {
                    List<Double> elevations = elevationMap.get(sat.getFirst());
                    elevations.add(Math.toDegrees(elevation));
                }

            }
        }
        return elevationMap;
    }
    public List<Integer> satellitesAtTheMoment(List<List<Double>> nav){
        double[][] R = rNeu(Math.toRadians(phi), Math.toRadians(lam));
        double[][] RT = new double[3][3];
        List<Integer> satellitesByHour = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                RT[i][j] = R[j][i];
            }
        }
        double[] XYZr = blh2xyz(phi, lam, height);
        int start = (int) weekSecond[1];
        int stop = start + 60 * 60 * hourInterval;
        int numberOfSatellites=0;
        for (int i = start; i <= stop; i += 60 * minuteInterval) {
            if(i>start){
                satellitesByHour.add(numberOfSatellites);
            }
            numberOfSatellites=0;

            for (List<Double> sat : nav) {
                double[] XYZs = getSatPos(i, weekSecond[0], sat);
                double[] XYZsr = {XYZs[0] - XYZr[0], XYZs[1] - XYZr[1], XYZs[2] - XYZr[2]};
                double[] neu = new double[RT.length];
                for (int j = 0; j < RT.length; j++) {
                    for (int k = 0; k < XYZsr.length; k++) {
                        neu[j] += RT[j][k] * XYZsr[k];
                    }
                }
                double elevation = Math.asin(neu[2] / Math.sqrt(Math.pow(neu[0], 2) + Math.pow(neu[1], 2) + Math.pow(neu[2], 2)));
                if(elevation>this.mask){
                    numberOfSatellites++;
                }
            }
        }
        return satellitesByHour;
    }
}