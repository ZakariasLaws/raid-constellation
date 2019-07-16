package nl.zakarias.constellation.edgeinference.models.mnist;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.models.ModelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class Mnist implements ModelInterface {
    private static Logger logger = LoggerFactory.getLogger(Mnist.class);

    static String modelName = "mnist";
    static String signatureString = "predict";

    static byte[][] readDataFile(String filePath) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(filePath));

        dataInputStream.readInt(); // Magic number
        int imageCount = dataInputStream.readInt();
        int rows = dataInputStream.readInt();
        int cols = dataInputStream.readInt();

        byte[][] images = new byte[imageCount][rows*cols];

        for(int i=0; i<imageCount; i++){
            for(int x=0; x<rows*cols; x++){
               images[i][x] = (byte) dataInputStream.readUnsignedByte();
            }
        }

        dataInputStream.close();

        return images;
    }

    static byte[] readLabelFile(String filePath) throws IOException {
        DataInputStream labelInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));

        labelInputStream.readInt(); // Magic number
        int labelCount = labelInputStream.readInt();

        byte[] labels = new byte[labelCount];

        for(int i=0; i<labelCount; i++){
            labels[i] = (byte) labelInputStream.readUnsignedByte();
        }

        return labels;
    }

    private void sendMnistImageBatch(byte[][] images, byte[] targets, Constellation constellation, ActivityIdentifier aid, AbstractContext contexts) throws IOException, NoSuitableExecutorException {
        // Generate activity
        MnistActivity activity = new MnistActivity(contexts, true, false, images, targets, aid);

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
        byte[][] images = readDataFile(sourceDir + "/t10k-images-idx3-ubyte");
        byte[] targets = readLabelFile(sourceDir + "/t10k-labels-idx1-ubyte");
        if (logger.isDebugEnabled()) {
            logger.debug("Done importing images");
        }

        // TODO implement batch size setting, currently sending images one and one
        for (int i=0; i<images.length; i++) {
            sendMnistImageBatch(new byte[][]{images[i]}, new byte[] {targets[i]}, constellation, target, contexts);
        }
    }

    @Override
    public void run(Constellation constellation, ActivityIdentifier targetActivityIdentifier, String sourceDir, AbstractContext contexts) throws IOException, NoSuitableExecutorException {
        runMnist(constellation, targetActivityIdentifier, sourceDir, contexts);
    }
}
