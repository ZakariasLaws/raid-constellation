package nl.zakarias.constellation.edgeinference.models.mnist_cnn;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.ResultEvent;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class MnistCnnActivity extends Activity {
    private static Logger logger = LoggerFactory.getLogger(MnistCnnActivity.class);

    private byte[][][][] data;
    private byte[] correctLabels; // Possible targets, should be null if labels are unknown

    private ResultEvent result;
    private ActivityIdentifier targetIdentifier;

    private CrunchifyGetIPHostname currentNetworkInfo;


    MnistCnnActivity(AbstractContext context, boolean mayBeStolen, boolean expectsEvents, byte[][][][] data, byte[] correctLabels, ActivityIdentifier aid) {
        super(context, mayBeStolen, expectsEvents);

        this.data = data;
        this.correctLabels = correctLabels;
        targetIdentifier = aid;
        result = null;
    }

    @Override
    public int initialize(Constellation constellation) {
        // Get the location of where we are currently executing
        try {
            currentNetworkInfo = new CrunchifyGetIPHostname();
        } catch (UnknownHostException e) {
            logger.error("Could not find host information");
            e.printStackTrace();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Executing on host: " + currentNetworkInfo.hostname());
        }
        try {
            this.result = MnistCnnClassifier.classify(this.data, 1, this.correctLabels, currentNetworkInfo);
        } catch (Exception e) {
            throw new Error(String.format("Error applying model with message: %s", e.getMessage()));
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
            logger.debug("Sending results to target");
        }

        if (this.result == null) {
            // Something went wrong during predictions
            logger.error("No predictions result transmitted to target " + targetIdentifier + ", result from predictions is null. Check that predictions executed correctly.");
        } else {
            constellation.send(new Event(identifier(), targetIdentifier, this.result));
        }
    }
}
