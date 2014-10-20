package jtree;

import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import utils.PainelDeControle;

public class DemoMain extends JFrame {

    public static void main(String[] args) {
        new DemoMain();
    }

    public DemoMain() {
        PainelDeControle.middleware.pedirXML();

        Document document = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            document = builder.parse(new File("demo.xml"));
            document.normalize();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
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
