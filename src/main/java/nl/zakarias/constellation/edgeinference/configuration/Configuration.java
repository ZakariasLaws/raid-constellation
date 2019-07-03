package nl.zakarias.constellation.edgeinference.configuration;

import ibis.constellation.*;

import java.io.Serializable;

public class Configuration {
    public enum NODE_ROLES {
        SOURCE, TARGET, PREDICTOR
    }

    public enum ModelName implements Serializable {
        MNIST,
    }

    public static final Context TARGET_CONTEXT = new Context("target");

    public static String nodeRoleValues(){
        StringBuilder result = new StringBuilder();

        for (NODE_ROLES node : NODE_ROLES.values()){
            result.append(node.toString()).append(" ");
        }

        return result.toString();
    }
}
