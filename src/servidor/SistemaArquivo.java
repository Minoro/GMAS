package servidor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.util.Date;
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
import jtree.DemoMain;
import model.Arquivo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import utils.PainelDeControle;

public class SistemaArquivo
    extends UnicastRemoteObject
    implements SistemaArquivoInterface {

    private GerenciadorArquivos gerenciadorArquivos;

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

//    public SistemaArquivo() {
//        //para testes sem servidor
//    }

    private boolean existePasta(String caminho) throws XPathExpressionException {
        String expressao = montaExpressao(caminho, true);

        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile(expressao);
        Object exprResult = expr.evaluate(PainelDeControle.xml, XPathConstants.NODESET);
        NodeList node = (NodeList) exprResult;
        if (node.getLength() != 0) {
            return true;
        }
        return false;
    }

    protected SistemaArquivo() throws RemoteException, IOException {
        super();
        // TODO Auto-generated constructor stub
        gerenciadorArquivos = new GerenciadorArquivos();

        new Thread(new MensagemBoasVindas()).start();
    }
    
    private class MensagemBoasVindas implements Runnable {

        private MulticastSocket s;

        public MensagemBoasVindas() throws IOException {
            InetAddress group = InetAddress.getByName(PainelDeControle.IP_MULTICAST);
            s = new MulticastSocket(PainelDeControle.PORTA_MULTICAST);
            s.joinGroup(group);
        }

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
                        System.out.println("Enviando");
                        ds.send(dp);
                        System.out.println("Novo usuário na parada! Mensagem enviada a ele. " + ipUsuario.getHostAddress());

                    } else if (mensagem.startsWith(PainelDeControle.USUARIO_EXISTENTE)) {
                        System.out.println("Usuário já existente! " + ipUsuario.getHostAddress());
                        System.out.println("FALTA IMPLEMENTAR");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Classe para politica de segurança do RMI, evitando arquivos externos
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

    @Override
    public boolean criarArquivo(String caminho, Arquivo arquivo)
            throws RemoteException, XPathExpressionException {

        if (existeArquivoPasta(caminho, false)) {
            return false;
        }
        String nomeArquivo = caminho.substring(caminho.lastIndexOf("/") + 1);//separa o nome do arquivo (ultimo item do caminho)
        String expressao = montaExpressao(caminho, false);

        try {
            String nomeArquivoServidor = gerenciadorArquivos.criarArquivo(arquivo);//cria arquivo e salva o nome que esta no servidor
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
            Logger.getLogger(DemoMain.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(DemoMain.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(SistemaArquivo.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(SistemaArquivo.class.getName()).log(Level.SEVERE, null, ex);
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
        gerenciadorArquivos.apagarArquivo(nomeArquivo);
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
            Logger.getLogger(SistemaArquivo.class.getName()).log(Level.SEVERE, null, ex);
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
        Arquivo arquivo = gerenciadorArquivos.abrirArquivo(nomeArquivo);
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
        Arquivo arquivo = gerenciadorArquivos.abrirArquivo(nomeArquivo);

        arquivo.setConteudo(texto);//passa informação para o arquivo

        //alterar nome para nome aleatório
        return gerenciadorArquivos.salvarArquivo(arquivo, nomeArquivo);

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
        Arquivo arquivo = gerenciadorArquivos.abrirArquivo(nomeArquivo);
        String conteudo = arquivo.getConteudo();

        String inicioTexto = conteudo.substring(0, posicao);
        String fimTexto = conteudo.substring(posicao);

        arquivo.setConteudo(inicioTexto + texto + fimTexto);

        gerenciadorArquivos.salvarArquivo(arquivo, nomeArquivo);
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
        Arquivo arquivo = gerenciadorArquivos.abrirArquivo(nomeArquivo);
        return null;
    }

    @Override
    public void setAtributes(String caminho, Arquivo arquivo)
            throws RemoteException, XPathExpressionException {

        String nomeArquivo = caminho.substring(caminho.lastIndexOf("/") + 1);

        Arquivo arquivoServidor = gerenciadorArquivos.abrirArquivo(nomeArquivo);
        arquivoServidor = arquivo;
        gerenciadorArquivos.salvarArquivo(arquivoServidor, nomeArquivo);

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
            JOptionPane.showMessageDialog(DemoMain.main, "Não há nós a serem retornados com a seguinte expressão: " + expressao);
        }
        if (node.getLength() > 1) {
            JOptionPane.showMessageDialog(DemoMain.main, "Mais de um nó foram encontrados com a seguinte expressão: " + expressao);
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
            JOptionPane.showMessageDialog(DemoMain.main, "Não há nós a serem retornados com a seguinte expressão: " + expressao);
        }
        if (node.getLength() > 1) {
            JOptionPane.showMessageDialog(DemoMain.main, "Mais de um nó foram encontrados com a seguinte expressão: " + expressao);
        }
        return node.item(0);
    }

}
