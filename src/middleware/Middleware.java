package middleware;

import cliente.InterfaceUsuario;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.LinkedList;
import java.util.List;
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
 * A comunicação é realizada através de chamadas RMI. Detecta falha de servidores e notifica um servidor desse fato,
 * desencadeando um processo de recuperação de falhas
 */
public class Middleware {

    /**
     * Lista dos Servidores de arquivos que a aplicação esta utilizando
     */
    private List<InetAddress> servidoresArquivo;
    private MantenedorServidores mantenedorServidores;

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
    public Middleware(String multicastGroup, String nomeUsuario, Boolean novoUsuario) throws IOException, UnknownHostException, RemoteException, XPathExpressionException, NotBoundException {
        PainelDeControle.username = nomeUsuario;
        servidoresArquivo = new LinkedList<>();
        mergeUsuario(multicastGroup, novoUsuario);
        carregaServidoresRMI();
        PainelDeControle.xml = pedirXML();
    }
    
    /**
     * Realiza a comunicação inicial de conexão. Envia uma solicitação via multicast para todos os servidores.
     * 
     * Se o cliente é um usuário novo, coleta as duas primeiras respostas de boas vindas dos servidores, e inicia a comunicação.
     * Se o cliente é um usuário já "cadastrado", coleta as respostas dos servidores de arquivo desse cliente em questão. Se apenas um servidor responder,
     * um procedimento de tratamento de erros é iniciado.
     * 
     * Se nenhum servidor responder a solicitação, o cliente é notificado e a aplicação é encerrada.
     * 
     * @param multicastGroup endereço de multicast utilizado para contatar os servidores
     * @param novoUsuario variável boleana que a ocorrência ou não de um novo usuário.
     * @throws UnknownHostException
     * @throws IOException
     * @throws RemoteException
     * @throws XPathExpressionException 
     */
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
                    welcome.setSoTimeout((PainelDeControle.deltaTRespostaMulticast) * 100); //bloqueia por (tempo em milisegundos de espera multicast)/10
                    try (Socket resp = welcome.accept()) {
                        System.out.println("Esperando mensagem server");
                        byte[] resposta = new byte[PainelDeControle.TAMANHO_BUFFER];
                        resp.getInputStream().read(resposta);
                        //confirmação do servidor
                        servidoresArquivo.add(resp.getInetAddress());
                        i++;
                    } catch (SocketTimeoutException ex) {
                        //faz nada e continua execução
                    }
                }
            }

            new Thread(new ServidorConexaoHeartBeat()).start(); //inicia Servidor de conexoes para Heartbeat

            for (InetAddress ip : servidoresArquivo) {
                System.out.println(ip.getHostAddress());
                try (Socket con = new Socket(ip, PainelDeControle.PORTA_SERVIDORES)) {
                    byte[] b = PainelDeControle.EU_ESCOLHO_VOCE.getBytes();
                    con.getOutputStream().write(b);
                }
            }

            if (servidoresArquivo.size() == 0) {
                JOptionPane.showMessageDialog(InterfaceUsuario.main, "Não foram encontrados servidores para o usuário");
                System.exit(0);
            }
            if (servidoresArquivo.size() == 1) {
                System.out.println("Falha detectada no MergeUsuario");
                new Thread(new NotificadorDeFalhas()).start();
            }
            mantenedorServidores = new MantenedorServidores();
            new Thread(mantenedorServidores).start(); //inicia atualizador de enderecos de servidor
        }
    }

    /**
     * Realiza o lookup (necessário para chamadas RMI) no servidores disponíveis para o usuário em
     * questão.
     * 
     * @throws NotBoundException 
     */
    private void carregaServidoresRMI() throws NotBoundException {
        servidoresRemotos = new ArrayList<>();
        for (int i = 0; i < servidoresArquivo.size(); i++) {
            SistemaArquivoInterface sistemaArquivoServidor = null;
            try {
                sistemaArquivoServidor = (SistemaArquivoInterface) Naming.lookup(getURLServidorRMI(i));
            } catch (MalformedURLException ex) {
                Logger.getLogger(Middleware.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                servidoresArquivo.remove(i);
                i = 0;
                continue;
            }
            servidoresRemotos.add(sistemaArquivoServidor);
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

    /**
     * Método intermediário entre a aplicação cliente e as chamadas RMI para os servidores.
     * 
     * Renomeia um arquivo em todos servidores que o armazenam.
     * @param caminho Caminho teórico do arquivo armazenado.
     * @param nome_digitado Novo nome para renomeação.
     * @return True, se o arquivo foi renomeado com sucesso. False, em caso de falhas na renomeação.
     * @throws RemoteException 
     */
    public boolean renomearArquivo(String caminho, String nome_digitado) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                boolean resultado = serverRemoto.renomearArquivo(caminho, nome_digitado, PainelDeControle.username);
                if (!resultado) {
                    return false;
                }
            }
            return true;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }

    /**
     * Cria um novo arquivo nos servidores de armazenamento
     * 
     * @param caminhoSelecionado Caminho do arquivo
     * @param arquivo Objeto representativo de arquivo (Model)
     * @return True, se o arquivo foi criado com sucesso. False, em caso de falhas na criação.
     * 
     * @throws RemoteException 
     */
    public boolean criarArquivo(String caminhoSelecionado, Arquivo arquivo) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                boolean resultado = serverRemoto.criarArquivo(caminhoSelecionado, arquivo, PainelDeControle.username);
                if (!resultado) {
                    return false;
                }
            }
            return true;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }

    /**
     * Cria uma nova pasta nos servidores de arquivos
     * 
     * @param caminhoSelecionado Caminho da nova pasta
     * @return true em caso de sucesso, senão false.
     * @throws RemoteException 
     */
    public boolean criarPasta(String caminhoSelecionado) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                boolean resultado = serverRemoto.criarPasta(caminhoSelecionado, PainelDeControle.username);
                if (!resultado) {
                    return false;
                }
            }
            return true;

        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }
    
    /**
     * Realiza uma cópia de um arquivo no servidores
     * 
     * @param caminhoOrigem Caminho de origem do arquivo
     * @param caminhoDestino Caminho de destino para a cópia
     * @return true em caso de sucesso, senão false.
     * @throws RemoteException 
     */
    public boolean copiarArquivo(String caminhoOrigem, String caminhoDestino) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                boolean resultado = serverRemoto.copiarArquivo(caminhoOrigem, caminhoDestino, PainelDeControle.username);
                if (!resultado) {
                    return false;
                }
            }
            return true;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }
    
    /**
     * Realiza a cópia de uma pasta nos servidores.
     * 
     * @param caminhoOrigem Caminho de origem da pasta.
     * @param caminhoDestino Caminho de destino para a nova pasta.
     * @return true em caso de sucesso, senão false.
     * @throws RemoteException 
     */
    public boolean copiarPasta(String caminhoOrigem, String caminhoDestino) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                boolean resultado = serverRemoto.copiarPasta(caminhoOrigem, caminhoDestino, PainelDeControle.username);
                if (!resultado) {
                    return false;
                }
            }
            return true;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }
    
    /**
     * Desbloqueia um arquivo para edição por outras conexões de um mesmo usuário (conexões simultâneas).
     * @param caminho Caminho do arquivo para desbloqueio.
     * @throws RemoteException 
     */
    public void unlock(String caminho) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                serverRemoto.unlock(caminho, PainelDeControle.username);
            }
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
    }
    
    /**
     * Move um arquivo em todos os servidores do cliente em questão.
     * @param caminhoOrigem Caminho de origem do arquivo a ser movido.
     * @param caminhoDestino Caminho de destino para o arquivo.
     * @return true em caso de sucesso, senão false.
     * @throws RemoteException 
     */
    public boolean moverArquivo(String caminhoOrigem, String caminhoDestino) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                boolean resultado = serverRemoto.moverArquivo(caminhoOrigem, caminhoDestino, PainelDeControle.username);
                if (!resultado) {
                    return false;
                }
            }
            return true;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }
    
    /**
     * Move uma pasta em todos os servidores de arquivo de um determinado cliente.
     * @param caminhoOrigem Caminho de origem do arquivo.
     * @param caminhoDestino Caminho de destino para mover a pasta.
     * @return true em caso de sucesso, senão false.
     * @throws RemoteException 
     */
    public boolean moverPasta(String caminhoOrigem, String caminhoDestino) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                boolean resultado = serverRemoto.moverPasta(caminhoOrigem, caminhoDestino, PainelDeControle.username);
                if (!resultado) {
                    return false;
                }
            }
            return true;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }
    
    /**
     * Deleta um arquivo em todos os servidores relacionados a um cliente.
     * @param caminhoOrigem Caminho do arquivo.
     * @return true em caso de sucesso, senão false.
     * @throws RemoteException 
     */
    public boolean deletarArquivo(String caminhoOrigem) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                boolean resultado = serverRemoto.deletarArquivo(caminhoOrigem, PainelDeControle.username);
                if (!resultado) {
                    return false;
                }
            }
            return true;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }
    
    /**
     * Deleta uma pasta em todos os servidores de um determinado cliente.
     * @param caminhoOrigem Caminho do arquivo
     * @return true em caso de sucesso, senão false.
     * @throws RemoteException 
     */
    public boolean deletarPasta(String caminhoOrigem) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                boolean resultado = serverRemoto.deletarPasta(caminhoOrigem, PainelDeControle.username);
                if (!resultado) {
                    return false;
                }
            }
            return true;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }
    
    /**
     * Salva as alterações feitas em um arquivo em todos os servidores que o armazenam
     * @param caminho Caminho do arquivo.
     * @param texto Conteúdo do arquivo.
     * @return true em caso de sucesso, senão false.
     * @throws RemoteException 
     */
    public boolean salvarArquivo(String caminho, String texto) throws RemoteException {
        try {
            for (SistemaArquivoInterface serverRemoto : servidoresRemotos) {
                boolean resultado = serverRemoto.escreverArquivo(caminho, texto, PainelDeControle.username);
                if (!resultado) {
                    return false;
                }
            }
            return true;
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return false;
    }
    
    /**
     * Carrega o conteúdo de um arquivo.
     * @param caminho Caminho do arquivo selecionado.
     * @return Retorna o conteúdo do arquivo selecionado na forma de uma string.
     * @throws RemoteException 
     */
    public String lerArquivo(String caminho) throws RemoteException {
        try {
            return servidoresRemotos.get(0).lerArquivo(caminho, PainelDeControle.username);
        } catch (XPathExpressionException ex) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, ex.getMessage());
        }
        return null;
    }
    
    /**
     * Requisita o xml para os servidores, representado na forma de um objeto. 
     * @return Retorna um objeto do tipo Document, representando o arquivo xml.
     * @throws RemoteException 
     */
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

    private void removeServidorFalho(InetAddress exS) {
        servidoresArquivo.remove(exS);
    }

    /**
     * Classe que recebe conexões para a transmissão de Heartbeats. Para cada
     * nova conexão dispara uma thread ListenerHeartBeat, que escuta as
     * mensagens enviadas dos servidores
     *
     * @author Guilherme
     */
    private class ServidorConexaoHeartBeat implements Runnable {

        @Override
        public void run() {
            try (ServerSocket s = new ServerSocket(PainelDeControle.PORTA_HEARTBEAT)) {
                while (true) {
                    Socket novoServidor = s.accept();
                    System.out.println("Iniciando listener!aeeeeeeee ki legaul");
                    new Thread(new ListenerHeartBeat(novoServidor)).start();
                }
            } catch (IOException ex) {
                System.out.println("Falha ao inicializar Listener Heartbeat");
                System.out.println(ex);
            }
        }
    }

    /**
     * Classe que escuta as mensagens de HeartBeat vindas do servidor e dispara
     * a notificação de erros caso o servidor que está escutando falhe.
     *
     * @author Mastelini
     */
    private class ListenerHeartBeat implements Runnable {

        private Socket conexaoServidor;
        private InputStream in;

        public ListenerHeartBeat(Socket conexaoServidor) {
            this.conexaoServidor = conexaoServidor;
        }

        @Override
        public void run() {
            int contadorFalhas = 0;
            try {
                conexaoServidor.setSoTimeout(1000 * PainelDeControle.deltaTRespostaServidor * 2); //define timeout de espera para mensagens de leitura
                in = conexaoServidor.getInputStream();
                while (true) {
                    try {
                        byte[] buffer = new byte[PainelDeControle.TAMANHO_BUFFER];
                        int i = in.read(buffer); //le HeartBeat
                        if (i == -1) {
                            throw new IOException();
                        }
                        contadorFalhas = 0;
                    } catch (IOException e) {
                        contadorFalhas++;
                        System.out.println("Falhas detectadas: " + contadorFalhas);
                        if (contadorFalhas == 3) { //servidor caiu
                            System.out.println("Falha de servidor detectada no Listener do HeartBeat");
                            new Thread(new NotificadorDeFalhas(conexaoServidor.getInetAddress())).start();
                            break;
                        }
                    }
                }
            } catch (IOException ex) {
                System.out.println("Nao peguei input stream");
            }
        }
    }

    /**
     * Classe que notifica a existencia de falhas a um dos servidores podendo
     * remover o IP do servidor falho caso este seja passado como parâmetro do
     * Construtor
     *
     * @author Mastelini
     */
    private class NotificadorDeFalhas implements Runnable {

        private final InetAddress servidorNotificacao;

        public NotificadorDeFalhas(InetAddress servidorFalho) {
            removeServidorFalho(servidorFalho); //remove da lista de servidores ativos
            servidorNotificacao = servidoresArquivo.get(0); //o unico servidor que resta
        }

        public NotificadorDeFalhas() {
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

        private List<InetAddress> tempIPServidores;

        public MantenedorServidores() {
            this.tempIPServidores = new LinkedList();
        }

        @Override
        public void run() {
            while (true) {
                getIPsServidoresArquivo();
                try {
                    Thread.sleep(4 * PainelDeControle.deltaTRespostaServidor * 1000); //aguarda um intervalo de tempo para atualizar a lista de servidores
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
                        tempIPServidores = new LinkedList();
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

            for (InetAddress IP : IPServidores) {
                boolean found = false;
                for (InetAddress IP2 : servidoresArquivo) {
                    if (IP.equals(IP2)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    try {
                        servidoresArquivo.add(IP);
                        carregaServidoresRMI();
                        try (Socket iniciaHeartBeat = new Socket(IP, PainelDeControle.PORTA_SERVIDORES)) {
                            byte[] beat = PainelDeControle.EU_ESCOLHO_VOCE.getBytes();
                            iniciaHeartBeat.getOutputStream().write(beat);
                        }
                    } catch (IOException | NotBoundException ex) { //E AGORA? O QUE FAZER?
                        Logger.getLogger(Middleware.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
}
