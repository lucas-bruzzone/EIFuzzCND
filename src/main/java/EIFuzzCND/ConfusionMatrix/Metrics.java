package EIFuzzCND.ConfusionMatrix;

public class Metrics {
    private double accuracy;
    private double precision;
    private double recall;
    private double f1Score;
    private int tempo;
    private int unkMem;
    private double unknownRate;
    public Metrics(double accuracy, double precision, double recall, double f1Score, int tempo, int unkMem, double unknownRate) {
        this.accuracy = accuracy;
        this.precision = precision;
        this.recall = recall;
        this.f1Score = f1Score;
        this.tempo = tempo;
        this.unkMem = unkMem;
        this.unknownRate = unknownRate;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getF1Score() {
        return f1Score;
    }

    public int getTempo(){
        return tempo;
    }


    public int getUnkMem() {
        return unkMem;
    }

    public double getUnknownRate() {
        return unknownRate;
    }
}

