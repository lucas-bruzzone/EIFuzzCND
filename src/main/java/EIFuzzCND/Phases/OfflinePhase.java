package EIFuzzCND.Phases;

import EIFuzzCND.Models.SupervisedModel;
import weka.core.Instances;

public class OfflinePhase {
    public SupervisedModel inicializar(String dataset, String caminho, Instances trainSet, double fuzzification, double alpha, double theta, int K, int minWeight) throws Exception {
        SupervisedModel supervisedModel = new SupervisedModel(dataset, caminho, fuzzification, alpha, theta, K, minWeight);
        supervisedModel.trainInitialModel(trainSet);
        return supervisedModel;
    }
}
