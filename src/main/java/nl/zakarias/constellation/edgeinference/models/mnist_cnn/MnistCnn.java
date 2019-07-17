package nl.zakarias.constellation.edgeinference.models.mnist_cnn;

import ibis.constellation.AbstractContext;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.NoSuitableExecutorException;
import nl.zakarias.constellation.edgeinference.models.ModelInterface;
import nl.zakarias.constellation.edgeinference.models.mnist.Mnist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class MnistCnn implements ModelInterface {
    private static Logger logger = LoggerFactory.getLogger(MnistCnn.class);

    static String modelName = "mnist_cnn";
    static String signatureString = "predict";

    static byte[][][][] readDataFile(String filePath) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(filePath));

        dataInputStream.readInt(); // Magic number
        int imageCount = dataInputStream.readInt();
        int rows = dataInputStream.readInt();
        int cols = dataInputStream.readInt();

        // Data must be structured as a matrix, with each number in a 1-digit array
        byte[][][][] images = new byte[imageCount][rows][cols][1];

        for(int image=0; image<imageCount; image++){
            for(int row=0; row<rows; row++){
                for(int col=0; col<cols; col++) {
                    images[image][row][col][0] = (byte) dataInputStream.readUnsignedByte();
                }
            }
        }

        dataInputStream.close();

        return images;
    }

    private void sendMnistImageBatch(byte[][][][] images, byte[] targets, Constellation constellation, ActivityIdentifier aid, AbstractContext contexts) throws IOException, NoSuitableExecutorException {
        // Generate activity
        MnistCnnActivity activity = new MnistCnnActivity(contexts, true, false, images, targets, aid);

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
        byte[][][][] images = readDataFile(sourceDir + "/t10k-images-idx3-ubyte");
        byte[] targets = Mnist.readLabelFile(sourceDir + "/t10k-labels-idx1-ubyte");
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
