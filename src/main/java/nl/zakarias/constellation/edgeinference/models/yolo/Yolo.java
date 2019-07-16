package nl.zakarias.constellation.edgeinference.models.yolo;

import ibis.constellation.AbstractContext;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.NoSuitableExecutorException;
import nl.zakarias.constellation.edgeinference.modelServing.API;
import nl.zakarias.constellation.edgeinference.models.ModelInterface;
import nl.zakarias.constellation.edgeinference.models.mnist.MnistClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Specific class used for classifying Mnist images. It uses the {@link API} for connecting to tensorflow model server
 */
public class Yolo implements ModelInterface {
    private static Logger logger = LoggerFactory.getLogger(MnistClassifier.class);

    static private int PORT = Integer.parseInt(System.getenv("EDGEINFERENCE_SERVING_PORT"));
    static public String modelName = "yolo";
    static public String signatureString = "predict";

    @Override
    public void run(Constellation constellation, ActivityIdentifier targetActivityIdentifier, String sourceDir, AbstractContext contexts) throws IOException, NoSuitableExecutorException {

    }
}