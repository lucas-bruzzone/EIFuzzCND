package EIFuzzCND.Structs;


import EIFuzzCND.FuzzyFunctions.DistanceMeasures;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Enumeration;

public class SPFMiC implements Instance {
    private double CF1pertinencias[];
    private double CF1tipicidades[];
    private double Me;
    private double Te;
    private double SSDe;
    private double N;
    private double t;
    private double updated;
    private double created;
    private double rotulo;
    private double rotuloReal;
    private double centroide[];
    private double alpha;
    private double theta;
    private boolean isObsolete;
    private boolean isNull;


    public SPFMiC(double[] centroide, int N, double alpha, double theta, int t) {
        this.CF1pertinencias = new double[centroide.length];
        this.CF1tipicidades = new double[centroide.length];
        this.centroide = centroide;
        System.arraycopy(centroide, 0, this.CF1pertinencias, 0, centroide.length);
        System.arraycopy(centroide, 0, this.CF1tipicidades, 0, centroide.length);
        this.N = N;
        this.alpha = alpha;
        this.theta = theta;
        this.Me = 1;
        this.Te = 1;
        this.updated = t;
        this.created = t;
        this.SSDe = 0;
        this.t = t;
        this.isObsolete = false;
    }

    public double[] getCF1pertinencias() {
        return CF1pertinencias;
    }

    public void setCF1pertinencias(double[] CF1pertinencias) {
        this.CF1pertinencias = CF1pertinencias;
    }

    public double[] getCF1tipicidades() {
        return CF1tipicidades;
    }

    public void setCF1tipicidades(double[] CF1tipicidades) {
        this.CF1tipicidades = CF1tipicidades;
    }



    public void setSSDe(double SSDe) {
        this.SSDe = SSDe;
    }
    public double getN() {
        return N;
    }

    public double getTheta() {
        return theta;
    }

    public double getT() {
        return t;
    }

    public double getRotulo() {
        return this.rotulo;
    }

    public void setRotulo(double rotulo) {
        this.rotulo = rotulo;
    }

    public double[] getCentroide() {
        return this.centroide;
    }

    public void setCentroide(double[] centroide) {
        this.centroide = centroide;
    }


    public void setMe(double me) {
        Me = me;
    }

    public boolean isNull() {
        return isNull;
    }

    public double getRotuloReal() {
        return rotuloReal;
    }

    public void setRotuloReal(double rotuloReal) {
        this.rotuloReal = rotuloReal;
    }

    public double getUpdated() {
        return updated;
    }

    public void setUpdated(double updated) {
        this.updated = updated;
    }

    public double getCreated() {
        return created;
    }

    private void atualizaCentroide(){
        int nAtributos = this.CF1pertinencias.length;
        this.centroide = new double[nAtributos];
        for(int i=0; i<nAtributos; i++) {
            this.centroide[i] = (
                    (this.alpha * CF1pertinencias[i] + this.theta * CF1tipicidades[i]) /
                            (this.alpha * this.Te + this.theta * Me)
            );
        }
    }

    public void atribuiExemplo(Example exemplo, double pertinencia, double tipicidade) {
        double dist = DistanceMeasures.calculaDistanciaEuclidiana(exemplo.getPonto(), this.centroide);
        this.N++;
        this.Me += Math.pow(pertinencia, this.alpha);
        this.Te += Math.pow(tipicidade, this.theta);
        this.SSDe += Math.pow(dist, 2) * pertinencia;
        for(int i=0; i<this.centroide.length; i++) {
            CF1pertinencias[i] += exemplo.getPontoPorPosicao(i) * pertinencia;
            CF1tipicidades[i] += exemplo.getPontoPorPosicao(i) * tipicidade;
        }
        this.atualizaCentroide();
    }

    public double calculaTipicidade(double[] exemplo, double n, double K) {
        double γi = this.getγi(K);
        double dist = DistanceMeasures.calculaDistanciaEuclidiana(exemplo, this.centroide);
        return (1 / (1 + Math.pow(((this.theta/ γi) * dist), (1/(n-1)))));
    }

    private double getγi(double K) {
        return  K * (this.SSDe / this.Me);
    }


