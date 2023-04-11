package EIFuzzCND.Output;

import EIFuzzCND.Structs.Example;
import EIFuzzCND.Evaluation.ResultsForExample;


import java.io.*;
import java.util.*;

public class HandlesFiles {

    public static void salvaNovidades(List<Double> novidades, String arquivo, int latencia, double percentLabeled) throws IOException {
        FileWriter writer;
        BufferedWriter buf_writer;
        String current = (new File(".")).getCanonicalPath();
        writer = new FileWriter(current + "/datasets/" + arquivo + "/" + arquivo + latencia +  "-" + percentLabeled  + "-EIFuzzCND-novelties" + ".csv");
        buf_writer = new BufferedWriter(writer);
        buf_writer.write("Linha, Novidade");
        buf_writer.newLine();
        for(int i = 0; i<novidades.size(); i++) {
            String ex = novidades.get(i).toString();
            buf_writer.write(i + "," + ex);
            buf_writer.newLine();
        }

        buf_writer.close();
    }

    public static void salvaResultados(List<Example> examples, String arquivo, int latencia, double percentLabeled) throws IOException {
        FileWriter writer;
        BufferedWriter buf_writer;
        String current = (new File(".")).getCanonicalPath();
        writer = new FileWriter(current + "/datasets/" + arquivo + "/" + arquivo  + latencia +  "-" + percentLabeled + "-EIFuzzCND-results" + ".csv");
        buf_writer = new BufferedWriter(writer);
        buf_writer.write("Linha, Rotulo Verdadeiro, Rotulo Classificado");
        buf_writer.newLine();
        for(int i = 0, j=1; i<examples.size(); i++, j++) {
            String ex = j + "," + examples.get(i).getRotuloVerdadeiro() + "," + (examples.get(i).getRotuloClassificado() == -1 ? "unknown" : examples.get(i).getRotuloClassificado());
            buf_writer.write(ex);
            buf_writer.newLine();
        }

        buf_writer.close();
    }

    public static void salvaAcuracia(int tempo, double acuracia, String dataset, int latencia, double percentLabeled, int unkMen, boolean append) throws IOException {
        String current = (new File(".")).getCanonicalPath();
        FileWriter writer = new FileWriter(current + "/datasets" + "/" + dataset + "/" + dataset  + latencia + "-" + percentLabeled + "-EIFuzzCND-acuracia.csv", append);
        BufferedWriter buf_writer = new BufferedWriter(writer);
        if (!append) {
            buf_writer.write("Tempo, Acurácia, unkMen");
            buf_writer.newLine();
        }
        String line = tempo + "," + acuracia + "," + unkMen;
        buf_writer.write(line);
        buf_writer.newLine();
        buf_writer.close();
    }



    public static ArrayList<ResultsForExample> loadResults(String caminho, int numAnalises) {
        BufferedReader inReader = null;
        try {
            inReader = new BufferedReader(new FileReader(caminho));
        } catch (FileNotFoundException var11) {
            System.err.println("loadResults - Não foi possível abrir o arquivo: " + caminho);
            System.exit(1);
        }
        try {
            String line = null;
            StringTokenizer str = null;
            ArrayList<ResultsForExample> measures = new ArrayList<>();
            // Skip header row
            inReader.readLine();
            for(int i = 0; i < numAnalises; i++) {
                line = inReader.readLine();
                str = new StringTokenizer(line, ",");
                str.nextToken();
                String temp2 = str.nextToken();
                String temp3 = str.nextToken();
                measures.add(new ResultsForExample(temp2, temp3));
            };
            inReader.close();
            return measures;
        } catch (IOException var9) {
            System.err.println(var9.getMessage());
        }
        return null;
    }




    public static ArrayList<Double> loadNovelties(String caminho, int numAnalises) {
        BufferedReader inReader = null;
        try {
            inReader = new BufferedReader(new FileReader(caminho));
        } catch (FileNotFoundException var11) {
            System.err.println("loadNovelties - Não foi possível abrir o arquivo: " + caminho);
            System.exit(1);
        }

        try {
            String line = null;
            StringTokenizer str = null;
            ArrayList<Double> measures = new ArrayList<>();
            // Skip header row
            inReader.readLine();
            for(int i = 0; i < numAnalises; i++) {
                line = inReader.readLine();
                str = new StringTokenizer(line, ",");
                str.nextToken(); // skip first column
                Double novelty = Double.parseDouble(str.nextToken());
                measures.add(novelty);
                System.out.println();
            };
            inReader.close();
            return measures;
        } catch (IOException var9) {
            System.err.println(var9.getMessage());
        }
        return null;
    }

    public static void loadAcuracia(String caminho, int numAnalises, List<Double> acuraciasFuzzCND, List<Double> unkRFuzzCND) {
        BufferedReader inReader = null;
        try {
            inReader = new BufferedReader(new FileReader(caminho));
        } catch (FileNotFoundException e) {
            System.err.println("loadAcuracia - Não foi possível abrir o arquivo: " + caminho);
            System.exit(1);
        }

        try {
            String line = null;
            StringTokenizer str = null;
            // Read header row
            line = inReader.readLine();
            // Check header
            if (!line.equals("Tempo, Acurácia, unkMen")) {
                System.err.println("loadAcuracia - Formato de cabeçalho inválido no arquivo: " + caminho);
                System.exit(1);
            }
            for (int i = 0; i < numAnalises; i++) {
                line = inReader.readLine();
                str = new StringTokenizer(line, ",");
                str.nextToken(); // skip first column
                Double acuracia = Double.parseDouble(str.nextToken()) * 100;
                Double unkMen = Double.parseDouble(str.nextToken());
                acuraciasFuzzCND.add(acuracia);
                unkRFuzzCND.add(unkMen);
            }
            inReader.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }





}

