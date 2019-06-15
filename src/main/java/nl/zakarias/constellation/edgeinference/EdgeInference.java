package nl.zakarias.constellation.edgeinference;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.configuration.Configuration.NODE_ROLES;

import nl.zakarias.constellation.edgeinference.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EdgeInference {

    private static Logger logger = LoggerFactory.getLogger(EdgeInference.class);

    public static void main(String[] args) throws Exception {
        NODE_ROLES role = null;
        String[] contextString = new String[0];
        int nrExecutors = 1;
        Context[] contexts;

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
                default:
                    throw new Error("Invalid argument: " + args[i] + " "
                            + "Usage: java EdgeInference "
                            + "[ -nrExecutors <num> ] "
                            + "[ -role <string [" + Configuration.nodeRoleValues() + "]> ");
            }
        }

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

        // Start the correct execution depending on the role of the device executing
        DeviceRoleFactory startExecution = new DeviceRoleFactory();
        startExecution.startDevice(role, contexts).run(constellation);

        logger.debug("calling Constellation.done()");
        constellation.done();
    }
}
