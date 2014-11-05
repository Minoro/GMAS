package jtree;

import model.Arquivo;

import java.awt.Dimension;
import java.util.List;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTextField;
import org.w3c.dom.Element;

public class XMLInfoPanel extends JPanel {

    static List<String> atributos;
    static Map<String, JTextField> map_jtf;

    public XMLInfoPanel() {
        map_jtf = new HashMap<>();
        atributos = new ArrayList();

        int i = 0;
        for (Field field : Arquivo.class.getDeclaredFields()) {
            //Para nao exibir o nome da pasta/arquivo, uma vez que já é exibido na árvore de hierarquia
            String variavel = field.getName();
            if (variavel.equals("nome")) {
                continue;
            }

            JTextField jtf = new JTextField();
            jtf.setEditable(false);
            jtf.setPreferredSize(new Dimension(400, 69));
            jtf.setLocation(0, 50 * i);
            map_jtf.put(variavel, jtf);
            atributos.add(variavel);
            add(jtf);
            i++;
        }
    }

    public static void alteraInfo(XMLTreeNode node) {
        Element no = node.getElement();
        for (String atributo : atributos) {
            String propriedade = atributo.replaceAll("([A-Z])", " $1").toUpperCase();
            String valor = no.getAttribute(atributo);
            if (valor.equals("")) {
                //String para quando o valor do atributo está vazio
                valor = "";
            }

            map_jtf.get(atributo).setText(propriedade + ": " + valor);
        }
    }

}
