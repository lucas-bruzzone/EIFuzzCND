package EIFuzzCND.Phases;

import EIFuzzCND.FuzzyFunctions.*;
import EIFuzzCND.Models.*;
import EIFuzzCND.Output.HandlesFiles;
import EIFuzzCND.Structs.*;
import EIFuzzCND.ConfusionMatrix.ConfusionMatrix;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.FuzzyKMeansClusterer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.*;

public class OnlinePhase {
    private final int kShort;
    private final int ts;
    private final int minWeight;
    private int T = 0;
    private final String caminho;
    private final int latencia;
    private final int tChunk;
    SupervisedModel supervisedModel;
    NotSupervisedModel notSupervisedModel;
    double phi = 0;
    boolean existNovelty = false;
    double nPCount = 100;
    public List<Example> exemplosEsperandoTempo = new ArrayList<>();
    List<Double> novelties = new ArrayList<>();
    List<Example> results = new ArrayList<>();


    public OnlinePhase(String caminho, SupervisedModel supervisedModel, int latencia, int tChunk, int T, int kShort, double phi, int ts, int minWeight) {
        this.caminho = caminho;
        this.supervisedModel = supervisedModel;
        this.latencia = latencia;
        this.tChunk = tChunk;
        this.T = T;
        this.kShort = kShort;
        this.phi = phi;
        this.ts = ts;
        this.minWeight = minWeight;
        this.notSupervisedModel = new NotSupervisedModel();
    }


