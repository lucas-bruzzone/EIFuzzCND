package EIFuzzCND.Models;

import EIFuzzCND.FuzzyFunctions.DistanceMeasures;
import EIFuzzCND.Structs.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotSupervisedModel {
    public List<SPFMiC> spfMiCS = new ArrayList<>();

    public double classify(Example example, int K, int updated) {
        List<Double> tipicidades = new ArrayList<>();
        List<Double> pertinencia = new ArrayList<>();
        List<SPFMiC> auxSPFMiCs = new ArrayList<>();
        boolean isOutlier = true;
        for(int i=0; i<this.spfMiCS.size(); i++) {
            double distancia = DistanceMeasures.calculaDistanciaEuclidiana(example, this.spfMiCS.get(i).getCentroide());
            if(distancia <= this.spfMiCS.get(i).getRadiusNsModel()) {
                isOutlier = false;
                tipicidades.add(this.spfMiCS.get(i).calculaTipicidade(example.getPonto(),this.spfMiCS.get(i).getN(), K));
                pertinencia.add(this.spfMiCS.get(i).calculaPertinencia(example.getPonto(), this.spfMiCS.get(i).getN(), K));
                auxSPFMiCs.add(this.spfMiCS.get(i));
            }
        }

        if(isOutlier) {
            return -1;
        }

        Double maxVal = Collections.max(tipicidades);
        int indexMax = tipicidades.indexOf(maxVal);

        SPFMiC spfmic = auxSPFMiCs.get(indexMax);
        int index = this.spfMiCS.indexOf(spfmic);

        this.spfMiCS.get(index).setUpdated(updated);
        return this.spfMiCS.get(index).getRotulo();
    }

    public void removeOldSPFMiCs(int ts, int currentTime) {
        List<SPFMiC> spfMiCSAux = this.spfMiCS;
        int k = 0;
        for(int i=0; i<spfMiCS.size(); i++) {
            double sub = currentTime - spfMiCS.get(i).getUpdated();
            if(currentTime - spfMiCS.get(i).getT() > ts && currentTime - spfMiCS.get(i).getUpdated() > ts) {
                spfMiCSAux.remove(spfMiCS.get(i));
                k++;
            }
        }
//        System.out.println("Ns removeu " + k + " SPFMiCs");
        this.spfMiCS = spfMiCSAux;
    }
}
