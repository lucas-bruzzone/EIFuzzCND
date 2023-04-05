package EIFuzzCND.FuzzyFunctions;


import EIFuzzCND.Structs.Example;
import EIFuzzCND.Structs.SPFMiC;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.FuzzyKMeansClusterer;

import java.util.*;

public class FuzzyFunctions {
    public static FuzzyKMeansClusterer fuzzyCMeans(List<Example> examples, int K, double fuzzification) {
        FuzzyKMeansClusterer fuzzyClusterer = new FuzzyKMeansClusterer(K, fuzzification);
        try {
            fuzzyClusterer.cluster(examples);
        } catch (Exception ex) {
            System.err.println("Erro fuzzyCMeans");
        }
        return fuzzyClusterer;
    }

    public static List<Double> fuzzySilhouette(FuzzyKMeansClusterer clusters, List<Example> desconhecidos, double alpha) {
        int nExemplos = desconhecidos.size();
        double[][] matriz = clusters.getMembershipMatrix().getData();
        double numerador = 0;
        double denominador = 0;
        double apj = 0;
        List<Double> dqj = new ArrayList<>();
        List<Double> silhuetas = new ArrayList<>();
        for(int i=0; i<clusters.getK(); i++) {
            for (int j = 0; j < nExemplos; j++) {
                int indexClasse = getIndiceDoMaiorValor(matriz[j]);
                if (indexClasse == i) {
                    for (int k = 0; k < nExemplos; k++) {
                        if (getIndiceDoMaiorValor(matriz[k]) == indexClasse) {
                            apj += DistanceMeasures.calculaDistanciaEuclidiana(desconhecidos.get(j).getPoint(), desconhecidos.get(k).getPoint());
                        } else {
                            dqj.add(DistanceMeasures.calculaDistanciaEuclidiana(desconhecidos.get(j).getPoint(), desconhecidos.get(k).getPoint()));
                        }
                    }

                    apj = apj / nExemplos;
                    if(dqj.size() != 0) {
                        double bpj = Collections.min(dqj);
                        double sj = (bpj - apj) / Math.max(apj, bpj);
                        double[] maiorESegundaMeiorPertinencia = getFirstAndSecondBiggerPertinence(matriz[j], j);
                        double upj = maiorESegundaMeiorPertinencia[0];
                        double uqj = maiorESegundaMeiorPertinencia[1];
                        numerador += Math.pow((upj - uqj), alpha) * sj;
                        denominador += Math.pow((upj - uqj), alpha);
                    }
                }
            }
            double fs = numerador / denominador;
            silhuetas.add(fs);
        }
        return silhuetas;
    }

    private static double[] getFirstAndSecondBiggerPertinence(double valores[], int j) {
        double[] resultado = new double[2];
        List<Double> lista = new ArrayList<>();
        for(int i=0; i<valores.length; i++) {
            lista.add(valores[i]);
        }
        Collections.sort(lista, Collections.reverseOrder());
        resultado[0] = lista.get(0);
        resultado[1] = lista.get(1);
        return resultado;
    }

    public static List<SPFMiC> separateExamplesByClusterClassifiedByFuzzyCMeans(List<Example> exemplos, FuzzyKMeansClusterer fuzzyClusterer, double rotulo, double alpha, double theta, int minWeight, int t) {
        List<SPFMiC> sfMiCS = new ArrayList<SPFMiC>();
        double[][] matrizMembership = fuzzyClusterer.getMembershipMatrix().getData();
        //matriz de Tipicidade
        double[][] matrizTipicidade = FuzzyFunctions.calculaTipicidade(matrizMembership);
        List<CentroidCluster> centroid = fuzzyClusterer.getClusters();
        for(int j=0; j<centroid.size(); j++) {
            SPFMiC sfMiC = null;
            double SSDe = 0, Me= 0, Te = 0 ;
            double[] CF1pertinencias = new double[exemplos.get(0).getPonto().length] ;
            double[] CF1tipicidades = new double[exemplos.get(0).getPonto().length] ;
            for(int k=0; k<exemplos.size(); k++) {
                int indiceMaior = getIndiceDoMaiorValor(matrizMembership[k]);
                if(indiceMaior == j) {
                    if (sfMiC == null) {
                        double valorPertinencia = matrizMembership[k][j];
                        double valorTipicidade = matrizTipicidade[k][j];
                        double[] ex = exemplos.get(k).getPonto();

                        sfMiC = new SPFMiC(centroid.get(j).getCenter().getPoint(),
                                centroid.get(j).getPoints().size(),
                                alpha,
                                theta, t);

                        sfMiC.setRotulo(rotulo);
                        double distancia = DistanceMeasures.calculaDistanciaEuclidiana(sfMiC.getCentroide(), ex);
                        // Mudança Me -- Validar
                        Me += Math.pow(valorPertinencia,alpha);
                        // Mudança Te -- Validar
                        Te += Math.pow(valorTipicidade,theta);
                        SSDe += valorPertinencia * Math.pow(distancia, 2);
                    } else {
                        double valorPertinencia = matrizMembership[k][j];
                        double valorTipicidade = matrizTipicidade[k][j];
                        double[] ex = exemplos.get(k).getPonto();
                        double distancia = DistanceMeasures.calculaDistanciaEuclidiana(sfMiC.getCentroide(), ex);
                        for (int i = 0; i < ex.length; i++) {
                            CF1pertinencias[i] += ex[i] * valorPertinencia;
                            CF1tipicidades[i] += ex[i] * valorTipicidade;
                        }
                        Me += Math.pow(valorPertinencia,alpha);
                        Te += Math.pow(valorTipicidade,theta);
                        SSDe += valorPertinencia * Math.pow(distancia, 2);
                    }
                }
            }


            if(sfMiC != null) {

                for (int i = 0; i < sfMiC.getCF1pertinencias().length; i++) {
                    CF1pertinencias[i] += sfMiC.getCF1pertinencias()[i] ;
                    CF1tipicidades[i] += sfMiC.getCF1tipicidades()[i] ;
                }

                if(sfMiC.getN() >= minWeight) {
                    sfMiC.setSSDe(SSDe);
                    sfMiC.setMe(Me);
                    sfMiC.setTe(Te);
                    sfMiC.setCF1pertinencias(CF1pertinencias);
                    sfMiC.setCF1tipicidades(CF1tipicidades);
                    sfMiCS.add(sfMiC);
                }
            }
        }
        return sfMiCS;
    }

