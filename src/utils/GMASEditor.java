package utils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

/**
 *
 * @author mastelini
 */
public class GMASEditor extends JFrame {

    private JTextArea areaEdicao;
    private String ultimaAlteracao;
    private JFileChooser selecaoArquivo = new JFileChooser();
    private Action _openAction = new OpenAction();
    private Action _saveAction = new SaveAction();

    public static void main(String[] args) {
        new GMASEditor();
    }

    public GMASEditor() {
        areaEdicao = new JTextArea(30, 80);
        areaEdicao.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        areaEdicao.setFont(new Font("monospaced", Font.PLAIN, 14));
        JScrollPane scrollingText = new JScrollPane(areaEdicao);

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.add(scrollingText, BorderLayout.CENTER);
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = menuBar.add(new JMenu("Menu"));
        fileMenu.setMnemonic('M');
        fileMenu.add(_openAction);
        fileMenu.add(_saveAction);

        setContentPane(content);
        setJMenuBar(menuBar);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (!areaEdicao.getText().equals(ultimaAlteracao)) {
                    int retorno = selecaoArquivo.showSaveDialog(GMASEditor.this);
                    if (retorno == JFileChooser.APPROVE_OPTION) {
                        File f = selecaoArquivo.getSelectedFile();
                        try {
                            FileWriter writer = new FileWriter(f);
                            areaEdicao.write(writer);
                        } catch (IOException ioex) {
                            JOptionPane.showMessageDialog(GMASEditor.this, ioex);
                        }
                        System.exit(1);
                    }
                }
                else 
                    System.exit(1);
            }
        }
        );

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle("GMAS Editor");
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private class OpenAction extends AbstractAction {

        public OpenAction() {
            super("Abrir arquivo");
            // putValue(MNEMONIC_KEY, new Integer('O'));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int retorno = selecaoArquivo.showOpenDialog(GMASEditor.this);
            if (retorno == JFileChooser.APPROVE_OPTION) {
                File f = selecaoArquivo.getSelectedFile();
                try {
                    FileReader reader = new FileReader(f);
                    areaEdicao.read(reader, "");
                    ultimaAlteracao = areaEdicao.getText();
                } catch (IOException ioex) {
                    // System.out.println(e);
                    System.exit(1);
                }
            }
        }
    }

    private class SaveAction extends AbstractAction {

        SaveAction() {
            super("Salvar arquivo");
            // putValue(MNEMONIC_KEY, new Integer('S'));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int retorno = selecaoArquivo.showSaveDialog(GMASEditor.this);
            if (retorno == JFileChooser.APPROVE_OPTION) {
                File f = selecaoArquivo.getSelectedFile();
                try {
                    FileWriter writer = new FileWriter(f);
                    areaEdicao.write(writer);
                    ultimaAlteracao = areaEdicao.getText();
                } catch (IOException ioex) {
                    JOptionPane.showMessageDialog(GMASEditor.this, ioex);
                    System.exit(1);
                }
            }
        }
    }
}
