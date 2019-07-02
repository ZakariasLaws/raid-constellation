package nl.zakarias.constellation.edgeinference.activites;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.ResultEvent;
import nl.zakarias.constellation.edgeinference.models.MnistCnn;
import nl.zakarias.constellation.edgeinference.models.ModelInterface;
import nl.zakarias.constellation.edgeinference.models.ModelInterface.InferenceModel;
import nl.zakarias.constellation.edgeinference.utils.CrunchifyGetIPHostname;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class InferenceActivity extends Activity {
    private static Logger logger = LoggerFactory.getLogger(InferenceActivity.class);

    private byte[][] data;
    private byte[] targetIndexes; // Possible targets, should be null if labels are unknown

    private ResultEvent result;
    private ActivityIdentifier targetIdentifier;

    private CrunchifyGetIPHostname submittedNetworkInfo;
    private CrunchifyGetIPHostname currentNetworkInfo;
    private InferenceModel model;


    public InferenceActivity(AbstractContext context, boolean mayBeStolen, boolean expectsEvents, byte[][] data, byte[] targetIndexes, ActivityIdentifier aid, InferenceModel model) throws UnknownHostException {
        super(context, mayBeStolen, expectsEvents);

        this.data = data;
        this.targetIndexes = targetIndexes;
        targetIdentifier = aid;
        submittedNetworkInfo = new CrunchifyGetIPHostname();
        result = null;
        this.model = model;
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

        ModelInterface model;

        // Specify what model to use for classification
        switch (this.model) {
            case MNIST:
                logger.debug("InferenceActivity: Performing inference with Mnist CNN...");
                model = new MnistCnn();
                break;
            default:
                logger.error("InferenceActivity: Invalid model " + this.model.toString());
                throw new IllegalArgumentException("Illegal argument: " + this.model.toString());
        }

        try {
            this.result = model.runClassification(this.data);
            this.result.correct = this.targetIndexes;

        } catch (Exception e) {
            logger.error(String.format("InferenceActivity: Error applying model with message: %s", e.getMessage()));
            e.printStackTrace();
        }

        return FINISH;
    }

    @Override
    public int process(Constellation constellation, Event event) {
        return FINISH;
    }

    @Override
    public void cleanup(Constellation constellation) {
        logger.debug("InferenceActivity: Sending results to target");
        if (this.result == null) {
            // Something went wrong during classification
            logger.error("No classification result transmitted to target " + targetIdentifier + ", result from classification is null. Check that classification executed correctly.");
        } else {
            constellation.send(new Event(identifier(), targetIdentifier, this.result));
        }
    }
}
