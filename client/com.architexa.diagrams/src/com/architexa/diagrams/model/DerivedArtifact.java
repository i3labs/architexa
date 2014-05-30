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
 * Created on Jul 29, 2004
 *
 */
package com.architexa.diagrams.model;

import java.io.IOException;

import org.openrdf.model.Resource;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.model.Artifact;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


/**
 * @author vineet
 * 
 * Essentially implements an anonymous Code Unit
 * 
 * DerivedArtifacts are not saved/loaded as part of the model - their
 * parent/enclosing Artifact is responsible for any persistence support
 */
public abstract class DerivedArtifact extends ArtifactFragment {

	private final Artifact parentArt;

    public DerivedArtifact(Artifact parentArt) {
        super(StoreUtil.createBNode());
        this.parentArt = parentArt;
	}
	
	public Artifact getEnclosingArtifact() {
	    return parentArt;
	}
	
    @Override
    public Artifact getNonDerivedBaseArtifact() {
        return getEnclosingArtifact();
    }

	@Override
    public Resource queryType(ReloRdfRepository repo) {
	    return null;
	}
    
	// make different classes of derived code units be different, but same 
	//  class and element be the same hashCode
	@Override
    public int hashCode() {
		return super.hashCode() + this.getClass().getName().hashCode();
	}

	@Override
    public String toString() {
	    return "^" + super.toString();
	}
	
    @Override
	protected void writeRDFNode(RdfDocumentWriter rdfWriter, Resource parentInstance) throws IOException {
    }
}
