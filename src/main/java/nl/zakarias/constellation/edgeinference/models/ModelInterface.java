package nl.zakarias.constellation.edgeinference.models;

import nl.zakarias.constellation.edgeinference.ResultEvent;

import java.io.Serializable;

public interface ModelInterface {
    enum InferenceModel implements Serializable {
        INCEPTION,
        MNIST_CNN,
    }

    ResultEvent runClassification(byte[] data) throws Exception;
}
