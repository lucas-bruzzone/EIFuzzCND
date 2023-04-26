package EIFuzzCND.Models;

import EIFuzzCND.FuzzyFunctions.DistanceMeasures;
import EIFuzzCND.Structs.*;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NotSupervisedModel {
    public List<SPFMiC> spfMiCS = new ArrayList<>();

    public double classify(Example example, double K, int updated) {
        List<Double> tipicidades = new ArrayList<>();
        List<SPFMiC> auxSPFMiCs = new ArrayList<>();
        boolean isOutlier = true;
        for(int i=0; i<this.spfMiCS.size(); i++) {
            double distancia = DistanceMeasures.calculaDistanciaEuclidiana(example, this.spfMiCS.get(i).getCentroide());
            if(distancia <= this.spfMiCS.get(i).getRadiusUnsupervised()) {
                isOutlier = false;
                tipicidades.add(this.spfMiCS.get(i).calculaTipicidade(example.getPonto(),this.spfMiCS.get(i).getN(), K));
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
        List<SPFMiC> spfMiCSAux = new ArrayList<>(this.spfMiCS);
        int k = 0;
        for (int i = 0; i < spfMiCSAux.size(); i++) {
            SPFMiC spfmic = spfMiCSAux.get(i);
            if (currentTime - spfmic.getT() > ts && currentTime - spfmic.getUpdated() > ts) {
                spfMiCSAux.remove(spfmic);
                k++;
            }
        }

        this.spfMiCS = spfMiCSAux;
    }


}