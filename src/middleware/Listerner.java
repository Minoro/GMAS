package middleware;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import utils.PainelDeControle;

/**
 *
 * @author Guilherme
 */
public class Listerner {

    public static void main(String[] args) throws SocketException {
        DatagramSocket s = new DatagramSocket(5555);

        while (true) {
            byte[] buffer = new byte[9999];
            DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
            try {
                s.receive(messageIn);
                String mensagem = new String(messageIn.getData());
                mensagem = mensagem.substring(0, mensagem.indexOf("\0"));
                System.out.println("Mensagem recebida: " + mensagem);
            } catch (IOException e){
                System.out.println(e);
            }
        }
    }
}
