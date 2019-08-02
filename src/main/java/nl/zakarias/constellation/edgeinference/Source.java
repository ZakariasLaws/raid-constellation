package nl.zakarias.constellation.edgeinference;

import ibis.constellation.*;
import ibis.constellation.impl.ActivityIdentifierImpl;
import ibis.constellation.impl.ConstellationIdentifierImpl;
import nl.zakarias.constellation.edgeinference.models.ModelInterface;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;

class Source {
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

    void run(Constellation constellation, String target, String sourceDir, Configuration.ModelName modelName, int batchSize) throws IOException, NoSuitableExecutorException {
        logger.info("\n\nStarting Source("+ submittedNetworkInfo.hostname() +") with contexts: " + this.contexts.toString() + "\n\n");

        // Use existing collectActivity
        // This is a "hackish" way of mimicking the activityID generated when submitting
        // the CollectAndProcessEvents activity. Perhaps Constellation should add some type of support
        // for targeting running activities when dynamically adding new nodes.
        String[] targetIdentifier = target.split(":");
        ActivityIdentifier aid = ActivityIdentifierImpl.createActivityIdentifier(new ConstellationIdentifierImpl(Integer.parseInt(targetIdentifier[0]), Integer.parseInt(targetIdentifier[1])), Integer.parseInt(targetIdentifier[2]), false);

        // Could be for example MNIST or YOLO
        ModelInterface model = Configuration.getModel(modelName);
        if (model == null){
            logger.error("Could not identify a valid model, options are: " + Configuration.InferenceModelEnumToString());
            return;
        }
        Timer timer = constellation.getTimer("java", constellation.identifier().toString(), "Source using model: " + modelName.toString());
        int timing = timer.start();

        model.run(constellation, aid, sourceDir, this.contexts, batchSize);

        timer.stop(timing);
    }
}
