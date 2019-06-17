package nl.zakarias.constellation.edgeinference.utils;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CrunchifyGetIPHostname implements Serializable {
    private InetAddress ip;
    private String hostname;

    public CrunchifyGetIPHostname() throws UnknownHostException {
        ip = InetAddress.getLocalHost();
        hostname = ip.getHostName();
    }

    public InetAddress ip(){
        return ip;
    }

    public String hostname(){
        return hostname;
    }
}