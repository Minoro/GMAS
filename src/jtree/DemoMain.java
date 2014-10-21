package jtree;

import java.awt.Dimension;
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import servidor.SistemaArquivoInterface;
import utils.PainelDeControle;

public class DemoMain extends JFrame {

    SistemaArquivoInterface server;
    public static void main(String[] args) {
        new DemoMain();
    }

    public DemoMain() {
        try {
            server = (SistemaArquivoInterface) Naming.lookup(PainelDeControle.middleware.montaURL_RMI("192.168.1.104"));
        } catch (NotBoundException | MalformedURLException | RemoteException ex) {
            Logger.getLogger(DemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        PainelDeControle.middleware.pedirXML();
        Document document = null;

        try {
            document = server.pedirXML("vellone");
        } catch (RemoteException ex) {
            Logger.getLogger(DemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(document == null){
            System.out.println("tá null!");
        }else{
            System.out.println("! tá null!");
        }
        
        System.exit(1);

        XMLTreePanel panel = new XMLTreePanel();
        XMLInfoPanel info = new XMLInfoPanel();
        panel.setDocument(document);
        getContentPane().add(panel, "West");
        getContentPane().add(info, "Center");
        getContentPane().setPreferredSize(new Dimension(800, 600));
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("GMAS");
        setLocationRelativeTo(null);
        setVisible(true);
    }

}
