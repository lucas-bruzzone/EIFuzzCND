package EIFuzzCND.ConfusionMatrix;

public class Metrics {
    private final double accuracy;
    private final double precision;
    private final double recall;
    private final double f1Score;
    private final int tempo;
    private final double unkMem;
    private final double unknownRate;
    public Metrics(double accuracy, double precision, double recall, double f1Score, int tempo, double unkMem, double unknownRate) {
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


    public double getUnkMem() {
        return unkMem;
    }

    public double getUnknownRate() {
        return unknownRate;
    }
}

