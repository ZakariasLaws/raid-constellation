package nl.zakarias.constellation.edgeinference;

import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;

public class ResultEvent implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public byte[] predictions;
    public byte[] correct;
    public float[] certainty;
    public CrunchifyGetIPHostname host;

    public ResultEvent(byte[] correctClassification, byte[] predictions, float[] certainty, CrunchifyGetIPHostname host) {
        this.correct = correctClassification;
        this.predictions = predictions;
        this.certainty = certainty;
        this.host = host;
    }
}
