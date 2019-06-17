package nl.zakarias.constellation.edgeinference.activites;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.ImageData;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class InferenceActivity extends Activity {
    private static Logger logger = LoggerFactory.getLogger(InferenceActivity.class);

    private int data;
    private ActivityIdentifier targetIdentifier;

    private CrunchifyGetIPHostname submittedNetworkInfo;
    private CrunchifyGetIPHostname currentNetworkInfo;


    public InferenceActivity(AbstractContext context, boolean mayBeStolen, boolean expectsEvents, int data, ActivityIdentifier aid) throws UnknownHostException {
        super(context, mayBeStolen, expectsEvents);

        this.data = data;
        targetIdentifier = aid;
        submittedNetworkInfo = new CrunchifyGetIPHostname();
    }

    @Override
    public int initialize(Constellation constellation) {
        // Get the location of where we are currently executing
        try {
            currentNetworkInfo = new CrunchifyGetIPHostname();
        } catch (UnknownHostException e) {
            logger.error("InferenceActivity: Could not find host information");
            e.printStackTrace();
        }
        logger.debug("InferenceActivity: Executing on host: " + currentNetworkInfo.hostname());


        logger.debug("InferenceActivity: Performing inference...");
        data = data + data;

        return FINISH;
    }

    @Override
    public int process(Constellation constellation, Event event) {
        return FINISH;
    }

    @Override
    public void cleanup(Constellation constellation) {
        logger.debug("InferenceActivity: Sending results to target");

        ImageData classification = new ImageData(data);
        constellation.send(new Event(identifier(), targetIdentifier, classification));
    }
}
