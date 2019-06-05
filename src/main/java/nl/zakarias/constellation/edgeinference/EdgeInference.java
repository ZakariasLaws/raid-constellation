package nl.junglecomputing.constellation.edgeinference;

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
            PrintStream out = new PrintStream("vectoradd.out");
            for (float v : array) {
                out.println(v);
            }
            out.close();
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        // this code is executed on every node

        PrintStream out = new PrintStream(System.out);

        // the number of executors per node in the cluster
        int nrExecutorsPerNode = 4;

        // number of nodes in the cluster
        int nrNodes = 1;

        // determine the number of tasks based on the size of the pool of nodes
        String ibisPoolSize = System.getProperty("ibis.pool.size");
        if (ibisPoolSize != null) {
            nrNodes = Integer.parseInt(ibisPoolSize);
        }

/*        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-nrExecutorsPerNode")) {
                i++;
                nrExecutorsPerNode = Integer.parseInt(args[i]);
            } else if (args[i].equals("-computeDivideThreshold")) {
                i++;
                computeDivideThreshold = Integer.parseInt(args[i]);
            } else if (args[i].equals("-n")) {
                i++;
                n = Integer.parseInt(args[i]);
            } else {
                throw new Error("Usage: java VectorAdd "
                        + "[ -nrExecutorsPerNode <num> ] "
                        + "[ -computeDivideThreshold <num> ] "
                        + "[ -n <num> ]");
            }
        }*/

        // Initialize Constellation with the following configuration for an
        // executor.  We create nrExecutorsPerNode on a node.
        ConstellationConfiguration config =
                new ConstellationConfiguration(new Context("TEST"),
                        StealStrategy.SMALLEST, StealStrategy.BIGGEST,
                        StealStrategy.BIGGEST);

        Constellation constellation =
                ConstellationFactory.createConstellation(config, nrExecutorsPerNode);

        constellation.activate();

        if (constellation.isMaster()) {
            // Runs only on Master node
            out.println("Starting Constellation");

            Timer overallTimer = constellation.getOverallTimer();
            int timing = overallTimer.start();
            overallTimer.stop(timing);

            // Do computation


//            writeFile(result.c);
        }

        logger.debug("calling Constellation.done()");
        constellation.done();
        logger.debug("called Constellation.done()");
    }
}
