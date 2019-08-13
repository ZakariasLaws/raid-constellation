package nl.zakarias.constellation.raid.models.mnist;

import com.google.gson.Gson;
import nl.zakarias.constellation.raid.ResultEvent;
import nl.zakarias.constellation.raid.configuration.Configuration;
import nl.zakarias.constellation.raid.modelServing.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Specific class used for classifying Mnist images. It uses the {@link API} for connecting to tensorflow model server
 */
class MnistClassifier {
    private static Logger logger = LoggerFactory.getLogger(MnistClassifier.class);

    static private int PORT = Integer.parseInt(System.getenv("TENSORFLOW_SERVING_PORT"));

    private class MnistResult {
        float[][] predictions;
    }

    /**
     * Run a classification on an image which already has a target label.
     *
     * @param data The batch of images we wish to classify
     * @param version The version number
     * @param target The target label of a correct classification, use _null_ if nonexistent
     *
     * @return ResultEvent(...) containing the certainty, prediction and correct label (if existing)
     * @throws IOException If something goes wrong with the connection to the server
     */
    static ResultEvent classify(byte[][] data, int version, byte[] target) throws IOException {
        if (logger.isDebugEnabled()){
            logger.debug("Performing prediction...");
        }
        String result = API.predict(PORT, Mnist.modelName, version, data, Mnist.signatureString);
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

        return new ResultEvent(Configuration.ModelName.MNIST, target, predictions, certainty);
    }
}
