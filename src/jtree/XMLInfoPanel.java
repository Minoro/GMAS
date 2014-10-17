package jtree;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;
import org.w3c.dom.Element;

public class XMLInfoPanel extends JPanel {

    static JTextField criacao;
    static JTextField modificacao;

    public XMLInfoPanel() {
        setLayout(new BorderLayout());

        criacao = new JTextField();
        criacao.setEditable(false);
        add(criacao);
    }

    public static void alteraInfo(XMLTreeNode node) {
        Element no = node.getElement();
        criacao.setText("Data de criação: " + no.getAttribute("criado"));
    }

}
