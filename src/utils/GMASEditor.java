package utils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import jtree.XMLTreePanel;

/**
 *
 * @author mastelini
 */
public class GMASEditor extends JFrame {

    private JTextArea areaEdicao;
    private JFileChooser selecaoArquivo = new JFileChooser();

    public GMASEditor(String conteudo) {
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
                PainelDeControle.middleware.salvarArquivo(XMLTreePanel.getCaminhoSelecionado(false), conteudo);
            }
        });

        menuBar.add(salvar);

        setContentPane(content);
        setJMenuBar(menuBar);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("GMAS Editor");
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
