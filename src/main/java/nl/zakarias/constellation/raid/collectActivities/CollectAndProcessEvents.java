package nl.zakarias.constellation.raid.collectActivities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ibis.constellation.*;
import nl.zakarias.constellation.raid.ResultEvent;
import nl.zakarias.constellation.raid.configuration.Configuration;
import nl.zakarias.constellation.raid.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;


/**
 * This Activity is submitted from {@link nl.zakarias.constellation.raid.Target} , used to collect results from predictors and
 * log them to a specified output file (can be specified with the command argument
 * -outputFile <name> when starting up the process.
 *
 * The Activity will run for as long as the Target is running.
 */
public class CollectAndProcessEvents extends Activity {
    private static final Logger logger = LoggerFactory.getLogger(CollectAndProcessEvents.class);

    private static final long serialVersionUID = -538414301465754654L;
    private FileOutputStream fw;

    private int count;

    /**
     * Constructor, setup output stream and initiate activity
     * @param c Context from {@link nl.zakarias.constellation.raid.Target}
     * @param filePath Where to log the output from the classifications
     */
    public CollectAndProcessEvents(AbstractContext c, String filePath){
        super(c, false, true);
        count = 1;

        String path = filePath == null ? Utils.DEFAULT_OUTPUT_FILE : filePath;

        try {
            logger.debug("Writing collected data to " + path);
            fw = new FileOutputStream(path);
        } catch (IOException ex) {
            logger.error("Could not open file " + path + " for writing");
        }
    }

    /**
     * @param result {@link nl.zakarias.constellation.raid.ResultEvent} sent from sent from {@link nl.zakarias.constellation.raid.Predictor}
     * @param index The results can contain multiple results (in the case of batch prediction)
     *              index specifies which result we are currently storing in the JSON array.
     * @return JSON array with predictions
     */
    private JsonArray getPrediction2D(ResultEvent result, int index){
        JsonArray data = new JsonArray();

        for (int z = 0; z < result.predictions_2D[index].length; z++) {
            data.add(result.predictions_2D[index][z]);
        }

        return data;
    }

    /**
     * @param result {@link nl.zakarias.constellation.raid.ResultEvent} sent from sent from {@link nl.zakarias.constellation.raid.Predictor}
     * @param index The results can contain multiple results (in the case of batch prediction)
     *              index specifies which result we are currently storing in the JSON array.
     * @return JSON array with predictions
     */
    private JsonArray getPrediction3D(ResultEvent result, int index){
        JsonArray data = new JsonArray();

        for (int z = 0; z < result.predictions_3D[index].length; z++) {
            JsonArray arr = new JsonArray();
            for (int x = 0; x < result.predictions_3D[index][z].length; x++) {
                arr.add(result.predictions_3D[index][z][x]);
            }
            data.add(arr);
        }

        return data;
    }

    /**
     * @param result {@link nl.zakarias.constellation.raid.ResultEvent} sent from sent from {@link nl.zakarias.constellation.raid.Predictor}
     * @param index The results can contain multiple results (in the case of batch prediction)
     *              index specifies which result we are currently storing in the JSON array.
     * @return JSON array with predictions
     */
    private JsonArray getPrediction4D(ResultEvent result, int index){
        JsonArray data = new JsonArray();

        for (int z = 0; z < result.predictions_4D[index].length; z++) {
            JsonArray arr = new JsonArray();
            for (int x = 0; x < result.predictions_4D[index][z].length; x++) {
                JsonArray arr2 = new JsonArray();
                for (int y = 0; y < result.predictions_4D[index][z][x].length; y++) {
                    arr2.add(result.predictions_4D[index][z][x][y]);
                }
                arr.add(arr2);
            }
            data.add(arr);
        }

        return data;
    }

