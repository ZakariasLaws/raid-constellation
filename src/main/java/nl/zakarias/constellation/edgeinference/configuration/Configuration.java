package nl.zakarias.constellation.edgeinference.configuration;

import ibis.constellation.*;

public class Configuration {
    public enum NODE_ROLES {
        SOURCE, TARGET, PREDICTOR
    }

    public static final Context TARGET_CONTEXT = new Context("target");
    public static final StealPool RESULT_POOL = new StealPool("result_pool");
    public static final StealPool SOURCE_POOL = new StealPool("source_pool");

    public static String nodeRoleValues(){
        StringBuilder result = new StringBuilder();

        for (NODE_ROLES node : NODE_ROLES.values()){
            result.append(node.toString()).append(" ");
        }

        return result.toString();
    }
}
