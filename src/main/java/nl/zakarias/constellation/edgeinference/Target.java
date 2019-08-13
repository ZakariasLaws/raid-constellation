package nl.zakarias.constellation.edgeinference;

import ibis.constellation.Constellation;
import ibis.constellation.Context;
import ibis.constellation.NoSuitableExecutorException;
import ibis.constellation.Timer;
import nl.zakarias.constellation.edgeinference.collectActivities.CollectAndProcessEvents;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

class Target {
    private static final Logger logger = LoggerFactory.getLogger(Target.class);

    private static final Context TARGET_CONTEXT = Configuration.TARGET_CONTEXT;

    private boolean done = false;

    private Timer timer;
    private int timing;

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

    void run(Constellation constellation, String outputFile, Configuration.ModelName modelName) throws NoSuitableExecutorException, UnknownHostException {
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
