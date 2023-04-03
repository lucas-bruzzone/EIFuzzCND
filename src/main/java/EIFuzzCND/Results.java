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
    public static void main(String[] args) throws IOException, ParseException {
        String current = (new File(".")).getCanonicalPath();
        String dataset = "moa";
        String[] latencia = {"10000000"};//"2000","5000","10000",

        Map<Integer, List<ResultsForExample>> resultsEIFuzzCND = new HashMap<>();
        ArrayList<Double> novidades = new ArrayList<>();

        for(int i =0 ; i<latencia.length;i++) {

            String caminhoTrain = current + "/datasets/" + dataset + "/" + dataset + "-train.csv";
            String caminhoResultados = current + "/datasets/" + dataset + "/" + dataset + latencia[i] + "-EIFuzzCND-results.csv";
            String caminhoNovidades = current + "/datasets/" + dataset + "/" + dataset + latencia[i] + "-EIFuzzCND-novelties.csv";

            ArrayList<Double> classesTreinamento = checkLastColumn(caminhoTrain);
            Integer countResults = countLinesInCsv(caminhoResultados);
            Integer countNovelties = countLinesInCsv(caminhoNovidades);
            ArrayList<Integer> novasClasses = storeLines(caminhoResultados, classesTreinamento);
            for (int j = 0; j < 1; j++) {
                resultsEIFuzzCND.put(j, HandlesFiles.loadResults(caminhoResultados, countResults));
            }

            novidades = HandlesFiles.loadNovelties(caminhoNovidades, countNovelties);

            int unknown = 0;
            int acertos = 0;
            int acertosCount = 0;
            int count = 0;
            int errors = 0;
            int novelty = 0;


            ArrayList<Double> acuraciasECSMiner = new ArrayList<>();
            Map<String, Integer> unkRiFuzzCND = new HashMap<>();
            Map<String, Integer> excFuzzCND = new HashMap<>();
            ArrayList<Double> unkRECSMiner = new ArrayList<>();

            Map<Integer, ArrayList<Double>> medidasAcuracias = new HashMap<>();
            Map<Integer, ArrayList<Double>> medidasUnkR = new HashMap<>();

            ArrayList<List<Double>> metricasFuzzCND = new ArrayList<>();
            ArrayList<List<Double>> metricasECSMiner = new ArrayList<>();
            ArrayList<Double> noveltiesFuzzCND = new ArrayList<>();

            for (int l = 0; l < resultsEIFuzzCND.size(); l++) {
                List<ResultsForExample> results = resultsEIFuzzCND.get(l);
                ArrayList<Double> acuraciasFuzzCND = new ArrayList<>();
                ArrayList<Double> unkRFuzzCND = new ArrayList<>();
                boolean existNovelty = false;
                List<Double> novelties = new ArrayList<>();
                for (int k = 0, j = 1; k < results.size(); j++, k++) {
                    if (excFuzzCND.containsKey(results.get(k).getRealClass())) {
                        excFuzzCND.replace(results.get(k).getRealClass(), excFuzzCND.get(results.get(k).getRealClass()) + 1);
                    } else {
                        excFuzzCND.put(results.get(k).getRealClass(), 1);
                    }

                    if (results.get(k).getClassifiedClass().equals("unknown")) {
                        unknown++;
                        if (unkRiFuzzCND.containsKey(results.get(k).getRealClass())) {
                            unkRiFuzzCND.replace(results.get(k).getRealClass(), unkRiFuzzCND.get(results.get(k).getRealClass()) + 1);
                        } else {
                            unkRiFuzzCND.put(results.get(k).getRealClass(), 1);
                        }
                    } else {
                        if (Double.parseDouble(results.get(k).getClassifiedClass()) > 100) {
                            if (!novelties.contains(Double.parseDouble(results.get(k).getClassifiedClass()))) {
                                existNovelty = true;
                                novelties.add(Double.parseDouble(results.get(k).getClassifiedClass()));
                            }
                            novelty++;
                        } else {
                            count++;
                            if (results.get(k).getClassifiedClass().equals(results.get(k).getRealClass())) {
                                acertos++;
                                acertosCount++;
                            } else {
                                errors++;
                            }
                        }
                    }
                    if (j == 1000) {
                        acuraciasFuzzCND.add(((double) acertosCount / count) * 100);
                        unkRFuzzCND.add(calculaUnkR(unkRiFuzzCND, excFuzzCND));
                        if (l == 0) {
                            if (existNovelty) {
                                noveltiesFuzzCND.add(Double.parseDouble(String.valueOf(1.0)));
                            } else {
                                noveltiesFuzzCND.add(Double.parseDouble(String.valueOf(0.0)));
                            }
                        }
                        j = 0;
                    }
                }
                acertosCount = 0;
                unkRiFuzzCND.clear();
                excFuzzCND.clear();
                count = 0;
                medidasAcuracias.put(l, acuraciasFuzzCND);
                medidasUnkR.put(l, unkRFuzzCND);
            }

            ArrayList<Double> acuraciasFinal = new ArrayList<>();
            ArrayList<Double> unkRFinal = new ArrayList<>();

            for (int k = 0; k < medidasAcuracias.get(0).size(); k++) {
                double somaAc = 0;
                double somaUnk = 0;
                for (int j = 0; j < medidasAcuracias.size(); j++) {
                    somaAc = somaAc + medidasAcuracias.get(j).get(k);
                    somaUnk = somaUnk + medidasUnkR.get(j).get(k);
                }
                acuraciasFinal.add(somaAc / medidasAcuracias.size());
                unkRFinal.add(somaUnk / medidasAcuracias.size());
            }


            List<String> rotulos = new ArrayList<>();
            rotulos.add("Accuracy");
            rotulos.add("UnkR");

            metricasFuzzCND.add(acuraciasFinal);
            metricasFuzzCND.add(unkRFinal);
            metricasFuzzCND.add(noveltiesFuzzCND);

            metricasECSMiner.add(acuraciasECSMiner);
            metricasECSMiner.add(unkRECSMiner);

            LineChart_AWT chart2 = new LineChart_AWT(
                    latencia[i],
                    latencia[i], metricasFuzzCND, rotulos, novidades, novasClasses);

            chart2.pack();
            RefineryUtilities.centerFrameOnScreen(chart2);
            chart2.setVisible(true);
        }




    }

    public static double calculaUnkR(Map<String, Integer> unki, Map<String, Integer> exci) {
        List<String> rotulos = new ArrayList<>();
        rotulos.addAll(unki.keySet());
        double unkR = 0;
        for(int i=0; i< unki.size(); i++) {
            double unk = unki.get(rotulos.get(i));
            double exc = exci.get(rotulos.get(i));
            unkR += (unk/exc);
        }
        return (unkR/ exci.size()) * 100;
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
                    lineValues.add((lineCounter - 1) / 1000);
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
