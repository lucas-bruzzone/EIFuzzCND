package EIFuzzCND.ConfusionMatrix;

import java.io.*;
import java.util.*;


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


    public void saveMatrix(String dataset, int latencia, double percentLabeled) throws IOException {
        String current = (new File(".")).getCanonicalPath();
        String filePath = current + "/datasets/" + dataset + "/graphics_data/" + dataset + latencia + "-" + percentLabeled + "-matrix.csv";

        File file = new File(filePath);

        FileWriter writer;
        BufferedWriter buf_writer;

        // Verifica se o arquivo já existe
        if (file.exists()) {
            // Se o arquivo já existe, abre-o para leitura
            BufferedReader reader = new BufferedReader(new FileReader(file));

            // Lê o conteúdo existente
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();

            // Abre o arquivo em modo de escrita, mas mantendo o conteúdo existente
            writer = new FileWriter(file, true);
            buf_writer = new BufferedWriter(writer);
            buf_writer.newLine();
            // Escreve o cabeçalho do arquivo
            buf_writer.write("Classes,");
            for (Double className : matrix.keySet()) {
                buf_writer.write(className + ",");
            }
            // Escreve abaixo do conteúdo existente
            if (content.length() > 0) {
                buf_writer.newLine();
            }

        } else {
            // Se o arquivo não existe, cria um novo arquivo
            writer = new FileWriter(file);
            buf_writer = new BufferedWriter(writer);

            // Escreve o cabeçalho do arquivo
            buf_writer.write("Classes,");
            for (Double className : matrix.keySet()) {
                buf_writer.write(className + ",");
            }
            buf_writer.newLine();
        }

        // Escreve os dados na matriz
        for (Double trueClass : matrix.keySet()) {
            buf_writer.write(trueClass + ",");
            for (Double predictedClass : matrix.keySet()) {
                int count = matrix.get(trueClass).get(predictedClass);
                buf_writer.write(count + ",");
            }
            buf_writer.newLine();
        }

        // Fecha o escritor do arquivo
        buf_writer.close();
    }






    public Map<Double, List<Double>> getClassesWithNonZeroCount() {
        Map<Double, List<Double>> result = new HashMap<>();
        for (Double trueClass : matrix.keySet()) {
            if (trueClass >= 0 && trueClass < 100) {
                List<Double> predictedClassesWithNonZeroCount = new ArrayList<>();
                int maxCount = 0;
                    for (Double predictedClass : matrix.keySet()) {
                    if (predictedClass > 100) {
                        int count = matrix.get(trueClass).get(predictedClass);
                        if (count > 0) {
                            predictedClassesWithNonZeroCount.add(predictedClass);
                        }
                    }
                }
                if (!predictedClassesWithNonZeroCount.isEmpty()) {
                    result.put(trueClass, predictedClassesWithNonZeroCount);
                }
            }
        }
        return result;
    }


    public void mergeClasses(Map<Double, List<Double>> labels) {

        // percorre todas as classes que precisam ser fundidas
        for (Map.Entry<Double, List<Double>> entry : labels.entrySet()) {
            Double srcLabel = entry.getKey();
            List<Double> destLabels = entry.getValue();

            // verifica se a classe de origem existe na matriz de confusão
            if (!matrix.containsKey(srcLabel)) {
                continue;
            }

            Map<Double, Integer> row1 = matrix.get(srcLabel);

            // percorre todas as classes de destino
            for (Double destLabel : destLabels) {
                // verifica se a classe de destino existe na matriz de confusão e se não é igual à classe de origem
                if (matrix.containsKey(destLabel) && !srcLabel.equals(destLabel)) {
                    Map<Double, Integer> row2 = matrix.get(destLabel);

                    // soma os valores de cada coluna da segunda linha na primeira linha
                    for (Map.Entry<Double, Integer> entry2 : row2.entrySet()) {
                        Double column = entry2.getKey();
                        Integer value2 = entry2.getValue();

                        row1.put(column, row1.getOrDefault(column, 0) + value2);
                    }

                    // remove a segunda linha da matriz
                    matrix.remove(destLabel);

                    // para cada coluna, soma os valores da segunda coluna na primeira coluna
                    for (Map.Entry<Double, Map<Double, Integer>> rowEntry : matrix.entrySet()) {
                        Double rowLabel = rowEntry.getKey();
                        Map<Double, Integer> row = rowEntry.getValue();

                        if (row.containsKey(destLabel)) {
                            Integer value2 = row.get(destLabel);
                            row.put(srcLabel, row.getOrDefault(srcLabel, 0) + value2);
                            row.remove(destLabel);
                        }
                    }

                    // adiciona o último merge ao mapa lastMerge
                    lastMerge.put(srcLabel, destLabel);
                }
            }
        }

        // verifica se as classes de origem do último merge ainda precisam ser fundidas
        for (Map.Entry<Double, Double> entry : lastMerge.entrySet()) {
            Double srcLabel = entry.getKey();
            Double destLabel = entry.getValue();
            if (matrix.containsKey(destLabel)) {
                mergeClasses(Collections.singletonMap(srcLabel, Arrays.asList(destLabel)));
            }
        }
    }



    public void updateConfusionMatrix(double trueLabel) {
        // Remove -1 no cruzamento entre trueLabel e -1
        Integer count = matrix.get(trueLabel).get(-1.0);
        matrix.get(trueLabel).put(-1.0, count - 1);
    }




    public Metrics calculateMetrics(int tempo, double unkMem, double exc) {
        double truePositive = 0;
        double falsePositive = 0;
        double trueNegative;
        double falseNegative = 0;
        double totalSamples = 0;
        double accuracy;
        double precision = 0;
        double recall = 0;
        double f1Score = 0;

        for (Map.Entry<Double, Map<Double, Integer>> rowEntry : matrix.entrySet()) {
            Double trueLabel = rowEntry.getKey();
            Map<Double, Integer> row = rowEntry.getValue();

            for (Map.Entry<Double, Integer> entry : row.entrySet()) {
                Double predictedLabel = entry.getKey();
                Integer count = entry.getValue();
                totalSamples += count;

                if (trueLabel.equals(predictedLabel)) {
                    truePositive += count;
                } else {
                    falsePositive += row.containsKey(predictedLabel) ? count : 0;
                    falseNegative += matrix.containsKey(trueLabel) ? count : 0;
                }
            }
        }

        trueNegative = totalSamples - truePositive - falsePositive - falseNegative;

        accuracy = (truePositive + trueNegative) / totalSamples;

        if (truePositive + falsePositive != 0) {
            precision = truePositive / (truePositive + falsePositive);
        }

        if (truePositive + falseNegative != 0) {
            recall = truePositive / (truePositive + falseNegative);
        }

        if (precision + recall != 0) {
            f1Score = 2 * precision * recall / (precision + recall);
        }

        double unknownRate = (unkMem / exc); // calcula a taxa de exemplos desconhecidos

        Metrics metrics = new Metrics(accuracy, precision, recall, f1Score,tempo,unkMem, unknownRate); // adiciona a taxa de exemplos desconhecidos nos resultados
        return metrics;
    }



    public int countUnknow() {
        int count = 0;
        for (Map.Entry<Double, Map<Double, Integer>> row : matrix.entrySet()) {
            Map<Double, Integer> colMap = row.getValue();
            if (colMap.containsKey(-1.0)) {
                count += colMap.get(-1.0);
            }
        }
        return count;
    }

    public int getNumberOfClasses() {
        return matrix.size();
    }



}