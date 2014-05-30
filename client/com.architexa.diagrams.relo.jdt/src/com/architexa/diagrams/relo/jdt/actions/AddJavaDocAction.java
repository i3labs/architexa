package com.architexa.diagrams.relo.jdt.actions;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jface.action.Action;

import com.architexa.diagrams.commands.AddCommentCommand;
import com.architexa.diagrams.commands.ArtifactRelCreationCommand;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.DerivedArtifact;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.store.ReloRdfRepository;

public class AddJavaDocAction extends Action {
	
		private AbstractGraphicalEditPart ep;
		public AddJavaDocAction(AbstractGraphicalEditPart _ep) {
			super("Add Java Doc to Diagram", ReloJDTPlugin.getImageDescriptor("icons/javadoc_location_attrib.gif"));
			ep = _ep;
		}
		@Override
		public void run() {
			CompoundCommand cc = new CompoundCommand("Add Java Doc");
			final Comment comment = new Comment();
			Comment.initComment(comment);
			Rectangle bounds = ep.getFigure().getBounds();
			Point loc = new Point(bounds.x+20+bounds.width, bounds.y+20+bounds.height);
			
			if (!(ep.getModel() instanceof ArtifactFragment)) return;
			ArtifactFragment af = (ArtifactFragment) ep.getModel();
			cc.add(new AddCommentCommand(af.getRootArt(), comment, loc));
			
			NamedRel namedRel = new NamedRel();
			ArtifactRelCreationCommand relCC = new ArtifactRelCreationCommand(namedRel);
			relCC.setSourceAF(comment, loc);
			relCC.setTargetAF(af, ep.getFigure().getBounds().getTopLeft());
			cc.add(relCC);
			Object javaDoc = getJavaDoc(af);
	        if (javaDoc!=null)
				comment.setAnnoLabelText(javaDoc.toString());
			
	        
			ep.getViewer().getEditDomain().getCommandStack().execute(cc);
		}
		
		public boolean canRun(Object obj) {
			if (!(obj instanceof ArtifactFragment)) return false;
			if (obj instanceof DerivedArtifact || obj instanceof Comment) return false;
			ArtifactFragment af = (ArtifactFragment) obj;
			if (af instanceof RootArtifact)
				return false;
			return getJavaDoc(af)!=null;
		}
		private Javadoc getJavaDoc(ArtifactFragment af) {
			CodeUnit cu = new CodeUnit(af.getArt());
			if (af.getParentArt()==null) return null;
			ReloRdfRepository repo = af.getRootArt().getRepo();
			IJavaElement javaElem = cu.getJDTElement(repo );
			//TODO check this and remove
//	        while (javaElem == null && !cu.isPackage(repo)) {
//	        	// deal with cases where we cannot find java element (for example default constructors)
//	        	cu = new CodeUnit(cu.getArt().queryParentArtifact(repo));
//	        	javaElem = cu.getJDTElement(repo);
//	        }
	        
	        if (javaElem == null || !(javaElem instanceof IMember)) return null;
	        
	        ASTNode node = RJCore.getCorrespondingASTNode((IMember) javaElem);
	        if (node == null || !(node instanceof BodyDeclaration)) return null;
	        Javadoc doc = ((BodyDeclaration)node).getJavadoc();
	        if (doc==null || doc.equals("")) return null;
	        return doc;
		}
}
