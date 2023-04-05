package EIFuzzCND.Evaluation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModelEvaluator {
    public static void main(String[] args) {
        // Ler o arquivo CSV
        String filePath = "C:\\Users\\lucas\\Documents\\Mestrado\\EIFuzzCND\\datasets\\moa\\moaConfusionMatrix10000000.csv";
        List<Double> trueLabels = new ArrayList<>();
        List<Double> predictedLabels = new ArrayList<>();
        List<Double> count = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                trueLabels.add(Double.parseDouble(values[0]));
                predictedLabels.add(Double.parseDouble(values[1]));
                count.add(Double.parseDouble(values[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calcular a acurácia
        double sum = 0;
        for (Double value : count) {
            sum += value;
        }

        int numCorrect = 0;
        int numTotal = trueLabels.size();

        for (int i = 0; i < numTotal; i++) {
            if (trueLabels.get(i).equals(predictedLabels.get(i))) {
                numCorrect += count.get(i);
            }
        }

        double accuracy = (double) numCorrect / sum;
        System.out.println("Acurácia: " + accuracy);
    }
}
