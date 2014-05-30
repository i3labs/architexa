package com.architexa.diagrams.jdt.extractors;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LineComment;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.ReloASTExtractor;
import com.architexa.diagrams.jdt.builder.asm.AsmUtil;
import com.architexa.diagrams.jdt.utils.UIUtils;


public class InternalCommentsExtractor extends ReloASTExtractor {
    static final Logger logger = Activator.getLogger(InternalCommentsExtractor.class);

	private CompilationUnit root;

	@Override
	public boolean visit(CompilationUnit node) {
		root = node;
		Iterator<?> commentIter = root.getCommentList().iterator();
		while(commentIter.hasNext()) {
			Object comment = commentIter.next();
			if(comment instanceof BlockComment) {
				visit((BlockComment)comment, RJCore.blockComment);
			} else if (comment instanceof LineComment) {
				visit((LineComment)comment, RJCore.lineComment);
			}
		}
		return true;
	}

	public boolean visit(Comment comment, URI commentType) {
		int start = comment.getStartPosition();
		int end = start + comment.getLength();
		ICompilationUnit compilationUnit = (ICompilationUnit)root.getJavaElement();
		Resource parentRes = null;
		String commentContent = null;
		try {
			IJavaElement commentParent = compilationUnit.getElementAt(start);
			String name = UIUtils.getName(commentParent);
			parentRes = (name==null) ? currFileRes : AsmUtil.toWkspcResource(rdfModel, UIUtils.getName(commentParent));
			commentContent = compilationUnit.getSource().substring(start, end);
		} catch (JavaModelException e) {
			logger.error("Could not extract comment.", e);
		}
		if(parentRes==null || commentContent==null) return false;

		rdfModel.addStatement(parentRes, commentType, rdfModel.createURI(commentContent));
		return true;
	}

}
