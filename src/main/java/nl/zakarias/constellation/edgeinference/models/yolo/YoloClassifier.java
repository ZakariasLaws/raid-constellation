package nl.zakarias.constellation.edgeinference.models.yolo;

import com.google.gson.Gson;
import nl.zakarias.constellation.edgeinference.ResultEvent;
import nl.zakarias.constellation.edgeinference.modelServing.API;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class YoloClassifier {
    private static Logger logger = LoggerFactory.getLogger(YoloClassifier.class);

    static private int PORT = Integer.parseInt(System.getenv("EDGEINFERENCE_SERVING_PORT"));

    private class YoloResults {
        float[][] predictions;
    }

    /**
     * Run a classification on an image.
     *
     * @param data The batch of images we wish to classify
     * @param version The version number
     * @param target The target label of a correct classification, use _null_ if nonexistent
     *
     * @return ResultEvent(...) containing the certainty, prediction and correct label (if existing)
     * @throws IOException If something goes wrong with the connection to the server
     */
    static ResultEvent classify(byte[][] data, int version, byte[] target, CrunchifyGetIPHostname host) throws IOException {
        if (logger.isDebugEnabled()){
            logger.debug("Performing prediction...");
        }
        String result = API.predict(PORT, Yolo.modelName, version, data, Yolo.signatureString);
        Gson g = new Gson();
        YoloResults yoloResults = g.fromJson(result, YoloResults.class);

        // TODO Perform prediction using yolo model
        byte[] predictions = new byte[1];
        float[] certainty = new float[1];

        return new ResultEvent(target, predictions, certainty, host);
    }
}
