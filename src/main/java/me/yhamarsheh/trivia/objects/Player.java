package me.yhamarsheh.trivia.objects;

import java.net.InetAddress;

public class Player {

    private InetAddress ipAddress;
    private int port;
    private String name;

    public Player(InetAddress ipAddress, int port, String name) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.name = name;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " ('" + ipAddress.getHostAddress() + "', " + port + ")";
    }
}
