package nl.zakarias.constellation.edgeinference;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibis.constellation.Constellation;
import ibis.constellation.Context;
import ibis.constellation.StealStrategy;
import ibis.constellation.ConstellationConfiguration;
import ibis.constellation.ConstellationFactory;
import ibis.constellation.Timer;

public class EdgeInference {

    private static Logger logger = LoggerFactory.getLogger(EdgeInference.class);

    public static void writeFile(float[] array) {
        try {
            PrintStream out = new PrintStream("edgeinference.out");
            for (float v : array) {
                out.println(v);
            }
            out.close();
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        // This code is executed on every device

//        int nrExecutorsPerNode = 2;
//        int nrNodes = 1;
//
//        // Determine the number of tasks based on the size of the pool of nodes
//        String ibisPoolSize = System.getProperty("ibis.pool.size");
//        if (ibisPoolSize != null) {
//            nrNodes = Integer.parseInt(ibisPoolSize);
//        }

        String role = "";
        int nrExecutors = 1;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-nrExecutors")) {
                i++;
                nrExecutors = Integer.parseInt(args[i]);
            } else if (args[i].equals("-role")) {
                i++;
                role = args[i];
            }  else {
                throw new Error("Invalid argument: " + args[i] + " "
                        + "Usage: java EdgeInference "
                        + "[ -nrExecutors <num> ] "
                        + "[ -role <string [source/target/predictor]> ");
            }
        }

        // TODO figure out what resource this is and set up configuration
        ConstellationConfiguration config =
                new ConstellationConfiguration(new Context("TEST"),
                        StealStrategy.SMALLEST, StealStrategy.BIGGEST,
                        StealStrategy.BIGGEST);

        Constellation constellation =
                ConstellationFactory.createConstellation(config, nrExecutors);

        constellation.activate();

        System.out.println("Constellation is Running " + role);

//        if (constellation.isMaster()) {
//            // Runs only on Master node
//            out.println("Starting Constellation");
//
//            Timer overallTimer = constellation.getOverallTimer();
//            int timing = overallTimer.start();
//            overallTimer.stop(timing);
//
//            // Do computation
//
//
////            writeFile(result.c);
//        }

        logger.debug("calling Constellation.done()");
        constellation.done();
        logger.debug("called Constellation.done()");
    }
}
