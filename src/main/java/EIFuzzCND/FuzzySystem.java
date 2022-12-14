package EIFuzzCND;

import EIFuzzCND.Models.SupervisedModel;
import EIFuzzCND.Phases.OfflinePhase;
import EIFuzzCND.Phases.OnlinePhase;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FuzzySystem {
    public static void main(String[] args) throws IOException, Exception {
        String dataset = "moa";
        String caminho = (new File(".")).getCanonicalPath() + "/datasets/" + dataset + "/";
        double fuzzyfication = 2; // Parametro de fuzzificação
        double alpha = 2; // Utilizado para calculo de pertinência
        double theta = 1; // Utilizado para calculo de tipicidade
        int K = 4; // Número de micro-grupos para determinar uma classe
        int minWeight = 0; // Número mínimo de membros para criar um novo SPFMICS

        ConverterUtils.DataSource source1;
        Instances data1;

        source1 = new ConverterUtils.DataSource(caminho + dataset +  "-train.arff");
        data1 = source1.getDataSet();
        data1.setClassIndex(data1.numAttributes() - 1);

        List<Instances> chunks = new ArrayList<>();
        chunks.add(data1);

        //moa
        OfflinePhase offlinePhase = new OfflinePhase();
        SupervisedModel supervisedModel = offlinePhase.inicializar(dataset, caminho , data1, fuzzyfication, alpha, theta, K, minWeight);
        OnlinePhase onlinePhase = new OnlinePhase();
        onlinePhase.initialize(caminho  , dataset, supervisedModel, 10000000, 500, 40, 4, 0.2, 200, 25);
    }
}



