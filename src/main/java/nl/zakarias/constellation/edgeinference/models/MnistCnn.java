package nl.zakarias.constellation.edgeinference.models;

import nl.zakarias.constellation.edgeinference.ResultEvent;
import nl.zakarias.constellation.edgeinference.modelServing.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MnistCnn implements ModelInterface {
    private static Logger logger = LoggerFactory.getLogger(MnistCnn.class);

    static private int PORT = Integer.parseInt(System.getenv("EDGEINFERENCE_SERVING_PORT"));

    @Override
    public ResultEvent runClassification(byte[][] data) {
        String result = "";
        try {
            // TODO Use this result
            result = API.predict(PORT, "mnist", 1, data);
            System.out.println("PREDICTED: " + result + "\n\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TODO THIS NEEDS TO CHANGE
        return new ResultEvent(new byte[3], new byte[2], new Float[]{0.32f, 0.35f, 0.35f});
    }
}
