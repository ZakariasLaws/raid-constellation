package nl.zakarias.constellation.edgeinference.models;

import nl.zakarias.constellation.edgeinference.ResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.*;
import org.tensorflow.types.UInt8;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class MnistCnn implements ModelInterface {
    private static Logger logger = LoggerFactory.getLogger(MnistCnn.class);

    static private String BASE_DIR = System.getenv("EDGEINFERENCE_MODEL_DIR");


    @Override
    public ResultEvent runClassification(byte[] data) throws Exception {
        ResultEvent result = performClassification(data);
        return result;
    }

    private ResultEvent performClassification(byte[] imageBytes) throws Exception {
        ResultEvent result = null;

        return result;
    }
}
