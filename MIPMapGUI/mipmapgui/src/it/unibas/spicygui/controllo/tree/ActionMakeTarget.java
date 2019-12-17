/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibas.spicygui.controllo.tree;

import it.unibas.spicy.model.correspondence.ValueCorrespondence;
import it.unibas.spicy.model.datasource.INode;
import it.unibas.spicy.model.datasource.JoinCondition;
import it.unibas.spicy.model.expressions.Expression;
import it.unibas.spicy.model.mapping.IDataSourceProxy;
import it.unibas.spicy.model.mapping.MappingTask;
import it.unibas.spicy.model.paths.PathExpression;
import it.unibas.spicy.model.paths.operators.GeneratePathExpression;
import it.unibas.spicy.persistence.xml.DAOXmlUtility;
import it.unibas.spicygui.Costanti;
import it.unibas.spicygui.vista.JLayeredPaneCorrespondences;
import it.unibas.spicygui.vista.MappingTaskTopComponent;
import it.unibas.spicygui.vista.treepm.TreeNodeAdapter;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public class ActionMakeTarget extends AbstractAction{
        
    private JLayeredPaneCorrespondences jLayeredPane;
    private JTree jTree;
    private MappingTask mappingTask;
    private boolean isTargetNodeProxySource;
    private GeneratePathExpression pathGenerator = new GeneratePathExpression();
    
    public ActionMakeTarget(JTree jTree, JLayeredPaneCorrespondences jLayeredPane, MappingTask mappingTask, boolean isTargetNodeProxySource) {
        this.jTree = jTree;
        this.mappingTask = mappingTask;
        this.jLayeredPane = jLayeredPane;
        this.isTargetNodeProxySource = isTargetNodeProxySource;
        this.putValue(NAME, NbBundle.getMessage(Costanti.class, Costanti.ACTION_MAKE_TARGET));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
        TreeNodeAdapter adapter = (TreeNodeAdapter) treeNode.getUserObject();
        INode targetNode = adapter.getINode();
        INode sourceNode = mappingTask.getSourceNode();
        if (sourceNode!=null && sourceNode!=targetNode){
            //source to target connection
            if (mappingTask.isSourceNodeProxySource() && !this.isTargetNodeProxySource){
                createConnection(sourceNode, targetNode);
            }
            //target to source warning
            else if (!mappingTask.isSourceNodeProxySource() && this.isTargetNodeProxySource){
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.MESSAGE_TARGET_TO_SOURCE_CONNECTION), DialogDescriptor.WARNING_MESSAGE));

            }
            //join constraint
            else {
                IDataSourceProxy dataSource = null;
                if (mappingTask.isSourceNodeProxySource())
                    dataSource = mappingTask.getSourceProxy();
                else
                    dataSource = mappingTask.getTargetProxy(); 
                createConstraint(sourceNode, targetNode, dataSource);
            }
            mappingTask.setSourceNode(null);
        } else if (sourceNode==null){
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.MESSAGE_NO_SOURCE_SELECTED), DialogDescriptor.WARNING_MESSAGE));
        } else{
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(Costanti.class, Costanti.MESSAGE_SOURCE_SAME_AS_TARGET), DialogDescriptor.WARNING_MESSAGE));
        }
    }
    
    private void createConnection(INode sourceNode, INode targetNode){
        PathExpression sourcePathExpression = pathGenerator.generatePathFromRoot(sourceNode);
        PathExpression targetPathExpression = pathGenerator.generatePathFromRoot(targetNode);
        List<PathExpression> sourcePathExpressions = new ArrayList<PathExpression>();
        sourcePathExpressions.add(sourcePathExpression);
        Expression transformationFunctionExpression = new Expression(DAOXmlUtility.cleanXmlString(sourcePathExpression.toString()));
        ValueCorrespondence vc = new ValueCorrespondence(sourcePathExpressions,null,targetPathExpression,transformationFunctionExpression,1.0);
        mappingTask.addCorrespondence(vc);
        recreateTree();
    }
    
    private void createConstraint(INode sourceNode, INode targetNode, IDataSourceProxy dataSource){
        List<PathExpression> fromPaths = new ArrayList<PathExpression>();
        List<PathExpression> toPaths = new ArrayList<PathExpression>();        
        PathExpression fromPathExpression = pathGenerator.generatePathFromRoot(sourceNode);
        PathExpression toPathExpression = pathGenerator.generatePathFromRoot(targetNode);
        fromPaths.add(fromPathExpression);
        toPaths.add(toPathExpression);
        JoinCondition joinCondition = new JoinCondition(fromPaths, toPaths);
        dataSource.addJoinCondition(joinCondition);
        recreateTree();
    }
    
    private void recreateTree() {
        MappingTaskTopComponent mappingTaskTopComponent = jLayeredPane.getMappingTaskTopComponent();
        mappingTaskTopComponent.lightclear();
        mappingTaskTopComponent.drawScene();
        mappingTaskTopComponent.drawConnections();
    }
}
