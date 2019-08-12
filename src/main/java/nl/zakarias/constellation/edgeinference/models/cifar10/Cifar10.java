package nl.zakarias.constellation.edgeinference.models.cifar10;

import ibis.constellation.AbstractContext;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.NoSuitableExecutorException;
import nl.zakarias.constellation.edgeinference.models.ModelInterface;
import nl.zakarias.constellation.edgeinference.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Cifar10 implements ModelInterface {
    private static Logger logger = LoggerFactory.getLogger(Cifar10.class);

    static String modelName = "cifar10";
    static String signatureString = "predict";

    private int batchSize = 1;

    private void sendCifar10ImageBatch(byte[][][][] images, byte[] targets, Constellation constellation, ActivityIdentifier aid, AbstractContext contexts) throws IOException, NoSuitableExecutorException {
        // Generate imageIdentifiers in order to link back the result to the image CURRENTLY DISCARDED UPON METHOD EXIT
        int[] imageIdentifiers = new int[images.length];
        // Create imageIdentifiers
        for(int i=0; i<imageIdentifiers.length; i++){
            imageIdentifiers[i] = Utils.imageIdentifier(images[i]);
        }

        // Generate activity
        Cifar10Activity activity = new Cifar10Activity(constellation.identifier().toString(), contexts, true, false, images, targets, aid, imageIdentifiers);

        // submit activity
        if (logger.isDebugEnabled()) {
            logger.debug("Submitting Cifar10Activity with contexts " + contexts.toString());
        }
        constellation.submit(activity);
    }

    private void runMnist(Constellation constellation, ActivityIdentifier target, String sourceDir, AbstractContext contexts) throws IOException, NoSuitableExecutorException {
        if (logger.isDebugEnabled()) {
            logger.debug("Reading Cifar10 image and label file...");
        }

        byte[][][][] images = Utils.readCifar10(sourceDir + "/data_batch_1.bin");

        for (int i = 0; i < images.length; i += batchSize) {
            byte[][][][] imageBatch = new byte[batchSize][images[i].length][images[i][0].length][images[i][0][0].length];
//            byte[] targetBatch = new byte[batchSize];

            for (int x = 0; x < batchSize; x++){
                imageBatch[x] = images[i+x];
//                targetBatch[x] = targets[i+x];
            }

            sendCifar10ImageBatch(imageBatch, null, constellation, target, contexts);
        }
    }

    @Override
    public void run(Constellation constellation, ActivityIdentifier targetActivityIdentifier, String sourceDir, AbstractContext contexts, int batchSize) throws IOException, NoSuitableExecutorException {
        this.batchSize = batchSize;
        runMnist(constellation, targetActivityIdentifier, sourceDir, contexts);
    }
}
