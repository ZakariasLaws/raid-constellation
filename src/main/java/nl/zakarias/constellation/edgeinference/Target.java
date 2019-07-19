package nl.zakarias.constellation.edgeinference;

import ibis.constellation.Constellation;
import ibis.constellation.Context;
import ibis.constellation.NoSuitableExecutorException;
import nl.zakarias.constellation.edgeinference.collectActivities.CollectAndProcessEvents;
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

    void run(Constellation constellation, String outputFile, Configuration.ModelName modelName) throws NoSuitableExecutorException {
        logger.info("\n\nStarting Target("+ submittedNetworkInfo.hostname() +") with context: " + TARGET_CONTEXT + "\n\n");

        CollectAndProcessEvents aid = new CollectAndProcessEvents(Configuration.TARGET_CONTEXT, outputFile);

        logger.debug("Submitting CollectAndProcessEvents activity");
        constellation.submit(aid);

        // Wait until activity is done
        aid.waitToFinish();
    }
}
