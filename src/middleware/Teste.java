package middleware;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author Guilherme
 */
public class Teste {
    public static void main(String[] args) throws UnknownHostException, IOException {
        InetAddress ip = InetAddress.getByName("192.168.1.102");
        new Thread(new Heartbeat(ip, 5555)).start();
        
        
    }
}
