package nl.zakarias.constellation.edgeinference;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class Predictor {
    private static final Logger logger = LoggerFactory.getLogger(Predictor.class);

    private AbstractContext contexts;
    private CrunchifyGetIPHostname submittedNetworkInfo;
    private int nrExecutors;

    Predictor(Context[] contexts, int nrExecutors) throws UnknownHostException {
        try {
            this.contexts = new OrContext(contexts);
        } catch (IllegalArgumentException e) {
            // Contexts.length < 2
            this.contexts = contexts[0];
        }

        submittedNetworkInfo = new CrunchifyGetIPHostname();
        this.nrExecutors = nrExecutors;
    }

    public void run(Constellation constellation) {
        logger.info("\n\nStarting Predictor("+ submittedNetworkInfo.hostname() +") with " + nrExecutors + " and with contexts: " + contexts + "\n\n");
    }
}