    /**
     * Write the output of the predictions in the log file, specified with command line argument
     * -outputFile <name> when starting up the Target
     * @param fw A stream for writing to the output file
     * @param result {@link nl.zakarias.constellation.raid.ResultEvent} sent from {@link nl.zakarias.constellation.raid.Predictor}
     * @throws IOException
     */
    private void writeOutput(FileOutputStream fw, ResultEvent result) throws IOException {
        LocalDateTime now = LocalDateTime.now();

        JsonObject json = new JsonObject();
        json.addProperty ("timestamp", now.toString());
        json.addProperty("src", result.src.uniqueHostname());
        json.addProperty("model", result.modelName.toString());
        json.addProperty("prediction_location", result.host.uniqueHostname());

        JsonArray predictions = new JsonArray();
        for(int i = 0; i < result.imageIdentifiers.length; i++) {
            System.out.println(String.format("Src %s classified at %s using model %s", result.src.uniqueHostname(), result.host.uniqueHostname(), result.modelName));

            JsonObject item = new JsonObject();
            item.addProperty("image_id", result.imageIdentifiers[i]);
            if (result.predictions_1D != null) {
                item.addProperty("prediction", result.predictions_1D[i]);
            } else if (result.predictions_2D != null) {
                item.add("prediction", getPrediction2D(result, i));
            } else if (result.predictions_3D != null) {
                item.add("prediction", getPrediction3D(result, i));
            } else { // 4D
                item.add("prediction", getPrediction4D(result, i));
            }

            if (result.certainty != null){
                item.addProperty("certainty", result.certainty[i]);
            } else {
                item.addProperty("certainty", "");
            }

            if (result.correct != null){
                item.addProperty("target_label", result.correct[i]);
            } else {
                item.addProperty("target_label", "");
            }
            predictions.add(item);
        }
        json.addProperty("batch_size", result.imageIdentifiers.length);

        if (Configuration.LOG_PREDICTIONS) {
            json.add("predictions", predictions);
        } else {
            json.add("predictions", new JsonArray());
        }

        fw.write(json.toString().getBytes());
        fw.write("\n".getBytes());
    }

    /**
     * Write result to file
     * @param result {@link nl.zakarias.constellation.raid.ResultEvent}
     */
    private void handleResult(ResultEvent result){
        // Write result to file
        try {
            writeOutput(fw, result);
        } catch (IOException ex) {
            logger.error("Failed to write classification result to file");
        }
    }

    /**
     * Overrides initialize method from {@link ibis.constellation.Activity}, setup Activity and immediately suspend
     * @param c {@link ibis.constellation.Constellation}
     * @return SUSPEND
     */
    @Override
    public int initialize(Constellation c) {
        logger.debug("initialized\n");

        String targetIdentifier = "";
        String[] identifier = identifier().toString().split(":");
        targetIdentifier += identifier[1] + ":";
        targetIdentifier += identifier[2] + ":";
        targetIdentifier += identifier[3];

        logger.info("In order to target this activity with classifications add the following as argument " +
                "(exactly as printed) when initializing the new SOURCE: \"" + targetIdentifier + "\"\n\n");

        // Immediately start waiting for events to process
        return SUSPEND;
    }

    /**
     * Overrides process method from {@link ibis.constellation.Activity}, collect a result from predictor and
     * log it to a output file, specified in -outputFile <name> when starting up the {@link nl.zakarias.constellation.raid.Target}
     *
     * This method will always return suspend, hence the only way to stop this executor is by force stopping the
     * {@link nl.zakarias.constellation.raid.Target} which started it.
     * @param c {@link ibis.constellation.Constellation}
     * @return SUSPEND
     */
    @Override
    public synchronized int process(Constellation c, Event e) {
        Timer timer = c.getTimer("java", c.identifier().toString(), "Process Result");
        int timing = timer.start();

        // Handle received event
        ResultEvent result = (ResultEvent) e.getData();

        if (logger.isDebugEnabled()) {
            logger.debug("received event number " + count + " from host " + result.host.hostname());
        }

        handleResult(result);

        count++;

        timer.stop(timing);

        return SUSPEND;
    }

    /**
     * Overrides cleanup method from {@link ibis.constellation.Activity}, closes the file output stream
     * @param c {@link ibis.constellation.Constellation}
     */
    @Override
    public void cleanup(Constellation c) {
        // empty
        try {
            fw.close();
        } catch (IOException e) {
            logger.error("Could not close filewriter");
        }
    }

    @Override
    public String toString() {
        return "CollectAndProcessEventsYolo(" + identifier() + ")";
    }

    /**
     * Call from parent process, waits until notify() is sent from somewhere
     */
    public synchronized void wakeOnEvent(){
        try {
            wait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}