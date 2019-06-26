package nl.zakarias.constellation.edgeinference;

import ibis.constellation.*;
import ibis.constellation.impl.ActivityIdentifierImpl;
import ibis.constellation.impl.ConstellationIdentifierImpl;
import nl.zakarias.constellation.edgeinference.activites.InferenceActivity;
import nl.zakarias.constellation.edgeinference.models.ModelInterface;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Source {
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


    private static byte[] readAllBytesOrExit(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    private void sendImage(Path filePath, Constellation constellation, ActivityIdentifier aid) throws IOException, NoSuitableExecutorException {
        byte[] imageBytes = readAllBytesOrExit(filePath);

        // Generate activity
        //            InferenceActivity activity = new InferenceActivity(this.contexts, true, false, imageBytes, aid, ModelInterface.InferenceModel.INCEPTION);
        InferenceActivity activity = new InferenceActivity(this.contexts, true, false, imageBytes, aid, ModelInterface.InferenceModel.MNIST_CNN);

        // submit activity
        logger.debug("Submitting InferenceActivity with contexts " + this.contexts.toString());
        constellation.submit(activity);

        try {
            Thread.sleep(333);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run(Constellation constellation, String target, Path sourceDir) throws IOException {
        logger.info("\n\nStarting Source("+ submittedNetworkInfo.hostname() +") with contexts: " + this.contexts.toString() + "\n\n");

        // Use existing collectActivity
        // This is a "hackish" way of mimicking the activityID generated when submitting
        // the CollectAndProcessEvents activity. Perhaps Constellation should add some type of support
        // for targeting running activities when dynamically adding new nodes.

        String[] targetIdentifier = target.split(":");
        ActivityIdentifier aid = ActivityIdentifierImpl.createActivityIdentifier(new ConstellationIdentifierImpl(Integer.parseInt(targetIdentifier[0]), Integer.parseInt(targetIdentifier[1])), Integer.parseInt(targetIdentifier[2]), false);

        // Go through the source directory and transmit each image
        Files.walk(sourceDir).forEach(filePath -> {
            try {
                sendImage(filePath, constellation, aid);
            } catch (IOException | NoSuitableExecutorException e) {
                throw new Error("Error when sending image: " + e.getMessage());
            }
        });

//        for (int i=0; i<100000; i++) {
//            // Read input
//
//            byte[] imageBytes = readAllBytesOrExit(Paths.get(System.getenv("EDGEINFERENCE_MODEL_DIR/inception") + "images/porcupine.jpg"));
//
//            // Generate activity
////            InferenceActivity activity = new InferenceActivity(this.contexts, true, false, imageBytes, aid, ModelInterface.InferenceModel.INCEPTION);
//            InferenceActivity activity = new InferenceActivity(this.contexts, true, false, imageBytes, aid, ModelInterface.InferenceModel.MNIST_CNN);
//
//            // submit activity
//            logger.debug("Submitting InferenceActivity with contexts " + this.contexts.toString());
//            constellation.submit(activity);
//
//            try {
//                Thread.sleep(333);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
