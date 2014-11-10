package Testes;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jtree.XMLInfoPanel;
import jtree.XMLMenu;
import jtree.XMLTreePanel;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import servidor.SistemaArquivo;
import utils.PainelDeControle;

/**
 *
 * @author Matheus
 */
public class Vellone extends JFrame {

    public static SistemaArquivo teste;
    public static String username = "vellone";

    public static void main(String[] args) {
        teste = PainelDeControle.getTeste();
        if (teste == null) {
            System.err.println("Não foi possível pegar uma instância do SistemaArquivo para testar!");
            System.exit(-1);
        }
        new Vellone();
    }

    public Vellone() throws HeadlessException {

        PainelDeControle.xml = pedirXML(username);
        PainelDeControle.username = username;

        Document document = PainelDeControle.xml;
        XMLTreePanel panel = new XMLTreePanel();
        XMLInfoPanel info = new XMLInfoPanel();
        XMLMenu menu = new XMLMenu();

        panel.setDocument(document);
        getContentPane().add(panel, "West");
        getContentPane().add(info, "Center");
        setJMenuBar(menu);
        getContentPane().setPreferredSize(new Dimension(800, 600));
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("GMAS");
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public Document pedirXML(String nomeUsuario) {
        System.out.println("PEDINDO XML LOCAL!!");
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
                bw.write("<raiz nomeFantasia='" + nomeUsuario + "'/>");//salva as informações no arquivo no disco
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
}
