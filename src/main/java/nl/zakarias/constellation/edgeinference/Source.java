package nl.zakarias.constellation.edgeinference;

import ibis.constellation.*;
import ibis.constellation.impl.ActivityIdentifierImpl;
import ibis.constellation.impl.ConstellationIdentifierImpl;
import nl.zakarias.constellation.edgeinference.activites.InferenceActivity;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.models.MnistFileParser;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;

public class Source {
    private static final Logger logger = LoggerFactory.getLogger(Source.class);

    private AbstractContext contexts;
    private CrunchifyGetIPHostname submittedNetworkInfo;

    Source(Context[] contexts) throws UnknownHostException {
        try {
            this.contexts = new OrContext(contexts);
        } catch (IllegalArgumentException e) {
            // Contexts.length < 2
            this.contexts = contexts[0];
        }

        submittedNetworkInfo = new CrunchifyGetIPHostname();
    }

    private void sendMnistImage(byte[][] images, byte[] targets, Constellation constellation, ActivityIdentifier aid, Configuration.ModelName modelName) throws IOException, NoSuitableExecutorException {
        // Generate activity
        InferenceActivity activity = new InferenceActivity(this.contexts, true, false, images, targets, aid, modelName);

        // submit activity
        if (logger.isDebugEnabled()) {
            logger.debug("Submitting InferenceActivity with contexts " + this.contexts.toString());
        }
        constellation.submit(activity);

        if (logger.isDebugEnabled()) {
            logger.debug("----------- Waiting for 333 seconds ---------");
        }
        try {
            Thread.sleep(333);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run(Constellation constellation, String target, String sourceDir, Configuration.ModelName modelName) throws IOException, NoSuitableExecutorException {
        logger.info("\n\nStarting Source("+ submittedNetworkInfo.hostname() +") with contexts: " + this.contexts.toString() + "\n\n");

        // Use existing collectActivity
        // This is a "hackish" way of mimicking the activityID generated when submitting
        // the CollectAndProcessEvents activity. Perhaps Constellation should add some type of support
        // for targeting running activities when dynamically adding new nodes.

        String[] targetIdentifier = target.split(":");
        ActivityIdentifier aid = ActivityIdentifierImpl.createActivityIdentifier(new ConstellationIdentifierImpl(Integer.parseInt(targetIdentifier[0]), Integer.parseInt(targetIdentifier[1])), Integer.parseInt(targetIdentifier[2]), false);

        switch (modelName) {
            case MNIST:
                if (logger.isDebugEnabled()) {
                    logger.debug("Reading MNIST image and label file...");
                }
                byte[][] images = MnistFileParser.readDataFile(sourceDir + "/t10k-images-idx3-ubyte");
                byte[] targets = MnistFileParser.readLabelFile(sourceDir + "/t10k-labels-idx1-ubyte");
                if (logger.isDebugEnabled()) {
                    logger.debug("Done importing images");
                }

                // TODO implement batch size setting, currently sending images one and one
                for (int i=0; i<images.length; i++) {
                    sendMnistImage(new byte[][]{images[i]}, new byte[] {targets[i]}, constellation, aid, modelName);
                }

                break;
        }
    }
}
