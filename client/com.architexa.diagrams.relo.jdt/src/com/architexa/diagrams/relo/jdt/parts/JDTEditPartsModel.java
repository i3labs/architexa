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
 * Created on Jan 30, 2005
 *
 */
package com.architexa.diagrams.relo.jdt.parts;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.ui.IStartup;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.model.Entity;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.parts.CommentEditPart;
import com.architexa.diagrams.parts.NamedRelationPart;
import com.architexa.diagrams.parts.NavAidsSpec;
import com.architexa.diagrams.parts.RelNavAidsSpec;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;
import com.architexa.diagrams.relo.jdt.services.PluggableEditPartSupport;
import com.architexa.diagrams.services.PluggableNameGuesser;
import com.architexa.diagrams.services.PluggableNameGuesser.NameGuesser;
import com.architexa.diagrams.services.PluggableNavAids;
import com.architexa.diagrams.services.PluggableNavAids.INavAidSpecSource;
import com.architexa.diagrams.services.PluggableTypeGuesser;
import com.architexa.diagrams.services.PluggableTypeGuesser.TypeGuesser;
import com.architexa.diagrams.services.PluggableTypes;
import com.architexa.diagrams.services.PluggableTypes.PluggableTypeInfo;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.store.ReloRdfRepository;



public class JDTEditPartsModel implements IStartup {
    static final Logger logger = ReloJDTPlugin.getLogger(JDTEditPartsModel.class);
	private static boolean registered = false;

	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	private void earlyStartupInternal() {
		if (!registered) {
			registered = true;	
			registerNodes();
			registerRelations();
			registerNavAids();
		}
	}
	
	//public void initRepository(ReloRdfRepository reloRepo) {
	//	//URI rdfsLabel = ReloRdfRepository.rdfsLabel;
	//	//String pluginId = ReloJDTPlugin.PLUGIN_ID;
	//    
	//	//reloRepo.addStatement(RJCore.publicAccess,    rdfsLabel, "public");
	//	//reloRepo.addStatement(RJCore.protectedAccess, rdfsLabel, "protected");
	//	//reloRepo.addStatement(RJCore.privateAccess,   rdfsLabel, "private");
	//	//reloRepo.addStatement(RJCore.noAccess,        rdfsLabel, "none");
	//}

	private void registerNodes() {
		PluggableTypes
			.registerType(new PluggableTypeInfo(RJCore.classType, "Class", ArtifactFragment.class, ClassEditPart.class, PluggableEditPartSupport.defaultCodeUnitIconProvider))
		    .isGraphNode = true;
		PluggableTypes
			.registerType(new PluggableTypeInfo(RJCore.interfaceType, "Interface", ArtifactFragment.class, ClassEditPart.class, PluggableEditPartSupport.defaultCodeUnitIconProvider))
			.isGraphNode = true;
		PluggableTypes.registerType(new PluggableTypeInfo(RJCore.fieldType, "Field", ArtifactFragment.class, FieldEditPart.class, PluggableEditPartSupport.defaultCodeUnitIconProvider));
		PluggableTypes.registerType(new PluggableTypeInfo(RJCore.methodType, "Method", ArtifactFragment.class, MethodEditPart.class, PluggableEditPartSupport.defaultCodeUnitIconProvider));
		PluggableTypes.registerType(new PluggableTypeInfo(RJCore.packageType, "Package", ArtifactFragment.class, PackageEditPart.class, PluggableEditPartSupport.defaultCodeUnitIconProvider)).isGraphContainer = true;
		PluggableTypes.registerType(new PluggableTypeInfo(RJCore.projectType, "Project", null, null, new ProjectEditPart()));
		
		// comment types
		PluggableTypes.registerType(new PluggableTypeInfo(RSECore.commentType, "Comment", Comment.class, CommentEditPart.class));
		PluggableTypes.registerType(new PluggableTypeInfo(RSECore.entityType, "Entity", Entity.class, CommentEditPart.class));
		
		PluggableTypeGuesser.registerTypeGuesser(new TypeGuesser() {
			public Resource getType(Resource elementRes, ReloRdfRepository repo) {
				String resourceStr = elementRes.toString();
	    		if (RSECore.ResourceIsEclipseProject(elementRes)) return RJCore.projectType;
	    		if (!RJCore.isJDTWksp(elementRes)) return null;

	    		if (!resourceStr.contains("$")) {
	    			if (resourceStr.endsWith(".*"))
	    				return RJCore.indirectPckgDirType;
	    			return RJCore.packageType;
	        	}

	    		int memberSeperatorNdx = resourceStr.indexOf(".", resourceStr.indexOf("$"));
        		if (memberSeperatorNdx>0) {
        			if (resourceStr.indexOf("(",memberSeperatorNdx)>0)
        				return RJCore.methodType;
        			else
        				return RJCore.fieldType;
    			}
    			Statement interfaceTypeStatement = repo.getStatement(elementRes, RJCore.isInterface, null);
    			if (interfaceTypeStatement !=null && interfaceTypeStatement.getObject()!=null && interfaceTypeStatement.getObject().equals(RJCore.interfaceType))
    				return (Resource) interfaceTypeStatement.getObject();
    			return RJCore.classType;
			}
		});
		ReloRdfRepository.nameGuesser = new PluggableNameGuesser();
		PluggableNameGuesser.registerNameGuesser(new NameGuesser() {
			public String getName(Resource res, ReloRdfRepository repo) {
				if (!RJCore.isJDTWksp(res)) return null;
				if (!(res instanceof URI)) {
					Statement stmt = repo.getStatement(res, RSECore.name, null);
					if(stmt!=null && stmt.getObject()!=null) 
						return stmt.getObject().toString();
					return null;
				}
				String name = ((URI)res).getLocalName();
				// package names to be returned as is
				if (!name.contains("$"))
					return name;
				// strip params from methods (they are cached and added back later)
				if (name.contains("("))
					name = name.substring(0, name.indexOf("("));
				// find the end part (strip package info)
				name = name.replaceAll("[^A-Za-z0-9 _<>]", "%");
				if (name.contains("%"))
					name = name.substring(name.lastIndexOf("%")+1);
				
				return name;
			}
		});
		
	}
	
