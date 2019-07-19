package nl.zakarias.constellation.edgeinference;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.modelServing.API;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;

class Predictor {
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

    void run(Constellation constellation) {
//        try {
//            System.out.println("mnist-cnn " + API.getModelMetadata(Integer.parseInt(System.getenv("EDGEINFERENCE_SERVING_PORT")), "yolo", 1));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        logger.info("\n\nStarting Predictor("+ submittedNetworkInfo.hostname() +") with " + nrExecutors + " and with contexts: " + contexts + "\n\n");
    }
}
