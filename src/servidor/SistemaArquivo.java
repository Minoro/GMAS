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
import java.util.logging.Level;
import java.util.logging.Logger;

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
import model.Arquivo;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import utils.PainelDeControle;

public class SistemaArquivo extends UnicastRemoteObject implements SistemaArquivoInterface {

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
                        ds.send(dp);
                        System.out.println("Novo usuário na parada! Mensagem enviada a ele. " + ipUsuario.getHostAddress());

                    } else if (mensagem.equals(PainelDeControle.USUARIO_EXISTENTE)) {
                        System.out.println("Usuário já existente! " + ipUsuario.getHostAddress());
                        System.out.println("FALTA IMPLEMENTAR");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(new String(messageIn.getData()));
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
    public boolean criarArquivo(String caminho)
            throws RemoteException, XPathExpressionException {

        if (existeArquivo(caminho)) {
            return false;
        }

        String pastas[] = caminho.split("/");//separa o caminho passado em pastas
        String nomeArquivo = pastas[pastas.length - 1];//separa o nome do arquivo (ultimo item do caminho)

        String nomeArquivoServidor = gerenciadorArquivos.criarArquivo();//cria arquivo e salva o nome que esta no servidor
        //TODO
        //faz o parsing do XML inserindo o caminho e o nome do arquivo
        //verifica se o caminho existe no XML, se não existir retorna falso

        return true;
    }

    @Override
    public Document pedirXML(String nomeUsuario) {
        Document retorno;
        String caminho = PainelDeControle.CAMINHO_XML;
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
        if (!existeArquivo(caminho)) {
            return false;
        }

        //TODO
        //se não existir caminho ou arquivo no XML retorna falso
        //pega o nome do arquivo no servidor
        gerenciadorArquivos.apagarArquivo(nomeArquivo);
        //apaga do xml

        return true;
    }

    @Override
    public boolean renomearArquivo(String caminhoOrigem, String caminhoDestino)
            throws RemoteException, XPathExpressionException {

        try {
            if (!existeArquivo(caminhoOrigem)) {
                return false;
            }
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //TODO
        //Alterar XML
        return true;
    }

    @Override
    public boolean moverArquivo(String caminhoOrigem, String caminhoDestino)
            throws RemoteException, XPathExpressionException {
        try {
            if (!existeArquivo(caminhoOrigem)) {
                return false;
            }
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // TODO
        //alterar XML

        return false;
    }

    @Override
    public boolean copiarArquivo(String caminhoOrigem, String caminhoDestino)
            throws RemoteException, XPathExpressionException {
        try {
            if (!existeArquivo(caminhoOrigem)) {
                return false;
            }
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // TODO 
        // alterar XML
        return false;
    }

    @Override
    public String lerArquivo(String caminho)
            throws RemoteException, XPathExpressionException {
        if (!existeArquivo(caminho)) {
            return null;
        }

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

        if (!existeArquivo(caminho)) {
            return false;
        }
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

        if (!existeArquivo(caminho)) {
            return false;
        }

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
        if (!existeArquivo(caminho)) {
            return null;
        }

        //TODO
        //Pega o nome do arquivo no XML
        Arquivo arquivo = gerenciadorArquivos.abrirArquivo(nome);
        return null;
    }

    @Override
    public void setAtributes(String caminho, Arquivo arquivo)
            throws RemoteException, XPathExpressionException {

        Arquivo arquivoServidor = gerenciadorArquivos.abrirArquivo(nome);
        arquivoServidor = arquivo;
        gerenciadorArquivos.salvarArquivo(arquivoServidor, nome);

    }

    /**
     * Verifica a existência de um ARQUIVO no XML
     *
     * @param caminho String - caminho do arquivo a ser checado, com a extensão
     * no fim. Exemplo: "Pasta 1/Pasta 2/Pasta 3/ultima/senha_facebook.txt"
     * @return boolean - true caso exista o arquivo, falso caso não exista
     * @throws XPathExpressionException
     */
    public boolean existeArquivo(String caminho)
            throws XPathExpressionException {
        String expressao = montaExpressao(caminho);

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
     * @return String - retorna uma String representando a expressão do caminho
     * do parâmetro
     */
    public String montaExpressao(String caminho) {
        String[] list = caminho.split("/");
        String expressao = "/raiz/";

        for (int i = 0; i < list.length; i++) {
            String string = list[i];
            if (i != list.length - 1) {
                expressao += "pasta[text()='" + string + "']/";
            } else {
                expressao += "arquivo[text()='" + string + "']";
            }
        }
        System.out.println("Expressao para checar existencia de arquivo: " + expressao);
        return expressao;
    }

    @Override
    public boolean salvaXML(Document xml, String nomeUsuario)
            throws RemoteException, TransformerConfigurationException, TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(xml);
        String caminho = PainelDeControle.CAMINHO_XML;
        StreamResult result = new StreamResult(new File(caminho));
        transformer.transform(source, result);
        return true;
    }

}
