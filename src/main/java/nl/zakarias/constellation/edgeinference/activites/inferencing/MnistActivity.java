package nl.zakarias.constellation.edgeinference.activites.inferencing;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.ResultEvent;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.models.MnistCnn;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class MnistActivity extends Activity {
    private static Logger logger = LoggerFactory.getLogger(MnistActivity.class);

    private byte[][] data;
    private byte[] correctLabels; // Possible targets, should be null if labels are unknown

    private ResultEvent result;
    private ActivityIdentifier targetIdentifier;

    private CrunchifyGetIPHostname submittedNetworkInfo;
    private CrunchifyGetIPHostname currentNetworkInfo;
    private Configuration.ModelName model;


    public MnistActivity(AbstractContext context, boolean mayBeStolen, boolean expectsEvents, byte[][] data, byte[] correctLabels, ActivityIdentifier aid) throws UnknownHostException {
        super(context, mayBeStolen, expectsEvents);

        this.data = data;
        this.correctLabels = correctLabels;
        targetIdentifier = aid;
        submittedNetworkInfo = new CrunchifyGetIPHostname();
        result = null;
    }

    @Override
    public int initialize(Constellation constellation) {
        // Get the location of where we are currently executing
        try {
            currentNetworkInfo = new CrunchifyGetIPHostname();
        } catch (UnknownHostException e) {
            logger.error("MnistActivity: Could not find host information");
            e.printStackTrace();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("MnistActivity: Executing on host: " + currentNetworkInfo.hostname());
        }
        try {
            this.result = MnistCnn.classify(this.data, "mnist", 1, this.correctLabels);
        } catch (Exception e) {
            throw new Error(String.format("MnistActivity: Error applying model with message: %s", e.getMessage()));
        }

        return FINISH;
    }

    @Override
    public int process(Constellation constellation, Event event) {
        return FINISH;
    }

    @Override
    public void cleanup(Constellation constellation) {
        if (logger.isDebugEnabled()){
            logger.debug("MnistActivity: Sending results to target");
        }

        if (this.result == null) {
            // Something went wrong during predictions
            logger.error("No predictions result transmitted to target " + targetIdentifier + ", result from predictions is null. Check that predictions executed correctly.");
        } else {
            constellation.send(new Event(identifier(), targetIdentifier, this.result));
        }
    }
}
