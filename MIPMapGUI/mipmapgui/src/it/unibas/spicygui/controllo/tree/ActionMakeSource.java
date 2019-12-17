/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicygui.controllo.tree;

import it.unibas.spicy.model.datasource.INode;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.controllo.Scenario;
import it.unibas.spicygui.vista.treepm.TreeNodeAdapter;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openide.util.NbBundle;


public class ActionMakeSource extends AbstractAction{
    private JTree jTree;
    private MappingTask mappingTask;
    private boolean isSourceNodeProxySource;
    
    public ActionMakeSource(JTree jTree, MappingTask mappingTask, boolean isSourceNodeProxySource) {
        this.jTree = jTree;
        this.mappingTask = mappingTask;
        this.isSourceNodeProxySource = isSourceNodeProxySource;
        this.putValue(NAME, NbBundle.getMessage(Costanti.class, Costanti.ACTION_MAKE_SOURCE));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
        TreeNodeAdapter adapter = (TreeNodeAdapter) treeNode.getUserObject();
        INode iNode = adapter.getINode();
        mappingTask.setSourceNode(iNode);
        mappingTask.setSourceNodeProxy(isSourceNodeProxySource);
    }
}
