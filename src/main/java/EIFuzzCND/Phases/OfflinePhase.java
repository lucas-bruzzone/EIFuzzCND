package EIFuzzCND.Phases;

import EIFuzzCND.Models.SupervisedModel;
import weka.core.Instances;

public class OfflinePhase {
    private String dataset;
    private String caminho;
    private double fuzzification;
    private double alpha;
    private double theta;
    private int K;
    private int minWeight;
    private SupervisedModel supervisedModel;

    // Constructor
    public OfflinePhase(String dataset, String caminho, double fuzzification, double alpha, double theta, int K, int minWeight) {
        this.dataset = dataset;
        this.caminho = caminho;
        this.fuzzification = fuzzification;
        this.alpha = alpha;
        this.theta = theta;
        this.K = K;
        this.minWeight = minWeight;
    }


    public SupervisedModel inicializar(Instances trainSet) throws Exception {
        if (this.supervisedModel == null) {
            this.supervisedModel = new SupervisedModel(this.dataset, this.caminho, this.fuzzification, this.alpha, this.theta, this.K, this.minWeight);
        }
        this.supervisedModel.trainInitialModel(trainSet);
        return this.supervisedModel;
    }

}