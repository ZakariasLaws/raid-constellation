package nl.zakarias.constellation.raid;

import ibis.constellation.Constellation;
import ibis.constellation.Context;
import ibis.constellation.NoSuitableExecutorException;
import ibis.constellation.Timer;
import nl.zakarias.constellation.raid.collectActivities.CollectAndProcessEvents;
import nl.zakarias.constellation.raid.configuration.Configuration;
import nl.zakarias.constellation.raid.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@link nl.zakarias.constellation.raid.Target} collects all results from all running
 * {@link nl.zakarias.constellation.raid.Predictor} and handles them appropriately (i.e. by writing them to a file).
 */
public class Target {
    private static final Logger logger = LoggerFactory.getLogger(Target.class);

    private static final Context TARGET_CONTEXT = Configuration.TARGET_CONTEXT;

    private boolean done = false;

    private Timer timer;
    private int timing;

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
     * Start the {@link nl.zakarias.constellation.raid.Target}
     *
     * @param constellation The {@link ibis.constellation.Constellation} instance for this process
     * @param outputFile Where to write results gathered from all {@link nl.zakarias.constellation.raid.Target}
     * @throws NoSuitableExecutorException Error within Constellation when starting up executors
     * @throws UnknownHostException Could not retrieve Host information from OS (such as hostname)
     */
    void run(Constellation constellation, String outputFile) throws NoSuitableExecutorException, UnknownHostException {
        CrunchifyGetIPHostname submittedNetworkInfo = new CrunchifyGetIPHostname(constellation.identifier().toString());
        addShutdownHook(constellation);

        logger.info("\n\nStarting Target("+ submittedNetworkInfo.hostname() +") with context: " + TARGET_CONTEXT + "\n\n");

        CollectAndProcessEvents aid = new CollectAndProcessEvents(Configuration.TARGET_CONTEXT, outputFile);

        timer = constellation.getOverallTimer();
        timing = timer.start();

        logger.debug("Submitting CollectAndProcessEvents activity");
        constellation.submit(aid);

        // Wait until shutdown hook has run
        aid.wakeOnEvent();

        timer.stop(timing);
    }
}
