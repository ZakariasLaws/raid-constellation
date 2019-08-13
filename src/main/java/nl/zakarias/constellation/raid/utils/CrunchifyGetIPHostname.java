package nl.zakarias.constellation.raid.utils;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CrunchifyGetIPHostname implements Serializable {
    private InetAddress ip;
    private String hostname;
    private String constellation_id;

    public CrunchifyGetIPHostname(String constId) throws UnknownHostException {
        ip = InetAddress.getLocalHost();
        hostname = ip.getHostName();
        constellation_id = constId;
    }

    public InetAddress ip(){
        return ip;
    }

    public String hostname(){
        return hostname;
    }

    /**
     * Returns a unique string for each Constellation agent, useful to distinguish multiple agents
     * on the same host
     * @return Unique ID consisting of hostname and constellation ID
     */
    public String uniqueHostname() {
        return hostname + "-" + constellation_id;
    }
}