package cliente;

import java.awt.Dimension;

import javax.swing.JFrame;
import jtree.XMLInfoPanel;
import jtree.XMLMenu;
import jtree.XMLTreePanel;
import org.w3c.dom.Document;
import utils.PainelDeControle;

public class InterfaceUsuario extends JFrame {

    private static final long serialVersionUID = 1L;
    //    public static SistemaArquivo server;
    public static InterfaceUsuario main;

    public static void main(String[] args) {
        main = new InterfaceUsuario();
    }

    public InterfaceUsuario() {
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
}
