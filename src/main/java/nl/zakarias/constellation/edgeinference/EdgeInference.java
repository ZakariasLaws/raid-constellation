package nl.zakarias.constellation.edgeinference;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.configuration.Configuration.NODE_ROLES;
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
                + "[ (target only) -outputFile </path/to/store/output> ] "
                + "[ (source only) -target <activity ID>] "
                + "[ (source only) -dataDir </source/dataset/path>] "
                + "[ (source only) -modelName [" + Configuration.InferenceModelEnumToString() + "] ";
    }

    /***
     * Generate ConstellationConfiguration based on what role the device calling this method
     * has. The configuration will identify if the executors will transmit new data (SOURCE)
     * compute results (PREDICTOR) or receive and display results (TARGET).
     * @param role The role of this node/device, must exist in NODE_ROLES
     * @param contexts An array of contexts to use
     * @return ConstellationConfiguration depending on the role of this instance
     * @throws Error Throws error when no role or an invalid one was provided
     */
    public static ConstellationConfiguration createRoleBasedConfig(Configuration.NODE_ROLES role, Context[] contexts) throws Error{
        logger.debug("Generating a new configuration based on role: " + role.toString() + " with contexts: " + Utils.printArray(contexts));

        if (contexts.length == 0 && !role.equals(Configuration.NODE_ROLES.TARGET)) {
            throw new Error("No context for executors");
        }
        switch (role){
            case SOURCE:
                // Configuration which can produce Activities to WORLD but may not steal from anywhere
                return new ConstellationConfiguration(Context.DEFAULT, StealPool.WORLD, StealPool.NONE, StealStrategy.SMALLEST, StealStrategy.SMALLEST, StealStrategy.SMALLEST);
            case PREDICTOR:
                // Configuration steals from WORLD and submits new Activities to WORLD using configurations from config file
                if (contexts.length == 1){
                    return new ConstellationConfiguration(contexts[0], StealPool.NONE, StealPool.WORLD, StealStrategy.SMALLEST, StealStrategy.BIGGEST, StealStrategy.BIGGEST);
                }
                return new ConstellationConfiguration(new OrContext(contexts), StealPool.NONE, StealPool.WORLD, StealStrategy.SMALLEST, StealStrategy.BIGGEST, StealStrategy.BIGGEST);
            case TARGET:
                return new ConstellationConfiguration(Configuration.TARGET_CONTEXT, StealPool.NONE, StealPool.NONE, StealStrategy.SMALLEST, StealStrategy.BIGGEST, StealStrategy.BIGGEST);
            default:
                throw new Error("Invalid node role");
        }
    }

    static void start(String[] args) throws Exception {
        NODE_ROLES role = null;
        String[] contextString = new String[0];
        int nrExecutors = 1;
        Context[] contexts;
        String targetActivity = null;
        String sourceDataDir = null;
        String outputFile = null;
        Configuration.ModelName modelName = null;

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
                    sourceDataDir = args[i];
                    break;
                case "-outputFile":
                    i++;
                    outputFile = args[i];
                    break;
                case "-modelName":
                    i++;
                    try {
                        modelName = Configuration.ModelName.valueOf(args[i].toUpperCase());
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

        ConstellationConfiguration config = createRoleBasedConfig(role, contexts);
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
                throw new IllegalArgumentException("Missing directory to retrieve predictions images from");
            } if (modelName == null) {
                throw new IllegalArgumentException("Specify the name of the predictions model to use (e.g. inception)");
            }

                source.run(constellation, targetActivity, sourceDataDir, modelName);
                break;
            case PREDICTOR:
                Predictor predictor = new Predictor(contexts, nrExecutors);
                predictor.run(constellation);
                // Constellation.done() is called from inside predictor.run(..)
                return;
            case TARGET:
                Target target = new Target();
                target.run(constellation, outputFile, modelName);
                break;
            default:
                throw new Error("No matching Java Class found for role: " + role.toString());
        }

        logger.debug("calling Constellation.done()");
        constellation.done();
    }

    public static void main(String[] args) {
        try {
            start(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(usage());
        }
    }
}
