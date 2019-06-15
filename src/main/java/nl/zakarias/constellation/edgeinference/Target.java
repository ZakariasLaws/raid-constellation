package nl.zakarias.constellation.edgeinference;

import ibis.constellation.Constellation;
import ibis.constellation.Context;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.interfaces.DeviceRoleInterface;

public class Target implements DeviceRoleInterface {

    private static final Context TARGET_CONTEXT = Configuration.TARGET_CONTEXT;

    @Override
    public void run(Constellation constellation) {
        System.out.println("\nStarting Target with context: " + TARGET_CONTEXT + "\n");

        int time = 100000;
        System.out.println("Sleeping for " + time);

        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
