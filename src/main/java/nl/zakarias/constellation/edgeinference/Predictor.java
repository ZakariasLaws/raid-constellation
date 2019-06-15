package nl.zakarias.constellation.edgeinference;

import ibis.constellation.*;
import nl.zakarias.constellation.edgeinference.interfaces.DeviceRoleInterface;

public class Predictor implements DeviceRoleInterface {

    private AbstractContext contexts;

    Predictor(Context[] contexts){
        try {
            this.contexts = new OrContext(contexts);
        } catch (IllegalArgumentException e) {
            // Contexts.length < 2
            this.contexts = contexts[0];
        }
    }

    @Override
    public void run(Constellation constellation) {
        System.out.println("\nStarting Predictor with contexts: " + contexts + "\n");


        int time = 100000;
        try {
            System.out.println("Predictor sleeps");
            Thread.sleep(time);
            System.out.println("Predictor woke up");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        // Create an activity which runs locally, waiting for events to process
//        CollectAndProcessEvents eventCollector = new CollectAndProcessEvents(contexts);
//
//        try {
//            constellation.submit(eventCollector);
//        } catch (NoSuitableExecutorException e) {
//            e.printStackTrace();
//        }
//
//        // Block this thread until eventCollector is done
//        eventCollector.waitToFinish();
    }
}
