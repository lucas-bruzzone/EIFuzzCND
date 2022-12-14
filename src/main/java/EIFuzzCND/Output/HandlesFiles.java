package EIFuzzCND.Output;

import EIFuzzCND.Evaluation.AcuraciaMedidas;
import EIFuzzCND.Evaluation.AnaliseErros;
import EIFuzzCND.Structs.Example;
import EIFuzzCND.Evaluation.ResultsForExample;


import java.io.*;
import java.util.*;

public class HandlesFiles {

    public static void salvaPredicoes(List<AcuraciaMedidas> acuracias, String arquivo) throws IOException {
        FileWriter writer;
        BufferedWriter buf_writer;
        String current = (new File(".")).getCanonicalPath();
        writer = new FileWriter(current + "/datasets/" + arquivo + "/" + arquivo + "-ensemblej48predctions" + ".txt");
        buf_writer = new BufferedWriter(writer);

        for(int i = 0; i<acuracias.size(); i++) {
            String ex = acuracias.get(i).getPonto()+ "," + acuracias.get(i).getAcuracia();
            buf_writer.write(ex);
            buf_writer.newLine();
        }

        buf_writer.close();
    }

    public static void salvaNovidades(List<Double> novidades, String arquivo) throws IOException {
        FileWriter writer;
        BufferedWriter buf_writer;
        String current = (new File(".")).getCanonicalPath();
        writer = new FileWriter(current + "/datasets/" + arquivo + "/" + arquivo + "-FuzzCND-novelties" + ".txt");
        buf_writer = new BufferedWriter(writer);

        for(int i = 0; i<novidades.size(); i++) {
            String ex = novidades.get(i).toString();
            buf_writer.write(ex);
            buf_writer.newLine();
        }

        buf_writer.close();
    }

    public static void salvaResultados(List<Example> examples, String arquivo) throws IOException {
        FileWriter writer;
        BufferedWriter buf_writer;
        String current = (new File(".")).getCanonicalPath();
        writer = new FileWriter(current + "/datasets/" + arquivo + "/" + arquivo + "-FuzzCND-results" + ".txt");
        buf_writer = new BufferedWriter(writer);

        for(int i = 0, j=1; i<examples.size(); i++, j++) {
            String ex = "Ex: " + j + "\t" + "Real Class: " + examples.get(i).getRotuloVerdadeiro() + "\t" + "Classe FuzzCND: " + (examples.get(i).getRotuloClassificado() == -1 ? "unknown" : examples.get(i).getRotuloClassificado());
            buf_writer.write(ex);
            buf_writer.newLine();
        }

        buf_writer.close();
    }

