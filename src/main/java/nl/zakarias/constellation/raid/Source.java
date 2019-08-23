package nl.zakarias.constellation.raid;

import ibis.constellation.*;
import ibis.constellation.impl.ActivityIdentifierImpl;
import ibis.constellation.impl.ConstellationIdentifierImpl;
import nl.zakarias.constellation.raid.models.ModelInterface;
import nl.zakarias.constellation.raid.configuration.Configuration;
import nl.zakarias.constellation.raid.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class Source {
    private static final Logger logger = LoggerFactory.getLogger(Source.class);

    private AbstractContext contexts;
    private CrunchifyGetIPHostname submittedNetworkInfo;
    private boolean done;

    private Timer timer;
    private int timing;

    Source(Context[] contexts) throws UnknownHostException {
        try {
            this.contexts = new OrContext(contexts);
        } catch (IllegalArgumentException e) {
            // Contexts.length < 2
            this.contexts = contexts[0];
        }
        done = false;
    }

    private boolean isDone(){
        return this.done;
    }

    /**
     * Catch SIGINTs to kill this process from the outside, make sure we gracefully exit by notifying all other
     * Constellation agents that we are leaving.
     * @param constellation {@link ibis.constellation.Constellation}
     */
    private void addShutdownHook(Constellation constellation){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            timer.stop(timing);

            logger.info("Shutdown hook leaving Constellation gracefully");
            AtomicInteger done = new AtomicInteger();

            new Thread((new Runnable() {
                AtomicInteger x;
                Constellation constellation1;
                public void run() {
                    constellation.done();
                    x.set(1);
                }
                Runnable pass(AtomicInteger x, Constellation constellation1) {
                    this.x = x;
                    this.constellation1 = constellation1;
                    return this;
                }
            }).pass(done, constellation)).start();

            // Wait for 60 seconds before timeout
            int counter = 0;
            while (done.get() == 0) {
                if (counter > Configuration.SHUTDOWN_HOOK_TIMEOUT) {
                    logger.info("Shutdown hook timeout");
                    break;
                } else if (counter % 10 == 0) {
                    System.out.println("Timeout in: " + (Configuration.SHUTDOWN_HOOK_TIMEOUT - counter) + " seconds");
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter++;
            }
            this.done = true;
        }));
    }

    /**
     * Start the {@link nl.zakarias.constellation.raid.Source}
     * @param constellation The {@link ibis.constellation.Constellation} instance for this process
     * @param target A string matching the {@link ibis.constellation.ActivityIdentifier} identifying the target where
     *               we want to send the result of all classifications
     * @param sourceDir The directory where the source images are located
     * @param modelName The model which we wish to use (must exist on the {@link Predictor} devices which steal
     *                  Activities from this source (matching contexts)
     * @param batchSize The number of images to send in each Activity
     * @param timeInterval Time between sending images (in milliseconds)
     * @param batchCount The number of batches to send in total, before exiting
     * @param endless Whether to keep sending batches forever or to sop after the batchCount has been reached.
     * @throws IOException Thrown if we experience problems reading the images from disc
     * @throws NoSuitableExecutorException Thrown if we experience problems submitting the activity
     */
    void run(Constellation constellation, String target, String sourceDir, Configuration.ModelName modelName, int batchSize, int timeInterval, int batchCount, boolean endless) throws IOException, NoSuitableExecutorException {
        submittedNetworkInfo = new CrunchifyGetIPHostname(constellation.identifier().toString());
        logger.info("\n\nStarting Source("+ submittedNetworkInfo.hostname() +") with contexts: " + this.contexts.toString() + "\n\n");

        addShutdownHook(constellation);

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
        timer = constellation.getTimer("java", constellation.identifier().toString(), "Source using model: " + modelName.toString());
        timing = timer.start();

        model.run(constellation, aid, sourceDir, this.contexts, batchSize, timeInterval, batchCount, endless);

        while (!isDone()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        timer.stop(timing);
    }
}
