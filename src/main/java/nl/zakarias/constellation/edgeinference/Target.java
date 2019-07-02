package nl.zakarias.constellation.edgeinference;

import ibis.constellation.Constellation;
import ibis.constellation.Context;
import ibis.constellation.NoSuitableExecutorException;
import nl.zakarias.constellation.edgeinference.activites.CollectAndProcessEvents;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;

public class Target {
    private static final Logger logger = LoggerFactory.getLogger(Target.class);

    private static final Context TARGET_CONTEXT = Configuration.TARGET_CONTEXT;
    private CrunchifyGetIPHostname submittedNetworkInfo;

    Target() throws UnknownHostException {
        submittedNetworkInfo = new CrunchifyGetIPHostname();
    }

    public void run(Constellation constellation, String sourceDir) throws NoSuitableExecutorException, IOException {
        logger.info("\n\nStarting Target("+ submittedNetworkInfo.hostname() +") with context: " + TARGET_CONTEXT + "\n\n");

        // Will be executed on the Target device
        logger.debug("Submitting CollectAndProcessEvents");
        CollectAndProcessEvents activity = new CollectAndProcessEvents(Configuration.TARGET_CONTEXT, sourceDir);

        logger.debug("Submitting CollectAndProcessEvents activity");
        constellation.submit(activity);

        // Wait until activity is done
        activity.waitToFinish();
    }
}
