package jtree;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import servidor.SistemaArquivo;
import servidor.SistemaArquivoInterface;
import utils.PainelDeControle;

public class DemoMain extends JFrame {

    public static SistemaArquivoInterface server;
    public static DemoMain main;
//    SistemaArquivoInterface server;

    public static void main(String[] args) {
        main = new DemoMain();
    }

    public DemoMain() {
        Document document = null;
        try {
            //servidor RMI
            server = (SistemaArquivoInterface) Naming.lookup(PainelDeControle.middleware.montaURL_RMI("localhost"));
            //teste local
//            server = new SistemaArquivo();
            document = server.pedirXML(PainelDeControle.username);

//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder builder;
//            builder = dbFactory.newDocumentBuilder();
//            String caminho_xml = PainelDeControle.PASTA_XML + PainelDeControle.username + ".xml";
//            document = builder.parse(new File(caminho_xml));
//            document.normalize();
            PainelDeControle.xml = document;
        } catch (IOException ex) {
            Logger.getLogger(DemoMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            Logger.getLogger(DemoMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(DemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }
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
}
