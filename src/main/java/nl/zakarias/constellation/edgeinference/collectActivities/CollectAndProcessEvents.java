package nl.zakarias.constellation.edgeinference.collectActivities;

import nl.zakarias.constellation.edgeinference.ResultEvent;
import nl.zakarias.constellation.edgeinference.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.AbstractContext;
import ibis.constellation.Activity;
import ibis.constellation.Constellation;
import ibis.constellation.Event;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

public class CollectAndProcessEvents extends Activity {
    private static final Logger logger = LoggerFactory.getLogger(CollectAndProcessEvents.class);

    private static final long serialVersionUID = -538414301465754654L;
    private FileOutputStream fw;

    private int count;

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

    private String getYoloClassificationString(ResultEvent result){
        // TODO FIGURE OUT WHAT WE WANT TO OUTPUT
        StringBuilder str = new StringBuilder();

        for(int i=0; i<result.predictions_yolo.length; i++) {
            str.append("[");
            for (int z = 0; z < result.predictions_yolo[i].length; z++) {
                str.append("[");

                for (int x = 0; x < result.predictions_yolo[i][z].length; x++) {
                    str.append("[");
                    for (int y = 0; y < result.predictions_yolo[i][z][x].length; y++) {
                        str.append(result.predictions_yolo[i][z][x][y]);
                        if (y != result.predictions_yolo[i][z][x].length-1) {
                            str.append(", ");
                        }
                    }
                    if (x == result.predictions_yolo[i][z].length-1) {
                        str.append("]");
                    } else {
                        str.append("],");
                    }
                }

                if (z == result.predictions_yolo[i].length-1) {
                    str.append("]");
                } else {
                    str.append("],");
                }
            }

            if (i == result.predictions_yolo.length-1) {
                str.append("]");
            } else {
                str.append("],");
            }
        }

        return str.toString();
    }

    private void writeOutput(FileOutputStream fw, ResultEvent result) throws IOException {
        LocalDateTime now = LocalDateTime.now();

        // Build prediction string
        StringBuilder predStr = new StringBuilder();

        for (byte prediction : result.predictions) {
            predStr.append(String.format("%s", prediction));
        }

        // Timestamp:src_host:IMG_ID:inference_host:model:prediction
        fw.write(String.format("%s|%s|[", now.toString(), result.src.hostname()).getBytes()); // Timestamp:src_host

        // Add all identifiers
        for(int i=0; i<result.imageIdentifiers.length; i++){
            fw.write(String.format("%d", result.imageIdentifiers[i]).getBytes());
            if(i == result.imageIdentifiers.length-1){
                fw.write("]|".getBytes());
            } else {
                fw.write(",".getBytes());
            }
        }

        fw.write(String.format("%s|%s|", result.host.hostname(), result.modelName).getBytes());
        fw.write(predStr.toString().getBytes());
        fw.write("\n".getBytes());
    }

    private void handleResult(ResultEvent result){
        for(int i = 0; i<result.predictions.length; i++){
            logger.info(String.format("Src %s classified at %s using model %s: %d", result.src.hostname(), result.host.hostname(), result.modelName, result.predictions[i]));

        }

        // Write result to file
        try {
            writeOutput(fw, result);
        } catch (IOException ex) {
            logger.error("Failed to write classification result to file");
        }
    }

    @Override
    public int initialize(Constellation c) {
        logger.debug("\ninitialized\n");

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

    @Override
    public synchronized int process(Constellation c, Event e) {
        // Handle received event
        ResultEvent result = (ResultEvent) e.getData();

        if (logger.isDebugEnabled()) {
            logger.debug("received event number " + count + " from host " + result.host.hostname());
        }

        handleResult(result);

        count++;
        return SUSPEND;
    }

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

    public synchronized void waitToFinish(){
        try {
            wait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}