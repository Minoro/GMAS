/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forms;

import cliente.InterfaceUsuario;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jtree.XMLTreePanel;
import utils.PainelDeControle;

/**
 *
 * @author Matheus
 */
public class CopiarArquivo extends DefaultDialog {

    String arquivoCopiado;

    /**
     * Creates new form CopiarArquivo
     */
    public CopiarArquivo(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        String tipoNoSelecionado = XMLTreePanel.node_selecionado.getNodeName();
        if (!tipoNoSelecionado.equals(PainelDeControle.TAG_ARQUIVO)) {
            JOptionPane.showMessageDialog(InterfaceUsuario.main, "Não é possível copiar uma " + tipoNoSelecionado);
            return;
        }
        arquivoCopiado = XMLTreePanel.getCaminhoSelecionado(false);
        JOptionPane.showMessageDialog(InterfaceUsuario.main, "Selecione o destino da cópia do arquivo e clique em OK");

        initComponents();
        setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jButton1.setText("OK");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        String caminhoDestino = XMLTreePanel.getCaminhoSelecionado(false);

        try {
            PainelDeControle.middleware.copiarArquivo(arquivoCopiado, caminhoDestino);
        } catch (RemoteException ex) {
            Logger.getLogger(CopiarArquivo.class.getName()).log(Level.SEVERE, null, ex);
        }
        close();
    }//GEN-LAST:event_jButton1MouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    // End of variables declaration//GEN-END:variables
}
