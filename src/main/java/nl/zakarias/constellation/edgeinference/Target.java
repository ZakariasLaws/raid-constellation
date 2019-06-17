package nl.zakarias.constellation.edgeinference;

import ibis.constellation.Constellation;
import ibis.constellation.Context;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.interfaces.DeviceRoleInterface;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Target implements DeviceRoleInterface {
    private static final Logger logger = LoggerFactory.getLogger(Target.class);

    private static final Context TARGET_CONTEXT = Configuration.TARGET_CONTEXT;
    private CrunchifyGetIPHostname submittedNetworkInfo;

    Target(){
        submittedNetworkInfo = new CrunchifyGetIPHostname();
    }

    @Override
    public void run(Constellation constellation) {
        logger.info("\n\nStarting Target("+ submittedNetworkInfo.hostname() +") with context: " + TARGET_CONTEXT + "\n\n");
    }
}
