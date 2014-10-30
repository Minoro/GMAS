package jtree;

import java.awt.Dimension;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import servidor.SistemaArquivoInterface;
import utils.PainelDeControle;

public class DemoMain extends JFrame {

//    public static SistemaArquivo server;
    public static SistemaArquivoInterface server;
    public static DemoMain main;

    public static void main(String[] args) {
        main = new DemoMain();
    }

    public DemoMain() {
        Document document = null;
        try {
        //servidor RMI
        server = (SistemaArquivoInterface) Naming.lookup(PainelDeControle.middleware.montaURL_RMI("localhost"));
            //teste local
//        server = new SistemaArquivo();
            document = server.pedirXML(PainelDeControle.username);
        } catch (RemoteException | XPathExpressionException | NotBoundException | MalformedURLException ex) {
            Logger.getLogger(DemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        PainelDeControle.xml = document;
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
