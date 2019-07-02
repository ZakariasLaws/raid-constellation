package nl.zakarias.constellation.edgeinference.activites;

import nl.zakarias.constellation.edgeinference.ResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.AbstractContext;
import ibis.constellation.Activity;
import ibis.constellation.Constellation;
import ibis.constellation.Event;

public class CollectAndProcessEvents extends Activity {
    private static final Logger logger = LoggerFactory.getLogger(CollectAndProcessEvents.class);

    private static final long serialVersionUID = -538414301465754654L;

    private int count;

    public CollectAndProcessEvents(AbstractContext c){
        super(c, false, true);
        count = 1;
    }

    @Override
    public int initialize(Constellation c) {
        logger.debug("\nCollectAndProcessEvents: initialized\n");

        String targetIdentifier = "";
        String[] identifier = identifier().toString().split(":");
        targetIdentifier += identifier[1] + ":";
        targetIdentifier += identifier[2] + ":";
        targetIdentifier += identifier[3];

        logger.info("In order to target this activity with classifications add the following as argument " +
                "(exactly as printed) when initializing the new SOURCE: \"" + targetIdentifier + "\"\n\n");

        // Immediately start waiting for events to process
        return SUSPEND;
    }

    @Override
    public synchronized int process(Constellation c, Event e) {
        if (logger.isDebugEnabled()) {
            logger.debug("CollectAndProcessEvents: received event number " + count + " from src id " + e.getSource().toString());
        }
        // Handle received event
        ResultEvent result = (ResultEvent) e.getData();

        // Loop through the batch of classifications and print if they were correct
        for(int i = 0; i<result.predictions.length; i++){
            // Check that we have correct classifications to compare to
            if (result.correct != null){
                if ((int)result.predictions[i] == result.correct[i]) {
                    logger.info(String.format("CollectAndProcessEvent: Correctly classified as %d with certainty %1.2f", result.predictions[i], result.certainty[i]));
                } else {
                    logger.info(String.format("CollectAndProcessEvent: Falsely classified %d as %d with certainty %1.2f", result.correct[i], result.predictions[i], result.certainty[i]));
                }
            }
        }

        count++;
        return SUSPEND;
    }

    @Override
    public void cleanup(Constellation c) {
        // empty
    }

    @Override
    public String toString() {
        return "CollectAndProcessEvents(" + identifier() + ")";
    }

    public synchronized void waitToFinish(){
        try {
            wait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}