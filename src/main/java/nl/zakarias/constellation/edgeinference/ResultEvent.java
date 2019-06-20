package nl.zakarias.constellation.edgeinference;

public class ResultEvent implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public String classification;
    public String correct;
    public Float certainty;

    public ResultEvent(String correctClassification, String classification, Float certainty) {
        this.correct = correctClassification;
        this.classification = classification;
        this.certainty = certainty;
    }

}
