package utils;

import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import jtree.XMLTreePanel;

/**
 *
 * @author mastelini
 */
public class GMASEditor extends JFrame {

    private JTextArea areaEdicao;

    public GMASEditor(String conteudo, final String caminho) {
        areaEdicao = new JTextArea(30, 80);
        areaEdicao.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        areaEdicao.setFont(new Font("monospaced", Font.PLAIN, 14));
        JScrollPane scrollingText = new JScrollPane(areaEdicao);
        areaEdicao.setText(conteudo);

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.add(scrollingText, BorderLayout.CENTER);
        JMenuBar menuBar = new JMenuBar();

        JMenu salvar = new JMenu("Salvar");
        salvar.setMnemonic('S');
        salvar.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                String conteudo = areaEdicao.getText();
                try {
                    PainelDeControle.middleware.salvarArquivo(XMLTreePanel.getCaminhoSelecionado(false), conteudo);
                    JOptionPane.showMessageDialog(GMASEditor.this, "O arquivo foi salvo!");
                } catch (RemoteException ex) {
                    Logger.getLogger(GMASEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        menuBar.add(salvar);

        setContentPane(content);
        setJMenuBar(menuBar);

//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    PainelDeControle.middleware.unlock(caminho);
                } catch (RemoteException ex) {
                    Logger.getLogger(GMASEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
                dispose();
            }
        }
        );

        setTitle("GMAS Editor");
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
