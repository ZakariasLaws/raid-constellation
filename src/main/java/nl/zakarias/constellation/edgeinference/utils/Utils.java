package nl.zakarias.constellation.edgeinference.utils;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
    public static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String printArray(String[] contexts){
        StringBuilder result = new StringBuilder();

        for (String context : contexts) {
            result.append(context).append(" ");
        }

        return result.toString();
    }

    public static String printArray(Context[] contexts){
        StringBuilder result = new StringBuilder();

        for (Context context : contexts) {
            result.append(context.toString()).append(" ");
        }

        return result.toString();
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
                    return new ConstellationConfiguration(contexts[0], StealPool.WORLD, StealPool.WORLD, StealStrategy.SMALLEST, StealStrategy.BIGGEST, StealStrategy.BIGGEST);
                }
                return new ConstellationConfiguration(new OrContext(contexts), StealPool.WORLD, StealPool.WORLD, StealStrategy.SMALLEST, StealStrategy.BIGGEST, StealStrategy.BIGGEST);
            case TARGET:
                // Configuration steals from WORLD using context TARGET_CONTEXT, it's own activities may not be stolen
                return new ConstellationConfiguration(Configuration.TARGET_CONTEXT, StealPool.NONE, StealPool.NONE, StealStrategy.SMALLEST, StealStrategy.BIGGEST, StealStrategy.BIGGEST);
            default:
                throw new Error("Invalid node role");
        }
    }
}
