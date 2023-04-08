package EIFuzzCND.ConfusionMatrix;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class ConfusionMatrix {
    private Map<Double, Map<Double, Integer>> matrix;

    private Map<Double, Double> lastMerge = new HashMap<>();


    public ConfusionMatrix() {
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


    public Map<Double, Double> getClassesWithMaxCount() {
        Map<Double, Double> result = new HashMap<>();
        for (Double trueClass : matrix.keySet()) {
            Double predictedClassWithMaxCount = null;
            int maxCount = 0;
            for (Double predictedClass : matrix.keySet()) {
                int count = matrix.get(trueClass).get(predictedClass);
                if (count > maxCount) {
                    predictedClassWithMaxCount = predictedClass;
                    maxCount = count;
                } else if (count == maxCount && predictedClassWithMaxCount != null && predictedClass > 100 && predictedClass < predictedClassWithMaxCount) {
                    predictedClassWithMaxCount = predictedClass;
                }
            }
            if (predictedClassWithMaxCount != null) {
                result.put(trueClass, predictedClassWithMaxCount);
            }
        }
        return result;
    }


    public void mergeClasses(Map<Double, Double> labels) {
        // percorre todos os pares de rótulos
        for (Map.Entry<Double, Double> entry : labels.entrySet()) {
            Double label1 = entry.getKey();
            Double label2 = entry.getValue();

            // verifica se ambos os rótulos existem na matriz de confusão e se não são iguais
            if (matrix.containsKey(label1) && matrix.containsKey(label2) && !label1.equals(label2)) {
                // verfica se nenhum dos rótulos tem o valor -1.0
                if (label1 != -1.0 && label2 != -1.0) {
                    Map<Double, Integer> row1 = matrix.get(label1);
                    Map<Double, Integer> row2 = matrix.get(label2);

                    // soma os valores de cada coluna da segunda linha na primeira linha
                    for (Map.Entry<Double, Integer> entry2 : row2.entrySet()) {
                        Double column = entry2.getKey();
                        Integer value2 = entry2.getValue();

                        row1.put(column, row1.getOrDefault(column, 0) + value2);
                    }

                    // remove a segunda linha da matriz
                    matrix.remove(label2);

                    // para cada coluna, soma os valores da segunda coluna na primeira coluna
                    for (Map.Entry<Double, Map<Double, Integer>> rowEntry : matrix.entrySet()) {
                        Double rowLabel = rowEntry.getKey();
                        Map<Double, Integer> row = rowEntry.getValue();

                        if (row.containsKey(label2)) {
                            Integer value2 = row.get(label2);
                            row.put(label1, row.getOrDefault(label1, 0) + value2);
                            row.remove(label2);
                        }
                    }

                    // adiciona o último merge ao mapa lastMerge
                    lastMerge.put(label1, label2);
                }
            }
        }

        // verifica se o último merge contém o rótulo que precisa ser verificado
        for (Map.Entry<Double, Double> entry : lastMerge.entrySet()) {
            Double label1 = entry.getKey();
            Double label2 = entry.getValue();
            if (matrix.containsKey(label2)) {
                mergeClasses(Collections.singletonMap(label1, label2));
                break;
            }
        }
    }


    public void updateConfusionMatrix(double trueLabel, double predictedLabel) {
        // Remove -1 no cruzamento entre trueLabel e -1
        Integer count = matrix.get(trueLabel).get(-1.0);
        matrix.get(trueLabel).put(-1.0, count - 1);
    }





    public double calculateAccuracy() {
        int correctPredictions = 0;
        int totalSamples = 0;

        for (Map.Entry<Double, Map<Double, Integer>> rowEntry : matrix.entrySet()) {
            Double trueLabel = rowEntry.getKey();
            Map<Double, Integer> row = rowEntry.getValue();

            for (Map.Entry<Double, Integer> entry : row.entrySet()) {
                Double predictedLabel = entry.getKey();
                Integer count = entry.getValue();

                if (trueLabel.equals(predictedLabel)) {
                    correctPredictions += count;
                }

                totalSamples += count;
            }
        }

        return (double) correctPredictions / totalSamples;
    }


}