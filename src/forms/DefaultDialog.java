package forms;

import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

import jtree.XMLTreePanel;

/**
 * @author Matheus
 */
public class DefaultDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    public DefaultDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

    }

    protected void close() {
        XMLTreePanel.atualizaArvore();
        WindowEvent winClosingEvent = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(winClosingEvent);
    }

}
