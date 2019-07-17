package nl.zakarias.constellation.edgeinference.models.yolo;

import ibis.constellation.AbstractContext;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.NoSuitableExecutorException;
import nl.zakarias.constellation.edgeinference.models.ModelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Yolo implements ModelInterface {
    private static Logger logger = LoggerFactory.getLogger(Yolo.class);

    static private int PORT = Integer.parseInt(System.getenv("EDGEINFERENCE_SERVING_PORT"));
    static public String modelName = "yolo";
    static public String signatureString = "predict";
    static int batchSize = 1;

    private void runYolo(Constellation constellation, ActivityIdentifier aid, String sourceDir, AbstractContext contexts) throws IOException, NoSuitableExecutorException {
        if (logger.isDebugEnabled()) {
            logger.debug("Checking source path");
        }
        Object[] files = Files.list(Paths.get(sourceDir)).filter(Files::isRegularFile).toArray();

        if (logger.isDebugEnabled()) {
            logger.debug("Starting to submit images in batches of " + batchSize);
        }

        long imageSize = new File(files[0].toString()).length();

        int pos = 0;
        while (pos < files.length){
            byte[][] images = new byte[batchSize][(int)imageSize];
            int min = pos + batchSize < files.length ? pos + batchSize : files.length;
            for (int i=pos; i<min; i++){
                images[i] = Files.readAllBytes(Paths.get(files[i].toString()));
            }

            // Generate activity
            YoloActivity activity = new YoloActivity(contexts, true, false, images, aid);

            // submit activity
            if (logger.isDebugEnabled()) {
                logger.debug("Submitting MnistActivity with contexts " + contexts.toString());
            }
            constellation.submit(activity);

            pos = min;
        }

    }

    @Override
    public void run(Constellation constellation, ActivityIdentifier targetActivityIdentifier, String sourceDir, AbstractContext contexts) throws IOException, NoSuitableExecutorException {
        runYolo(constellation, targetActivityIdentifier, sourceDir, contexts);
    }
}