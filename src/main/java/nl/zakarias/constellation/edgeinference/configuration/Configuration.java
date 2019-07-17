package nl.zakarias.constellation.edgeinference.configuration;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.models.ModelInterface;
import nl.zakarias.constellation.edgeinference.models.mnist.Mnist;
import nl.zakarias.constellation.edgeinference.models.mnist_cnn.MnistCnn;
import nl.zakarias.constellation.edgeinference.models.yolo.Yolo;

import java.io.Serializable;

public class Configuration {
    public enum NODE_ROLES {
        SOURCE, TARGET, PREDICTOR
    }

    /**
     * All available models. If manually adding a new model to tensorflow/tensorflow_serving/models/ it must be
     * added to this enum, as well as returned from {@link #getModel(ModelName) getModel}.
     */
    public enum ModelName implements Serializable {
        MNIST, MNIST_CNN, YOLO
    }

    /**
     * Return a new model instance matching the input parameter. If manually adding a new model to
     * tensorflow/tensorflow_serving/models/ it must be returned from this methos as well as added to the
     * {@link ModelName ModelName} enum.
     *
     * @param modelName Name of the model, from command line
     * @return A new model to classify with
     */
    public static ModelInterface getModel(ModelName modelName){
        switch (modelName) {
            case MNIST:
                return new Mnist();
            case MNIST_CNN:
                return new MnistCnn();
            case YOLO:
                return new Yolo();
            default:
                return null;
        }
    }

    public static final Context TARGET_CONTEXT = new Context("target");

    public static String nodeRoleValues(){
        StringBuilder result = new StringBuilder();

        for (NODE_ROLES node : NODE_ROLES.values()){
            result.append(node.toString()).append(" ");
        }

        return result.toString();
    }

    public static String InferenceModelEnumToString(){
        StringBuilder result = new StringBuilder();

        for (ModelName model : ModelName.values()){
            result.append(model.toString()).append(" ");
        }

        return result.toString();
    }

}
