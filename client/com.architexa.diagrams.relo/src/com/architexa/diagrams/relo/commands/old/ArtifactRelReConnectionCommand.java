///* 
// * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
// * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
// * project at MIT. Permission is hereby granted, free of charge, to any person
// * obtaining a copy of this software and associated documentation files (the 
// * "Software"), to deal in the Software without restriction, including without 
// * limitation the rights to use, copy, modify, merge, publish, distribute, 
// * sublicense, and/or sell copies of the Software, and to permit persons to whom
// * the Software is furnished to do so, subject to the following conditions: 
// * 
// * The above copyright notice and this permission notice shall be included in 
// * all copies or substantial portions of the Software. 
// * 
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE. 
// */
//
///*
// * Created on Jul 26, 2005
// */
//package com.architexa.diagrams.relo.commands.old;
//
//import org.eclipse.gef.commands.Command;
//
//import com.architexa.diagrams.relo.parts.ArtifactEditPart;
//import com.architexa.diagrams.relo.parts.ArtifactRelEditPart;
//
//public class ArtifactRelReConnectionCommand extends Command {
//
//	// Replaced by AddNodeAndRelCmd??
//	
//    private final ArtifactRelEditPart linkPart;
//    private ArtifactEditPart origSource;
//    private ArtifactEditPart origTarget;
//    private ArtifactEditPart newTarget;
//    private ArtifactEditPart newSource;
//
//    public ArtifactRelReConnectionCommand(ArtifactRelEditPart linkPart) {
//        this.linkPart = linkPart;
//        this.origSource = (ArtifactEditPart) linkPart.getSource();
//        this.origTarget = (ArtifactEditPart) linkPart.getTarget();
//    }
//
//    public void setNewConnectionPoints(ArtifactEditPart newSource, ArtifactEditPart newTarget) {
//        this.newSource = newSource;
//        this.newTarget = newTarget;
//    }
//
//    @Override
//    public boolean canExecute() {
//        return newSource.isActive() && newTarget.isActive();
//    }
//
//    @Override
//    public void execute() {
//        linkPart.getArtifactRel().init(newSource.getArtifact(), newTarget.getArtifact(), linkPart.getArtifactRel().relationRes);
//        newSource.getRootController().reConnectRel(newSource, linkPart, newTarget);
//    }
//
//    @Override
//	public void undo() { 
//        linkPart.getArtifactRel().init(origSource.getArtifact(), origTarget.getArtifact(), linkPart.getArtifactRel().relationRes);
//        origSource.getRootController().reConnectRel(origSource, linkPart, origTarget);
//    }
//
//}