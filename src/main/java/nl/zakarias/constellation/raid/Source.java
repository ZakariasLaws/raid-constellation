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

class Source {
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
