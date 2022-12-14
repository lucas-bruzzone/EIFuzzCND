package EIFuzzCND.Evaluation;

public class ResultsPFuzzND {
    private double acuracia;
    private double unkR;

    public ResultsPFuzzND(String acuracia, String unkR) {
        this.acuracia = Double.parseDouble(acuracia) * 100;
        this.unkR = Double.parseDouble(unkR) * 100;
    }
}
