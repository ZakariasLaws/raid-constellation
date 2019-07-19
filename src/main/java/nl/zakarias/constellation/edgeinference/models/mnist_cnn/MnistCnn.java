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

    static String modelName = "mnist_cnn";
    static String signatureString = "predict";

    private void sendMnistImageBatch(byte[][][][] images, byte[] targets, Constellation constellation, ActivityIdentifier aid, AbstractContext contexts) throws IOException, NoSuitableExecutorException {
        // Generate imageIdentifiers in order to link back the result to the image CURRENTLY DISCARDED UPON METHOD EXIT
        int[] imageIdentifiers = new int[images.length];
        // Create imageIdentifiers
        for(int i=0; i<imageIdentifiers.length; i++){
            imageIdentifiers[i] = Utils.imageIdentifier(images[i]);
        }

        // Generate activity
        MnistCnnActivity activity = new MnistCnnActivity(contexts, true, false, images, targets, aid, imageIdentifiers);

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
        byte[][][][] images = Utils.MNISTReadDataFile_3D(sourceDir + "/t10k-images-idx3-ubyte");
        byte[] targets = Utils.MNISTReadLabelFile(sourceDir + "/t10k-labels-idx1-ubyte");
        if (logger.isDebugEnabled()) {
            logger.debug("Done importing images");
        }

        // TODO implement batch size setting, currently sending images one and one
        for (int i=0; i<images.length; i++) {
            sendMnistImageBatch(new byte[][][][]{images[i]}, new byte[] {targets[i]}, constellation, target, contexts);
        }
    }

    @Override
    public void run(Constellation constellation, ActivityIdentifier targetActivityIdentifier, String sourceDir, AbstractContext contexts) throws IOException, NoSuitableExecutorException {
        runMnist(constellation, targetActivityIdentifier, sourceDir, contexts);
    }
}
