package nl.zakarias.constellation.edgeinference.models;

import nl.zakarias.constellation.edgeinference.ResultEvent;

import java.io.Serializable;

public interface ModelInterface {
    enum InferenceModel implements Serializable {
        MNIST,
    }

    ResultEvent runClassification(byte[][] data) throws Exception;
}
