package servidor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import cliente.InterfaceUsuario;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import model.Arquivo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import utils.ManipuladorXML;

import utils.PainelDeControle;

public class SistemaArquivo extends UnicastRemoteObject implements SistemaArquivoInterface {

    private final ManipuladorXML manipuladorXML;

    private final MulticastSocket s;
    private final InetAddress group;
    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        try {
            System.out.println("Servidor iniciado");

            Policy.setPolicy(new MyPolicy());
            System.setSecurityManager(new RMISecurityManager());

            LocateRegistry.createRegistry(1099);
            SistemaArquivo sistemaArquivo = new SistemaArquivo();
            Naming.rebind("rmi://:/teste", sistemaArquivo);
        } catch (IOException ex) {
            Logger.getLogger(SistemaArquivo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*public SistemaArquivo() {
     //para testes sem servidor
     }*/
    protected SistemaArquivo() throws RemoteException, IOException {
        super();
        manipuladorXML = new ManipuladorXML();
        group = InetAddress.getByName(PainelDeControle.IP_MULTICAST);
        s = new MulticastSocket(PainelDeControle.PORTA_MULTICAST);
        s.joinGroup(group);
        new Thread(new MulticastMonitor()).start();
        new Thread(new MonitorServidores()).start();
    }

    /**
     * @author mastelini
     *
     * Thread que escuta mensagens enviadas via multicast dos clientes,
     * requisitando por servidores de arquivos. O servidor responde ao
     * requisitante via UDP.
     *
     * Se a instância em questão é o servidor de arquivos do cliente
     * requisitante (cliente já existente) envia uma resposta informando esse
     * fato. Caso contrário não responde à requisição.
     *
     * Se o cliente requisitante é um novo cliente, uma resposta de "boas
     * vindas" é enviada, indicando que o servidor em questão está disponível
     * para atender o cliente.
     *
     *
     */
    private class MulticastMonitor implements Runnable {

        @Override
        public void run() {
            while (true) {
                byte[] buffer = new byte[PainelDeControle.TAMANHO_BUFFER];
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                try {
                    s.receive(messageIn);
                    String mensagem = new String(messageIn.getData());
                    mensagem = mensagem.substring(0, mensagem.indexOf("\0"));
                    System.out.println("Mensagem recebida: " + mensagem);

                    InetAddress ipUsuario = messageIn.getAddress();
                    if (mensagem.equals(PainelDeControle.NOVO_USUARIO)) {
                        String respostaNovoUsuario = PainelDeControle.RESPOSTA_NOVO_USUARIO;
                        byte[] responder = respostaNovoUsuario.getBytes();
                        try (Socket welcome = new Socket(ipUsuario, PainelDeControle.PORTA_MULTICAST)) {
                            welcome.getOutputStream().write(responder);
                            System.out.println("Novo usuário na parada! Mensagem enviada a ele. " + ipUsuario.getHostAddress());
                        } catch (IOException e) {
                            //faz nada
                            System.out.println("Tempo de resposta excedido. 1");
                        }

                    } else if (mensagem.startsWith(PainelDeControle.USUARIO_EXISTENTE)) {
                        String nomeUsuario = mensagem.split("-")[1];
                        File arq = new File(PainelDeControle.PASTA_XML + nomeUsuario + ".xml"); //procura pela raiz (xml) do usuario
                        if (arq.exists()) {
                            String respostaUsuarioExistente = PainelDeControle.RESPOSTA_USUARIO_EXISTENTE;
                            try (Socket welcome = new Socket(ipUsuario, PainelDeControle.PORTA_MULTICAST)) {
                                byte[] resposta = respostaUsuarioExistente.getBytes();
                                welcome.getOutputStream().write(resposta);
                                System.out.println("Usuário já existente na parada! Mensagem enviada a ele. " + ipUsuario.getHostAddress());
                            } catch (IOException e) {
                                //faz nada
                                System.out.println("Tempo de resposta excedido. 2");
                            }
                        }
                    } else if (mensagem.equals(PainelDeControle.USUARIOS_ARMAZENADOS)) {
                        try (Socket resp = new Socket(messageIn.getAddress(), PainelDeControle.PORTA_ERROS)) {
                            String msg = "";
                            for (String u : getUsuarios()) {
                                msg += u + ";";
                            }
                            byte[] m = msg.getBytes();
                            resp.getOutputStream().write(m);
                        } catch (IOException e) {
                            //do nothing
                            System.out.println("Tempo excedido");
                        }

                    }
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }

    }

    /**
     * Classe para politica de segurança do RMI, evitando arquivos externos.
     *
     */
    public static class MyPolicy extends Policy {

        @Override
        public PermissionCollection getPermissions(CodeSource codesource) {
            Permissions p = new Permissions();
            p.add(new java.security.AllPermission());
            return p;
        }
    }

    /**
     * Classe que controla a replicacao de dados caso um middleware detecte uma
     * falha em um de seus servidores
     *
     */
    private class ControleReplicacao implements Runnable {

        private String mensagem;
        private long tempoInicio, tempoTeste;
        private final HashMap<String, Integer> contagemUsuarios;
        private HashMap<String, String> servidor_X_usuario;
        private final List<String> respostas;
        private final List<String> usuariosExistentes, servidoresExistentes;
        private final String middlewareSolicitante;
        private final InetAddress endMiddleware;

        //FAZER RESPONDER AO MIDDLEWARE QUE O AVISOU DA FALHA QUEM É O NOVO SERVIDOR DE ARQUIVOS
        public ControleReplicacao(String middlewareSolicitante, InetAddress endMiddleware) {
            mensagem = PainelDeControle.USUARIOS_ARMAZENADOS;
            contagemUsuarios = new HashMap<>();
            respostas = new LinkedList<>();
            usuariosExistentes = new LinkedList<>();
            servidoresExistentes = new LinkedList<>();
            this.middlewareSolicitante = middlewareSolicitante;
            this.endMiddleware = endMiddleware;
        }

        @Override
        public void run() {
            int contadorRespostas = 0;
            try (MulticastSocket mSckt = new MulticastSocket();
                    ServerSocket server = new ServerSocket(PainelDeControle.PORTA_ERROS)) { //escuta respostas dos nomes de usuarios armazenados
                //requisita aos outros servidores os usuarios que estes possuem
                byte[] m = mensagem.getBytes();
                DatagramPacket messageOut = new DatagramPacket(m, m.length, group, PainelDeControle.PORTA_MULTICAST);
                mSckt.send(messageOut);
                //definicao de um delta de tempo para aceitacao de respostas

                tempoInicio = System.nanoTime();
                while (true) {
                    tempoTeste = System.nanoTime();
                    //N segundos aguardando respostas => Definido na classe Painel de controle
                    if ((tempoTeste - tempoInicio) / 1000000000 > PainelDeControle.deltaTRespostaMulticast) {
                        break;
                    }
                    try (Socket recebimento = server.accept()) {
                        byte[] buffer = new byte[PainelDeControle.TAMANHO_BUFFER];
                        recebimento.getInputStream().read(buffer);
                        mensagem = new String(buffer);
                        mensagem = mensagem.substring(0, mensagem.indexOf("\0")); //elimina caracteres inuteis
                        mensagem += "::" + recebimento.getInetAddress(); //concatena o IP do servidor
                        respostas.add(mensagem);
                        contadorRespostas++;
                    }
                }
                if(contadorRespostas > 1) {
                    processaUsuarios();
                    delegaBackup();
                }
            } catch (IOException ex) {
                Logger.getLogger(SistemaArquivo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * Realiza a contagem de cópias de dados de usuários e organiza a
         * relação de servidores X usuários
         */
        private void processaUsuarios() {
            String[] r;
            String aux;
            for (String msg : respostas) {
                r = msg.split("::");
                aux = r[1]; //salva nome(IP) do servidor
                r = r[0].split(";");
                servidoresExistentes.add(aux);
                for (int i = 0; i < r.length; i++) {
                    Integer value = contagemUsuarios.get(r[i]);
                    if (value == null) {
                        value = 0;
                    }
                    value++;
                    contagemUsuarios.put(r[i], value);
                    servidor_X_usuario.put(r[i], aux); //relaciona um usuario e um servidor
                    usuariosExistentes.add(r[i]); //lista de todos os usuarios existentes
                }
            }
        }

        /**
         * Escolhe de maneira aleatória um dos servidores para realizar o backup
         * dos dados de um usuário e envia uma mensagem solicitando essa ação.
         *
         * Envia ao middleware que detectou o erro o IP do novo servidor
         * relacionado a este (através de TCP).
         *
         * @throws SocketException
         * @throws UnknownHostException
         * @throws IOException
         */
        private void delegaBackup() throws SocketException, UnknownHostException, IOException {
            for (String u : usuariosExistentes) {
                if (contagemUsuarios.get(u) == 1) {
                    //seleciona aleatoriamente servidor para Backup e envia solicitacao
                    Random rand = new Random();
                    String msg = PainelDeControle.FACA_BACKUP + "-" + u + "-" + servidor_X_usuario.get(u); //cabecalhoMsg-nomeUsuario-IPServidorQueOAtende
                    int indServidor = rand.nextInt(servidoresExistentes.size());
                    while (servidoresExistentes.get(indServidor).equals(servidor_X_usuario.get(u))) { //evita que envie requisicao de backup para o proprio servidor que armazena o usuario em questao
                        indServidor = rand.nextInt(servidoresExistentes.size());
                    }
                    try (Socket backup = new Socket(InetAddress.getByName(servidoresExistentes.get(indServidor)), PainelDeControle.PORTA_SERVIDORES)) {
                        byte[] solicitacao = msg.getBytes();
                        backup.getOutputStream().write(solicitacao); //requisita a execucao de backup
                    }

                    //envia para o middleware solicitante o IP do novo servidor deste
                    if (u.equals(middlewareSolicitante)) {
                        try (Socket resp = new Socket(endMiddleware, PainelDeControle.PORTA_RESOLUCAO_FALHA)) {
                            resp.getOutputStream().write(servidoresExistentes.get(indServidor).getBytes()); //envia o IP do novo servidor
                        } //envia o IP do novo servidor
                    }
                }
            }
        }
    }

    /**
     * Classe que escuta requisições de backup de dados feita pelos outros
     * servidores e avisos de falha, notificados pelo middleware (?)
     */
    private class MonitorServidores implements Runnable {

        @Override
        public void run() {
            try (ServerSocket server = new ServerSocket(PainelDeControle.PORTA_SERVIDORES);) {
                while (true) {
                    try (Socket conexao = server.accept()) {
                        byte[] buffer = new byte[PainelDeControle.TAMANHO_BUFFER];
                        conexao.getInputStream().read(buffer);
                        String mensagem = new String(buffer);
                        mensagem = mensagem.substring(0, mensagem.indexOf("\0"));
                        if (mensagem.startsWith(PainelDeControle.FACA_BACKUP)) {
                            realizarBackup(mensagem);

                        } else if (mensagem.startsWith(PainelDeControle.FALHA_SERVIDOR)) { //falha detectada
                            String nomeSolicitante = mensagem.split("-")[1]; //nome do usuario (middleware) que detectou a falha
                            System.out.println("Thread de tratamento de erros iniciada no Servidor");
                            new Thread(new ControleReplicacao(nomeSolicitante, conexao.getInetAddress())).start(); //dispara gerenciador de replicao
                        } else if (mensagem.startsWith(PainelDeControle.EU_ESCOLHO_VOCE)) {
                            new Thread(new Heartbeat(conexao.getInetAddress(), PainelDeControle.PORTA_HEARTBEAT)).start();//inicia Heartbeat
                        }
                    } catch (NotBoundException | MalformedURLException | RemoteException | XPathExpressionException ex) {
                        Logger.getLogger(SistemaArquivo.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(SistemaArquivo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void realizarBackup(String mensagem) throws NotBoundException, MalformedURLException, RemoteException, XPathExpressionException {
            SistemaArquivoInterface server;
            List<Arquivo> arquivosBackup;

            String mensagemSeparada[] = mensagem.split("-");
            String usuario = mensagemSeparada[1];
            String ipServidor = mensagemSeparada[2];
            String urlRMI = "rmi://" + ipServidor + ":/teste";

            server = (SistemaArquivoInterface) Naming.lookup(urlRMI);
            arquivosBackup = server.backupArquivosUsuario(usuario);

            for (Arquivo arquivo : arquivosBackup) {
                GerenciadorArquivos.salvarArquivo(arquivo, arquivo.getNome());
            }

        }
    }

    /**
     * Classe de Heartbeat
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
            while (true) {
                try (Socket send = new Socket(ip, port)) {
                    byte[] heartbeat = PainelDeControle.MENSAGEM_HEARTBEAT.getBytes();
                    send.getOutputStream().write(heartbeat);
                } catch (IOException ex) {
                    System.out.println("SAIU");
                    break;
                }
            }
        }
    }

    /* IMPLEMENTAÇÃO DOS METODOS RMI*/
    @Override
    public boolean criarArquivo(String caminho, Arquivo arquivo, String nomeUsuario)
            throws RemoteException, XPathExpressionException {
        Document xml = pedirXML(nomeUsuario);

        if (manipuladorXML.existeArquivo(caminho, xml)) {
            return false;
        }
        String nomeArquivo = caminho.substring(caminho.lastIndexOf("/") + 1);//separa o nome do arquivo (ultimo item do caminho)
        String expressao = manipuladorXML.montarExpressaoArquivo(caminho);

        try {
            String nomeArquivoServidor = GerenciadorArquivos.criarArquivo(arquivo);//cria arquivo e salva o nome que esta no servidor
            //faz o parsing do XML inserindo o caminho e o nome do arquivo
            Node ultima_pasta = manipuladorXML.pegaUltimaPasta(expressao, xml);
            Element newelement = xml.createElement(PainelDeControle.TAG_ARQUIVO);

            SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-YYYY HH:MM");
            String dataAgora = sdf.format(new Date());
            String tamanho = arquivo.getConteudo().length() + "";
            newelement.setAttribute("dataCriacao", dataAgora);
            newelement.setAttribute("dataUltimaModificacao", dataAgora);
            newelement.setAttribute("tamanho", tamanho);
            newelement.setAttribute("nome", nomeArquivoServidor);
            newelement.setTextContent(nomeArquivo);

            ultima_pasta.appendChild(newelement);

            manipuladorXML.salvarXML(xml, nomeUsuario);

        } catch (TransformerException ex) {
            Logger.getLogger(InterfaceUsuario.class
                    .getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean criarPasta(String caminho, String nomeUsuario)
            throws RemoteException, XPathExpressionException {

        Document xml = pedirXML(nomeUsuario);
        if (manipuladorXML.existePasta(caminho, xml)) {
            return false;
        }
        String nomePasta = caminho.substring(caminho.lastIndexOf("/") + 1);//separa o nome da pasta €(ultimo item do caminho)
        String expressao = manipuladorXML.montarExpressaoPasta(caminho);
        try {
            //faz o parsing do XML inserindo o caminho e o nome do arquivo
            Node ultima_pasta = manipuladorXML.pegaUltimaPasta(expressao, xml);
            Element newelement = xml.createElement(PainelDeControle.TAG_PASTA);

            SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-YYYY HH:MM");
            String dataAgora = sdf.format(new Date());
            newelement.setAttribute("dataCriacao", dataAgora);
            newelement.setAttribute("dataUltimaModificacao", dataAgora);
            newelement.setAttribute("tamanho", "0");
            newelement.setTextContent(nomePasta);

            ultima_pasta.appendChild(newelement);
            manipuladorXML.salvarXML(xml, nomeUsuario);
            //verifica se o caminho existe no XML, se não existir retorna falso (vellone pergunta: ?)

        } catch (TransformerException ex) {
            Logger.getLogger(InterfaceUsuario.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    @Override
    public Document pedirXML(String nomeUsuario) {
        Document retorno;
        String caminho = PainelDeControle.PASTA_XML + nomeUsuario + ".xml";
        System.out.println("Caminho do XML => " + caminho);
        File file = new File(caminho);
        if (!file.exists()) { //cria e inicializa o arquivo xml
            try {
                file.createNewFile();
                FileWriter fw;
                fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write("<raiz>" + nomeUsuario + "</raiz>");//salva as informações no arquivo no disco
                bw.close();

            } catch (IOException ex) {
                Logger.getLogger(SistemaArquivo.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = dbFactory.newDocumentBuilder();
            retorno = builder.parse(file);
            retorno.normalize();
            return retorno;

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(SistemaArquivo.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public boolean deletarArquivo(String caminho, String nomeUsuario)
            throws RemoteException, XPathExpressionException {
        Document xml = pedirXML(nomeUsuario);
        if (!manipuladorXML.existeArquivo(caminho, xml)) {
            return false;
        }

        String nomeArquivoServidor = manipuladorXML.getNomeArquivoFisico(caminho, xml);
        GerenciadorArquivos.apagarArquivo(nomeArquivoServidor);

        /**
         * TODO opcional - Retona o nome do arquivo (fisico) apagado para então
         * apagar pelo gerenciador de arquivo apaga do xml
         */
        return true;
    }

    @Override
    public boolean renomearArquivo(String caminhoOrigem, String novoNome, String nomeUsuario)
            throws RemoteException, XPathExpressionException {
        Document xml = pedirXML(nomeUsuario);

        //TODO
        //Alterar XML
        String expressao;
        if (caminhoOrigem.endsWith(".txt")) {
            //Renomeando arquivo
            if (!manipuladorXML.existeArquivo(caminhoOrigem, xml)) {
                return false;
            }
            expressao = manipuladorXML.montarExpressaoArquivo(caminhoOrigem);
        } else {
            //Renomeando pasta
            if (!manipuladorXML.existePasta(caminhoOrigem, xml)) {
                return false;
            }
            expressao = manipuladorXML.montarExpressaoArquivo(caminhoOrigem);
        }

        Node node = manipuladorXML.pegaUltimoNode(expressao, xml);

        node.setTextContent(novoNome);
        try {
            manipuladorXML.salvarXML(xml, nomeUsuario);

        } catch (TransformerException ex) {
            Logger.getLogger(SistemaArquivo.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @Override
    public boolean moverArquivo(String caminhoOrigem, String caminhoDestino, String nomeUsuario)
            throws RemoteException, XPathExpressionException {
        Document xml = pedirXML(nomeUsuario);

        //verifica se o arquivo de origem existe e se não há arquivo com o mesmo
        //nome no destino
        if (!manipuladorXML.existeArquivo(caminhoOrigem, xml)
                || manipuladorXML.existeArquivo(caminhoDestino, xml)) {
            return false;
        }

        Arquivo arquivo = getArquivo(caminhoOrigem, nomeUsuario);
        criarArquivo(caminhoDestino, arquivo, nomeUsuario);
        deletarArquivo(caminhoOrigem, nomeUsuario);

        return true;
    }

    @Override
    public boolean copiarArquivo(String caminhoOrigem, String caminhoDestino, String nomeUsuario)
            throws RemoteException, XPathExpressionException {
        Document xml = pedirXML(nomeUsuario);

        if (!manipuladorXML.existeArquivo(caminhoOrigem, xml)) {
            return false;
        }
        //carrega arquivo a ser copiado
        Arquivo arquivoCopiado = getArquivo(caminhoOrigem, nomeUsuario);

        //cria uma cópia do arquivo
        if (!criarArquivo(caminhoDestino, arquivoCopiado, nomeUsuario)) {
            return false;//erro ao copiar o arquivo
        }

        String nomeArquivoServidor = manipuladorXML.getNomeArquivoFisico(caminhoDestino, xml);//nome do arquivo fisico copiado
        return GerenciadorArquivos.salvarArquivo(arquivoCopiado, nomeArquivoServidor);
    }

    @Override
    public String lerArquivo(String caminho, String nomeUsuario)
            throws RemoteException, XPathExpressionException {
        Document xml = pedirXML(nomeUsuario);

        if (!manipuladorXML.existeArquivo(caminho, xml)) {
            return null;
        }
        String nomeArquivoServidor = manipuladorXML.getNomeArquivoFisico(caminho, xml);
//        Element arquivoNode =(Element) pegaUltimoNode(montaExpressao(caminho, false));

        Arquivo arquivo = GerenciadorArquivos.abrirArquivo(nomeArquivoServidor);
        String conteudo = arquivo.getConteudo();

        return conteudo;
    }

    @Override
    public boolean escreverArquivo(String caminho, String texto, String nomeUsuario)
            throws RemoteException, XPathExpressionException {

        Document xml = pedirXML(nomeUsuario);

        if (!manipuladorXML.existeArquivo(caminho, xml)) {
            return false;
        }
        String nomeArquivoServidor = manipuladorXML.getNomeArquivoFisico(caminho, xml);
        Arquivo arquivo = GerenciadorArquivos.abrirArquivo(nomeArquivoServidor);

        arquivo.setConteudo(texto);//passa informação para o arquivo

        //alterar nome para nome aleatório
        return GerenciadorArquivos.salvarArquivo(arquivo, nomeArquivoServidor);

    }

    @Override
    public Arquivo getArquivo(String caminho, String nomeUsuario)
            throws RemoteException, XPathExpressionException {
        Document xml = pedirXML(nomeUsuario);

        if (!manipuladorXML.existeArquivo(caminho, xml)) {
            return null;
        }

        //Pega o nome do arquivo no XML        
        String nomeArquivoServidor = manipuladorXML.getNomeArquivoFisico(caminho, xml);
        Arquivo arquivo = GerenciadorArquivos.abrirArquivo(nomeArquivoServidor);
        return arquivo;
    }

    @Override
    public List<Arquivo> backupArquivosUsuario(String nomeUsuario) throws RemoteException, XPathExpressionException {
        List<Arquivo> backup = new ArrayList<>();

        Document xml = pedirXML(nomeUsuario);
        List<String> nomeArquivosUsuario = manipuladorXML.getNomesArquivosFisicos(xml);

        for (String nomeArquivo : nomeArquivosUsuario) {
            Arquivo arquivo = GerenciadorArquivos.abrirArquivo(nomeArquivo);
            backup.add(arquivo);
        }

        return backup;
    }

    public List<String> getUsuarios() {
        List<String> usuarios = new ArrayList();
        File folder = new File(PainelDeControle.PASTA_XML);
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                String usuario = fileEntry.getName();
                usuario = usuario.substring(0, usuario.indexOf("."));
                usuarios.add(usuario);
                System.out.println("Listando usuários = " + usuario);
            }
        }
        return usuarios;
    }

}
