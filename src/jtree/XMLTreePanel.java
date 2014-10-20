package jtree;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

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
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                Object lpc = e.getPath().getLastPathComponent();
                if (lpc instanceof XMLTreeNode) {
                    text.setText(((XMLTreeNode) lpc).getNodeName());
                    XMLInfoPanel.alteraInfo((XMLTreeNode) lpc);
                }
            }
        });
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    if (e.getClickCount() == 2) {
                        XMLTreeNode no = (XMLTreeNode) selPath.getLastPathComponent();
                        System.out.println("Double Click em " + no.toString() + "!");
                    }
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
