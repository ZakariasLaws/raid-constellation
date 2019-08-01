package nl.zakarias.constellation.edgeinference;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

class Predictor {
    private static final Logger logger = LoggerFactory.getLogger(Predictor.class);

    private AbstractContext contexts;
    private CrunchifyGetIPHostname submittedNetworkInfo;

    private boolean done;

    Predictor(Context[] contexts, int nrExecutors) throws UnknownHostException {
        try {
            this.contexts = new OrContext(contexts);
        } catch (IllegalArgumentException e) {
            // Contexts.length < 2
            this.contexts = contexts[0];
        }

        done = false;
        submittedNetworkInfo = new CrunchifyGetIPHostname();
    }

    private boolean isDone(){
        return this.done;
    }

    public void addShutdownHook(Constellation constellation){
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

    void run(Constellation constellation) {
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
