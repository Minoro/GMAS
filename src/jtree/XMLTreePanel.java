package jtree;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.w3c.dom.Document;

public class XMLTreePanel extends JPanel {

    private JTree tree;
    private XMLTreeModel model;
//    private XMLInfoPanel

    public XMLTreePanel() {
        setLayout(new BorderLayout());

        model = new XMLTreeModel();
        tree = new JTree();
        tree.setModel(model);
        tree.setShowsRootHandles(true);
        tree.setEditable(false);

        JScrollPane pane = new JScrollPane(tree);
        pane.setPreferredSize(new Dimension(400, 600));

        add(pane, "Center");

        final JTextField text = new JTextField();
        text.setEditable(false);
        add(text, "South");

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                Object lpc = e.getPath().getLastPathComponent();
                if (lpc instanceof XMLTreeNode) {
                    text.setText(((XMLTreeNode) lpc).getNodeName());
                    XMLInfoPanel.alteraInfo((XMLTreeNode) lpc);
                }
            }
        });

    }

    /* methods that delegate to the custom model */
    public void setDocument(Document document) {
        model.setDocument(document);
    }

    public Document getDocument() {
        return model.getDocument();
    }

}
