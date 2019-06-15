package nl.zakarias.constellation.edgeinference;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.activites.CollectAndProcessEvents;
import nl.zakarias.constellation.edgeinference.activites.InferenceActivity;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.interfaces.DeviceRoleInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Source implements DeviceRoleInterface {
    public static final Logger logger = LoggerFactory.getLogger(Source.class);

    private AbstractContext contexts;

    Source(Context[] contexts){
        try {
            this.contexts = new OrContext(contexts);
        } catch (IllegalArgumentException e) {
            // Contexts.length < 2
            this.contexts = contexts[0];
        }
    }

    @Override
    public void run(Constellation constellation) {
        System.out.println("\nStarting Source with contexts: " + this.contexts.toString() + "\n");
        ActivityIdentifier aid = null;

        // Create Target activity for collecting classifications
        CollectAndProcessEvents collectActivity = new CollectAndProcessEvents(Configuration.TARGET_CONTEXT);

        // Will be executed on the Target device
        try {
            aid = constellation.submit(collectActivity);
        } catch (NoSuitableExecutorException e) {
            e.printStackTrace();
        }

        for (int i=20; i>=-1; i--) {
            // Read input

            // Generate activity
            InferenceActivity activity = new InferenceActivity(this.contexts, true, false, i, aid);

            // submit activity
            try {
                logger.debug("Submitting InferenceActivity");
                constellation.submit(activity);
                logger.debug("Submitted InferenceActivity");
            } catch (NoSuitableExecutorException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }
}
