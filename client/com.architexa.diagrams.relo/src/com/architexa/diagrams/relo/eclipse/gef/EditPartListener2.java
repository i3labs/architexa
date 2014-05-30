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
 * Created on Jul 29, 2005
 */
package com.architexa.diagrams.relo.eclipse.gef;


import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartListener;

/**
 * The listener interface for receiving basic events from an EditPart.
 * We add more events to EditPartListener.
 * Listeners interested in only one type of Event can extend the 
 * {@link EditPartListener2.Stub} implementation rather than implementing 
 * the entire interface.
 * 
 * @author vineet
 * WE CAN DELETE THIS. THIS HAS BEEN MOVED TO THE DIAGRAMS PROJECT
 */
public interface EditPartListener2 extends EditPartListener {

    /**
     * Listeners interested in just a subset of Events can extend this stub implementation.
     * Also, extending the Stub will reduce the impact of new API on this interface.
     */
    public class Stub extends EditPartListener.Stub implements EditPartListener2 {
        /**
         * @see com.architexa.diagrams.relo.eclipse.gef.EditPartListener2#deletingChild(com.architexa.org.eclipse.gef.EditPart)
         */
        public void deletingChild(EditPart child) { }
        /**
         * @see com.architexa.diagrams.relo.eclipse.gef.EditPartListener2#removedChild(com.architexa.org.eclipse.gef.EditPart)
         */
        public void removedChild(EditPart child) { }
    };

    /**
     * Called before a child EditPart has been deleted from its parent, this is 
     * in addition to removing which might be adding the child to another 
     * EditPart.
     * @param child the Child
     */
    void deletingChild(EditPart child);

    /**
     * Called after a child EditPart is removed from its parent.
     * @param child the remove Child
     */
    void removedChild(EditPart child);

}




