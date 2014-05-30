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
 * Created on Jul 26, 2005
 */
package com.architexa.diagrams.commands;

import org.apache.log4j.Logger;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.parts.CommentEditPart;
import com.architexa.diagrams.parts.IRSERootEditPart;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.commands.Command;


public class ArtifactRelCreationCommand extends Command {
	// Replaced by AddNodeAndRelCmd??
	private static final Logger logger = Logger.getLogger(ArtifactRelCreationCommand.class);

    private ArtifactRel link;
    private EditPart target;
    private EditPart source;

	private ArtifactFragment sourceAF;
	private ArtifactFragment targetAF;

	private Point srcLoc;

	private Point targetLoc;

	
    public ArtifactRelCreationCommand(ArtifactRel link, EditPart src) {
    	 super("Link Creation");
         this.link = link;
         source = src;
	}

    public ArtifactRelCreationCommand(ArtifactRel link) {
   	 super("Link Creation");
        this.link = link;
	}
    
	public void setSource(EditPart source) {
        this.source = source;
    }
    public EditPart getSource() {
        return source;
    }

    public void setTarget(EditPart editPart) {
        this.target = editPart;
    }
    public EditPart getTarget() {
        return target;
    }

    @Override
    public boolean canExecute() {
        if (source!=null && source.isActive() && target!= null && target.isActive())
        	return true;
        if (sourceAF!=null && targetAF!= null )
        	return true;
        return false;
    }

    @Override
    public void execute() {
    	if (source instanceof IRSERootEditPart || target instanceof IRSERootEditPart  ) return;
    	
    	ArtifactFragment srcAF;
		ArtifactFragment tgtAF;
		if (sourceAF == null || targetAF == null) {
    		srcAF = (ArtifactFragment)source.getModel(); 
        	tgtAF = (ArtifactFragment)target.getModel();
    	} else {
    		srcAF = sourceAF;
    		tgtAF = targetAF;
    	}
    	link.init(srcAF, tgtAF, link.relationRes);
    	link.connect(srcAF, tgtAF);
    	
    	// Support to add relative distance for anchored comments
    	if (srcAF instanceof Comment) {
    		((Comment) srcAF).setAnchored(true);
    		Point srcTopLeft;
			Point targetTopLeft;
			if (sourceAF == null || targetAF == null) {
    			srcTopLeft = ((GraphicalEditPart)source).getFigure().getBounds().getTopLeft();
        		targetTopLeft = ((GraphicalEditPart)target).getFigure().getBounds().getTopLeft();
        	} else {
    			srcTopLeft = srcLoc;
        		targetTopLeft = targetLoc;
        	}
    		Point relDist = new Point(Math.abs(srcTopLeft.x - targetTopLeft.x), Math.abs(srcTopLeft.y - targetTopLeft.y));
    		((Comment) srcAF).setRelDistance(relDist);
    	}
    }
    
    @Override
    public void undo() {
    	RootArtifact.hideRel(link);
    	if (source != null && source.getModel() instanceof CommentEditPart)
    		((Comment) source.getModel()).setAnchored(false);
    }

	public void setTargetAF(ArtifactFragment _targetAF, Point topLeft) {
		targetAF = _targetAF;
		targetLoc = topLeft;
	}

	public void setSourceAF(ArtifactFragment _sourceAF, Point topLeft) {
		sourceAF = _sourceAF;
		srcLoc = topLeft;
	}
}