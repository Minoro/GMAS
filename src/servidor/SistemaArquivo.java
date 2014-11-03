package servidor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import cliente.InterfaceUsuario;
import java.util.ArrayList;
import model.Arquivo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import utils.PainelDeControle;

public class SistemaArquivo extends UnicastRemoteObject implements SistemaArquivoInterface {

    private MulticastSocket s;
    private InetAddress group;
    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        try {
            System.out.println("Servidor iniciado");

            Policy.setPolicy(new MyPolicy());
            System.setSecurityManager(new RMISecurityManager());

            SistemaArquivo sistemaArquivo = new SistemaArquivo();
            Naming.rebind("rmi://:/teste", sistemaArquivo);
        } catch (IOException ex) {
            Logger.getLogger(SistemaArquivo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*public SistemaArquivo() {
     //para testes sem servidor
     }*/

    /*private boolean existePasta(String caminho) throws XPathExpressionException {
     String expressao = montaExpressao(caminho, true);

     XPath xpath = XPathFactory.newInstance().newXPath();
     XPathExpression expr = xpath.compile(expressao);
     Object exprResult = expr.evaluate(PainelDeControle.xml, XPathConstants.NODESET);
     NodeList node = (NodeList) exprResult;
     if (node.getLength() != 0) {
     return true;
     }
     return false;
     }*/
    protected SistemaArquivo() throws RemoteException, IOException {
        super();
        group = InetAddress.getByName(PainelDeControle.IP_MULTICAST);
        s = new MulticastSocket(PainelDeControle.PORTA_MULTICAST);
        s.joinGroup(group);
        new Thread(new MulticastMonitor()).start();
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
            System.out.println("Thread para receber mensagem de boas vindas iniciou!");
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
                        DatagramPacket dp = new DatagramPacket(responder, responder.length, ipUsuario, PainelDeControle.PORTA_MULTICAST);
                        DatagramSocket ds = new DatagramSocket();
                        ds.send(dp);
                        System.out.println("Novo usuário na parada! Mensagem enviada a ele. " + ipUsuario.getHostAddress());
                        ds.close();

                    } else if (mensagem.startsWith(PainelDeControle.USUARIO_EXISTENTE)) {
                        System.out.println("Usuário já existente! " + ipUsuario.getHostAddress());
                        System.out.println("FALTA IMPLEMENTAR");
                    } else if (mensagem.equals(PainelDeControle.USUARIOS_ARMAZENADOS)) {
                        try (DatagramSocket resp = new DatagramSocket()) {
                            String msg = "";
                            for (String u : getUsuarios()) {
                                msg += u + ";";
                            }
                            byte[] m = msg.getBytes();
                            DatagramPacket messageOut = new DatagramPacket(m, m.length, group, PainelDeControle.PORTA_SERVIDORES + 1); //responde solicitação do controlador de erros
                            resp.send(messageOut);
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
        private HashMap<String, Integer> contagemUsuarios;
        private HashMap<String, String> servidor_X_usuario;
        private List<String> respostas;
        private List<String> usuariosExistentes, servidoresExistentes;

        public ControleReplicacao() {
            mensagem = PainelDeControle.USUARIOS_ARMAZENADOS;
            contagemUsuarios = new HashMap<>();
            respostas = new LinkedList<>();
            usuariosExistentes = new LinkedList<>();
            servidoresExistentes = new LinkedList<>();
        }

        @Override
        public void run() {
            try (MulticastSocket mSckt = new MulticastSocket();
                    DatagramSocket server = new DatagramSocket(PainelDeControle.PORTA_SERVIDORES + 1)) { //escuta respostas dos nomes de usuarios armazenados
                //requisita aos outros servidores os usuarios que estes possuem
                byte[] m = mensagem.getBytes();
                DatagramPacket messageOut = new DatagramPacket(m, m.length, group, PainelDeControle.PORTA_MULTICAST);
                mSckt.send(messageOut);
                //definicao de um delta de tempo para aceitacao de respostas
                tempoInicio = System.nanoTime();
                while (true) {
                    tempoTeste = System.nanoTime();

                    //N segundos aguardando respostas => Definido na classe Painel de controle
                    if ((tempoTeste - tempoInicio) / 1000000000.0 > PainelDeControle.deltaTRespostaMulticast) {
                        break;
                    }
                    byte[] buffer = new byte[PainelDeControle.TAMANHO_BUFFER];
                    DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                    server.receive(messageIn);
                    mensagem = new String(messageIn.getData());
                    mensagem = mensagem.substring(0, mensagem.indexOf("\0")); //elimina caracteres inuteis
                    mensagem += "::" + messageIn.getAddress().getHostAddress(); //concatena o IP do servidor
                    respostas.add(mensagem);
                }
                //TODO
                //adiciona os proprios usuarios
                processaUsuarios();
                delegaBackup();
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
                    while(servidoresExistentes.get(indServidor).equals(servidor_X_usuario.get(u))) { //evita que envie requisicao de backup para o proprio servidor que armazena o usuario em questao
                        indServidor = rand.nextInt(servidoresExistentes.size());
                    }
                    byte[] resposta = msg.getBytes();
                    DatagramPacket dp = new DatagramPacket(resposta, resposta.length, InetAddress.getByName(servidoresExistentes.get(indServidor)), PainelDeControle.PORTA_SERVIDORES);
                    DatagramSocket ds = new DatagramSocket();
                    boolean pararSolicitacao = false;
                    while (!pararSolicitacao) {
                        //envio da mensagem
                        ds.send(dp);
                        ds = new DatagramSocket(PainelDeControle.PORTA_SERVIDORES + 1);
                        tempoInicio = System.nanoTime();
                        while (true) {
                            tempoTeste = System.nanoTime();
                            //N' segundos aguardando respostas => Definido na classe Painel de controle
                            if ((tempoTeste - tempoInicio) / 1000000000.0 > PainelDeControle.deltaTRespostaServidor) {
                                break;
                            }
                            byte[] buffer = new byte[PainelDeControle.TAMANHO_BUFFER];
                            DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                            ds.receive(messageIn);
                            mensagem = new String(messageIn.getData());
                            mensagem = mensagem.substring(0, mensagem.indexOf("\0")); //elimina caracteres inuteis
                            if (mensagem.equals(PainelDeControle.CONFIRMACAO_BACKUP)) {
                                //tratar falha na resposta nos outros servidores
                                pararSolicitacao = true; //resposta recebida
                                break;
                            }
                        }
                    }
                    ds.close();
                }
            }
        }
    }

    /**
     * Classe que escuta requisições de backup de dados feita pelos outros
     * servidores e avisos de falha, notificados pelo middleware (?)
     */
    private class MonitorInterServidores implements Runnable {

        @Override
        public void run() {
            try (DatagramSocket server = new DatagramSocket(PainelDeControle.PORTA_SERVIDORES);) {
                while (true) {
                    byte[] buffer = new byte[PainelDeControle.TAMANHO_BUFFER];
                    DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                    server.receive(messageIn);
                    String mensagem = new String(messageIn.getData());
                    mensagem = mensagem.substring(0, mensagem.indexOf("\0"));
                    if(mensagem.startsWith(PainelDeControle.FACA_BACKUP)) {
                        //TODO -> chamar funcao de backup
//                        if(naoexisteusuario)
                        String msg = PainelDeControle.CONFIRMACAO_BACKUP;
                        byte [] m = msg.getBytes();
                        DatagramPacket resposta = new DatagramPacket(m, m.length, group, PainelDeControle.PORTA_SERVIDORES+1);
                        server.send(resposta); //envia confirmacao de backup
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(SistemaArquivo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public boolean criarArquivo(String caminho, Arquivo arquivo)
            throws RemoteException, XPathExpressionException {
        if (existeArquivoPasta(caminho, false)) {
            return false;
        }
        String nomeArquivo = caminho.substring(caminho.lastIndexOf("/") + 1);//separa o nome do arquivo (ultimo item do caminho)
        String expressao = montaExpressao(caminho, false);

        try {
            String nomeArquivoServidor = GerenciadorArquivos.criarArquivo(arquivo);//cria arquivo e salva o nome que esta no servidor
            //TODO
            //faz o parsing do XML inserindo o caminho e o nome do arquivo
            Node ultima_pasta = pegaUltimaPasta(expressao);
            Element newelement = PainelDeControle.xml.createElement(PainelDeControle.TAG_ARQUIVO);
            System.out.println("ARRUMAR ATRIBUTOS DO XML AO CRIAR UM NOVO ARQUIVO");
            newelement.setAttribute("dataCriacao", new Date().toString());
            //usar atributo nome do XML para armazenar o nome físico do arquivo, com o objetivo de saber qual arquivo físico abrir
            newelement.setAttribute("nome", nomeArquivoServidor);
            newelement.setTextContent(nomeArquivo);
            ultima_pasta.appendChild(newelement);
            salvaXML(PainelDeControle.xml, PainelDeControle.username);
            //verifica se o caminho existe no XML, se não existir retorna falso (vellone pergunta: ?)

        } catch (TransformerException ex) {
            Logger.getLogger(InterfaceUsuario.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    @Override
    public boolean criarPasta(String caminho)
            throws RemoteException, XPathExpressionException {

        if (existeArquivoPasta(caminho, true)) {
            return false;
        }
        String nomePasta = caminho.substring(caminho.lastIndexOf("/") + 1);//separa o nome da pasta €(ultimo item do caminho)
        String expressao = montaExpressao(caminho, true);
        try {
                //TODO
            //faz o parsing do XML inserindo o caminho e o nome do arquivo
            Node ultima_pasta = pegaUltimaPasta(expressao);
            Element newelement = PainelDeControle.xml.createElement(PainelDeControle.TAG_PASTA);
            System.out.println("ARRUMAR ATRIBUTOS DO XML AO CRIAR UMA NOVA PASTA");
            newelement.setAttribute("dataCriacao", new Date().toString());
            //usar atributo nome do XML para armazenar o nome físico do arquivo, com o objetivo de saber qual arquivo físico abrir
            newelement.setAttribute("nome", nomePasta);
            newelement.setTextContent(nomePasta);
            ultima_pasta.appendChild(newelement);
            salvaXML(PainelDeControle.xml, PainelDeControle.username);
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
    public boolean deletarArquivo(String caminho)
            throws RemoteException, XPathExpressionException {
        if (!existeArquivoPasta(caminho, false)) {
            return false;
        }

        String nomeArquivo = caminho.substring(caminho.lastIndexOf("/") + 1);

            //TODO
        //se não existir caminho ou arquivo no XML retorna falso
        //pega o nome do arquivo no servidor
        GerenciadorArquivos.apagarArquivo(nomeArquivo);
        //apaga do xml

        return true;
    }

    @Override
    public boolean renomearArquivo(String caminhoOrigem, String novoNome)
            throws RemoteException, XPathExpressionException {

            //TODO
        //Alterar XML
        String expressao;
        if (caminhoOrigem.endsWith(".txt")) {
            //Renomeando arquivo
            if (!existeArquivoPasta(caminhoOrigem, false)) {
                return false;
            }
            expressao = montaExpressao(caminhoOrigem, false);
        } else {
            //Renomeando pasta
            if (!existeArquivoPasta(caminhoOrigem, true)) {
                return false;
            }
            expressao = montaExpressao(caminhoOrigem, true);
        }

        Node node = pegaUltimoNode(expressao);

        node.setTextContent(novoNome);
        try {
            salvaXML(PainelDeControle.xml, PainelDeControle.username);

        } catch (TransformerException ex) {
            Logger.getLogger(SistemaArquivo.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @Override
    public boolean moverArquivo(String caminhoOrigem, String caminhoDestino)
            throws RemoteException, XPathExpressionException {
        if (!existeArquivoPasta(caminhoOrigem, false)) {
            return false;
        }
            // TODO
        //alterar XML

        return false;
    }

    @Override
    public boolean copiarArquivo(String caminhoOrigem, String caminhoDestino)
            throws RemoteException, XPathExpressionException {
        if (!existeArquivoPasta(caminhoOrigem, false)) {
            return false;
        }

            // TODO 
        // alterar XML
        return false;
    }

    @Override
    public String lerArquivo(String caminho)
            throws RemoteException, XPathExpressionException {
        if (!existeArquivoPasta(caminho, false)) {
            return null;
        }
        String nomeArquivo = caminho.substring(caminho.lastIndexOf("/") + 1);

            //TODO
        //se não existir caminho ou arquivo no XML retorna falso
        //pega o nome do arquivo no servidor
        Arquivo arquivo = GerenciadorArquivos.abrirArquivo(nomeArquivo);
        String conteudo = arquivo.getConteudo();

        return conteudo;
    }

    @Override
    public boolean escreverArquivo(String caminho, String texto)
            throws RemoteException, XPathExpressionException {

        if (!existeArquivoPasta(caminho, false)) {
            return false;
        }
        String nomeArquivo = caminho.substring(caminho.lastIndexOf("/") + 1);
            //TODO
        //se não existir caminho ou arquivo no XML retorna falso
        //pega o nome do arquivo no servidor
        Arquivo arquivo = GerenciadorArquivos.abrirArquivo(nomeArquivo);

        arquivo.setConteudo(texto);//passa informação para o arquivo

        //alterar nome para nome aleatório
        return GerenciadorArquivos.salvarArquivo(arquivo, nomeArquivo);

    }

    @Override
    public boolean escreverArquivo(String caminho, String texto, int posicao)
            throws RemoteException, XPathExpressionException {

        if (!existeArquivoPasta(caminho, false)) {
            return false;
        }
        String nomeArquivo = caminho.substring(caminho.lastIndexOf("/") + 1);

            //TODO
        //se não existir caminho ou arquivo no XML retorna falso
        //pega o nome do arquivo no servidor
        Arquivo arquivo = GerenciadorArquivos.abrirArquivo(nomeArquivo);
        String conteudo = arquivo.getConteudo();

        String inicioTexto = conteudo.substring(0, posicao);
        String fimTexto = conteudo.substring(posicao);

        arquivo.setConteudo(inicioTexto + texto + fimTexto);

        GerenciadorArquivos.salvarArquivo(arquivo, nomeArquivo);
        return false;
    }

    @Override
    public Arquivo getAtributes(String caminho)
            throws RemoteException, XPathExpressionException {
        if (!existeArquivoPasta(caminho, false)) {
            return null;
        }
        String nomeArquivo = caminho.substring(caminho.lastIndexOf("/") + 1);

            //TODO
        //Pega o nome do arquivo no XML
        Arquivo arquivo = GerenciadorArquivos.abrirArquivo(nomeArquivo);
        return null;
    }

    @Override
    public void setAtributes(String caminho, Arquivo arquivo)
            throws RemoteException, XPathExpressionException {

        String nomeArquivo = caminho.substring(caminho.lastIndexOf("/") + 1);

        Arquivo arquivoServidor = GerenciadorArquivos.abrirArquivo(nomeArquivo);
        arquivoServidor = arquivo;
        GerenciadorArquivos.salvarArquivo(arquivoServidor, nomeArquivo);

    }

    /**
     * Verifica a existência de um ARQUIVO ou PASTA no XML
     *
     * @param caminho String - caminho do arquivo a ser checado, com a extensão
     * no fim. Exemplo: "Pasta 1/Pasta 2/Pasta 3/ultima/senha_facebook.txt"
     * @return boolean - true caso exista o arquivo, falso caso não exista
     * @throws XPathExpressionException
     */
    public boolean existeArquivoPasta(String caminho, boolean pasta)
            throws XPathExpressionException {
        String expressao = montaExpressao(caminho, pasta);

        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile(expressao);
        Object exprResult = expr.evaluate(PainelDeControle.xml, XPathConstants.NODESET);
        NodeList node = (NodeList) exprResult;
        if (node.getLength() != 0) {
            return true;
        }
        return false;
    }

    /**
     * Monta uma expressão para o XPath compilar
     *
     * @param caminho String - caminho a ser gerado a expressao
     * @param pasta boolean - true caso a expressão deva ser montada com pasta
     * como último elemento, e false caso o último elemento seja um arquivo
     * @return String - retorna uma String representando a expressão do caminho
     * do parâmetro
     */
    public String montaExpressao(String caminho, boolean pasta) {
        String[] list = caminho.split("/");
        String expressao = "/raiz/";

        for (int i = 0; i < list.length; i++) {
            String string = list[i];
            if (pasta || i != list.length - 1) {
                expressao += "pasta[text()='" + string + "']/";
            } else {
                expressao += "arquivo[text()='" + string + "']";
            }
        }

        if (pasta) {
            expressao = expressao.substring(0, expressao.lastIndexOf("/"));
        }
        System.out.println("Expressão gerada: " + expressao);
        return expressao;
    }

    /**
     * Salva o xml do parâmetro no arquivo físico
     *
     * @param nomeUsuario String - nome do usuário para ser salvo o arquivo XML
     * @param xml Document - Objeto representando o arquivo xml
     * @return boolean - true caso salve o arquivo, false caso haja algum erro
     * @throws TransformerConfigurationException
     */
    public boolean salvaXML(Document xml, String nomeUsuario)
            throws TransformerConfigurationException, TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(xml);
        String caminho = PainelDeControle.PASTA_XML + nomeUsuario + ".xml";
        StreamResult result = new StreamResult(new File(caminho));
        transformer.transform(source, result);
        return true;
    }

    /**
     * Dada uma expressão, retorna a última pasta dela. Utilizado para dar
     * appendChild.
     *
     * @param expressao String - expressao a ser
     * @return Node - o nó da última pasta da expressão
     * @throws XPathExpressionException
     */
    public Node pegaUltimaPasta(String expressao)
            throws XPathExpressionException {
        expressao = expressao.substring(0, expressao.lastIndexOf("/"));
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile(expressao);
        Object exprResult = expr.evaluate(PainelDeControle.xml, XPathConstants.NODESET);
        NodeList node = (NodeList) exprResult;
        if (node.getLength() == 0) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, "Não há nós a serem retornados com a seguinte expressão: " + expressao);
        }
        if (node.getLength() > 1) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, "Mais de um nó foram encontrados com a seguinte expressão: " + expressao);
        }
        return node.item(0);
    }

    public Node pegaUltimoNode(String expressao)
            throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile(expressao);
        Object exprResult = expr.evaluate(PainelDeControle.xml, XPathConstants.NODESET);
        NodeList node = (NodeList) exprResult;
        if (node.getLength() == 0) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, "Não há nós a serem retornados com a seguinte expressão: " + expressao);
        }
        if (node.getLength() > 1) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, "Mais de um nó foram encontrados com a seguinte expressão: " + expressao);
        }
        return node.item(0);
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
            } else {
            }
        }
        return usuarios;
    }

}
