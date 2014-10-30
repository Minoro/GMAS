/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forms;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.xml.xpath.XPathExpressionException;
import jtree.DemoMain;
import jtree.XMLTreeNode;
import jtree.XMLTreePanel;
import utils.PainelDeControle;

/**
 *
 * @author Matheus
 */
public class Renomear extends DefaultDialog {
	private static final long serialVersionUID = 1L;
	/**
     * Creates new form Renomear
     */
    public Renomear(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        XMLTreeNode node = XMLTreePanel.node_selecionado;
        String tipo_de_no = node.getNodeName();
        if (tipo_de_no.equals(PainelDeControle.TAG_ARQUIVO)) {
            label.setText("Nome do arquivo");
        } else if (tipo_de_no.equals((PainelDeControle.TAG_PASTA))) {
            label.setText("Nome da pasta");
        } else {
            JOptionPane.showMessageDialog(DemoMain.main, "Você não pode renomear o tipo de nó " + tipo_de_no);
            this.close();
        }
        nome.setText(node.toString());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        nome = new javax.swing.JTextField();
        label = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jButton1.setText("Renomear");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        label.setText("Nome do");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nome, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1))
                    .addComponent(label))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        //Pega o nome da pasta selecionada e retira a última '/'
        String caminho = XMLTreePanel.getCaminhoSelecionado(false);
        caminho = caminho.substring(0, caminho.length() - 1);
        String nome_digitado = nome.getText();
        //Se o caminho termina com .txt e o nome digitado nao termina com .txt, adiciona .txt no final do novo nome
        if (caminho.endsWith(".txt") && !nome_digitado.endsWith(".txt")) {
            nome_digitado += ".txt";
        }
        System.out.println("Caminho do arquivo/pasta: " + caminho);
        System.out.println("Novo nome: " + nome_digitado);
        //Se for renomear algo que nao é um arquivo, e colocar extensao de arquivo, nao deixa.
        if (!caminho.endsWith(".txt") && nome_digitado.endsWith(".txt")) {
            JOptionPane.showMessageDialog(DemoMain.main, "Você não pode adicionar uma extensão de arquivo (.txt) em uma pasta");
            return;
        }
        try {
            if (DemoMain.server.renomearArquivo(caminho, nome_digitado)) {
                JOptionPane.showMessageDialog(DemoMain.main, "Arquivo/Pasta renomeado para " + nome_digitado);
            } else {
                JOptionPane.showMessageDialog(DemoMain.main, "Arquivo para renomeação não existente");
            }
        } catch (RemoteException | XPathExpressionException ex) {
            Logger.getLogger(Renomear.class.getName()).log(Level.SEVERE, null, ex);
        }
        close();
    }//GEN-LAST:event_jButton1MouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Renomear.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Renomear.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Renomear.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Renomear.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Renomear dialog = new Renomear(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel label;
    private javax.swing.JTextField nome;
    // End of variables declaration//GEN-END:variables
}
