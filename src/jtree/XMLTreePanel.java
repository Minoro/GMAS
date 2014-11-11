package jtree;

import cliente.InterfaceUsuario;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.w3c.dom.Document;
import utils.GMASEditor;

import utils.PainelDeControle;

public class XMLTreePanel extends JPanel {

    public static JTree tree;
    public static XMLTreeNode node_selecionado;
    private XMLTreeModel model;
//    private XMLInfoPanel

    public XMLTreePanel() {
        XMLTreeCellRenderer renderer = new XMLTreeCellRenderer();

        setLayout(new BorderLayout());
        model = new XMLTreeModel();
        tree = new JTree();
        tree.setModel(model);
        tree.setShowsRootHandles(true);
        tree.setEditable(false);
        tree.setCellRenderer(renderer);

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
                    text.setText("Tipo de Elemento: " + ((XMLTreeNode) lpc).getNodeName());
                    node_selecionado = (XMLTreeNode) lpc;
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
                        if (((XMLTreeNode) selPath.getLastPathComponent()).getNodeName().equals(PainelDeControle.TAG_ARQUIVO)) {
                            String conteudo;
                            try {
                                String caminho = getCaminhoSelecionado(false);
                                conteudo = PainelDeControle.middleware.lerArquivo(caminho);
                                new GMASEditor(conteudo, caminho);
                            } catch (RemoteException ex) {
                                Logger.getLogger(XMLTreePanel.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
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

    /**
     * Monta uma String com o caminho do item selecionado. Se o item selecionado
     * é um arquivo e o parametro é true, o último elemento é excluido do
     * caminho
     *
     * @param excluirUltimo boolean - true: excluir o último item do caminho,
     * caso este seja um arquivo; false: não tira o último elemento, seja um
     * arquivo ou não
     * @return String - caminho do item selecionado na árvore de hierarquia
     */
    public static String getCaminhoSelecionado(boolean excluirUltimo) {
        TreePath tp = tree.getSelectionPath();
        String caminho = "raiz/";
        if (tp != null) {
            Object[] no = (Object[]) tp.getPath();
            //Monta a 'url' do elemento que está selecionado
            for (Object obj : no) {
                caminho += obj.toString() + "/";
            }
            // Se o último selecionado é um arquivo, retira o último elemento
            if (((XMLTreeNode) tp.getLastPathComponent()).getNodeName().equals(PainelDeControle.TAG_ARQUIVO) && excluirUltimo) {
                caminho = caminho.substring(0, caminho.length() - 1);
                caminho = caminho.substring(0, caminho.lastIndexOf("/"));
            }
        } else {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, "Não há nenhum caminho selecionado.\nSelecione um elemento na árvore de hierarquia e tente novamente");
        }
        // Retira o nome da raiz, pois a função montaExpressao já inclui a raiz por padrão
        caminho = caminho.substring(caminho.indexOf("/") + 1, caminho.length());
        System.out.println("Caminho do item selecionado: " + caminho + ".");
        return caminho;
    }

    /**
     * Função para atualizar a árvore de hierarquia após modificar o XML
     */
    public static void atualizaArvore() {
        try {
            PainelDeControle.xml = PainelDeControle.middleware.pedirXML();
        } catch (RemoteException ex) {
            Logger.getLogger(XMLTreePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        InterfaceUsuario.panel.setDocument(PainelDeControle.xml);
//        PainelDeControle.xml = Vellone.pedirXML(Vellone.username);
//        Vellone.panel.setDocument(PainelDeControle.xml);
    }
}
