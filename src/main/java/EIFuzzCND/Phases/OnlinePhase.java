package EIFuzzCND.Phases;

import EIFuzzCND.ConfusionMatrix.Metrics;
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
    List<Double> novelties = new ArrayList<>();
    private double percentLabeled;
    List<Example> results = new ArrayList<>();
    private double divisor = 1000;
    private int tamConfusion;
    public OnlinePhase(String caminho, SupervisedModel supervisedModel, int latencia, int tChunk, int T, int kShort, double phi, int ts, int minWeight,double percentLabeled) {
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
        this.percentLabeled = percentLabeled;
    }


    public void initialize( String dataset) throws Exception {
        DataSource source;
        Instances data;

        //intermediária
        Instances esperandoTempo;
        int nExeTemp = 0;

        //ConfusionMatrix
        ConfusionMatrix confusionMatrix = new ConfusionMatrix();
        ConfusionMatrix confusionMatrixOriginal = new ConfusionMatrix();
        boolean append = false;
        Metrics metrics;
        List<Metrics> listaMetricas = new ArrayList<>();



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
            Set<Double> trueLabels = new HashSet<>();


            for(int tempo = 0, tempoLatencia = 0; tempo <data.size(); tempo++, tempoLatencia++) {
                Instance ins = data.get(tempo);
                Example exemplo = new Example(ins.toDoubleArray(), true, tempo);
                double rotulo = this.supervisedModel.classifyNew(ins, tempo);
                exemplo.setRotuloClassificado(rotulo);

                // Verifica se o rótulo verdadeiro é novo
                if (!trueLabels.contains(exemplo.getRotuloVerdadeiro()) || confusionMatrixOriginal.getNumberOfClasses() != tamConfusion) {
                    trueLabels.add(exemplo.getRotuloVerdadeiro());
                    tamConfusion = confusionMatrixOriginal.getNumberOfClasses();
                    //confusionMatrixOriginal.saveMatrix(dataset,latencia,percentLabeled);
                }

                if (rotulo == -1) {
                    rotulo = notSupervisedModel.classify(exemplo, this.supervisedModel.K, tempo);
                    exemplo.setRotuloClassificado(rotulo);
                    if(rotulo == -1) {
                        unkMem.add(exemplo);
                        if (unkMem.size() >= T) {
                            unkMem = this.multiClassNoveltyDetection(unkMem, tempo,confusionMatrix,confusionMatrixOriginal);
                        }
                    }
                }

                results.add(exemplo);
                confusionMatrix.addInstance(exemplo.getRotuloVerdadeiro(),exemplo.getRotuloClassificado());
                //confusionMatrixOriginal.addInstance(exemplo.getRotuloVerdadeiro(),exemplo.getRotuloClassificado());

                if(tempoLatencia >= latencia) {
                    if (Math.random() < percentLabeled || labeledMem.isEmpty()) {
                        Example labeledExample = new Example(esperandoTempo.get(nExeTemp).toDoubleArray(), true, tempo);
                        labeledMem.add(labeledExample);
                    }
                    if(labeledMem.size() >= tChunk) {
                        labeledMem = this.supervisedModel.trainNewClassifier(labeledMem, tempo);
                        labeledMem.clear();
                    }
                    nExeTemp++;
                }

                supervisedModel.removeOldSPFMiCs(latencia + ts, tempo);
                //notSupervisedModel.removeOldSPFMiCs(latencia+ts, tempo);
                this.removeOldUnknown(unkMem, ts, tempo);

                if ( tempo > 0 && tempo%divisor == 0) {
                    confusionMatrix.mergeClasses(confusionMatrix.getClassesWithNonZeroCount());
                    metrics = confusionMatrix.calculateMetrics(tempo,confusionMatrix.countUnknow(),divisor);
                    System.out.println("Tempo:" + tempo + " Acurácia: " + metrics.getAccuracy() + " Precision: " + metrics.getPrecision() );
                    listaMetricas.add(metrics);
                    if (existNovelty) {
                        novelties.add(1.0);
                        existNovelty = false;
                    } else {
                        novelties.add(0.0);
                    }
                }
            }

            //confusionMatrixOriginal.saveMatrix(dataset,latencia,percentLabeled);

            //tamConfusion = confusionMatrixOriginal.getNumberOfClasses();
            //confusionMatrixOriginal.printMatrix();
            // Salva todas as métricas no arquivo
            for (Metrics metrica : listaMetricas) {
                HandlesFiles.salvaMetrics((int) (metrica.getTempo()/divisor), metrica.getAccuracy(), metrica.getPrecision(), metrica.getRecall(), metrica.getF1Score(), dataset, latencia, percentLabeled, metrica.getUnkMem(),metrica.getUnknownRate(), append);
                append = true;
            }

            HandlesFiles.salvaNovidades(novelties, dataset,latencia,percentLabeled);
            HandlesFiles.salvaResultados(results, dataset,latencia,percentLabeled);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    private List<Example> multiClassNoveltyDetection(List<Example> listaDesconhecidos, int tempo, ConfusionMatrix confusionMatrix, ConfusionMatrix confusionMatrixOriginal) {
        if (listaDesconhecidos.size() > kShort) {
            FuzzyKMeansClusterer clusters = FuzzyFunctions.fuzzyCMeans(listaDesconhecidos, kShort, supervisedModel.fuzzification);
            List<CentroidCluster> centroides = clusters.getClusters();
            List<Double> silhuetas = FuzzyFunctions.fuzzySilhouette(clusters, listaDesconhecidos, supervisedModel.alpha);
            List<Integer> silhuetasValidas = new ArrayList<>();

            for (int i = 0; i < silhuetas.size(); i++) {
                if (silhuetas.get(i) > 0 && centroides.get(i).getPoints().size() >= minWeight) {
                    silhuetasValidas.add(i);
                }
            }

            List<SPFMiC> sfMiCS = FuzzyFunctions.newSeparateExamplesByClusterClassifiedByFuzzyCMeans(listaDesconhecidos, clusters, -1, supervisedModel.alpha, supervisedModel.theta, minWeight, tempo);
            List<SPFMiC> sfmicsConhecidos = supervisedModel.getAllSPFMiCs();
            List<Double> frs = new ArrayList<>();

            for (int i = 0; i < centroides.size(); i++) {
                if (silhuetasValidas.contains(i) && !sfMiCS.get(i).isNull()) {
                    frs.clear();
                    for (int j = 0; j < sfmicsConhecidos.size(); j++) {
                        double di = sfmicsConhecidos.get(j).getRadiusND();
                        double dj = sfMiCS.get(i).getRadiusND();
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
                                // DEBUG
                                listaDesconhecidos.remove(examples.get(j));

                                double trueLabel = examples.get(j).getRotuloVerdadeiro();
                                double predictedLabel = sfMiCS.get(i).getRotulo();
                                updateConfusionMatrix(trueLabel,predictedLabel,confusionMatrix);
                                //updateConfusionMatrix(trueLabel,predictedLabel,confusionMatrixOriginal);


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
                                updateConfusionMatrix(trueLabel,predictedLabel,confusionMatrix);
                                //updateConfusionMatrix(trueLabel,predictedLabel,confusionMatrixOriginal);
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


    public static void updateConfusionMatrix(double trueLabel, double predictedLabel, ConfusionMatrix confusionMatrix) {
        confusionMatrix.addInstance(trueLabel, predictedLabel);
        confusionMatrix.updateConfusionMatrix(trueLabel);
    }


    public int getTamConfusion() {
        return tamConfusion;
    }

    public void setTamConfusion(int tamConfusion) {
        this.tamConfusion = tamConfusion;
    }
}