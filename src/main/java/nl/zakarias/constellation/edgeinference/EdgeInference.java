package nl.zakarias.constellation.edgeinference;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.configuration.Configuration.NODE_ROLES;

import nl.zakarias.constellation.edgeinference.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EdgeInference {

    private static Logger logger = LoggerFactory.getLogger(EdgeInference.class);

    private static void startExecution(Constellation constellation, NODE_ROLES role, Context[] contexts, String targetActivity) throws Exception {
        switch (role){
            case SOURCE:
                Source source = new Source(contexts);
                if (targetActivity == null) {
                    throw new IllegalArgumentException("Missing activity ID to send results to");
                }

                source.run(constellation, targetActivity);
                break;
            case PREDICTOR:
                Predictor predictor = new Predictor(contexts);
                predictor.run(constellation);
                break;
            case TARGET:
                Target target = new Target();
                target.run(constellation);
                break;
            default:
                throw new Error("No matching Java Class found for role: " + role.toString());
        }
    }

    private static String usage(){
        return "Usage: java EdgeInference "
                + "[ -nrExecutors <num> ] "
                + "[ -role <string [" + Configuration.nodeRoleValues() + "]> ";
    }

    public static void main(String[] args) throws Exception {
        NODE_ROLES role = null;
        String[] contextString = new String[0];
        int nrExecutors = 1;
        Context[] contexts;
        String targetActivity = null;

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
                default:
                    throw new Error("Invalid argument: " + args[i] + " " + usage());
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

        try {
            startExecution(constellation, role, contexts, targetActivity);
        } catch (IllegalArgumentException e) {
            throw new Error(e.getMessage() + " " + usage());
        }
        logger.debug("calling Constellation.done()");
        constellation.done();
    }
}
