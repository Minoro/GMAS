package middleware;

import cliente.InterfaceUsuario;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.xml.xpath.XPathExpressionException;
import model.Arquivo;
import org.w3c.dom.Document;
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
    private ListenerHeartBeat listenerHeartBeat;
    private MantenedorServidores mantenedorServidores;
    public SistemaArquivoInterface server;

    public List<SistemaArquivoInterface> servidoresRemotos;

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
     * @throws java.rmi.NotBoundException
     */
    //Adicionar arquivo de persistencia de servidores.
    public Middleware(String multicastGroup, String nomeUsuario, Boolean novoUsuario) throws IOException, UnknownHostException, RemoteException, XPathExpressionException, NotBoundException {
        PainelDeControle.username = nomeUsuario;
        servidoresArquivo = new LinkedList<>();
        mergeUsuario(multicastGroup, novoUsuario);
        carregaServidoresRMI();
        server = (SistemaArquivoInterface) Naming.lookup(PainelDeControle.middleware.getURLServidorRMI(0));
        PainelDeControle.xml = pedirXML();
    }

    private void mergeUsuario(String multicastGroup, Boolean novoUsuario) throws UnknownHostException, IOException, RemoteException, XPathExpressionException {
        InetAddress group = InetAddress.getByName(multicastGroup);
        try (MulticastSocket mSckt = new MulticastSocket();) { //usuario "escuta" na mesma porta do multicast
            String mensagem;
            if (novoUsuario) {
                mensagem = PainelDeControle.NOVO_USUARIO;
            } else {
                mensagem = PainelDeControle.USUARIO_EXISTENTE + "-" + PainelDeControle.username;
            }
            byte[] m = mensagem.getBytes();
            DatagramPacket messageOut = new DatagramPacket(m, m.length, group, PainelDeControle.PORTA_MULTICAST);
            mSckt.send(messageOut);
            try (ServerSocket welcome = new ServerSocket(PainelDeControle.PORTA_MULTICAST)) {
                long tempoInicio, tempoTeste;
                int i = 0;
                tempoInicio = System.nanoTime();
                while (true) {
                    if (i == 2) {
                        break;
                    }
                    tempoTeste = System.nanoTime();
                    //N segundos aguardando respostas => Definido na classe Painel de controle
                    if ((tempoTeste - tempoInicio) / 1000000000 > PainelDeControle.deltaTRespostaMulticast) {
                        break;
                    }
                    welcome.setSoTimeout((PainelDeControle.deltaTRespostaMulticast) * 1000);
                    try (Socket resp = welcome.accept()) {
                        System.out.println("Esperando mensagem server");
                        byte[] resposta = new byte[PainelDeControle.TAMANHO_BUFFER];
                        resp.getInputStream().read(resposta);
                        //confirmação do servidor
                        servidoresArquivo.add(resp.getInetAddress());
                        i++;
                    } catch (SocketTimeoutException e) {
                        break;
                    }
                }
            }
            //verificar sincronismo:
            listenerHeartBeat = new ListenerHeartBeat(servidoresArquivo);
            new Thread(listenerHeartBeat).start(); //inicia listener de Heartbeat

            for (InetAddress ip : servidoresArquivo) {
                System.out.println(ip.getHostAddress());
                try (Socket con = new Socket(ip, PainelDeControle.PORTA_SERVIDORES)) {
                    byte[] b = PainelDeControle.EU_ESCOLHO_VOCE.getBytes();
                    con.getOutputStream().write(b);
                }
            }
            //
            if (servidoresArquivo.size() == 1) {
                System.out.println("Falha detectada no MergeUsuario");
                new Thread(new GerenciadorDeFalhas()).start();
            }
            mantenedorServidores = new MantenedorServidores();
            new Thread(mantenedorServidores).start(); //inicia atualizador de enderecos de servidor
        }
    }

    private void carregaServidoresRMI() throws NotBoundException {
        servidoresRemotos = new ArrayList<>();
        for (int i = 0; i < servidoresArquivo.size(); i++) {
            try {
                SistemaArquivoInterface sistemaArquivoServidor = (SistemaArquivoInterface) Naming.lookup(getURLServidorRMI(i));
                servidoresRemotos.add(sistemaArquivoServidor);
            } catch (MalformedURLException | RemoteException ex) {
                Logger.getLogger(Middleware.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Retorna a URL RMI de um dos servidores relacionados ao cliente em
     * questão.
     *
     * @param indiceServidor int - indice do servidor para recuperação de IP
     * @return URL RMI do servidor de arquivo de índice "indiceServidor"
     */
    public String getURLServidorRMI(int indiceServidor) {
        if (indiceServidor >= servidoresArquivo.size()) {
            return null;
        }
        return "rmi://" + servidoresArquivo.get(indiceServidor).getHostAddress() + ":/teste";
    }

    public boolean renomearArquivo(String caminho, String nome_digitado) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                serverRemoto.renomearArquivo(caminho, nome_digitado, PainelDeControle.username);
            }
            return true;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }

    public boolean criarArquivo(String caminhoSelecionado, Arquivo arquivo) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                serverRemoto.criarArquivo(caminhoSelecionado, arquivo, PainelDeControle.username);
            }
            return true;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }

    public boolean criarPasta(String caminhoSelecionado) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                serverRemoto.criarPasta(caminhoSelecionado, PainelDeControle.username);
            }
            return true;

        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }

    public boolean copiarArquivo(String caminhoOrigem, String caminhoDestino) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                serverRemoto.copiarArquivo(caminhoOrigem, caminhoDestino, PainelDeControle.username);
            }
            return true;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }
    
    public boolean moverArquivo(String caminhoOrigem, String caminhoDestino) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                serverRemoto.moverArquivo(caminhoOrigem, caminhoDestino, PainelDeControle.username);
            }
            return true;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }

    public boolean deletarArquivo(String caminhoOrigem) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                server.deletarArquivo(caminhoOrigem, PainelDeControle.username);
            }
            return true;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }

    public boolean salvarArquivo(String caminho, String texto) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                serverRemoto.escreverArquivo(caminho, texto, PainelDeControle.username);
            }
            return true;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }

    public String lerArquivo(String caminho) throws RemoteException {
        try {
            return server.lerArquivo(caminho, PainelDeControle.username);
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return null;
    }

    public Document pedirXML() throws RemoteException {
        Document r = null;
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                r = serverRemoto.pedirXML(PainelDeControle.username);
            }
            return r;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return null;
    }

    /**
     *
     * @author Guilherme
     */
    private class ListenerHeartBeat implements Runnable {

        private List<InetAddress> ipServidores;
        private Map<InetAddress, Long> isAlive;

        public ListenerHeartBeat(List<InetAddress> ipServidores) {
            this.ipServidores = ipServidores;
            this.isAlive = new HashMap<>();
            for (InetAddress i : ipServidores) {
                isAlive.put(i, System.nanoTime()); //para controle de tempo de resposta
            }
        }

        @Override
        public void run() {
            try (ServerSocket s = new ServerSocket(PainelDeControle.PORTA_HEARTBEAT)) {
                while (true) {
                    try (Socket socket = s.accept()) {
                        byte[] buffer = new byte[PainelDeControle.TAMANHO_BUFFER];
                        socket.getInputStream().read(buffer);
                        isAlive.put(socket.getInetAddress(), System.nanoTime());
                        for (InetAddress i : ipServidores) {
                            if ((System.nanoTime() - isAlive.get(i)) / 1000000000 > PainelDeControle.deltaTRespostaServidor) { //servidor caiu
                                isAlive.remove(i);
                                System.out.println("Falha detectada no ListenerHeartbeat");
                                new Thread(new GerenciadorDeFalhas(i)).start(); //avisa erro
                                ipServidores.remove(i);
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                System.out.println("Falha ao inicializar Listener Heartbeat");
                System.out.println(ex);
            }
        }

        public void adicionaNovoServidor(InetAddress novoServidor) { //sera que ocorre algum conflito?
            ipServidores.add(novoServidor);
            isAlive.put(novoServidor, System.nanoTime());
        }
    }

    private void removeServidorFalho(InetAddress exS) {
        servidoresArquivo.remove(exS);
    }

    /**
     * Classe que notifica a existencia de falhas a um dos servidores e recebe a
     * resposta do gerenciador eleito com o IP do novo servidor de arquivos do
     * Cliente (middleware) em questão
     *
     * @author Mastelini
     */
    private class GerenciadorDeFalhas implements Runnable {

        private final InetAddress servidorNotificacao;

        public GerenciadorDeFalhas(InetAddress servidorFalho) {
            removeServidorFalho(servidorFalho); //remove da lista de servidores ativos
            servidorNotificacao = servidoresArquivo.get(0); //o unico servidor que resta
        }

        public GerenciadorDeFalhas() {
            servidorNotificacao = servidoresArquivo.get(0); //o unico servidor que resta
        }

        @Override
        public void run() {
            try (ServerSocket tcpServer = new ServerSocket(PainelDeControle.PORTA_RESOLUCAO_FALHA)) {
                //laco de envio com resposta
                String msg = PainelDeControle.FALHA_SERVIDOR;
                try (Socket solicitacao = new Socket(servidorNotificacao, PainelDeControle.PORTA_SERVIDORES)) {
                    byte[] resposta = msg.getBytes();
                    solicitacao.getOutputStream().write(resposta);
                }
            } catch (IOException ex) {
                Logger.getLogger(Middleware.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Classe que periodicamente solicita via Multicast os IPS dos servidores de
     * arquivos de um determinado cliente (middleware)
     *
     * @author Mastelini
     */
    private class MantenedorServidores implements Runnable {
        private final List<InetAddress> tempIPServidores;

        public MantenedorServidores() {
            this.tempIPServidores = new LinkedList<>();
        }

        @Override
        public void run() {
            while(true) {
                getIPsServidoresArquivo();
                try {
                    Thread.sleep(PainelDeControle.deltaTRespostaServidor * 1000); //aguarda um intervalo de tempo para atualizar a lista de servidores
                } catch (InterruptedException ex) {
                    //Logger.getLogger(Middleware.class.getName()).log(Level.SEVERE, null, ex);
                    //NAO FAZ NADA
                }
            }
        }

        private void getIPsServidoresArquivo() {
            try (MulticastSocket mSckt = new MulticastSocket();) { //usuario "escuta" na mesma porta do multicast
                InetAddress group = InetAddress.getByName(PainelDeControle.IP_MULTICAST);
                String mensagem = PainelDeControle.USUARIO_EXISTENTE + "-" + PainelDeControle.username;

                byte[] m = mensagem.getBytes();
                DatagramPacket messageOut = new DatagramPacket(m, m.length, group, PainelDeControle.PORTA_MULTICAST);
                mSckt.send(messageOut);
                try (ServerSocket welcome = new ServerSocket(PainelDeControle.PORTA_MULTICAST)) {
                    long tempoInicio, tempoTeste;
                    int i = 0;
                    tempoInicio = System.nanoTime();
                    while (true) {
                        if (i == 2) {
                            break;
                        }
                        tempoTeste = System.nanoTime();
                        //N segundos aguardando respostas => Definido na classe Painel de controle
                        if ((tempoTeste - tempoInicio) / 1000000000 > PainelDeControle.deltaTRespostaMulticast) {
                            break;
                        }
                        welcome.setSoTimeout((PainelDeControle.deltaTRespostaMulticast) * 1000);
                        try (Socket resp = welcome.accept()) {
                            byte[] resposta = new byte[PainelDeControle.TAMANHO_BUFFER];
                            resp.getInputStream().read(resposta);
                            //confirmação do servidor
                            tempIPServidores.add(resp.getInetAddress());
                            i++;
                        } catch (SocketTimeoutException e) {
                            break;
                        }
                    }
                    mergeListaServidores(tempIPServidores);
                }
            } catch (IOException ex) {
                Logger.getLogger(Middleware.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void mergeListaServidores(List<InetAddress> IPServidores) {
            
            for(InetAddress IP : IPServidores) {
                boolean found = false;
                for(InetAddress IP2 : servidoresArquivo) {
                    if(IP.equals(IP2)) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    try {
                        servidoresArquivo.add(IP);
                        listenerHeartBeat.adicionaNovoServidor(IP);
                        carregaServidoresRMI();
                        try (Socket iniciaHeartBeat = new Socket(IP, PainelDeControle.PORTA_SERVIDORES)) {
                            byte[] beat = PainelDeControle.EU_ESCOLHO_VOCE.getBytes();
                            iniciaHeartBeat.getOutputStream().write(beat);
                        }
                    }   catch (IOException | NotBoundException ex) { //E AGORA? O QUE FAZER?
                        Logger.getLogger(Middleware.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
}
