package nl.zakarias.constellation.edgeinference.activites;

import nl.zakarias.constellation.edgeinference.ImageData;
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

    public CollectAndProcessEvents(AbstractContext c) {
        super(c, true, true);
        count = 1;
    }

    @Override
    public int initialize(Constellation c) {
        logger.debug("\nCollectAndProcessEvents: initialized\n");

        // Immediately start waiting for events to process
        return SUSPEND;
    }

    @Override
    public synchronized int process(Constellation c, Event e) {
        logger.debug("CollectAndProcessEvents: received event number " + count + " from src id " + e.getSource().toString());
        // Handle received event

        if (((ImageData)e.getData()).data < 0){
            logger.debug("CollectAndProcessEvent: event contains " + ((ImageData)e.getData()).data + ", stop execution");
            return FINISH;
        }


        logger.debug("CollectAndProcessEvent: Contains " + ((ImageData)e.getData()).data);

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
}