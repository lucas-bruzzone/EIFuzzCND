package EIFuzzCND.Models;

import EIFuzzCND.Structs.Example;
import EIFuzzCND.FuzzyFunctions.*;
import EIFuzzCND.Structs.SPFMiC;
import org.apache.commons.math3.ml.clustering.FuzzyKMeansClusterer;
import weka.core.Instance;
import weka.core.Instances;

import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.*;

public class SupervisedModel {
    public String dataset;
    public String caminho;
    public double fuzzification;
    public double alpha;
    public double theta;
    public int K;
    public int minWeight;
    public List<Double> knowLabels = new ArrayList<>();
    static Map<Double, List<SPFMiC>> classifier = new HashMap<>();

    public SupervisedModel(String dataset, String caminho, double fuzzification, double alpha, double theta, int K, int minWeight) {
        this.dataset = dataset;
        this.caminho = caminho;
        this.fuzzification = fuzzification;
        this.alpha = alpha;
        this.theta = theta;
        this.K = K;
        this.minWeight = minWeight;
    }

    public void trainInitialModel(Instances trainSet) throws Exception {
        List<Example> chunk = new ArrayList<>();
        for(int i=0; i<trainSet.size(); i++) {
            Example ex = new Example(trainSet.instance(i).toDoubleArray(), true);
            chunk.add(ex);
        }
        Map<Double, List<Example>> examplesByClass = FuzzyFunctions.separateByClasses(chunk);
        List<Double> classes = new ArrayList<>();
        classes.addAll(examplesByClass.keySet());
        for(int j=0; j<examplesByClass.size(); j++) {
            if(examplesByClass.get(classes.get(j)).size() > this.K) {
                if (!this.knowLabels.contains(classes.get(j))) {
                    this.knowLabels.add(classes.get(j));
                }
                FuzzyKMeansClusterer clusters = FuzzyFunctions.fuzzyCMeans(examplesByClass.get(classes.get(j)), this.K, this.fuzzification);
                List<SPFMiC> spfmics = FuzzyFunctions.separateExamplesByClusterClassifiedByFuzzyCMeans(examplesByClass.get(classes.get(j)), clusters, classes.get(j), this.alpha, this.theta, this.minWeight, 0);
                classifier.put(classes.get(j), spfmics);
            }
        }
    }

    public double classifyNew(Instance ins, int updateTime) throws Exception {
        List<SPFMiC> allSPFMiCSOfClassifier = new ArrayList<>();
        allSPFMiCSOfClassifier.addAll(this.getAllSPFMiCsFromClassifier(classifier));

        return this.classify(allSPFMiCSOfClassifier, new Example(ins.toDoubleArray(), true), updateTime);
    }
    private List<SPFMiC> getAllSPFMiCsFromClassifier(Map<Double, List<SPFMiC>> classifier) {
        List<SPFMiC> spfMiCS = new ArrayList<>();
        List<Double> keys = new ArrayList<>();
        keys.addAll(classifier.keySet());
        for(int i=0; i<classifier.size(); i++) {
            spfMiCS.addAll(classifier.get(keys.get(i)));
        }
        return spfMiCS;
    }
    public double classify(List<SPFMiC> spfMiCS, Example example, int updateTime) {
        List<Double> tipicidades = new ArrayList<>();
        List<Double> pertinencia = new ArrayList<>();
        List<SPFMiC> auxSPFMiCs = new ArrayList<>();
        boolean isOutlier = true;
        for(int i=0; i<spfMiCS.size(); i++) {
            double distancia = DistanceMeasures.calculaDistanciaEuclidiana(example, spfMiCS.get(i).getCentroide());
            if(distancia <= spfMiCS.get(i).getRadiusWithWeight()) {
                isOutlier = false;
                tipicidades.add(spfMiCS.get(i).calculaTipicidade(example.getPonto(), spfMiCS.get(i).getN(), K));
                pertinencia.add(SupervisedModel.calculaPertinencia(example.getPonto(), spfMiCS.get(i).getCentroide(), fuzzification));
                auxSPFMiCs.add(spfMiCS.get(i));
            }
        }

        if(isOutlier) {
            return -1;
        }

        Double maxValTip = Collections.max(tipicidades);
        int indexMaxTip = tipicidades.indexOf(maxValTip);

        Double maxValPer = Collections.max(pertinencia);

        SPFMiC spfmic = auxSPFMiCs.get(indexMaxTip);
        int index = spfMiCS.indexOf(spfmic);

        spfMiCS.get(index).setUpdated(updateTime);
        spfMiCS.get(index).atribuiExemplo(example,maxValPer,1);

        return spfMiCS.get(index).getRotulo();
    }

    public static double calculaPertinencia(double[] dataPoints, double[] clusterCentroids, double m) {
        int n = dataPoints.length; // number of features of the data point
        double distance = 0;
        for (int i = 0; i < n; i++) {
            distance += Math.pow(dataPoints[i] - clusterCentroids[i], 2);
        }
        return Math.exp(-distance/m);
    }
    public List<SPFMiC> getAllSPFMiCs() {
        List<SPFMiC> spfMiCS = new ArrayList<>();
        spfMiCS.addAll(this.getAllSPFMiCsFromClassifier(classifier));

        return spfMiCS;
    }


    public List<Example> trainNewClassifier(List<Example> chunk, int t) throws Exception {
        List<Example> newChunk = new ArrayList<>();
        Map<Double, List<Example>> examplesByClass = FuzzyFunctions.separateByClasses(chunk);
        List<Double> classes = new ArrayList<>();
        Map<Double, List<SPFMiC>> classifier = new HashMap<>();
        classes.addAll(examplesByClass.keySet());
        for(int j=0; j<examplesByClass.size(); j++) {
            if(examplesByClass.get(classes.get(j)).size() >= this.K * 2) {
                if(!this.knowLabels.contains(classes.get(j))) {
                    this.knowLabels.add(classes.get(j));
                }
                FuzzyKMeansClusterer clusters = FuzzyFunctions.fuzzyCMeans(examplesByClass.get(classes.get(j)), this.K, this.fuzzification);
                List<SPFMiC> spfmics = FuzzyFunctions.separateExamplesByClusterClassifiedByFuzzyCMeans(examplesByClass.get(classes.get(j)), clusters, classes.get(j), this.theta, this.alpha, this.minWeight, t);
                classifier.put(classes.get(j), spfmics);
            } else {
                newChunk.addAll(examplesByClass.get(classes.get(j)));
            }
        }
        return newChunk;
    }


    public void removeOldSPFMiCs(int ts, int currentTime) {
        for (int i = 0; i < classifier.size(); i++) {
            Map<Double, List<SPFMiC>> classifierOld = (Map<Double, List<SPFMiC>>) classifier.get(i);
            if (classifierOld == null) {
                continue;
            }
            List<Double> keys = new ArrayList<>();
            keys.addAll(classifierOld.keySet());
            for(int j = 0; j< classifierOld.size(); j++) {
                List<SPFMiC> spfMiCSatuais = classifierOld.get(keys.get(j));
                List<SPFMiC> spfMiCSAux = spfMiCSatuais;
                for(int k=0; k<spfMiCSatuais.size(); k++) {
                    if(currentTime - spfMiCSatuais.get(k).getT() > ts && currentTime - spfMiCSatuais.get(k).getUpdated() > ts) {
                        spfMiCSAux.remove(spfMiCSatuais.get(k));
                    }
                }
                classifierOld.put(keys.get(j), spfMiCSAux);
            }
        }
    }


}