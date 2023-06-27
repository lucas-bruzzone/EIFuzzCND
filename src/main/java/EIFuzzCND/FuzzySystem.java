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
        String dataset = "synedc";
        String caminho = (new File(".")).getCanonicalPath() + "/datasets/" + dataset + "/";


        double fuzzyfication = 2;
        double alpha = 2;
        double theta = 1;
        int K = 4;
        int kshort = 4; // número de clusters
        int T = 40;
        int minWeightOffline = 0;
        int minWeightOnline = 15;
        int [] latencia = {10000};// 2000, 5000, 10000,10000000
        int tChunk = 2000;
        int ts = 200;

        double phi = 0.98;
        double[] percentLabeled = {0.2,0.5,0.8};

        ConverterUtils.DataSource source;
        Instances data;

        source = new ConverterUtils.DataSource(caminho + dataset +  "-train.arff");
        data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);

        List<Instances> chunks = new ArrayList<>();
        chunks.add(data);


        for (int i = 0; i < latencia.length; i++) {
            for (double labeled : percentLabeled) {
                boolean condicaoSatisfeita = false;
                while (!condicaoSatisfeita) {
                    OfflinePhase offlinePhase = new OfflinePhase(dataset, caminho, fuzzyfication, alpha, theta, K, minWeightOffline);
                    SupervisedModel supervisedModel = offlinePhase.inicializar(data);
                    OnlinePhase onlinePhase = new OnlinePhase(caminho, supervisedModel, latencia[i], tChunk, T, kshort, phi, ts, minWeightOnline, labeled);
                    onlinePhase.initialize(dataset);
                    if (onlinePhase.getTamConfusion() > 999) {
                        // Condição satisfeita, executar novamente para a mesma latência
                    } else {
                        // Condição não satisfeita, passa para a próxima latência
                        condicaoSatisfeita = true;
                        break;
                    }
                }
            }
        }



    }
}