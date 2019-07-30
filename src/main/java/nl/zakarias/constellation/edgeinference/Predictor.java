package nl.zakarias.constellation.edgeinference;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

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

    public void done(){
        done = true;
    }

    void run(Constellation constellation) {
        System.out.println(constellation);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown hook leaving Constellation gracefully");
                constellation.done();
            }
        ));

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
