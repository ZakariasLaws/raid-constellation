package nl.zakarias.constellation.edgeinference;

import ibis.constellation.Constellation;
import ibis.constellation.Context;
import ibis.constellation.NoSuitableExecutorException;
import nl.zakarias.constellation.edgeinference.collectActivities.CollectAndProcessEventsNumeric;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

class Target {
    private static final Logger logger = LoggerFactory.getLogger(Target.class);

    private static final Context TARGET_CONTEXT = Configuration.TARGET_CONTEXT;
    private CrunchifyGetIPHostname submittedNetworkInfo;

    Target() throws UnknownHostException {
        submittedNetworkInfo = new CrunchifyGetIPHostname();
    }

    void run(Constellation constellation) throws NoSuitableExecutorException {
        logger.info("\n\nStarting Target("+ submittedNetworkInfo.hostname() +") with context: " + TARGET_CONTEXT + "\n\n");

        CollectAndProcessEventsNumeric activity = new CollectAndProcessEventsNumeric(Configuration.TARGET_CONTEXT);

        logger.debug("Submitting CollectAndProcessEventsNumeric activity");
        constellation.submit(activity);

        // Wait until activity is done
        activity.waitToFinish();
    }
}