	private void registerRelations() {
		PluggableTypes.registerType(new PluggableTypeInfo(RJCore.inherits, "inherits", ArtifactRel.class, InheritanceRelationPart.class));
		PluggableTypes.registerType(new PluggableTypeInfo(RJCore.calls, "calls", ArtifactRel.class, MethodCallRelationPart.class));
		PluggableTypes.registerType(new PluggableTypeInfo(RJCore.returns, "returns", ArtifactRel.class, MethodCallRelationPart.class));
		PluggableTypes.registerType(new PluggableTypeInfo(RJCore.refType, "refers", ArtifactRel.class, TypeRefRelationPart.class));
		PluggableTypes.registerType(new PluggableTypeInfo(RJCore.overrides, "overrides", ArtifactRel.class, OverridesRelationPart.class));
		// comment rel
		PluggableTypes.registerType(new PluggableTypeInfo(RSECore.namedRel, "named relationship", NamedRel.class, NamedRelationPart.class));
	}

	private void registerNavAids() {
		PluggableNavAids.registerNavAidsSource(PluggableNavAids.getFWDSpec(RJCore.calls));
		PluggableNavAids.registerNavAidsSource(PluggableNavAids.getREVSpec(RJCore.calls));

		PluggableNavAids.registerNavAidsSource(new INavAidSpecSource() {
			public NavAidsSpec getNavAids(List<NavAidsSpec> prevDecorations, EditPart hostEP) {
			    return new RelNavAidsSpec(hostEP, DirectedRel.getFwd(RJCore.overrides)) {
		            @Override
		            public Point getHandlesPosition(IFigure containerFig) {
		                Dimension prefSize = decorationFig.getPreferredSize();
		                Rectangle bounds = containerFig.getBounds();
		                int x = bounds.x;
		                int y = bounds.y - prefSize.height;
		                return new Point(x, y);
		            }
		        };
			}
		});
		PluggableNavAids.registerNavAidsSource(new INavAidSpecSource() {
			public NavAidsSpec getNavAids(List<NavAidsSpec> prevDecorations, EditPart hostEP) {
			    return new RelNavAidsSpec(hostEP, DirectedRel.getRev(RJCore.overrides)) {
		            @Override
		            public Point getHandlesPosition(IFigure containerFig) {
		                //Dimension prefSize = decorationFig.getPreferredSize();
		                Rectangle bounds = containerFig.getBounds();
		                int x = bounds.x;
		                int y = bounds.y + bounds.height;
		                return new Point(x, y);
		            }
		        };
			}
		});

		PluggableNavAids.registerNavAidsSource(new INavAidSpecSource() {
			public NavAidsSpec getNavAids(List<NavAidsSpec> prevDecorations, EditPart hostEP) {
			    return new RelNavAidsSpec(hostEP, DirectedRel.getFwd(RJCore.inherits)) {
		            @Override
		            public Point getHandlesPosition(IFigure containerFig) {
		                Dimension prefSize = decorationFig.getPreferredSize();
		                Rectangle bounds = containerFig.getBounds();
		                int x = bounds.x + (bounds.width - prefSize.width)/2;
		                int y = bounds.y - prefSize.height;
		                return new Point(x, y);
		            }
		        };
			}
		});
		PluggableNavAids.registerNavAidsSource(new INavAidSpecSource() {
			public NavAidsSpec getNavAids(List<NavAidsSpec> prevDecorations, EditPart hostEP) {
			    return new RelNavAidsSpec(hostEP, DirectedRel.getRev(RJCore.inherits)) {
		            @Override
		            public Point getHandlesPosition(IFigure containerFig) {
		                Dimension prefSize = decorationFig.getPreferredSize();
		                Rectangle bounds = containerFig.getBounds();
		                int x = bounds.x + (bounds.width - prefSize.width)/2;
		                int y = bounds.y + bounds.height;
		                return new Point(x, y);
		            }
		        };
			}
		});
	}
}