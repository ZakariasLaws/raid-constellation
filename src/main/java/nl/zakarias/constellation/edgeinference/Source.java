package nl.zakarias.constellation.edgeinference;

import ibis.constellation.*;
import ibis.constellation.impl.ActivityIdentifierImpl;
import ibis.constellation.impl.ConstellationIdentifierImpl;
import nl.zakarias.constellation.edgeinference.models.ModelInterface;
import nl.zakarias.constellation.edgeinference.models.mnist.Mnist;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.models.yolo.Yolo;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import nl.zakarias.constellation.edgeinference.utils.Utils;
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

    void run(Constellation constellation, String target, String sourceDir, Configuration.ModelName modelName) throws IOException, NoSuitableExecutorException {
        logger.info("\n\nStarting Source("+ submittedNetworkInfo.hostname() +") with contexts: " + this.contexts.toString() + "\n\n");

        // Use existing collectActivity
        // This is a "hackish" way of mimicking the activityID generated when submitting
        // the CollectAndProcessEventsNumeric activity. Perhaps Constellation should add some type of support
        // for targeting running activities when dynamically adding new nodes.

        String[] targetIdentifier = target.split(":");
        ActivityIdentifier aid = ActivityIdentifierImpl.createActivityIdentifier(new ConstellationIdentifierImpl(Integer.parseInt(targetIdentifier[0]), Integer.parseInt(targetIdentifier[1])), Integer.parseInt(targetIdentifier[2]), false);


        //TODO Loop through all folders in models/ in a dynamic way instead of using configuration enum

        if (modelName == Configuration.ModelName.MNIST) {
            ModelInterface model = new Mnist();
            model.run(constellation, aid, sourceDir, this.contexts);
        } else if (modelName == Configuration.ModelName.YOLO) {
            ModelInterface model = new Yolo();
            model.run(constellation, aid, sourceDir, this.contexts);
        } else {
            logger.error("Could not identify a valid model, options are: " + Utils.InferenceModelEnumToString());
        }
    }
}
