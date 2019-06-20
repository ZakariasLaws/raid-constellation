package nl.zakarias.constellation.edgeinference.models;

import nl.zakarias.constellation.edgeinference.ResultEvent;

public interface ModelInterface {
    ResultEvent runClassification(byte[] data) throws Exception;
}
