package jtree;

import java.awt.Dimension;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import org.w3c.dom.Document;
import servidor.SistemaArquivoInterface;
import utils.PainelDeControle;

public class DemoMain extends JFrame {

    SistemaArquivoInterface server;

    public static void main(String[] args) {
        new DemoMain();
    }

    public DemoMain() {
        Document document = null;
        try {
            server = (SistemaArquivoInterface) Naming.lookup(PainelDeControle.middleware.montaURL_RMI("192.168.1.104"));
            document =server.pedirXML("vellone");
        } catch (NotBoundException | MalformedURLException | RemoteException ex) {
            Logger.getLogger(DemoMain.class.getName()).log(Level.SEVERE, null, ex);
        }

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
