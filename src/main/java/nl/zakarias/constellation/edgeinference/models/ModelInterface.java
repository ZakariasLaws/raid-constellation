package nl.zakarias.constellation.edgeinference.models;

import nl.zakarias.constellation.edgeinference.ResultEvent;

public interface ModelInterface {
    enum InferenceModel {
        INCEPTION,
        MNIST_CNN,
    }

    ResultEvent runClassification(byte[] data) throws Exception;
}
