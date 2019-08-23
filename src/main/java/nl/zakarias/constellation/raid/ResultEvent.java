package nl.zakarias.constellation.raid;

import nl.zakarias.constellation.raid.configuration.Configuration;
import nl.zakarias.constellation.raid.utils.CrunchifyGetIPHostname;

/**
 * The event passed between {@link nl.zakarias.constellation.raid.Predictor} and {@link nl.zakarias.constellation.raid.Target}
 *
 * All predictions going from the Predictor to the Target *MUST* implement this object using one of the provided
 * constructors. The event will be serialized and handled appropriately at the Target. All results can be one or more,
 * since we allow batches of images.
 */
public class ResultEvent implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public byte[] predictions_1D = null; // MNIST, CIFAR-10
    public byte[][] predictions_2D = null;
    public byte[][][] predictions_3D = null;
    public float[][][][] predictions_4D = null; // Yolo
    public byte[] correct;
    public float[] certainty;
    public int[] imageIdentifiers;
    public Configuration.ModelName modelName;
    public CrunchifyGetIPHostname host = null;
    public CrunchifyGetIPHostname src = null;

    /**
     * Constructor for results which contain only one element per predictions (e.g. a batch of MNIST classified images)
     *
     * @param modelName The model name that was used
     * @param correctClassification The correct classification (may be null)
     * @param predictions_1D The batch of predictions
     * @param certainty A list of certainties for these predictions
     */
    public ResultEvent(Configuration.ModelName modelName, byte[] correctClassification, byte[] predictions_1D, float[] certainty) {
        this.modelName = modelName;
        this.correct = correctClassification;
        this.predictions_1D = predictions_1D;
        this.certainty = certainty;

        // The following must be set from the outside
        this.imageIdentifiers = new int[predictions_1D.length]; // Needs to be set from the outside
    }

    /**
     * Constructor for results which contain a batch where each prediction is one vector
     *
     * @param modelName The model name that was used
     * @param correctClassification The correct classification (may be null)
     * @param predictions The batch of predictions
     * @param certainty A list of certainties for these predictions
     */
    public ResultEvent(Configuration.ModelName modelName, byte[] correctClassification, byte[][] predictions, float[] certainty) {
        this.modelName = modelName;
        this.correct = correctClassification;
        this.certainty = certainty;
        this.predictions_2D = predictions;

        // The following must be set from the outside
        this.imageIdentifiers = new int[predictions.length]; // Needs to be set from the outside
    }

    /**
     * Constructor for results which contain a batch of 2D matrices
     *
     * @param modelName The model name that was used
     * @param correctClassification The correct classification (may be null)
     * @param predictions The batch of predictions
     * @param certainty A list of certainties for these predictions
     */
    public ResultEvent(Configuration.ModelName modelName, byte[] correctClassification, byte[][][] predictions, float[] certainty) {
        this.modelName = modelName;
        this.correct = correctClassification;
        this.certainty = certainty;
        this.predictions_3D = predictions;

        // The following must be set from the outside
        this.imageIdentifiers = new int[predictions.length]; // Needs to be set from the outside
    }

    /**
     * Constructor for results which contain a batch of 3D matrices per prediction (i.e. YOLO)
     *
     * @param modelName The model name that was used
     * @param correctClassification The correct classification (may be null)
     * @param predictions The batch of predictions
     * @param certainty A list of certainties for these predictions
     */
    public ResultEvent(Configuration.ModelName modelName, byte[] correctClassification, float[][][][] predictions, float[] certainty) {
        this.modelName = modelName;
        this.correct = correctClassification;
        this.certainty = certainty;
        this.predictions_4D = predictions;

        // The following must be set from the outside
        this.imageIdentifiers = new int[predictions.length]; // Needs to be set from the outside
    }
}