    public void initialize( String dataset) throws Exception {
        DataSource source;
        Instances data;

        //intermediária
        Instances esperandoTempo;
        int nExeTemp = 0;

        //ConfusionMatrix
        ConfusionMatrix confusionMatrix = new ConfusionMatrix();

        try {
            source = new DataSource(caminho + dataset + "-instances.arff");
            data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);
            ArrayList<Attribute> atts = new ArrayList<>();
            for (int i = 0; i < data.numAttributes(); i++) {
                atts.add(data.attribute(i));
            }

            List<Example> unkMem = new ArrayList<>();

            //intermediaria
            esperandoTempo = data;
            List<Example> labeledMem = new ArrayList<>();



            for(int tempo = 0, novidade = 0, tempoLatencia = 0; tempo <data.size(); tempo++,novidade++, tempoLatencia++) {
                Instance ins = data.get(tempo);
                Example exemplo = new Example(ins.toDoubleArray(), true, tempo);
                double rotulo = this.supervisedModel.classifyNew(ins, tempo);
                exemplo.setRotuloClassificado(rotulo);
                if (rotulo == -1) {
                    rotulo = notSupervisedModel.classify(exemplo, this.supervisedModel.K, tempo);
                    exemplo.setRotuloClassificado(rotulo);
                    if(rotulo == -1) {
                        unkMem.add(exemplo);
                        if (unkMem.size() >= T) {
                            unkMem = this.multiClassNoveltyDetection(unkMem, tempo,confusionMatrix);
                        }
                    }
                }

                results.add(exemplo);


                confusionMatrix.addInstance(exemplo.getRotuloVerdadeiro(),exemplo.getRotuloClassificado());


                this.exemplosEsperandoTempo.add(exemplo);


               if(tempoLatencia >= latencia) {
                    Example labeledExample = new Example(esperandoTempo.get(nExeTemp).toDoubleArray(), true, tempo);
                    labeledMem.add(labeledExample);
                    if(labeledMem.size() >= tChunk) {
                        labeledMem = this.supervisedModel.trainNewClassifier(labeledMem, tempo);
                        labeledMem.clear();
                    }
                    nExeTemp++;
                }

                supervisedModel.removeOldSPFMiCs(latencia + ts, tempo);
                this.removeOldUnknown(unkMem, ts, tempo);


                if (novidade == 1000) {
                    novidade = 0;
                    confusionMatrix.mergeClasses(confusionMatrix.getClassesWithMaxCount());
                    System.out.println(confusionMatrix.calculateAccuracy());
                    if (existNovelty) {
                        novelties.add(1.0);
                        existNovelty = false;
                    } else {
                        novelties.add(0.0);
                    }
                }

            }
            confusionMatrix.printMatrix();

            confusionMatrix.mergeClasses(confusionMatrix.getClassesWithMaxCount());

            confusionMatrix.printMatrix();
            System.out.println("Desconhecidos:" + unkMem.size());
            HandlesFiles.salvaNovidades(novelties, dataset,latencia);
            HandlesFiles.salvaResultados(results, dataset,latencia);



        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    private List<Example> multiClassNoveltyDetection(List<Example> listaDesconhecidos, int tempo, ConfusionMatrix confusionMatrix) {
        if (listaDesconhecidos.size() > kShort) {
            FuzzyKMeansClusterer clusters = FuzzyFunctions.fuzzyCMeans(listaDesconhecidos, kShort, this.supervisedModel.fuzzification);
            List<CentroidCluster> centroides = clusters.getClusters();
            List<Double> silhuetas = FuzzyFunctions.fuzzySilhouette(clusters, listaDesconhecidos, this.supervisedModel.alpha);
            List<Integer> silhuetasValidas = new ArrayList<>();

            for (int i = 0; i < silhuetas.size(); i++) {
                if (silhuetas.get(i) > 0 && centroides.get(i).getPoints().size() >= minWeight) {
                    silhuetasValidas.add(i);
                }
            }

            List<SPFMiC> sfMiCS = FuzzyFunctions.newSeparateExamplesByClusterClassifiedByFuzzyCMeans(listaDesconhecidos, clusters, -1, this.supervisedModel.alpha, this.supervisedModel.theta, minWeight, tempo);
            List<SPFMiC> sfmicsConhecidos = supervisedModel.getAllSPFMiCs();
            List<Double> frs = new ArrayList<>();

            for (int i = 0; i < centroides.size(); i++) {
                if (silhuetasValidas.contains(i) && !sfMiCS.get(i).isNull()) {
                    frs.clear();
                    for (int j = 0; j < sfmicsConhecidos.size(); j++) {
                        double di = sfmicsConhecidos.get(j).getRadius();
                        double dj = sfMiCS.get(i).getRadius();
                        double dist = (di + dj) / DistanceMeasures.calculaDistanciaEuclidiana(sfmicsConhecidos.get(j).getCentroide(), sfMiCS.get(i).getCentroide());
                        frs.add((di + dj) / dist);
                    }

                    if (frs.size() > 0) {
                        Double minFr = Collections.min(frs);
                        int indexMinFr = frs.indexOf(minFr);
                        if (minFr <= phi) {
                            sfMiCS.get(i).setRotulo(sfmicsConhecidos.get(indexMinFr).getRotulo());
                            List<Example> examples = centroides.get(i).getPoints();
                            HashMap<Double, Integer> rotulos = new HashMap<>();
                            for (int j = 0; j < examples.size(); j++) {
                                listaDesconhecidos.remove(examples.get(j));
                                if (rotulos.containsKey(examples.get(j).getRotuloVerdadeiro())) {
                                    rotulos.put(examples.get(j).getRotuloVerdadeiro(), rotulos.get(examples.get(j).getRotuloVerdadeiro()) + 1);
                                } else {
                                    rotulos.put(examples.get(j).getRotuloVerdadeiro(), 1);
                                }
                            }

                            Double[] keys = rotulos.keySet().toArray(new Double[0]);
                            double maiorValor = Double.MIN_VALUE;
                            double maiorRotulo = -1;
                            for (int k = 0; k < rotulos.size(); k++) {
                                if (maiorValor < rotulos.get(keys[k])) {
                                    maiorValor = rotulos.get(keys[k]);
                                    maiorRotulo = keys[k];
                                }
                            }

                            if (maiorRotulo == sfMiCS.get(i).getRotulo()) {
                                sfMiCS.get(i).setRotuloReal(maiorRotulo);
                                notSupervisedModel.spfMiCS.add(sfMiCS.get(i));

                                for (int j = 0; j < examples.size(); j++) {
                                    double trueLabel = examples.get(j).getRotuloVerdadeiro();
                                    double predictedLabel = sfMiCS.get(i).getRotulo(); // usar o rótulo do SFMiCS
                                    System.out.println("True Label:" + trueLabel + "predicted Label:" + predictedLabel);
                                    confusionMatrix.addInstance(trueLabel, predictedLabel);
                                    confusionMatrix.updateConfusionMatrix(trueLabel,predictedLabel);
                                }
                            }

                        } else {
                            existNovelty = true;
                            sfMiCS.get(i).setRotulo(this.generateNPLabel());
                            List<Example> examples = centroides.get(i).getPoints();
                            HashMap<Double, Integer> rotulos = new HashMap<>();
                            for (int j = 0; j < examples.size(); j++) {
                                listaDesconhecidos.remove(examples.get(j));
                                double trueLabel = examples.get(j).getRotuloVerdadeiro();
                                double predictedLabel = sfMiCS.get(i).getRotulo();
                                confusionMatrix.addInstance(trueLabel, predictedLabel);
                                confusionMatrix.updateConfusionMatrix(trueLabel, predictedLabel);
                                if (rotulos.containsKey(examples.get(j).getRotuloVerdadeiro())) {
                                    rotulos.put(examples.get(j).getRotuloVerdadeiro(), rotulos.get(examples.get(j).getRotuloVerdadeiro()) + 1);
                                } else {
                                    rotulos.put(examples.get(j).getRotuloVerdadeiro(), 1);
                                }
                            }

                            Double[] keys = rotulos.keySet().toArray(new Double[0]);
                            double maiorValor = Double.MIN_VALUE;
                            double maiorRotulo = -1;
                            for (int k = 0; k < rotulos.size(); k++) {
                                if (maiorValor < rotulos.get(keys[k])) {
                                    maiorValor = rotulos.get(keys[k]);
                                    maiorRotulo = keys[k];
                                }
                            }

                            sfMiCS.get(i).setRotuloReal(maiorRotulo);
                            notSupervisedModel.spfMiCS.add(sfMiCS.get(i));
                        }
                    }
                }
            }
        }
        return listaDesconhecidos;
    }

    private double generateNPLabel() {
        nPCount++;
        return nPCount;
    }

    private List<Example> removeOldUnknown(List<Example> unkMem, int ts, int ct) {
        List<Example> newUnkMem = new ArrayList<>();
        for(int i=0; i<unkMem.size(); i++) {
            if(ct - unkMem.get(i).getTime() >= ts) {
                newUnkMem.add(unkMem.get(i));
            }
        }
        return newUnkMem;
    }


}