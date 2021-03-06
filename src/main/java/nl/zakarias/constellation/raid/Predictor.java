package nl.zakarias.constellation.raid;

import ibis.constellation.*;
import nl.zakarias.constellation.raid.configuration.Configuration;
import nl.zakarias.constellation.raid.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class Predictor {
    private static final Logger logger = LoggerFactory.getLogger(Predictor.class);

    private AbstractContext contexts;
    private CrunchifyGetIPHostname submittedNetworkInfo;

    private boolean done;

    Predictor(Context[] contexts, int nrExecutors) {
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
     * Start the {@link nl.zakarias.constellation.raid.Predictor}
     *
     * @param constellation The {@link ibis.constellation.Constellation} instance for this process
     * @throws UnknownHostException Could not retrieve Host information from OS (such as hostname)
     */
    void run(Constellation constellation)  throws UnknownHostException{
        submittedNetworkInfo = new CrunchifyGetIPHostname(constellation.identifier().toString());

        addShutdownHook(constellation);

        logger.info("\n\nStarting Predictor("+ submittedNetworkInfo.hostname() +") with contexts: " + contexts + "\n\n");

        while (!isDone()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
