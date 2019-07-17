package nl.zakarias.constellation.edgeinference.collectActivities;

import ibis.constellation.AbstractContext;
import ibis.constellation.Activity;
import ibis.constellation.Constellation;
import ibis.constellation.Event;
import nl.zakarias.constellation.edgeinference.ResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectAndProcessEventsYolo extends Activity {
    private static final Logger logger = LoggerFactory.getLogger(CollectAndProcessEventsYolo.class);

    private static final long serialVersionUID = -538414301465754654L;

    private int count;

    public CollectAndProcessEventsYolo(AbstractContext c){
        super(c, false, true);
        count = 1;
    }

    @Override
    public int initialize(Constellation c) {
        logger.debug("\ninitialized\n");

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
        // Handle received event
        ResultEvent result = (ResultEvent) e.getData();

        if (logger.isDebugEnabled()) {
            logger.debug("received event number " + count + " from host " + result.host.hostname());
        }

        // Loop through the batch of classifications
        for(int i = 0; i<result.predictions.length; i++){
            // TODO Do something yolo specific

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
        return "CollectAndProcessEventsNumeric(" + identifier() + ")";
    }

    public synchronized void waitToFinish(){
        try {
            wait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}