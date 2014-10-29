/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forms;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import jtree.XMLTreePanel;

/**
 *
 * @author Matheus
 */
public class DefaultDialog extends JDialog {

    public DefaultDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

    }
    
    protected void close() {
        XMLTreePanel.atualizaArvore();
        WindowEvent winClosingEvent = new WindowEvent(this, WindowEvent.WINDOW_CLOS­ING);
        Toolkit.getDefaultToolkit().getSystemEve­ntQueue().postEvent(winClosingEvent);
    }

}
