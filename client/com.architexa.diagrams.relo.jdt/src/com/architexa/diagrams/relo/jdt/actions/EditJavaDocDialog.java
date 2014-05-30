package com.architexa.diagrams.relo.jdt.actions;


import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.compat.ASTUtil;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.utils.ITFBMUtil;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.intro.AtxaIntroPlugin;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.store.ReloRdfRepository;


public class EditJavaDocDialog extends Dialog {
	static final Logger s_logger = AtxaIntroPlugin.getLogger(EditJavaDocDialog.class);

    private Text javaDocEdit;
	private String label;
	private List<Object> javaDoc;
	private ArtifactFragment af;
	private IJavaElement javaElem;
    
    protected EditJavaDocDialog(Shell parentShell, EditPart ep, ReloRdfRepository repo) {
		super(parentShell);
		af = (ArtifactFragment) ep.getModel();
		label = CodeUnit.getLabel(repo, af);

		CodeUnit cu = new CodeUnit(af.getArt());
		javaElem = cu.getJDTElement(repo );
		
		javaDoc = getJavaDoc();
		
	}

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Edit Java Doc");
     }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Label messageLabel = new Label(composite, SWT.WRAP);
        String space = label.endsWith(" ") ? "" : " ";
        messageLabel.setText("Edit the Java Doc for "+label+space+"below. Changes will " +
        		"be applied to the code upon clicking Save. (Source code will be modified!)");
        GridData data = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_BEGINNING);
        data.widthHint = convertHorizontalDLUsToPixels(215);
        messageLabel.setLayoutData(data);
        
        createBodyArea(composite);
        
        return composite;
     }

	private void createBodyArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL
                | GridData.VERTICAL_ALIGN_BEGINNING);
        data.widthHint = convertHorizontalDLUsToPixels(215);
        composite.setLayoutData(data);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);
        javaDocEdit = new Text(composite, SWT.BORDER | SWT.WRAP);
        javaDocEdit.setLayoutData(new GridData(300,150));
        if (javaDoc!=null) {
        	String content= "";
        	for (Object tag : javaDoc) {
        		if (tag instanceof TagElement)
        			content = content +tag.toString()+"";			
        	}
        	javaDocEdit.setText(content);
        }
        
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Save", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.CANCEL_ID) close();
    	
    	if (buttonId == IDialogConstants.OK_ID) {
    		setJavaDoc(af,javaDocEdit.getText());
    		close();
    	}
    }
	
	
	private void setJavaDoc(ArtifactFragment af, String comment) {

		ICompilationUnit icu = getICUFromAF();
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		Document document = (Document) ITFBMUtil.getCurrentFileDocument((File) icu.getResource());
		IJavaProject javaProject =icu.getJavaProject();
	        
		
		// I already have an ICompilationUnit icu.
		parser.setSource(icu);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		
		cu.recordModifications();
		List cuTypes = cu.types();
	
		// Creating the new JavaDoc node AST 
		AST ast = cu.getAST();
		Javadoc jc = ast.newJavadoc();
		TagElement tag = ast.newTagElement();
		TextElement te = ast.newTextElement();
		tag.fragments().add(te);
		te.setText(comment.replace("\n\n", "\n"));
		jc.tags().add(tag);
		tag = ast.newTagElement();
		jc.tags().add(tag);
		
		//type
		ASTNode node = ((JavaElement)javaElem).findNode(cu);
		if (node instanceof BodyDeclaration) {
			((BodyDeclaration)node).setJavadoc(jc);
		}
		applyChanges(document, javaProject, icu, cu);
	}	
	
	private void applyChanges(Document document, IJavaProject javaProject, ICompilationUnit icu, CompilationUnit cu) {
		// Applying changes	
		TextEdit text = cu.rewrite(document, javaProject.getOptions(true));
		try {
			text.apply(document);
		} catch (MalformedTreeException e1) { e1.printStackTrace(); } catch (BadLocationException e1) { e1.printStackTrace(); }
	
		String newSource = document.get();
		try {
			icu.getBuffer().setContents(newSource);
		} catch (JavaModelException e1) { e1.printStackTrace(); }
	}

	private ICompilationUnit getICUFromAF(){
		 IResource document2 = null;
	        IJavaElement javaElem2 = javaElem;
			try {
				while (!(javaElem2 instanceof org.eclipse.jdt.internal.core.CompilationUnit)) {
					javaElem2= javaElem2.getParent();
					document2 = javaElem2.getCorrespondingResource();
				}
				
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			return JavaCore.createCompilationUnitFrom((IFile) document2);
	}
	
	private List<Object> getJavaDoc() {
        if (javaElem == null || !(javaElem instanceof IMember)) return null;
        
        // remove this specific item from the ast cache so that we get the most recent modifications to the javadoc
        ASTUtil.emptyCache((IMember) javaElem);
        
        ASTNode node = RJCore.getCorrespondingASTNode((IMember) javaElem);
        if (node == null || !(node instanceof BodyDeclaration)){
        	return null;
        }
        Javadoc doc = ((BodyDeclaration)node).getJavadoc();
        if (doc==null || doc.equals("")) return null;
        return doc.tags();
	}
}
