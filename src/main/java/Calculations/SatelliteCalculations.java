package Calculations;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SatelliteCalculations {
    public SatelliteCalculations() throws IOException {
    }

    List<List<Double>> nav = AlmanacModule.readAlmanac("src/main/resources/Almanac2024053.alm");

    public static double[][] rNeu(double phi, double lam) {
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

    public static double julday(int year, int month, int day, int hour) {
        if (month <= 2) {
            year = year - 1;
            month = month + 12;
        }
        return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + (double) hour / 24 - 1537.5;
    }

    public static double[] getGPSTime(int year, int month, int day, int hour, int minute, int second) {
        double days = julday(year, month, day, 0) - julday(1980, 1, 6, 0);
        double week = Math.floor(days / 7);
        double CalculatedDay = days % 7;
        double secondOfWeek = CalculatedDay * 86400 + hour * 3600 + minute * 60 + second;
        return new double[]{week, secondOfWeek};
    }

    public static double Np(int phi, double a, double e2) {
        return a / (Math.pow((1 - e2 * Math.sin(phi) * Math.sin(phi)), 0.5));
    }

    static int hour = 12;
    static int minute = 0;
    static int second = 0;
    static int year = 2024;
    static int month = 2;
    static int day = 29;
    public static double[] weekSecond = getGPSTime(year, month, day, hour, minute, second);

    public static double[] getSatPos(double t, double week, List<Double> rowNav) {
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
        } while (!(Math.abs(E.get(j) - E.get(j - 1)) < 10e-12));
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

    public static double[] blh2xyz(double phi, double lam, double height) {
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


    public static List<List<Double>> satellitePositionInTime(List<List<Double>> nav) {
        int phi = 52;
        int lam = 21;
        int h = 100;
        double e2 = 0.00669438002290;
        double N = Np(phi, lam, e2);
        double[][] R = rNeu(Math.toRadians(phi), Math.toRadians(lam));
        double[][] RT = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                RT[i][j] = R[j][i];
            }
        }
        double[] XYZr = blh2xyz(phi,lam,h);
        double mask = Math.toRadians(10);
        int start =(int) weekSecond[1];
        int stop = start + 60*60*24;
        List<List<Double>> azimuthElevationCoords = new ArrayList<>();
        List<List<Double>> A = new ArrayList<>();
        int iter=0;
        for(int i=start;i<stop;i+=60*10){
            A.clear();
            for(List<Double> sat : nav){
//                System.out.println("i:"+i);
//                System.out.println("iter" +iter);
                iter++;
                double[] XYZs = getSatPos(i,weekSecond[0],sat);
                double[] XYZsr = {XYZs[0]-XYZr[0],XYZs[1]-XYZr[1],XYZs[2]-XYZr[2]};
                double[] neu = new double[RT.length];
                for (int j = 0; j < RT.length; j++) {
                    for (int k = 0; k < XYZsr.length; k++) {
                        neu[j] += RT[j][k] * XYZsr[k];
                    }
                }
                if(iter==1) {
                    System.out.println("neu[1]" + neu[1]);

                    System.out.println("neu[0]" + neu[0]);
                    System.out.println("neu[2]" + neu[2]);
                }

                double azimuth = Math.atan2(neu[1],neu[0]);
                if(azimuth<0){
                    azimuth+=2*Math.PI;
                }
                double elevation = Math.asin(neu[2] / Math.sqrt(Math.pow(neu[0], 2) + Math.pow(neu[1], 2) + Math.pow(neu[2], 2)));
                List<Double> coords = new ArrayList<>();
                coords.add(sat.getFirst());
                coords.add((double) i - weekSecond[1]);
                coords.add(XYZs[0]);
                coords.add(XYZs[1]);
                coords.add(XYZs[2]);
                coords.add(Math.toDegrees(azimuth));
                coords.add(Math.toDegrees(elevation));
                azimuthElevationCoords.add(coords);
                double p = Math.sqrt(Math.pow(XYZs[0] - XYZr[0], 2) + Math.pow(XYZs[1] - XYZr[1], 2) + Math.pow(XYZs[2] - XYZr[2], 2));
                if (elevation > mask) {
                    List<Double> wierszA = new ArrayList<>();
                    wierszA.add(-(XYZs[0] - XYZr[0]) / p);
                    wierszA.add(-(XYZs[1] - XYZr[1]) / p);
                    wierszA.add(-(XYZs[2] - XYZr[2]) / p);
                    wierszA.add(1.0);
                    A.add(wierszA);
                }
            }

        }
        return azimuthElevationCoords;
    }
}
