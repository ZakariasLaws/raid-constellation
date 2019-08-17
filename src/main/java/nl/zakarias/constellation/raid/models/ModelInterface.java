package nl.zakarias.constellation.raid.models;

import ibis.constellation.AbstractContext;
import ibis.constellation.ActivityIdentifier;
import ibis.constellation.Constellation;
import ibis.constellation.NoSuitableExecutorException;

import java.io.IOException;

public interface ModelInterface {
    void run(Constellation constellation, ActivityIdentifier targetActivityIdentifier, String sourceDir, AbstractContext contexts, int batchSize, int timeInterval, int batchCount, boolean endless) throws IOException, NoSuitableExecutorException;
}