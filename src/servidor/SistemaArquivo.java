package servidor;

import java.io.File;
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

import model.Arquivo;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import utils.PainelDeControle;

public class SistemaArquivo extends UnicastRemoteObject implements SistemaArquivoInterface {

    public static void main(String[] args) {
        try {
           System.out.println("Servidor iniciado");
           
           Policy.setPolicy(new MyPolicy());           
           System.setSecurityManager(new RMISecurityManager());
           
           
           SistemaArquivo sistemaArquivo =  new SistemaArquivo();
           Naming.rebind("rmi://:/teste", sistemaArquivo);
       
        } catch (IOException ex) {
            Logger.getLogger(SistemaArquivo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected SistemaArquivo() throws RemoteException, IOException {
        super();
        // TODO Auto-generated constructor stub
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
    
    public static class MyPolicy extends Policy {
     	@Override
        public PermissionCollection getPermissions(CodeSource codesource) {
            Permissions p = new Permissions();
            p.add(new java.security.AllPermission());          
            return p;
        }
    }

    @Override
    public boolean criarArquivo(String caminho) throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Document pedirXML(String nomeUsuario) {
        Document retorno;
        String caminho = PainelDeControle.HOME + PainelDeControle.SEPARADOR + PainelDeControle.RAIZ + PainelDeControle.SEPARADOR + nomeUsuario + ".xml";
        System.out.println("Caminho do XML => " + caminho);
        File file = new File(caminho);
        if (!file.exists()) {
            try {
                file.createNewFile();
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
    public boolean deletarArquivo(String caminho) throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean renomearArquivo(String caminhoOrigem, String caminhoDestino)
            throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean moverArquivo(String caminhoOrigem, String caminhoDestino)
            throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean copiarArquivo(String caminhoOrigem, String caminhoDestino)
            throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String lerArquivo(String caminho) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean escreverArquivo(String caminho, String texto)
            throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean escreverArquivo(String caminho, String texto, int posicao)
            throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Arquivo getAtributes(String caminho) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAtributes(String caminho, Arquivo arquivo)
            throws RemoteException {
        // TODO Auto-generated method stub

    }

}
