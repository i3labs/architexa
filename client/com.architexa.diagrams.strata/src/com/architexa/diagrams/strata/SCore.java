/* 
 * Copyright (c) 2004-2006 Massachusetts Institute of Technology. This code was
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
 * Created on Aug 14, 2007
 */
package com.architexa.diagrams.strata;

import org.apache.log4j.Logger;
import org.eclipse.ui.IStartup;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.parts.CommentEditPart;
import com.architexa.diagrams.parts.NamedRelationPart;
import com.architexa.diagrams.services.PluggableTypes;
import com.architexa.diagrams.services.PluggableTypes.PluggableTypeInfo;
import com.architexa.diagrams.strata.model.DependencyRelation;
import com.architexa.diagrams.strata.parts.DependencyRelationEditPart;


public class SCore implements IStartup {
	static final Logger logger = StrataPlugin.getLogger(SCore.class);

	// support for a containment cache - the below code needs to be in the core
	// since the builder adds to it. We need the registration here since we
	// define the model and the edit parts here
    /*
    public static final URI containmentBasedDepStrength = createReloURI("jdt#depStrength");
    public static final URI containmentCacheValid = createReloURI("jdt#virtualCacheValid");
    public static final URI containmentBasedCalls = createReloURI("jdt#containmentBasedCalls");
    public static final URI containmentBasedInherits = createReloURI("jdt#containmentBasedInherits");
    public static final URI containmentBasedRefType = createReloURI("jdt#containmentBasedRefType");
    */

	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	private void earlyStartupInternal() {
        PluggableTypes.registerType(new PluggableTypeInfo(RJCore.containmentBasedRefType, "contained references", DependencyRelation.class, DependencyRelationEditPart.class));
        PluggableTypes.registerType(new PluggableTypeInfo(RJCore.containmentBasedCalls, "contained calls", DependencyRelation.class, DependencyRelationEditPart.class));
        PluggableTypes.registerType(new PluggableTypeInfo(RJCore.containmentBasedInherits, "contained inherits", DependencyRelation.class, DependencyRelationEditPart.class));
        PluggableTypes.registerType(new PluggableTypeInfo(DependencyRelation.depRel, "dependency relation", DependencyRelation.class, DependencyRelationEditPart.class));
        PluggableTypes.registerType(new PluggableTypeInfo(RSECore.commentType, "Comment", Comment.class, CommentEditPart.class));
        PluggableTypes.registerType(new PluggableTypeInfo(RSECore.namedRel, "named relationship", NamedRel.class, NamedRelationPart.class));
	}

}
