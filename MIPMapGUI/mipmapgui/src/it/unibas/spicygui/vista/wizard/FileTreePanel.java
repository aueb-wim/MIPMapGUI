package it.unibas.spicygui.vista.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

public class FileTreePanel extends JPanel {

	protected static FileSystemView fsv = FileSystemView.getFileSystemView();
       // protected static FileSystemView fileSystemView;


	private static class FileTreeCellRenderer extends DefaultTreeCellRenderer {
		/**
		 * Icon cache to speed the rendering.
		 */
		private Map<String, Icon> iconCache = new HashMap<String, Icon>();

		/**
		 * Root name cache to speed the rendering.
		 */
		private Map<File, String> rootNameCache = new HashMap<File, String>();

                
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			FileTreeNode ftn = (FileTreeNode) value;
			File file = ftn.file;
			String filename = "";
                        //puts the expand "+" icon on root nodes
                        tree.expandRow(0);
                        //set the root node as already expanded
                        tree.setShowsRootHandles(true);
			if (file != null) {                            
                            filename = this.rootNameCache.get(file);
                            if (filename == null) {
                                    if (file.isFile()){
                                        filename = file.getName();
                                    }
                                    else{
                                        filename = fsv.getSystemDisplayName(file);
                                    }
                                    //fsv.getSystemTypeDescription(file); equals("Shortcut")                                    
                                    this.rootNameCache.put(file, filename);
                            }
			}
			JLabel result = (JLabel) super.getTreeCellRendererComponent(tree,
					filename, sel, expanded, leaf, row, hasFocus);
			if (file != null) {
				Icon icon = this.iconCache.get(filename);
				if (icon == null) {
					icon = fsv.getSystemIcon(file);
					this.iconCache.put(filename, icon);
				}
				result.setIcon(icon);
			}
			return result;
		}
	}
        
        public File getSelectedFile(){
            FileTreeNode filenode = (FileTreeNode) tree.getLastSelectedPathComponent();
            File file = filenode.file;
            return file;
        }


	private static class FileTreeNode implements TreeNode {

		private File file;
		private File[] children;
		private TreeNode parent;
		private boolean isFileSystemRoot;

		/**
		 * Creates a new file tree node.
		 */
		public FileTreeNode(File file, boolean isFileSystemRoot, TreeNode parent) {
			this.file = file;
			this.isFileSystemRoot = isFileSystemRoot;
			this.parent = parent;
			this.children = this.file.listFiles();
			if (this.children == null)
				this.children = new File[0];
		}

		/**
		 * Creates a new file tree node.
		 */
		public FileTreeNode(File[] children) {
			this.file = null;
			this.parent = null;
			this.children = children;
		}

		public Enumeration<?> children() {
			final int elementCount = this.children.length;
			return new Enumeration<File>() {
                            int count = 0;
                            public boolean hasMoreElements() {
                                return this.count < elementCount;
                            }
                            public File nextElement() {
                                if (this.count < elementCount) {
                                        return FileTreeNode.this.children[this.count++];
                                }
                                throw new NoSuchElementException("Vector Enumeration");
                            }
			};

		}

		public boolean getAllowsChildren() {
			return true;
		}

		public TreeNode getChildAt(int childIndex) {
			return new FileTreeNode(this.children[childIndex],
					this.parent == null, this);
		}

		public int getChildCount() {
			return this.children.length;
		}

		public int getIndex(TreeNode node) {
                    FileTreeNode ftn = (FileTreeNode) node;
			for (int i = 0; i < this.children.length; i++) {
                            if (ftn.file.equals(this.children[i]))
                                    return i;
			}
                    return -1;
		}

		public TreeNode getParent() {
			return this.parent;
		}

		public boolean isLeaf() {
			return (this.getChildCount() == 0);
		}
	}       
        
	/*FilenameFilter textFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                if (lowercaseName.endsWith(".lnk")||dir.isHidden()) {
                        return false;
                }
                else {
                        return true;
                }
            }
        };*/        
	private JTree tree;

	/**
	 * Creates the file tree panel.
	 */
	public FileTreePanel() {
		this.setLayout(new BorderLayout());
                fsv = FileSystemView.getFileSystemView();                          
                File[] roots = fsv.getRoots(); 
                //File rootDirectory = fsv.getHomeDirectory();
                //File[] roots = rootDirectory.listFiles(textFilter);
                //File[] roots = fsv.getFiles(fsv.getHomeDirectory(), true);
		FileTreeNode rootTreeNode = new FileTreeNode(roots);
		this.tree = new JTree(rootTreeNode);                    
		this.tree.setCellRenderer(new FileTreeCellRenderer());
		this.tree.setRootVisible(false);
		final JScrollPane jsp = new JScrollPane(this.tree);
		jsp.setBorder(new EmptyBorder(0, 0, 0, 0));
		this.add(jsp, BorderLayout.CENTER);
                this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}

}