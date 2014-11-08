package jtree;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XMLTreeNode {

    Element element;

    public XMLTreeNode(Element element) {
        this.element = element;
    }

    public Element getElement() {
        return element;
    }

    /**
     * Retorna o valor do nó
     *
     * @return String
     */
    public String toString() {
        return element.getAttribute("nomeFantasia");
    }

    public String getNodeName() {
        return element.getNodeName();
    }
}