    private static double[][] calculaTipicidade(double[][] membershipMatrix) {
        int n = membershipMatrix.length; // number of data points
        int k = membershipMatrix[0].length; // number of clusters
        double[][] typicality = new double[n][k];
        for (int i = 0; i < n; i++) {
            double max_u_i = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < k; j++) {
                max_u_i = Math.max(max_u_i, membershipMatrix[i][j]);
            }
            for (int j = 0; j < k; j++) {
                typicality[i][j] = membershipMatrix[i][j] / max_u_i;
            }
        }
        return typicality;
    }

    public static List<SPFMiC> newSeparateExamplesByClusterClassifiedByFuzzyCMeans(List<Example> exemplos, FuzzyKMeansClusterer fuzzyClusterer, double rotulo, double alpha, double theta, int minWeight, int t) {
        List<SPFMiC> sfMiCS = new ArrayList<SPFMiC>();
        double[][] matrizMembership = fuzzyClusterer.getMembershipMatrix().getData();
        List<CentroidCluster> centroides = fuzzyClusterer.getClusters();
        for(int j=0; j<centroides.size(); j++) {
            SPFMiC sfMiC = null;
            double SSD = 0;
            List<Example> examples = centroides.get(j).getPoints();
            for(int k=0; k<examples.size(); k++) {
                int indexExample = exemplos.indexOf(examples.get(k));
                if (sfMiC == null) {
                    sfMiC = new SPFMiC(centroides.get(j).getCenter().getPoint(),
                            centroides.get(j).getPoints().size(),
                            alpha,
                            theta, t);
                    sfMiC.setRotulo(rotulo);
                    double valorPertinencia = matrizMembership[indexExample][j];
                    double[] ex = exemplos.get(k).getPonto();
                    double distancia = DistanceMeasures.calculaDistanciaEuclidiana(sfMiC.getCentroide(), ex);
                    SSD += valorPertinencia * Math.pow(distancia, 2);
                } else {
                    double valorPertinencia = matrizMembership[k][j];
                    double[] ex = exemplos.get(k).getPonto();
                    double distancia = DistanceMeasures.calculaDistanciaEuclidiana(sfMiC.getCentroide(), ex);
                    SSD += valorPertinencia * Math.pow(distancia, 2);
                }
            }
            if(sfMiC != null) {
                if(sfMiC.getN() >= minWeight) {
                    sfMiC.setSSDe(SSD);
                }
            }
            sfMiCS.add(sfMiC);
        }
        return sfMiCS;
    }

    private static int getIndiceDoMaiorValor(double[] array) {
        int index = 0;
        double maior = -1000000;
        for(int i=0; i<array.length; i++) {
            if(array[i] > maior && array[i] < 1){
                index = i;
                maior = array[i];
            }
        }
        return index;
    }

    public static Map<Double, List<Example>> separateByClasses(List<Example> chunk) {
        Map<Double, List<Example>> examplesByClass = new HashMap<>();
        for(int i=0; i<chunk.size(); i++) {
            if(examplesByClass.containsKey(chunk.get(i).getRotuloVerdadeiro())) {
                examplesByClass.get(chunk.get(i).getRotuloVerdadeiro()).add(chunk.get(i));
            } else {
                List<Example> put = examplesByClass.put(chunk.get(i).getRotuloVerdadeiro(), new ArrayList());
                examplesByClass.get(chunk.get(i).getRotuloVerdadeiro()).add(chunk.get(i));
            }
        }
        return examplesByClass;
    }

}
