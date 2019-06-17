package nl.zakarias.constellation.edgeinference;

import ibis.constellation.*;
import ibis.constellation.impl.ActivityIdentifierImpl;
import ibis.constellation.impl.ConstellationIdentifierImpl;
import nl.zakarias.constellation.edgeinference.activites.InferenceActivity;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

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

    public void run(Constellation constellation, String target) throws NoSuitableExecutorException, UnknownHostException {
        logger.info("\n\nStarting Source("+ submittedNetworkInfo.hostname() +") with contexts: " + this.contexts.toString() + "\n\n");

        // Use existing collectActivity
        // This is a "hackish" way of mimicking the activityID generated when submitting
        // the CollectAndProcessEvents activity. Perhaps Constellation should add some type of support
        // for targeting running activities when dynamically adding new nodes.

        String[] targetIdentifier = target.split(":");
        ActivityIdentifier aid = ActivityIdentifierImpl.createActivityIdentifier(new ConstellationIdentifierImpl(Integer.parseInt(targetIdentifier[0]), Integer.parseInt(targetIdentifier[1])), Integer.parseInt(targetIdentifier[2]), false);

        for (int i=100; i>=-1; i--) {
            // Read input

            // Generate activity
            InferenceActivity activity = new InferenceActivity(this.contexts, true, false, i, aid);

            // submit activity
            logger.debug("Submitting InferenceActivity");
            constellation.submit(activity);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
