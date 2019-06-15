package nl.zakarias.constellation.edgeinference.utils;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CrunchifyGetIPHostname implements Serializable {
    private InetAddress ip;
    private String hostname;

    public CrunchifyGetIPHostname() {
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public InetAddress ip(){
        return ip;
    }

    public String hostname(){
        return hostname;
    }
}