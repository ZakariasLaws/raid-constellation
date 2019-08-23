package nl.zakarias.constellation.raid.models;

import ibis.constellation.AbstractContext;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.NoSuitableExecutorException;

import java.io.IOException;

/**
 * All models *MUST* implement this interface in order to properly be initiated in the Activities
 */
public interface ModelInterface {
    /**
     * This method should produce the data which needs to be classified, this might vary depending on what model
     * is used. For example see {@link nl.zakarias.constellation.raid.models.yolo.Yolo} and
     * {@link nl.zakarias.constellation.raid.models.mnist.Mnist}
     *
     * @param constellation The {@link ibis.constellation.Constellation} instance initiated by this process
     * @param targetActivityIdentifier {@link ibis.constellation.ActivityIdentifier} matching a {@link nl.zakarias.constellation.raid.Target}
     * @param sourceDir The source directory where all data to be classified is stored (e.g. a directory of images)
     * @param contexts The {@link ibis.constellation.AbstractContext} for this process, provided by the argument
     *                -contexts <list_of_contexts>
     * @param batchSize The number of images to send in each Activity
     * @param timeInterval Time between sending images (in milliseconds)
     * @param batchSize The number of images to send in each Activity
     * @param endless Whether to keep sending batches forever or to sop after the batchCount has been reached.
     * @throws IOException In case we cannot read the data in the sourceDir
     * @throws NoSuitableExecutorException In case something goes wrong when submitting an
     *                                     {@link ibis.constellation.Activity} associated with a one input batch
     *                                     (e.g. a batch of images)
     */
    void run(Constellation constellation, ActivityIdentifier targetActivityIdentifier, String sourceDir, AbstractContext contexts, int batchSize, int timeInterval, int batchCount, boolean endless) throws IOException, NoSuitableExecutorException;
}