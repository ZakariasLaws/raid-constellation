package nl.zakarias.constellation.raid.models.cifar10;

import com.google.gson.Gson;
import nl.zakarias.constellation.raid.ResultEvent;
import nl.zakarias.constellation.raid.configuration.Configuration;
import nl.zakarias.constellation.raid.modelServing.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class Cifar10Classifier {
    private static Logger logger = LoggerFactory.getLogger(Cifar10Classifier.class);

    static private int PORT = Integer.parseInt(System.getenv("TENSORFLOW_SERVING_PORT"));

    private class Cifar10Results {
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
    static ResultEvent classify(byte[][][][] data, int version, byte[] target) throws IOException {
        if (logger.isDebugEnabled()){
            logger.debug("Performing prediction...");
        }
        String result = API.predict(PORT, Cifar10.modelName, version, data, Cifar10.signatureString);
        Gson g = new Gson();
        Cifar10Results cifar10Results = g.fromJson(result, Cifar10Results.class);

        byte[] predictions = new byte[cifar10Results.predictions.length];
        float[] certainty = new float[cifar10Results.predictions.length];

        //TODO Implement possible checking to find classification
        // Check each result
        for (int i=0; i<cifar10Results.predictions.length; i++){
            float val = 0;
            int pos = 0;
            for (int x=0; x<cifar10Results.predictions[i].length; x++){
                if (cifar10Results.predictions[i][x] > val){
                    val = cifar10Results.predictions[i][x];
                    pos = x;
                }
            }
            certainty[i] = val; // Store the certainty of the result
            predictions[i] = (byte) pos; // Store the predictions, can be 0, 1 ... 8, 9
        }

        return new ResultEvent(Configuration.ModelName.CIFAR10, target, predictions, certainty);
    }
}
