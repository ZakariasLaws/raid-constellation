package nl.zakarias.constellation.raid;

import nl.zakarias.constellation.raid.configuration.Configuration;
import nl.zakarias.constellation.raid.utils.CrunchifyGetIPHostname;

public class ResultEvent implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public byte[] predictions; // MNIST
    public float[][][][] predictions_yolo; // Yolo
    public byte[] correct;
    public float[] certainty;
    public int[] imageIdentifiers;
    public Configuration.ModelName modelName;
    public CrunchifyGetIPHostname host;
    public CrunchifyGetIPHostname src;

    public ResultEvent(Configuration.ModelName modelName, byte[] correctClassification, byte[] predictions, float[] certainty) {
        this.modelName = modelName;
        this.correct = correctClassification;
        this.predictions = predictions;
        this.certainty = certainty;

        // The following must be set from the outside
        this.host = null;
        this.src = null;
        this.imageIdentifiers = new int[predictions.length]; // Needs to be set from the outside
        this.predictions_yolo = null;
    }

    public ResultEvent(Configuration.ModelName modelName, byte[] correctClassification, float[][][][] predictions, float[] certainty) {
        this.modelName = modelName;
        this.correct = correctClassification;
        this.certainty = certainty;
        this.predictions_yolo = predictions;

        // The following must be set from the outside
        this.host = null;
        this.src = null;
        this.imageIdentifiers = new int[predictions.length]; // Needs to be set from the outside
        this.predictions = null;
    }
}
