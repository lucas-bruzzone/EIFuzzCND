package EIFuzzCND.Evaluation;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfusionMatrix {
    private Map<Double, Map<Double, Integer>> matrix;
    private int maxClasses;

    public ConfusionMatrix(int maxClasses) {
        this.maxClasses = maxClasses;
        matrix = new HashMap<>();
    }

    public void addInstance(double trueClass, double predictedClass) {
        if (!matrix.containsKey(trueClass)) {
            addClass(trueClass);
        }
        if (!matrix.containsKey(predictedClass)) {
            addClass(predictedClass);
        }
        int count = matrix.get(trueClass).get(predictedClass);
        matrix.get(trueClass).put(predictedClass, count + 1);
    }

    private void addClass(double classLabel) {
        matrix.put(classLabel, new HashMap<>());
        for (double otherClass : matrix.keySet()) {
            matrix.get(classLabel).put(otherClass, 0);
            matrix.get(otherClass).put(classLabel, 0);
        }
    }

    public void printMatrix() {
        System.out.println("\nConfusion Matrix:");
        System.out.print("\t");
        for (Double className : matrix.keySet()) {
            System.out.print(className + "\t");
        }
        System.out.println();

        for (Double trueClass : matrix.keySet()) {
            System.out.print(trueClass + "\t");
            for (Double predictedClass : matrix.keySet()) {
                int count = matrix.get(trueClass).get(predictedClass);
                System.out.print(count + "\t");
            }
            System.out.println();
        }
    }

    public void saveToFile(String filename) {
        try {
            FileWriter writer = new FileWriter(filename);

            // Escreve o cabeçalho
            writer.write("True Class,Predicted Class,Count\n");

            // Escreve as células da matriz
            for (Map.Entry<Double, Map<Double, Integer>> trueClassEntry : matrix.entrySet()) {
                Double trueClass = trueClassEntry.getKey();
                Map<Double, Integer> predictions = trueClassEntry.getValue();
                for (Map.Entry<Double, Integer> predictedClassEntry : predictions.entrySet()) {
                    Double predictedClass = predictedClassEntry.getKey();
                    Integer count = predictedClassEntry.getValue();
                    writer.write(trueClass + "," + predictedClass + "," + count + "\n");
                }
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Error saving confusion matrix to file " + filename + ": " + e.getMessage());
        }
    }

}