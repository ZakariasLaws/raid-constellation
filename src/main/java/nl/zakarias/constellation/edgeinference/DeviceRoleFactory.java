package nl.zakarias.constellation.edgeinference;

import ibis.constellation.Context;
import nl.zakarias.constellation.edgeinference.configuration.Configuration;
import nl.zakarias.constellation.edgeinference.interfaces.DeviceRoleInterface;

public class DeviceRoleFactory {
    public DeviceRoleInterface startDevice(Configuration.NODE_ROLES role, Context[] contexts){
        switch (role){
            case SOURCE:
                return new Source(contexts);
            case TARGET:
                return new Target();
            case PREDICTOR:
                return new Predictor(contexts);
            default:
                throw new Error("No matching Java Class found for role: " + role.toString());
        }
    }
}
