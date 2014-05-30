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

import org.openrdf.model.URI;

/**
 * @author vineet
 *
 */
public class DirectedRel {

    public final URI res;
    public final boolean isFwd;

    public DirectedRel(URI relationRes, boolean isFwd) {
        this.res = relationRes;
        this.isFwd = isFwd;
    }
    
    public static DirectedRel getFwd(URI relationRes) {
        return new DirectedRel(relationRes, true);
    }

    public static DirectedRel getRev(URI relationRes) {
        return new DirectedRel(relationRes, false);
    }
    
    
	@Override
    public String toString() {
        return (isFwd ? "fwd" : "rev") + "-" + res.toString();
	}
	
	/*
	 * Define hashcode and equals so that they are semantic and not instance based 
	 */
    @Override
    public int hashCode() {
        return 2*res.hashCode() + (isFwd ? 1 : 0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DirectedRel)) return false;
        DirectedRel dr = (DirectedRel) o; 
        if (dr.res.equals(res) && dr.isFwd == isFwd) return true;
        return false;
    }
}