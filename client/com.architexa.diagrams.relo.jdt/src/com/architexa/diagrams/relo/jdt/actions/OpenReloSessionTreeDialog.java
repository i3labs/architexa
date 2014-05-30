/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */

/*
 * Created on Aug 26, 2005
 */
package com.architexa.diagrams.relo.jdt.actions;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.architexa.diagrams.model.DocPath;
import com.architexa.diagrams.relo.ReloPlugin;


public class OpenReloSessionTreeDialog extends Dialog {
    private final TreeNode rootTNode;

    private final LabelProvider lblProvider;
    private StructuredViewer treeViewer;
    private final List<?> navPaths;
    
    private static class TreeNode {
        public Object node;
        public Object reason;                   // pointer to creator of this tree node
        public List<TreeNode> children;
        private TreeNode(Object node, Object reason) {
            this.node = node;
            this.reason = reason;
            this.children = null;
        }
        public TreeNode() {
            this.node = null;
            this.children = new ArrayList<TreeNode>(5);
        }
        public TreeNode addTNodeChild(TreeNode childTNode) {
            if (children == null)
                children = new ArrayList<TreeNode>(5);
            children.add(childTNode);
            return childTNode;
        }
        public TreeNode addChild(Object child, Object reason) {
            return addTNodeChild( new TreeNode(child, reason) );
        }

        private TreeNode findOrCreateTNode(Map<Object, TreeNode> treeNdxMap, Object node, Object reason) {
            TreeNode parentTNode = this;
            TreeNode retTNode = treeNdxMap.get(node);
            if (retTNode == null) {
                retTNode = parentTNode.addChild(node, reason);
                treeNdxMap.put(node, retTNode);
                //System.err.println("\tAdded: " + dump(parentTNode) + " / " + dump(retTNode));
            }
            return retTNode;
        }
        // difference from above is that when a path exists we don't just switch over to it, because the parent must also match
        // makes sure that this child is a node of this parent, i.e. doesn't matter if it is in the tree in other places as well
        private TreeNode createTNode(Map<Object, TreeNode> treeNdxMap, Object newNode, Object reason) {
            TreeNode parentTNode = this;
            // check for dupes
            if (parentTNode.children != null) {
                for(TreeNode child: parentTNode.children) {
                    if (child.node.equals(newNode)) {
                        treeNdxMap.put(newNode, child);
                        return child;
                    }
                }
            }
            TreeNode retTNode = parentTNode.addChild(newNode, reason);
            treeNdxMap.put(newNode, retTNode);
            //System.err.println("\tAdded: " + dump(parentTNode) + " / " + dump(retTNode));
            return retTNode;
        }
    };

    
    private final class NavPathsLabelProvider extends JavaElementLabelProvider {
        public NavPathsLabelProvider() {
            super(SHOW_SMALL_ICONS | SHOW_PARAMETERS);
        }
        @Override
        public String getText(Object element) {
            if (element == null) return null;
            if (element instanceof TreeNode) {
                TreeNode tn = (TreeNode) element;
                if (tn.node instanceof IJavaElement) return super.getText(tn.node);
            }
            System.err.println("Unexpected element for getText. Type: " + element.getClass());
            if (element instanceof IJavaElement)
                return super.getText(element);
                //return ((IJavaElement)element).getElementName();
            if (element instanceof DocPath) {
                return "-> " + getText( ((DocPath)element).src );
            }
            return element.toString();
        }
        @Override
        public Image getImage(Object element) {
            if (element instanceof TreeNode) {
                TreeNode tn = (TreeNode) element;
                if (tn.node instanceof IJavaElement) return super.getImage(tn.node);
            }
            System.err.println("Unexpected element for getImage. Type: " + element.getClass());
            if (element instanceof IJavaElement)
                return super.getImage(element);
            else if (element instanceof DocPath)
                return super.getImage( ((DocPath)element).dst );
            return null;
        }
    }
    private class TreeContentProvider implements ITreeContentProvider {

        public Object[] getElements(Object inputElement) {
            return rootTNode.children.toArray();
        }

        public Object[] getChildren(Object parentElement) {
            if (hasChildren(parentElement))
                return ((TreeNode) parentElement).children.toArray();
            else
                return new Object[] {};
        }

        public boolean hasChildren(Object parentElement) {
            if (((TreeNode) parentElement).children == null)
                return false;
            else
                return true;
        }

        public Object getParent(Object element) {
            return null;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    };

