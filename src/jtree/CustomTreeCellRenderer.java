package jtree;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

class CustomeTreeCellRenderer extends DefaultTreeCellRenderer {

    public CustomeTreeCellRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, leaf, expanded, leaf, row, hasFocus);

        if (!leaf) {
            CustomTreeNode node = (CustomTreeNode) value;
            System.out.println(((Employee) node.getUserObject()).name);

            if (node.getIcon() != null) {
                System.out.println(node.getIcon().toString());
                setClosedIcon(node.getIcon());
                setOpenIcon(node.getIcon());
            } else {
                setClosedIcon(getDefaultClosedIcon());
                setClosedIcon(getDefaultOpenIcon());
                setOpenIcon(getDefaultOpenIcon());
            }
        }

        return this;
    }
}
