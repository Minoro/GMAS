/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jtree;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import utils.PainelDeControle;

/**
 *
 * @author Matheus
 */
class XMLTreeCellRenderer extends DefaultTreeCellRenderer {

    private ImageIcon[] XMLTreeIcons
            = {
                new ImageIcon(PainelDeControle.PASTA_ICONES + "pasta.jpg"),
                new ImageIcon(PainelDeControle.PASTA_ICONES + "arquivo.jpg"),
                new ImageIcon(PainelDeControle.PASTA_ICONES + "raiz.jpg"),
            };

    @Override
    // rendering the tree
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        String tag = ((XMLTreeNode) value).getNodeName();
        if (tag.equals(PainelDeControle.TAG_ARQUIVO)){
            setIcon(XMLTreeIcons[1]);
        }else if (tag.equals(PainelDeControle.TAG_PASTA)){
            setIcon(XMLTreeIcons[0]);
        }else{
            setIcon(XMLTreeIcons[2]);
        }
        return this;
    }
}
