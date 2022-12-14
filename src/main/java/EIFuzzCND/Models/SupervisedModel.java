package EIFuzzCND.Models;

import EIFuzzCND.Structs.Example;
import EIFuzzCND.FuzzyFunctions.*;
import EIFuzzCND.Structs.SPFMiC;
import org.apache.commons.math3.ml.clustering.FuzzyKMeansClusterer;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

public class SupervisedModel {
    public String dataset;
    public String caminho;
    public double fuzzification;
    public double alpha;
    public double theta;
    public int K;
    //public int N;
    public int minWeight;
    public List<Double> knowLabels = new ArrayList<>();
    Map<Double, List<SPFMiC>> classifier = new HashMap<>();

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

    public List<Example> trainNewClassifier(List<Example> chunk, int t, int minWeight) throws Exception {
        List<Example> newChunk = new ArrayList<>();
        Map<Double, List<Example>> examplesByClass = FuzzyFunctions.separateByClasses(chunk);
        List<Double> classes = new ArrayList<>();
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
                pertinencia.add(spfMiCS.get(i).calculaPertinencia(example.getPonto(), spfMiCS.get(i).getN(), K));
                auxSPFMiCs.add(spfMiCS.get(i));
            }
        }

        if(isOutlier) {
            return -1;
        }

        Double maxVal = Collections.max(tipicidades);
        int indexMax = tipicidades.indexOf(maxVal);

        SPFMiC spfmic = auxSPFMiCs.get(indexMax);
        int index = spfMiCS.indexOf(spfmic);



        spfMiCS.get(index).setUpdated(updateTime);
        return spfMiCS.get(index).getRotulo();
    }

    public SPFMiC classifyComErro(Instance ins, int updateTime) throws Exception {
        List<SPFMiC> allSPFMiCSOfClassifier = new ArrayList<>();
        allSPFMiCSOfClassifier.addAll(this.getAllSPFMiCsFromClassifier(classifier));
        SPFMiC spfMiC = this.classifyComErro(allSPFMiCSOfClassifier, new Example(ins.toDoubleArray(), true), updateTime);

        return spfMiC;
    }

    public SPFMiC classifyComErro(List<SPFMiC> spfMiCS, Example example, int updateTime) {
        List<Double> tipicidades = new ArrayList<>();
        List<Double> distancias = new ArrayList<>();
        List<Double> todasTipicidades = new ArrayList<>();
        List<SPFMiC> auxSPFMiCs = new ArrayList<>();
        boolean isOutlier = true;
        for(int i=0; i<spfMiCS.size(); i++) {
            double distancia = DistanceMeasures.calculaDistanciaEuclidiana(example, spfMiCS.get(i).getCentroide());
            todasTipicidades.add(spfMiCS.get(i).calculaTipicidade(example.getPonto(), spfMiCS.get(i).getN(), K));
            if(distancia <= spfMiCS.get(i).getRadiusWithWeight()) {
                isOutlier = false;
                distancias.add(distancia);
                tipicidades.add(spfMiCS.get(i).calculaTipicidade(example.getPonto(), spfMiCS.get(i).getN(), K));
                auxSPFMiCs.add(spfMiCS.get(i));
            }
        }

        Double maxVal = Collections.max(tipicidades);
        int indexMax = tipicidades.indexOf(maxVal);

        SPFMiC spfmic = auxSPFMiCs.get(indexMax);
        int index = spfMiCS.indexOf(spfmic);
        //System.out.println(spfMiCS.get(index).getUpdated());
        spfMiCS.get(index);
        return spfMiCS.get(index);
    }

    public void removeOldSPFMiCs(int ts, int currentTime) {
        List<Double> keys = new ArrayList<>();
        keys.addAll(classifier.keySet());
        for(int j=0; j<classifier.size(); j++) {
            List<SPFMiC> spfMiCSatuais = classifier.get(keys.get(j));
            List<SPFMiC> spfMiCSAux = spfMiCSatuais;
            for(int k=0; k<spfMiCSatuais.size(); k++) {
                if(currentTime - spfMiCSatuais.get(k).getT() > ts && currentTime - spfMiCSatuais.get(k).getUpdated() > ts) {
                    spfMiCSAux.remove(spfMiCSatuais.get(k));
                }
            }
            classifier.put(keys.get(j), spfMiCSAux);
        }
    }

    public List<SPFMiC> getAllSPFMiCs() {
        List<SPFMiC> spfMiCS = new ArrayList<>();
        spfMiCS.addAll(this.getAllSPFMiCsFromClassifier(classifier));

        return spfMiCS;
    }
}