    //1.5 kdd
    //1.5 ou 2 cover
    //4 synedc
    //2 rbf e moa
    public double getRadiusWithWeight() {
        return Math.sqrt((this.SSDe/this.N))  * 2 ;
    }
    public double getRadiusND() {
        return Math.sqrt((this.SSDe/this.N)) ;
    }
    public double getRadiusUnsupervised() {
        return Math.sqrt((this.SSDe/this.N)) ;
    }

    @Override
    public Attribute attribute(int i) {
        return null;
    }

    @Override
    public Attribute attributeSparse(int i) {
        return null;
    }

    @Override
    public Attribute classAttribute() {
        return null;
    }

    @Override
    public int classIndex() {
        return 0;
    }

    @Override
    public boolean classIsMissing() {
        return false;
    }

    @Override
    public double classValue() {
        return 0;
    }

    @Override
    public Instance copy(double[] doubles) {
        return null;
    }

    @Override
    public Instances dataset() {
        return null;
    }

    @Override
    public void deleteAttributeAt(int i) {

    }

    @Override
    public Enumeration<Attribute> enumerateAttributes() {
        return null;
    }

    @Override
    public boolean equalHeaders(Instance instance) {
        return false;
    }

    @Override
    public String equalHeadersMsg(Instance instance) {
        return null;
    }

    @Override
    public boolean hasMissingValue() {
        return false;
    }

    @Override
    public int index(int i) {
        return 0;
    }

    @Override
    public void insertAttributeAt(int i) {

    }

    @Override
    public boolean isMissing(int i) {
        return false;
    }

    @Override
    public boolean isMissingSparse(int i) {
        return false;
    }

    @Override
    public boolean isMissing(Attribute attribute) {
        return false;
    }

    @Override
    public Instance mergeInstance(Instance instance) {
        return null;
    }

    @Override
    public int numAttributes() {
        return 0;
    }

    @Override
    public int numClasses() {
        return 0;
    }

    @Override
    public int numValues() {
        return 0;
    }

    @Override
    public void replaceMissingValues(double[] doubles) {

    }

    @Override
    public void setClassMissing() {

    }

    @Override
    public void setClassValue(double v) {

    }

    @Override
    public void setClassValue(String s) {

    }

    @Override
    public void setDataset(Instances instances) {

    }

    @Override
    public void setMissing(int i) {

    }

    @Override
    public void setMissing(Attribute attribute) {

    }

    @Override
    public void setValue(int i, double v) {

    }

    @Override
    public void setValueSparse(int i, double v) {

    }

    @Override
    public void setValue(int i, String s) {

    }

    @Override
    public void setValue(Attribute attribute, double v) {

    }

    @Override
    public void setValue(Attribute attribute, String s) {

    }

    @Override
    public void setWeight(double v) {

    }

    @Override
    public Instances relationalValue(int i) {
        return null;
    }

    @Override
    public Instances relationalValue(Attribute attribute) {
        return null;
    }

    @Override
    public String stringValue(int i) {
        return null;
    }

    @Override
    public String stringValue(Attribute attribute) {
        return null;
    }

    @Override
    public double[] toDoubleArray() {
        return this.centroide;
    }

    @Override
    public String toStringNoWeight(int i) {
        return null;
    }

    @Override
    public String toStringNoWeight() {
        return null;
    }

    @Override
    public String toStringMaxDecimalDigits(int i) {
        return null;
    }

    @Override
    public String toString(int i, int i1) {
        return null;
    }

    @Override
    public String toString(int i) {
        return null;
    }

    @Override
    public String toString(Attribute attribute, int i) {
        return null;
    }

    @Override
    public String toString(Attribute attribute) {
        return null;
    }

    @Override
    public double value(int i) {
        return 0;
    }

    @Override
    public double valueSparse(int i) {
        return 0;
    }

    @Override
    public double value(Attribute attribute) {
        return 0;
    }

    @Override
    public double weight() {
        return 0;
    }

    @Override
    public Object copy() {
        return null;
    }

    public void setTe(double te) {
        Te = te;
    }

    public void setObsolete(boolean b) {
        this.isObsolete = true;
    }

    public boolean isObsolete() {
        return isObsolete;
    }
}