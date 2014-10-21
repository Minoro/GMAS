package middleware;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import utils.PainelDeControle;

/**
 * Classe que realiza a comunicação entre a aplicação do cliente e os servidores
 * de arquivo.
 *
 * A comunicação é realizada através de chamadas RMI.
 */
public class Middleware {

    /**
     * Lista dos Servidores de arquivos que a aplicação esta utilizando
     */
    private List<InetAddress> servidoresArquivo;
    private String nomeUsuario;

    /**
     * Construtor da Classe de Middleware. A partir do endereço de um grupo
     * Multicast passado por parametro envia uma solicitação para todos os
     * servidores de arquivos remotos e, armazena na lista de servidores os dois
     * primeiros que responderem a solicitação.
     *
     * @param multicastGroup Endereço do grupo de multicast
     * @param nomeUsuario Nome do usuário
     * @param novoUsuario Boolean que representa se é novo usuário ou não
     * @throws IOException
     */
    //Adicionar arquivo de persistencia de servidores.
    public Middleware(String multicastGroup, String nomeUsuario, Boolean novoUsuario) throws IOException {
        servidoresArquivo = new LinkedList<>();
        this.nomeUsuario = nomeUsuario;
        mergeUsuario(multicastGroup, novoUsuario);

    }

    private void mergeUsuario(String multicastGroup, Boolean novoUsuario) throws UnknownHostException, IOException {
        InetAddress group = InetAddress.getByName(multicastGroup);
        try (MulticastSocket mSckt = new MulticastSocket(); DatagramSocket server = new DatagramSocket(PainelDeControle.PORTA_MULTICAST)) {
            String mensagem;
            if (novoUsuario) {
                mensagem = PainelDeControle.NOVO_USUARIO;
            } else {
                mensagem = PainelDeControle.USUARIO_EXISTENTE + "-" + nomeUsuario;
            }
            byte[] m = mensagem.getBytes();
            DatagramPacket messageOut = new DatagramPacket(m, m.length, group, PainelDeControle.PORTA_MULTICAST);
            mSckt.send(messageOut);

            //recebe a confirmação dos 2 primeiros servidores de arquivo
            for (int i = 0; i < 2; i++) {
//                while (true) {
                byte[] resposta = new byte[PainelDeControle.TAMANHO_BUFFER];
                DatagramPacket receivePacket = new DatagramPacket(resposta, resposta.length);
                server.receive(receivePacket);
                //confirmação do servidor
                servidoresArquivo.add(receivePacket.getAddress());
                System.out.println("Novo server adicionado! " + receivePacket.getAddress().getHostAddress());
                System.out.println("Mensagem: " + new String(receivePacket.getData()));
//                }
            }
        }
    }

    public String montaURL_RMI(String ip){
        String url = "rmi://" + ip + ":/samuray";
        return url;
    }
    
    public Document pedirXML() {
        Document document = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            document = builder.parse(new File("10.12.5.12/" + nomeUsuario + ".xml"));
            document.normalize();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return document;
    }
}
