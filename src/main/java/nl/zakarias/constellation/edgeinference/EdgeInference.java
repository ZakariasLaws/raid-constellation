package nl.zakarias.constellation.edgeinference;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.configuration.Configuration.NODE_ROLES;
import nl.zakarias.constellation.edgeinference.models.ModelInterface;
import nl.zakarias.constellation.edgeinference.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeInference {

    private static Logger logger = LoggerFactory.getLogger(EdgeInference.class);

    private static String usage(){
        return "Usage: java EdgeInference "
                + "[ -nrExecutors <num> ] "
                + "[ -role [" + Configuration.nodeRoleValues() + "] "
                + "[ -context <String,String,String...>] "
                + "[ -target <activity ID>] "
                + "[ -dataDir </source/dataset/path>] "
                + "[ -modelName [" + Utils.InferenceModelEnumToString() + "] ";
    }

    public static void main(String[] args) throws Exception {
        NODE_ROLES role = null;
        String[] contextString = new String[0];
        int nrExecutors = 1;
        Context[] contexts;
        String targetActivity = null;
        String sourceDataDir = null;
        ModelInterface.InferenceModel modelName = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-nrExecutors":
                    i++;
                    nrExecutors = Integer.parseInt(args[i]);
                    break;
                case "-role":
                    i++;
                    try {
                        role = NODE_ROLES.valueOf(args[i]);
                    } catch (IllegalArgumentException e) {
                        throw new Error("Invalid node role: " + args[i]);
                    }
                    break;
                case "-context":
                    i++;
                    contextString = args[i].split(",");
                    break;
                case "-target":
                    i++;
                    targetActivity = args[i];
                    break;
                case "-dataDir":
                    i++;
                    sourceDataDir =args[i];
                    break;
                case "-modelName":
                    i++;
                    try {
                        modelName = ModelInterface.InferenceModel.valueOf(args[i].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new Error("Invalid model name: " + args[i]);
                    }
                    break;
                default:
                    throw new Error("Invalid argument: " + args[i] + "\n\n" + usage());
            }
        }

        // Check if valid context was submitted, create array of Context objects from argument string
        if (role == null){
            throw new Error("Missing node role, must be one of following: " + Configuration.nodeRoleValues());
        } else if (contextString.length == 0 && !role.equals(NODE_ROLES.TARGET)) {
            throw new Exception("No context for executors");
        } else if (role.equals(NODE_ROLES.TARGET)) {
            contexts = new Context[1];
            contexts[0] = Configuration.TARGET_CONTEXT;
        } else {
            contexts = new Context[contextString.length];

            for (int i = 0; i < contextString.length; i++) {
                contexts[i] = new Context(contextString[i]);
            }
        }

        ConstellationConfiguration config = Utils.createRoleBasedConfig(role, contexts);
        Constellation constellation = ConstellationFactory.createConstellation(config, nrExecutors);
        logger.debug("Created Constellation with " + nrExecutors + " executor(s)");

        logger.debug("Calling Constellation.activate()");
        constellation.activate();

        switch (role){
            case SOURCE:
                Source source = new Source(contexts);
                if (targetActivity == null) {
                    throw new IllegalArgumentException("Missing activity ID to send results to");
                } if (sourceDataDir == null) {
                    throw new IllegalArgumentException("Missing directory to retrieve classification images from");
                } if (modelName == null) {
                    throw new IllegalArgumentException("Specify the name of the classification model to use (e.g. inception)");
                }

                source.run(constellation, targetActivity, sourceDataDir, modelName);
                break;
            case PREDICTOR:
                Predictor predictor = new Predictor(contexts, nrExecutors);
                predictor.run(constellation);
                break;
            case TARGET:
                Target target = new Target();

                if (sourceDataDir == null) {
                    throw new IllegalArgumentException("Missing directory to retrieve classification labels from");
                }
                target.run(constellation, sourceDataDir);
                break;
            default:
                throw new Error("No matching Java Class found for role: " + role.toString());
        }

        logger.debug("calling Constellation.done()");
        constellation.done();
    }
}
