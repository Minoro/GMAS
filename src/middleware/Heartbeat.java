package middleware;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author Guilherme
 */
public class Heartbeat implements Runnable {

    private final InetAddress ip;
    private final int port;

    public InetAddress getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public Heartbeat(InetAddress ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            String hb = "batida do meu <3";
            byte[] heartbeat = hb.getBytes();
            while (true) {
                DatagramPacket dp = new DatagramPacket(heartbeat, heartbeat.length, getIp(), getPort());
                DatagramSocket ds = new DatagramSocket();
                ds.send(dp);
                ds.close();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
