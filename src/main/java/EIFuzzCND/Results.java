package EIFuzzCND;

import EIFuzzCND.Evaluation.ResultsForExample;
import EIFuzzCND.Output.HandlesFiles;
import EIFuzzCND.Output.LineChart_AWT;
import org.jfree.ui.RefineryUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;


public class Results {
    private static int divisor = 1000;

    public static void main(String[] args) throws IOException, ParseException {
        String current = (new File(".")).getCanonicalPath();
        String dataset = "cover";
        String[] latencia = {"10000"};//"2000","5000","10000","10000000"
        String[] percentedLabeled = {"0.2", "0.5", "0.8"};
        Map<Integer, List<ResultsForExample>> resultsEIFuzzCND = new HashMap<>();
        ArrayList<Double> novidades;
        for (int i = 0; i < latencia.length; i++) {
            for (int j = 0; j < percentedLabeled.length; j++) {

                String caminhoTrain = current + "/datasets/" + dataset + "/" + dataset + "-train.csv";
                String caminhoResultados = current + "/datasets/" + dataset + "/graphics_data/" + dataset + latencia[i] + "-" + percentedLabeled[j] + "-EIFuzzCND-results.csv";
                String caminhoNovidades = current + "/datasets/" + dataset + "/graphics_data/" + dataset + latencia[i] + "-" + percentedLabeled[j] + "-EIFuzzCND-novelties.csv";
                String caminhoAcuracia = current + "/datasets/" + dataset + "/graphics_data/" + dataset + latencia[i] + "-" + percentedLabeled[j] + "-EIFuzzCND-acuracia.csv";


                ArrayList<Double> classesTreinamento = checkLastColumn(caminhoTrain);
                Integer countResults = countLinesInCsv(caminhoResultados);
                Integer countNovelties = countLinesInCsv(caminhoNovidades);
                Integer countAcuracias = countLinesInCsv(caminhoAcuracia);
                ArrayList<Integer> novasClasses = storeLines(caminhoResultados, classesTreinamento);
                for (int k = 0; k < 1; k++) {
                    resultsEIFuzzCND.put(k, HandlesFiles.loadResults(caminhoResultados, countResults));
                }

                novidades = HandlesFiles.loadNovelties(caminhoNovidades, countNovelties);


                ArrayList<Double> precisoesFuzzCND = new ArrayList<>();
                ArrayList<Double> recallsFuzzCND = new ArrayList<>();
                ArrayList<Double> f1ScoresFuzzCND = new ArrayList<>();
                ArrayList<Double> unknownRate = new ArrayList<>();
                ArrayList<Double> acuraciasFuzzCND = new ArrayList<>();
                ArrayList<Double> unkRFuzzCND = new ArrayList<>();

                HandlesFiles.loadMetrics(caminhoAcuracia, countAcuracias, acuraciasFuzzCND, precisoesFuzzCND, recallsFuzzCND, f1ScoresFuzzCND, unkRFuzzCND, unknownRate);

                ArrayList<List<Double>> metricasFuzzCND = new ArrayList<>();


                List<String> rotulos = new ArrayList<>();
                rotulos.add("Accuracy");
                rotulos.add("unknownRate");

                metricasFuzzCND.add(precisoesFuzzCND);
                metricasFuzzCND.add(unknownRate);


                LineChart_AWT chart2 = new LineChart_AWT(
                        latencia[i],
                        latencia[i], metricasFuzzCND, rotulos, novidades, novasClasses, dataset, percentedLabeled[j]);

                chart2.pack();
                RefineryUtilities.centerFrameOnScreen(chart2);
                chart2.setVisible(true);
            }
        }
    }


    public static int countLinesInCsv(String filePath) throws IOException {
        int count = 0;
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        while (reader.readLine() != null) {
            count++;
        }
        reader.close();
        return count - 1;
    }

    public static ArrayList<Double> checkLastColumn(String pathToFile) {
        ArrayList<Double> lastColumnValues = new ArrayList<>();
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        try {
            br = new BufferedReader(new FileReader(pathToFile));
            while ((line = br.readLine()) != null) {
                String[] row = line.split(cvsSplitBy);
                String lastColumnValue = row[row.length - 1];
                try {
                    Double value = Double.parseDouble(lastColumnValue);
                    if (!lastColumnValues.contains(value)) {
                        lastColumnValues.add(value);
                    }
                } catch (NumberFormatException e) {
                    // value is not a number, ignore it
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for (int i = 0; i < lastColumnValues.size(); i++) {
            lastColumnValues.set(i, (double) i);
        }
        return lastColumnValues;
    }

    public static ArrayList<Integer> storeLines(String pathToFile, ArrayList<Double> distinctValues) {
        ArrayList<Integer> lineValues = new ArrayList<>();
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        HashSet<Double> seenValues = new HashSet<>();

        try {
            br = new BufferedReader(new FileReader(pathToFile));
            int lineCounter = 0;
            while ((line = br.readLine()) != null) {
                lineCounter++;
                if (lineCounter == 1) { // Skip header row
                    continue;
                }
                String[] row = line.split(cvsSplitBy);
                Double value = Double.parseDouble(row[1]);
                if (!seenValues.contains(value) && !distinctValues.contains(value)) {
                    seenValues.add(value);
                    lineValues.add((lineCounter - 1) / divisor);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return lineValues;
    }
}
