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
 * Created on Jul 17, 2004
 *  
 */
package com.architexa.diagrams.model;

import java.io.IOException;
import java.util.List;

import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.utils.DbgRes;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


/**
 * This class needs to phsically exist to be able to add rdf about it as in BuilderDefaultModel
 * 
 * @author vineet
 *
 */
public class NamedRel extends AnnotatedRel {
	public DbgRes tag = new DbgRes(NamedRel.class, this);
	
	public NamedRel() {
		this.relationRes = RSECore.namedRel;
	}

    private String text = "  ";
    public void setAnnoLabelText(String text) {
        this.text = text;
        this.firePropChang("comment");
    }
    public String getAnnoLabelText() {
        return text;
    }
    
	@Override
    public String toString() {
		return  "[" + text + "] " + srcArt + " --> " + dstArt + tag;
	}

	/**
     * @param rdfWriter
     * @throws IOException
     */
    @Override
	public void writeRDF(RdfDocumentWriter rdfWriter, List<ArtifactRel> savedRels) throws IOException {
        super.writeRDF(rdfWriter, savedRels);
        rdfWriter.writeStatement(
                getInstanceRes(), 
                Comment.commentTxt, 
                StoreUtil.createMemLiteral(getAnnoLabelText()) );
        if (isUserCreated())
        	rdfWriter.writeStatement(getInstanceRes(), RSECore.userCreated, StoreUtil.createMemLiteral("true"));
        //rdfWriter.writeStatement(getArtifactRel().relationRes, StoreUtil.createMemURI(RDF.TYPE), ReloCore.namedRel);
    }

    @Override
    protected void readRDF(ReloRdfRepository queryRepo) {
        // need to write basic data before we reposition and initialize figure (in base class)
        String relTxt = queryRepo.getStatement(getInstanceRes(), Comment.commentTxt, null).getObject().toString();
        setAnnoLabelText(relTxt);
        //logger.debug("After setText: " + comment.getText());
        
        super.readRDF(queryRepo);
    }

}