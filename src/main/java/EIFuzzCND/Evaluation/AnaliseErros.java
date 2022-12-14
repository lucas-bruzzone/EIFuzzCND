package EIFuzzCND.Evaluation;

public class AnaliseErros {
    private double N;
    private double created;
    private int numErros;

    public AnaliseErros(double N, double created) {
        this.N = N;
        this.created = created;
        this.numErros = 1;
    }

    public void updateNumErros() {
        this.numErros++;
    }

    public String toString() {
        return "Created=" + this.created + ", N=" + this.N + ", numErros=" + this.numErros;
    }
}
