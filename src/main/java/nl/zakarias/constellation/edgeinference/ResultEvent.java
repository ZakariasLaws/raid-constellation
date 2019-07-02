package nl.zakarias.constellation.edgeinference;

public class ResultEvent implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public byte[] classification;
    public byte[] correct;
    public Float[] certainty;

    public ResultEvent(byte[] correctClassification, byte[] classification, Float[] certainty) {
        this.correct = correctClassification;
        this.classification = classification;
        this.certainty = certainty;
    }
}
