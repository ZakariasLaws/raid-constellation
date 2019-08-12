package nl.zakarias.constellation.edgeinference.models.mnist_cnn;

import ibis.constellation.AbstractContext;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.NoSuitableExecutorException;
import nl.zakarias.constellation.edgeinference.models.ModelInterface;
import nl.zakarias.constellation.edgeinference.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MnistCnn implements ModelInterface {
    private static Logger logger = LoggerFactory.getLogger(MnistCnn.class);

    static String modelName = "mnist_cnn";  // Matches tensorflow_serving
    static String signatureString = "predict";  // Matches tensorflow_serving

    private int batchSize;

    private void sendMnistImageBatch(byte[][][][] images, byte[] targets, Constellation constellation, ActivityIdentifier aid, AbstractContext contexts) throws IOException, NoSuitableExecutorException {
        // Generate imageIdentifiers in order to link back the result to the image CURRENTLY DISCARDED UPON METHOD EXIT
        int[] imageIdentifiers = new int[images.length];
        // Create imageIdentifiers
        for(int i=0; i<imageIdentifiers.length; i++){
            imageIdentifiers[i] = Utils.imageIdentifier(images[i]);
        }

        // Generate activity
        MnistCnnActivity activity = new MnistCnnActivity(constellation.identifier().toString(), contexts, true, false, images, targets, aid, imageIdentifiers);

        // submit activity
        if (logger.isDebugEnabled()) {
            logger.debug("Submitting MnistActivity with contexts " + contexts.toString());
        }
        constellation.submit(activity);
    }

    private void runMnist(Constellation constellation, ActivityIdentifier target, String sourceDir, AbstractContext contexts) throws IOException, NoSuitableExecutorException {
        if (logger.isDebugEnabled()) {
            logger.debug("Reading MNIST image and label file...");
        }
        byte[][][][] images = Utils.readMnist_3D(sourceDir + "/t10k-images-idx3-ubyte");
        byte[] targets = Utils.readLabelsMnist(sourceDir + "/t10k-labels-idx1-ubyte");
        if (logger.isDebugEnabled()) {
            logger.debug("Done importing images");
        }

        for (int i = 0; i < images.length; i += batchSize) {
            byte[][][][] imageBatch = new byte[batchSize][images[i].length][images[i][0].length][images[i][0][0].length];
            byte[] targetBatch = new byte[batchSize];

            for (int x = 0; x < batchSize; x++){
                imageBatch[x] = images[i+x];
                targetBatch[x] = targets[i+x];
            }

            sendMnistImageBatch(imageBatch, targetBatch, constellation, target, contexts);
        }
    }

    @Override
    public void run(Constellation constellation, ActivityIdentifier targetActivityIdentifier, String sourceDir, AbstractContext contexts, int batchSize) throws IOException, NoSuitableExecutorException {
        this.batchSize = batchSize;
        runMnist(constellation, targetActivityIdentifier, sourceDir, contexts);
    }
}
