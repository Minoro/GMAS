package cliente;

import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import jtree.XMLInfoPanel;
import jtree.XMLMenu;
import jtree.XMLTreePanel;
import utils.PainelDeControle;

public class InterfaceUsuario extends JFrame {
    
    private static final long serialVersionUID = 1L;
    //    public static SistemaArquivo server;
    public static InterfaceUsuario main;
    public static XMLTreePanel panel;
    
    public static void main(String[] args) {
        main = new InterfaceUsuario();
    }
    
    public InterfaceUsuario() {
        new Thread(new AtualizadorArvore()).start();
        
        panel = new XMLTreePanel();
        XMLInfoPanel info = new XMLInfoPanel();
        XMLMenu menu = new XMLMenu();
        
        panel.setDocument(PainelDeControle.xml);
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
    
    class AtualizadorArvore implements Runnable {
        
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(PainelDeControle.INTERVALO_ATUALIZAR_ARVORE * 1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(InterfaceUsuario.class.getName()).log(Level.SEVERE, null, ex);
                }
                XMLTreePanel.atualizaArvore();
            }
        }
        
    }
}