    // this is just a method for debugging (hence suppressing unused)
    @SuppressWarnings("unused")
	private static String dump(Object node) {
        if (node == null) {
            return "{null}";
        } else if (node instanceof IJavaElement) {
            return ((IJavaElement)node).getElementName();
        } else if (node instanceof DocPath) {
            DocPath dp = (DocPath) node;
            return dump(dp.src) +  " --> " + dump(dp.dst);
        } else if (node instanceof TreeNode) {
            TreeNode tn = (TreeNode) node;
            if (tn.children != null)
                return dump(tn.node) + "[" + tn.children.size() + "]"; 
            else
                return dump(tn.node) + "[]"; 
        } else
            return node.toString();
    }

    
    public OpenReloSessionTreeDialog(Shell shell, List<?> navPaths) {
        super(shell);
        this.navPaths = navPaths;
        TreeNode convertedRootTNode = convertPathsToTree(navPaths);
        this.rootTNode = addProjectTNode(convertedRootTNode);
        this.lblProvider = new NavPathsLabelProvider();
    }

    private static TreeNode convertPathsToTree(List<?> navPaths) {
        TreeNode rootTNode = new TreeNode();
        TreeNode prevParentTNode = rootTNode;
        Map<Object,TreeNode> treeNdxMap = new HashMap<Object,TreeNode>();
        for (Object node: navPaths) {
            TreeNode tNode;

            //System.err.println("processing: " + dump(node));

            if (node instanceof DocPath) {
                DocPath dp = (DocPath) node;
                TreeNode srcTNode = prevParentTNode.findOrCreateTNode(treeNdxMap, dp.src, node);
                tNode = srcTNode.createTNode(treeNdxMap, dp.dst, node);

                prevParentTNode = tNode;
            } else {
                tNode = prevParentTNode.findOrCreateTNode(treeNdxMap, node, node);

                //prevParentTNode = tNode;
            }
        }
        return rootTNode;
    }

    private static TreeNode addProjectTNode(TreeNode rootTNode) {
        TreeNode newRootTNode = new TreeNode();
        
        Map<IJavaProject, TreeNode> projTNodeMap = new HashMap<IJavaProject, TreeNode>();
        for (TreeNode topChildTNode: rootTNode.children) {
            if (!(topChildTNode.node instanceof IJavaElement)) continue;
            IJavaProject curProj = ((IJavaElement)topChildTNode.node).getJavaProject();
            TreeNode curProjTNode = projTNodeMap.get(curProj);
            if (curProjTNode == null) {
                curProjTNode = newRootTNode.addChild(curProj, curProj);
                projTNodeMap.put(curProj, curProjTNode);
            }
            curProjTNode.addTNodeChild(topChildTNode);
        }
        return newRootTNode;
    }

    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Select Paths");
     }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dlgComposite = (Composite)super.createDialogArea(parent);

        Composite composite = new Composite(dlgComposite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Label lbl = new Label(composite, SWT.NO_FOCUS);
        lbl.setText("Select path to open in Relo Session:");
        
        ToolBar imgTB = new ToolBar(composite, SWT.FLAT | SWT.HORIZONTAL);
        ToolItem imgToolItem = new ToolItem(imgTB, SWT.PUSH);
        try {
            URL url = ReloPlugin.getDefault().getBundle().getEntry("icons/remove.gif");
            imgToolItem.setImage(new Image(Display.getDefault(), url.openStream()));
        } catch (IOException e) {
            imgToolItem.setText("x");
        }
        imgToolItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                removeSelected();
            }
        });
        imgTB.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        treeViewer = new TreeViewer(composite, SWT.BORDER);
        GridData treeGridData = new GridData(GridData.FILL_BOTH);
        treeGridData.minimumWidth = convertHorizontalDLUsToPixels(3*IDialogConstants.BUTTON_WIDTH);
        treeGridData.minimumHeight = convertVerticalDLUsToPixels(4*IDialogConstants.BUTTON_BAR_HEIGHT);
        treeGridData.horizontalSpan = 2;
        treeViewer.getControl().setLayoutData(treeGridData);
        treeViewer.setContentProvider(new TreeContentProvider());
        treeViewer.setLabelProvider(lblProvider);
        treeViewer.setInput(rootTNode);
        
        treeViewer.getControl().setFocus();
        
        //viewer.setSelection(new StructuredSelection(navPaths[0]));
        return dlgComposite;
     }


	/**
	 * Removes selected imtes from the tree view (called when the 'x' button is
	 * clicked
	 */
	private void removeSelected() {
		TreeItem[] selItems = ((Tree)treeViewer.getControl()).getSelection();
		for (TreeItem selItem: selItems) {
		    TreeItem parentTItem = selItem.getParentItem();
		    TreeNode parentTNode = null;
		    if (parentTItem == null)
		        parentTNode = rootTNode;
		    else
		        parentTNode = (TreeNode) parentTItem.getData();
		    TreeNode selTNode = (TreeNode) selItem.getData();
		    parentTNode.children.remove(selTNode);
		    if (selTNode.reason instanceof IJavaProject) {
		        for (TreeNode projChildrenTNodes: selTNode.children) {
		            navPaths.remove(projChildrenTNodes.reason);
		        }
		    } else {
		        navPaths.remove(selTNode.reason);
		    }
		}
		treeViewer.refresh();
	}
}