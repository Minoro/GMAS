package middleware;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import model.Arquivo;
import servidor.SistemaArquivoInterface;

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
    public SistemaArquivoInterface server;

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
     * @throws java.net.UnknownHostException
     * @throws java.rmi.RemoteException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    //Adicionar arquivo de persistencia de servidores.
    public Middleware(String multicastGroup, String nomeUsuario, Boolean novoUsuario) throws IOException, UnknownHostException, RemoteException, XPathExpressionException {
        PainelDeControle.username = nomeUsuario;
        servidoresArquivo = new LinkedList<>();
        PainelDeControle.username = nomeUsuario;
        mergeUsuario(multicastGroup, novoUsuario);

    }

    private void mergeUsuario(String multicastGroup, Boolean novoUsuario) throws UnknownHostException, IOException, RemoteException, XPathExpressionException {
        InetAddress group = InetAddress.getByName(multicastGroup);
        try (MulticastSocket mSckt = new MulticastSocket();
                DatagramSocket server = new DatagramSocket(PainelDeControle.PORTA_MULTICAST)) {
            String mensagem;
            if (novoUsuario) {
                mensagem = PainelDeControle.NOVO_USUARIO;
            } else {
                mensagem = PainelDeControle.USUARIO_EXISTENTE + "-" + PainelDeControle.username;
            }
            byte[] m = mensagem.getBytes();
            DatagramPacket messageOut = new DatagramPacket(m, m.length, group, PainelDeControle.PORTA_MULTICAST);
            mSckt.send(messageOut);

            //recebe a confirmação dos 2 primeiros servidores de arquivo
//            for (int i = 0; i < 2; i++) {
//                while (true) {
            System.out.println("Esperando mensagem server");
            byte[] resposta = new byte[PainelDeControle.TAMANHO_BUFFER];
            DatagramPacket receivePacket = new DatagramPacket(resposta, resposta.length);
            server.receive(receivePacket);
            //confirmação do servidor
            servidoresArquivo.add(receivePacket.getAddress());
//                }
//            }
        }
        PainelDeControle.xml = server.pedirXML(PainelDeControle.username);
    }
    
    /**
     * Retorna a URL RMI de um dos servidores relacionados ao cliente em questão.
     * 
     * @param indiceServidor indice do servidor para recuperação de IP
     * @return URL RMI do servidor de arquivo de índice "indiceServidor"
     */
    
    public String getURLServidorRMI(int indiceServidor) {
    	if(indiceServidor >= servidoresArquivo.size())
    		return null;
    	return "rmi://" + servidoresArquivo.get(indiceServidor).getHostAddress() + ":/teste";
    }

    public boolean renomearArquivo(String caminho, String nome_digitado) throws RemoteException, XPathExpressionException {
        return server.renomearArquivo(caminho, nome_digitado, PainelDeControle.username);
    }

    public boolean criarArquivo(String caminhoSelecionado, Arquivo arquivo) throws RemoteException, XPathExpressionException {
        return server.criarArquivo(caminhoSelecionado, arquivo, PainelDeControle.username);
    }

    public boolean criarPasta(String caminhoSelecionado) throws RemoteException, XPathExpressionException {
        return server.criarPasta(caminhoSelecionado, PainelDeControle.username);
    }
}
