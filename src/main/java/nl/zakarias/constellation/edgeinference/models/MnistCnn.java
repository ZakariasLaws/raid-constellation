package nl.zakarias.constellation.edgeinference.models;

import com.google.gson.Gson;
import nl.zakarias.constellation.edgeinference.ResultEvent;
import nl.zakarias.constellation.edgeinference.modelServing.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Specific class used for classifying Mnist images. It uses the {@link API} for connecting to tensorflow model server
 */
public class MnistCnn {
    private static Logger logger = LoggerFactory.getLogger(MnistCnn.class);

    static private int PORT = Integer.parseInt(System.getenv("EDGEINFERENCE_SERVING_PORT"));

    private class MnistResult {
        float[][] predictions;
    }

    /**
     * Run a classification on an image which already has a target label.
     *
     * @param data The batch of images we wish to classify
     * @param modelName The name of the model
     * @param version The version number
     * @param target The target Identifier of where to send the result.
     *
     * @return ResultEvent(...) containing the certainty, prediction and correct label (if existing)
     * @throws IOException If something goes wrong with the connection to the server
     */
    public static ResultEvent classify(byte[][] data, String modelName, int version, byte[] target) throws IOException {
        if (logger.isDebugEnabled()){
            logger.debug("MnistCnn: Performing prediction...");
        }
        String result = API.predict(PORT, modelName, version, data);
        Gson g = new Gson();
        MnistResult mnistResult = g.fromJson(result, MnistResult.class);

        byte[] predictions = new byte[mnistResult.predictions.length];
        float[] certainty = new float[mnistResult.predictions.length];

        // Check each result
        for (int i=0; i<mnistResult.predictions.length; i++){
            float val = 0;
            int pos = 0;
            for (int x=0; x<mnistResult.predictions[i].length; x++){
                if (mnistResult.predictions[i][x] > val){
                    val = mnistResult.predictions[i][x];
                    pos = x;
                }
            }
            certainty[i] = val; // Store the certainty of the result
            predictions[i] = (byte) pos; // Store the predictions, can be 0, 1 ... 8, 9

        }

        return new ResultEvent(target, predictions, certainty);
    }

    /**
     * Run a classification on an image which does not have a target.
     *
     * @param data The batch of images we wish to classify
     * @param modelName The name of the model
     * @param version The version number
     *
     * @return ResultEvent(...) containing the certainty, prediction and correct label (if existing)
     * @throws IOException If something goes wrong with the connection to the server
     */
    public static ResultEvent runClassification(byte[][] data, String modelName, int version) throws Exception {
        return classify(data, modelName, version, null);
    }
}