    public static void salvaAnaliseDeErros(HashMap<String, AnaliseErros> analises, String arquivo) throws IOException {
        FileWriter writer;
        BufferedWriter buf_writer;
        String current = (new File(".")).getCanonicalPath();
        writer = new FileWriter(current + "/datasets/" + arquivo + "/" + arquivo + "-FuzzCND-analise-erros" + ".txt");
        buf_writer = new BufferedWriter(writer);
        Set<String> keys = analises.keySet();
        keys.stream().forEach(key -> {
            try {
                buf_writer.write(analises.get(key).toString());
                buf_writer.newLine();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        });
        buf_writer.close();
    }

    public static ArrayList<ResultsForExample> loadResults(String caminho, String dataset, String algoritmo, int numAnalises) {
        BufferedReader inReader = null;
        try {
            inReader = new BufferedReader(new FileReader(caminho + dataset +"-" + algoritmo + "-0-results.txt"));
        } catch (FileNotFoundException var11) {
            System.err.println("carregaParticao - Não foi possível abrir o arquivo FuzzCND: " + caminho);
            System.exit(1);
        }

        try {
            String line = null;
            StringTokenizer str = null;
            ArrayList<ResultsForExample> measures = new ArrayList<>();
            for(int i=0; i<numAnalises; i++) {
                line = inReader.readLine();
                System.out.println(line);
                str = new StringTokenizer(line);
                String temp = str.nextToken();
                str.nextToken();
                str.nextToken();
                str.nextToken();
                String temp5 = str.nextToken();
                str.nextToken();
                str.nextToken();
                String temp8 = str.nextToken();
                measures.add(new ResultsForExample(temp5,temp8));
                System.out.println();
            };
            inReader.close();
            return measures;
        } catch (IOException var9) {
            System.err.println(var9.getMessage());
        }
        return null;
    }

    public static ArrayList<ResultsForExample> loadResultsMinas(String caminho, int numAnalises) {
        BufferedReader inReader = null;
        try {
            inReader = new BufferedReader(new FileReader(caminho + "minas-MOA-results-examples.txt"));
        } catch (FileNotFoundException var11) {
            System.err.println("carregaParticao - Não foi possível abrir o arquivo Minas: " + caminho);
            System.exit(1);
        }

        try {
            String line = null;
            StringTokenizer str = null;
            ArrayList<ResultsForExample> measures = new ArrayList<>();
            for(int i=0; i<numAnalises; i++) {
                line = inReader.readLine();
                System.out.println(line);
                str = new StringTokenizer(line);
                String temp = str.nextToken();
                str.nextToken();
                str.nextToken();
                str.nextToken();
                String temp5 = str.nextToken();
                str.nextToken();
                str.nextToken();
                String temp8 = str.nextToken();
                if(temp8.equals("C") || temp8.equals("ExtCon") || temp8.equals("N")) {
                    temp8 = str.nextToken();
                }
                System.out.println(temp5 + "   " + temp8);
                measures.add(new ResultsForExample(temp5,temp8));
            };
            inReader.close();
            return measures;
        } catch (IOException var9) {
            System.err.println(var9.getMessage());
        }
        return null;
    }

    public static ArrayList<List<Double>> loadResultsPFuzzND(String caminho, String dataset, int numAnalises) {
        BufferedReader inReader = null;
        try {
            inReader = new BufferedReader(new FileReader(caminho + dataset + ".txt"));
        } catch (FileNotFoundException var11) {
            System.err.println("carregaParticao - Não foi possível abrir o arquivo: " + caminho);
            System.exit(1);
        }

        try {
            String line = null;
            StringTokenizer str = null;
            ArrayList<List<Double>> measures = new ArrayList<>();
            ArrayList<Double> aux1 = new ArrayList<Double>();
            ArrayList<Double> aux2 = new ArrayList<Double>();
            for(int i=0; i<numAnalises; i++) {
                line = inReader.readLine();
                System.out.println(line);
                str = new StringTokenizer(line);
                String temp = str.nextToken();
                String[] parts =  temp.split(",");
                aux1.add(Double.parseDouble(parts[0])* 100);
                aux2.add(Double.parseDouble(parts[1])* 100);
                System.out.println();
            };
            measures.add(aux1);
            measures.add(aux2);
            inReader.close();
            return measures;
        } catch (IOException var9) {
            System.err.println(var9.getMessage());
        }
        return null;
    }

    public static ArrayList<List<Double>> loadResultsMinas(String caminho, String dataset, int numAnalises) {
        BufferedReader inReader = null;
        try {
            inReader = new BufferedReader(new FileReader(caminho + "minas-" + dataset + "-results.txt"));
        } catch (FileNotFoundException var11) {
            System.err.println("carregaParticao - Não foi possível abrir o arquivo: " + caminho + "minas-" + dataset + "-results.txt");
            System.exit(1);
        }

        try {
            String line = null;
            StringTokenizer str = null;
            ArrayList<List<Double>> measures = new ArrayList<>();
            ArrayList<Double> aux1 = new ArrayList<Double>();
            ArrayList<Double> aux2 = new ArrayList<Double>();
            for(int i=0; i<numAnalises; i++) {
                line = inReader.readLine();
                System.out.println(line);
                str = new StringTokenizer(line);
                String temp = str.nextToken();
                String[] parts =  temp.split(",");
                aux1.add(Double.parseDouble(parts[0]));
                aux2.add(Double.parseDouble(parts[1]));
                System.out.println();
            };
            measures.add(aux1);
            measures.add(aux2);
            inReader.close();
            return measures;
        } catch (IOException var9) {
            System.err.println(var9.getMessage());
        }
        return null;
    }

    public static ArrayList<ResultsForExample> loadResults(String caminho, String dataset, int numAnalises) {
        BufferedReader inReader = null;
        try {
            inReader = new BufferedReader(new FileReader(caminho + dataset +"-FuzzCND-results.txt"));
        } catch (FileNotFoundException var11) {
            System.err.println("carregaParticao - Não foi possível abrir o arquivo: " + caminho);
            System.exit(1);
        }

        try {
            String line = null;
            StringTokenizer str = null;
            ArrayList<ResultsForExample> measures = new ArrayList<>();
            for(int i=0; i<numAnalises; i++) {
                line = inReader.readLine();
                str = new StringTokenizer(line);
                String temp = str.nextToken();
                str.nextToken();
                str.nextToken();
                str.nextToken();
                String temp5 = str.nextToken();
                str.nextToken();
                str.nextToken();
                String temp8 = str.nextToken();
                measures.add(new ResultsForExample(temp5,temp8));
                System.out.println();
            };
            inReader.close();
            return measures;
        } catch (IOException var9) {
            System.err.println(var9.getMessage());
        }
        return null;
    }

    public static ArrayList<ResultsForExample> loadResults(String caminho, String dataset, String algoritmo, int numAnalises, int execution) {
        BufferedReader inReader = null;
        try {
            inReader = new BufferedReader(new FileReader(caminho + dataset +"-" + algoritmo + "-results-10000.txt"));
//            inReader = new BufferedReader(new FileReader("/home/andre/Desktop/ResultadosFinais/rbf/rbf-FuzzCND-results.txt"));
//            inReader = new BufferedReader(new FileReader(caminho + "/kdd-ECSMiner-results-2000.txt"));
        } catch (FileNotFoundException var11) {
            System.err.println("carregaParticao - Não foi possível abrir o arquivo: " + caminho);
            System.exit(1);
        }

        try {
            String line = null;
            StringTokenizer str = null;
            ArrayList<ResultsForExample> measures = new ArrayList<>();
            for(int i=0; i<numAnalises; i++) {
                line = inReader.readLine();
                str = new StringTokenizer(line);
                String temp = str.nextToken();
                str.nextToken();
                str.nextToken();
                str.nextToken();
                String temp5 = str.nextToken();
                str.nextToken();
                str.nextToken();
                String temp8 = str.nextToken();
                measures.add(new ResultsForExample(temp5,temp8));
                System.out.println();
            };
            inReader.close();
            return measures;
        } catch (IOException var9) {
            System.err.println(var9.getMessage());
        }
        return null;
    }

    public static ArrayList<Double> loadNovelties(String caminho, String dataset, String algoritmo, int numAnalises) {
        BufferedReader inReader = null;
        try {
            inReader = new BufferedReader(new FileReader(caminho + dataset +"-" + algoritmo + "-" + "novelties.txt"));
//            inReader = new BufferedReader(new FileReader("/home/andre/Desktop/ResultadosFinais/rbf/rbf-FuzzCND-novelties.txt"));
        } catch (FileNotFoundException var11) {
            System.err.println("carregaParticao - Não foi possível abrir o arquivo de novidades: " + caminho);
            System.exit(1);
        }

        try {
            String line = null;
            StringTokenizer str = null;
            ArrayList<Double> measures = new ArrayList<>();
            for(int i=0; i<numAnalises; i++) {
                System.out.println(i);
                line = inReader.readLine();
//                str = new StringTokenizer(line);
//                String temp = str.nextToken();
                measures.add(Double.parseDouble(line));
                System.out.println();
            };
            inReader.close();
            return measures;
        } catch (IOException var9) {
            System.err.println(var9.getMessage());
        }
        return null;
    }

    public static Set<Double> loadNoveltiesPFuzzND(String caminho, String dataset, int numAnalises) {
        BufferedReader inReader = null;
        try {
            inReader = new BufferedReader(new FileReader(caminho + dataset +"-" + "novelties.txt"));
        } catch (FileNotFoundException var11) {
            System.err.println("carregaParticao - Não foi possível abrir o arquivo de novidades: " + caminho);
            System.exit(1);
        }

        try {
            String line = null;
            StringTokenizer str = null;
            Set<Double> measures = new HashSet<>();
            for(int i=0; i<numAnalises; i++) {
                System.out.println(i);
                line = inReader.readLine();
//                str = new StringTokenizer(line);
//                String temp = str.nextToken();
                measures.add(Double.parseDouble(line));
                System.out.println();
            };
            inReader.close();
            return measures;
        } catch (IOException var9) {
            System.err.println(var9.getMessage());
        }
        return null;
    }

    public static Set<Double> loadNoveltiesMinas(String caminho, String dataset, int numAnalises) {
        BufferedReader inReader = null;
        try {
            inReader = new BufferedReader(new FileReader(caminho + "minas-" + dataset +"-" + "novelties.txt"));
        } catch (FileNotFoundException var11) {
            System.err.println("carregaParticao - Não foi possível abrir o arquivo de novidades: " + caminho + "minas-" + dataset +"-" + "novelties.txt");
            System.exit(1);
        }

        try {
            String line = null;
            StringTokenizer str = null;
            Set<Double> measures = new HashSet<>();
            for(int i=0; i<numAnalises; i++) {
                System.out.println(i);
                line = inReader.readLine();
//                str = new StringTokenizer(line);
//                String temp = str.nextToken();
                measures.add(Double.parseDouble(line));
                System.out.println();
            };
            inReader.close();
            return measures;
        } catch (IOException var9) {
            System.err.println(var9.getMessage());
        }
        return null;
    }

    public static List<AcuraciaMedidas> carregaAcuracias(String caminho, int numAnalises) {
        BufferedReader inReader = null;
        List<String> teste = new ArrayList<>();
        try {
            inReader = new BufferedReader(new FileReader(caminho));
        } catch (FileNotFoundException var11) {
            System.err.println("carregaParticao - Não foi possível abrir o arquivo: " + caminho);
            System.exit(1);
        }

        try {
            String line = null;
            StringTokenizer str = null;
            List<AcuraciaMedidas> acuracias = new ArrayList<>();
            for(int i=0; i<numAnalises; i++) {
                line = inReader.readLine();
                str = new StringTokenizer(line);
                String temp = str.nextToken();
                String[] lixo = temp.split(",");
                acuracias.add(new AcuraciaMedidas(Integer.parseInt(lixo[0].replace(".0", "")), Double.parseDouble(lixo[1])));
            };
            inReader.close();
            return acuracias;
        } catch (IOException var9) {
            System.err.println(var9.getMessage());
        }
        return null;
    }
}

