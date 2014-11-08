/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jtree;

import cliente.InterfaceUsuario;
import forms.CopiarArquivo;
import forms.MoverArquivo;
import forms.NovaPasta;
import forms.NovoArquivo;
import forms.Renomear;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import utils.PainelDeControle;

/**
 *
 * @author Matheus
 */
public class XMLMenu extends JMenuBar {

    public XMLMenu() {
        JMenu menu;
        JMenuItem item;

        menu = new JMenu("Ações");

        item = new JMenuItem("Copiar Arquivo");
        item.addMouseListener(new MouseAdapter() {
            String nome_da_pasta;

            @Override
            public void mousePressed(MouseEvent e) {
                new CopiarArquivo(InterfaceUsuario.main, false);
            }
        });
        menu.add(item);
        
        item = new JMenuItem("Mover Arquivo");
        item.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                new MoverArquivo(InterfaceUsuario.main, false);
            }
        });
        menu.add(item);

        item = new JMenuItem("Renomear");
        item.addMouseListener(new MouseAdapter() {
            String nome_da_pasta = "Novo Arquivo";

            @Override
            public void mousePressed(MouseEvent e) {
                new Renomear(InterfaceUsuario.main, true);
            }
        });
        menu.add(item);

        item = new JMenuItem("Novo Arquivo");
        item.addMouseListener(new MouseAdapter() {
            String nome_da_pasta = "Novo Arquivo";

            @Override
            public void mousePressed(MouseEvent e) {
                new NovoArquivo(InterfaceUsuario.main, true);
            }
        });
        menu.add(item);

        item = new JMenuItem("Nova Pasta");
        item.addMouseListener(new MouseAdapter() {
            String nome_da_pasta = "Nova Pasta";

            @Override
            public void mousePressed(MouseEvent e) {
                new NovaPasta(InterfaceUsuario.main, true);
            }
        });
        menu.add(item);

        item = new JMenuItem("Excluir Arquivo");
        item.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                XMLTreeNode arquivo = XMLTreePanel.node_selecionado;
                String caminho = XMLTreePanel.getCaminhoSelecionado(false);
                caminho = caminho.substring(0, caminho.length() - 1);
                int resposta = JOptionPane.showConfirmDialog(
                        InterfaceUsuario.main,
                        "Deseja realmente apagar este arquivo " + arquivo.toString() + "(" + caminho + ") ?",
                        "Confirmação",
                        JOptionPane.YES_NO_OPTION
                );
                if (resposta == JOptionPane.YES_OPTION) {
                    try {
                        PainelDeControle.middleware.deletarArquivo(caminho);
                    } catch (RemoteException ex) {
                        Logger.getLogger(XMLMenu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                XMLTreePanel.atualizaArvore();
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
