/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jtree;

import forms.NovaPasta;
import forms.NovoArquivo;
import forms.Renomear;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author Matheus
 */
public class XMLMenu extends JMenuBar {

    public XMLMenu() {
        JMenu menu;
        JMenuItem item;

        menu = new JMenu("Ações");

        item = new JMenuItem("Renomear");
        item.addMouseListener(new MouseAdapter() {
            String nome_da_pasta = "Novo Arquivo";

            @Override
            public void mousePressed(MouseEvent e) {
                Renomear renomear = new Renomear(DemoMain.main, true);
                renomear.setVisible(true);
            }
        });
        menu.add(item);
        
        item = new JMenuItem("Novo Arquivo");
        item.addMouseListener(new MouseAdapter() {
            String nome_da_pasta = "Novo Arquivo";

            @Override
            public void mousePressed(MouseEvent e) {
                NovoArquivo na = new NovoArquivo(DemoMain.main, true);
                na.setVisible(true);
            }
        });
        menu.add(item);
        
        item = new JMenuItem("Nova Pasta");
        item.addMouseListener(new MouseAdapter() {
            String nome_da_pasta = "Nova Pasta";

            @Override
            public void mousePressed(MouseEvent e) {
                NovaPasta np = new NovaPasta(DemoMain.main, true);
                np.setVisible(true);
            }
        });
        menu.add(item);

        menu.add(new JPopupMenu.Separator());
        
        item = new JMenuItem("Sair");
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.exit(0);
            }
        });
        menu.add(item);

        add(menu);
        
        menu = new JMenu("Atualizar Árvore");
        menu.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                XMLTreePanel.atualizaArvore();
            }
        });
        add(menu);
    }

}
