package Calculations;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AlmanacModule {
    public static List<List<Double>> readAlmanac (String file) throws IOException {
        BufferedReader reader = Files.newBufferedReader(Paths.get(file));
        List<List<Double>> data = new ArrayList<>();
        String line;
        int counter = 0;
        while ((line = reader.readLine()) != null) {
            counter++;
            if (counter % 14 == 0) {
                continue;
            }
            line = line.replace("ff", "-1");
            line = line.replaceAll("(-)", " $1").trim();
            String[] parts = line.trim().split("\\s+");
            List<Double> row = new ArrayList<>();
            for (String part : parts) {
                row.add(Double.parseDouble(part));
            }
            if (row.size() < 6) {
                for (int i = 6 - row.size(); i < 6; i++) {
                    row.add(Double.NaN);
                }
            }
            data.add(row);

        }
        List<List<Double>> finalData = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++) {
            finalData.add(new ArrayList<>());
        }

        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < 6; j++) {
                finalData.get((i / 13) * 6 + j).add(data.get(i).get(j));
            }
        }
        data.clear();
        finalData.removeIf(row -> row.isEmpty() ||
                row.contains(Double.NaN) ||
                (row.getFirst() >101 && row.getFirst() < 202)||
                row.getFirst()>236
                );

        return finalData;
   }
}


