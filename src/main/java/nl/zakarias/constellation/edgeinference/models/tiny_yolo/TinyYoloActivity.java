package nl.zakarias.constellation.edgeinference.models.tiny_yolo;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.ResultEvent;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class TinyYoloActivity extends Activity {
    private static Logger logger = LoggerFactory.getLogger(TinyYoloActivity.class);

    private byte[][][][] data;

    private ResultEvent result;
    private ActivityIdentifier targetIdentifier;
    private int[] imageIdentifiers;

    private CrunchifyGetIPHostname currentNetworkInfo;
    private CrunchifyGetIPHostname sourceNetworkInfo;


    TinyYoloActivity(String constellationID, AbstractContext context, boolean mayBeStolen, boolean expectsEvents, byte[][][][] data, ActivityIdentifier aid, int[] imageIdentifiers) throws UnknownHostException {
        super(context, mayBeStolen, expectsEvents);

        this.data = data;
        targetIdentifier = aid;
        result = null;
        this.imageIdentifiers = imageIdentifiers;
        this.sourceNetworkInfo = new CrunchifyGetIPHostname(constellationID);
    }

    @Override
    public int initialize(Constellation constellation) {
        Timer timer = constellation.getTimer("java", constellation.identifier().toString(), "Tiny_Yolo");
        int timing = timer.start();

        // Get the location of where we are currently executing
        try {
            currentNetworkInfo = new CrunchifyGetIPHostname(constellation.identifier().toString());
        } catch (UnknownHostException e) {
            logger.error("Could not find host information");
            e.printStackTrace();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Executing on host: " + currentNetworkInfo.hostname());
        }
        try {
            this.result = TinyYoloClassifier.classify(this.data, 1, null);
        } catch (Exception e) {
            throw new Error(String.format("Error applying model with message: %s", e.getMessage()));
        }

        timer.stop(timing);

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
            // Set network src and host
            this.result.host = this.currentNetworkInfo;
            this.result.src = this.sourceNetworkInfo;
            this.result.imageIdentifiers = this.imageIdentifiers;
            constellation.send(new Event(identifier(), targetIdentifier, this.result));
        }
    }
}
