package com.architexa.extensions.entJava;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IStartup;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.AtxaBuildVisitor;
import com.architexa.diagrams.jdt.builder.PluggableExtensionBuilderSupport;
import com.architexa.diagrams.jdt.builder.ResourceToProcess;
import com.architexa.diagrams.jdt.builder.asm.AsmPackageSupport;
import com.architexa.diagrams.jdt.builder.asm.DepAndChildrenStrengthSummarizer;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.parts.NavAidsSpec;
import com.architexa.diagrams.parts.RelNavAidsSpec;
import com.architexa.diagrams.services.PluggableNavAids;
import com.architexa.diagrams.services.PluggableNavAids.INavAidSpecSource;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.store.ReloRdfRepository;

public class Struts1ExtensionBuilder implements IStartup, PluggableExtensionBuilderSupport.IAtxaExtensionBuilder {
    public static final Logger logger = Activator.getLogger(Struts1ExtensionBuilder.class);

	private AtxaBuildVisitor builder = null;
	private ReloRdfRepository rdfRepo = null;
	private Resource projectRes = null;

	private IWorkspaceRoot wkspcRoot = null;
	private String warRootPath = null; // used as the basis for war file paths

    static {
    	DepAndChildrenStrengthSummarizer.addContainmentRelMapping(EJCore.implementedBy, EJCore.containmentBasedImplementedBy);
    	DepAndChildrenStrengthSummarizer.addContainmentRelMapping(EJCore.actionData, EJCore.containmentBasedActionData);

    	DepAndChildrenStrengthSummarizer.addContainmentRelMapping(EJCore.tilesCall, EJCore.containmentBasedTilesCall);
    	DepAndChildrenStrengthSummarizer.addContainmentRelMapping(EJCore.tilesExtend, EJCore.containmentBasedTilesExtend);
    	DepAndChildrenStrengthSummarizer.addContainmentRelMapping(EJCore.tilesReference, EJCore.containmentBasedTilesCall);
    };

	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	private void earlyStartupInternal() {
		addStrutsPluggableTypes();
	}

	
	private void addStrutsPluggableTypes() {
		PluggableExtensionBuilderSupport.registerExtensionBuilder("xml", this);
		PluggableNavAids.registerNavAidsSource(PluggableNavAids.getFWDSpec(EJCore.implementedBy));
		PluggableNavAids.registerNavAidsSource(PluggableNavAids.getREVSpec(EJCore.implementedBy));

		PluggableNavAids.registerNavAidsSource(PluggableNavAids.getFWDSpec(EJCore.tilesCall));
		PluggableNavAids.registerNavAidsSource(PluggableNavAids.getREVSpec(EJCore.tilesCall));
		PluggableNavAids.registerNavAidsSource(PluggableNavAids.getFWDSpec(EJCore.tilesReference));
		PluggableNavAids.registerNavAidsSource(PluggableNavAids.getREVSpec(EJCore.tilesReference));
		
		PluggableNavAids.registerNavAidsSource(new INavAidSpecSource() {
			public NavAidsSpec getNavAids(final List<NavAidsSpec> prevDecorations, EditPart hostEP) {
			    return new RelNavAidsSpec(hostEP, DirectedRel.getFwd(EJCore.tilesExtend)) {
		            @Override
		            public Point getHandlesPosition(IFigure containerFig) {
		            	 Point decPos = containerFig.getBounds().getTopRight();
			                decPos.x = Math.max(
					                        decPos.x, 
					                        firstNAS.decorationFig.getBounds().getTopRight().x);
			                for (NavAidsSpec naSpec : prevDecorations) {
			                	if (naSpec.decorationFig==null) continue;
			                	if (naSpec.decorationFig.containsPoint(decPos.getCopy().getTranslated(3, 3))) {
			                		decPos.y = decPos.y + naSpec.decorationFig.getBounds().height;	
			                	}
			                }
			                return decPos; 
		            }
		        };
			}
		});
		PluggableNavAids.registerNavAidsSource(new INavAidSpecSource() {
			public NavAidsSpec getNavAids(List<NavAidsSpec> prevDecorations, EditPart hostEP) {
			    return new RelNavAidsSpec(hostEP, DirectedRel.getRev(EJCore.tilesExtend)) {
		            @Override
		            public Point getHandlesPosition(IFigure containerFig) {
		            	Dimension prefSize = decorationFig.getPreferredSize();
		                Rectangle bounds = containerFig.getBounds();
		                int x = bounds.x - prefSize.width;
		                int y = bounds.y;
		                return new Point(x, y);
		            }
		        };
			}
		});
		
		PluggableNavAids.registerNavAidsSource(PluggableNavAids.getFWDSpec(EJCore.actionData));
		PluggableNavAids.registerNavAidsSource(PluggableNavAids.getREVSpec(EJCore.actionData));		
	}
	public void processProject(AtxaBuildVisitor builder, IProject resource) {
	}

	public List<Resource> processExtensionResource(AtxaBuildVisitor builder, ResourceToProcess rtp) {
		this.builder = builder;
		this.rdfRepo = builder.getRepo();
		this.projectRes = builder.getProjectResource();
		
		// System.err.println("Processing: " + rtp.getName());
		try {
			if (rtp.getName().endsWith("web.xml") && rtp.resource.exists()) {
		    	wkspcRoot  = rtp.resource.getWorkspace().getRoot();
		    	
		    	String webXMLPath = rtp.resource.getFullPath().toString();
		    	warRootPath = webXMLPath.substring(0, webXMLPath.length() - "/WEB-INF/web.xml".length());

				logger.info("Processing: " + rtp.getName());
				processWebXML((IFile) rtp.resource);
			}
		} catch (Throwable t) {
			// sometimes file does not exist even though the api says it does -
			// this happens if a 'refresh' is not done
			//logger.error("Unexpected Exception", t);
		}
		// doesn't need to call the runProcessors
		return null;
	}

	////////////////////////
	// Processing Methods //
	////////////////////////

	private void processWebXML(IFile webXmlRes) throws CoreException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		// look for: web-app/servlet/servlet-class==org.apache.struts.action.ActionServlet
		//  & send init-param/param-name init-param/param-value for processStrutsConfig 
		//  filter: those with param-name=config/\(.*\)
		if (!webXmlRes.exists()) return;
		Document webXMLDoc = EJCore.getXMLDoc(webXmlRes.getContents());
		String xpathQuery = "//servlet[child::servlet-class[text() = 'org.apache.struts.action.ActionServlet']]/init-param";
		NodeList strutsInitParams =  EJCore.xPathQueryEval(xpathQuery, webXMLDoc);
		if (strutsInitParams.getLength()==0) {
			processStrutsConfig("", "/WEB-INF/struts-config.xml");
			return;
		}
		for (int i=0; i<strutsInitParams.getLength(); i++) {
			 Element initParam = (Element) strutsInitParams.item(i);
			 NodeList paramNameNodes = initParam.getElementsByTagName("param-name");
			 if (paramNameNodes.getLength() > 0) {
				 Node paramItem = paramNameNodes.item(0);
				 String paramText = paramItem.getTextContent();
				 if (paramText != null && paramText.startsWith("config")) {
					 logger.info(" Config Name: " + paramItem.getTextContent());
					 String baseDir = paramText.substring("config".length());
					 String configPath = initParam.getElementsByTagName("param-value").item(0).getTextContent();
					 processStrutsConfig(baseDir, configPath);
				 }
			 }
		 }
	}
	

	private void processStrutsConfig(String strutsModuleName, String strutsConfigPath) throws SAXException, IOException, ParserConfigurationException, CoreException, XPathExpressionException {
		Map<String, String> formNameToClassMap = new HashMap<String, String>();
		
		IResource configRes = wkspcRoot.findMember(warRootPath + strutsConfigPath);
		if (configRes == null || !configRes.exists()) return; // we are likely not processing the main 'web.xml' file
		
		Document configXMLDoc = EJCore.getXMLDoc(((IFile)configRes).getContents());

		// look for: struts-config/form-beans/form-bean ... and add all name->type in a map
		NodeList formBeans =  EJCore.xPathQueryEval("//form-bean", configXMLDoc);
		for (int i=0; i<formBeans.getLength(); i++) {
			Node formBeanEntry = formBeans.item(i);
			NamedNodeMap attribs = formBeanEntry.getAttributes();
			formNameToClassMap.put(attribs.getNamedItem("name").getTextContent(), attribs.getNamedItem("type").getTextContent());
		}
		
		// look for: struts-config/action-mappings/action ... and add all 
		// baseDir+path <implementedBy> type 
		// baseDir+path <forwards>  dest 
		NodeList actions =  EJCore.xPathQueryEval("//action", configXMLDoc);
		processActions(actions, strutsModuleName, strutsConfigPath, formNameToClassMap);
		
		// get tiles config file
		// look for: plug-in/set-property/ property="definitions-config"  value
		IFile tilesConfigRes = TilesExtensionBuilder.getTilesConfigFile(configXMLDoc, wkspcRoot, warRootPath);
		
		// add Tiles definitions
		// look for: definition/put   add all extends, paths, and values with file extensions
		rdfRepo.startTransaction();
    	try {
    		if (tilesConfigRes != null && tilesConfigRes.exists()) 
    			TilesExtensionBuilder.getTilesDefinitionsAndTargets((IFile) tilesConfigRes, strutsModuleName, rdfRepo, projectRes);
    	} catch (Throwable t) {
			logger.error("Unexpected Exception", t);
    	} finally {
			rdfRepo.commitTransaction();
    	}
	}
	
	private void processActions(NodeList actions, String strutsModuleName, String strutsConfigPath, Map<String, String> formNameToClassMap) {
		for (int i=0; i<actions.getLength(); i++) {
			NamedNodeMap actionAttribs = actions.item(i).getAttributes();
			String actionPath = strutsModuleName + EJCore.getNodeText(actionAttribs,"path");
			String actionClass = EJCore.getNodeText(actionAttribs,"type");
			if (actionClass == null || actionClass.length() == 0) continue;
			String actionFormClass = formNameToClassMap.get(EJCore.getNodeText(actionAttribs,"name"));
			String forwardPathProp = EJCore.getNodeText(actionAttribs, "forward");
			Element action = (Element)actions.item(i);
			NodeList forwards =  action.getElementsByTagName("forward");
			List<String> forwardPaths = new ArrayList<String>();
			for (int j = 0; j < forwards.getLength(); j++) {
				NamedNodeMap forwardAttribs = forwards.item(j).getAttributes();
				String path = EJCore.getNodeText(forwardAttribs, "path");
				forwardPaths.add(updatePath(path, strutsModuleName));
			}	
	    	rdfRepo.startTransaction();
	    	try {
	    		if (!forwardPaths.isEmpty() && forwardPathProp.length() == 0)
	    			addActionStatements(actionPath, actionClass, actionFormClass, forwardPaths);
	    		else {
	    			forwardPaths.add(updatePath(forwardPathProp, strutsModuleName));
	    			addActionStatements(actionPath, actionClass, actionFormClass, forwardPaths);
	    		}
	    	} catch (Throwable t) {
				logger.error("Unexpected Exception", t);
	    	} finally {
				rdfRepo.commitTransaction();
	    	}
		}
		
	}
	
	private String updatePath(String path, String strutsModuleName) {
		String forwardPath = "";
		// jsp / do: 
		// add '%' to mark file extensions so they can be processed properly when adding statements
		if (path.startsWith("/") || path.startsWith(".")) 
			forwardPath = "%"+ path;
		// tiles
		else forwardPath = strutsModuleName + "/"+ path;
		return forwardPath;
	}
	
	////////////////////
	// Add Statements //
	////////////////////

	private void addActionStatements(String actionPath, String actionClass, String actionFormClass, List<String> forwardPaths) {
		ItemFldrFwdsRes actionRes = getPathRes(actionPath, forwardPaths);

		DepAndChildrenStrengthSummarizer dss = builder.getDSS(); 
		dss.updateSrcFldrCache(actionRes.fldrRes);
		
		Resource actionClassRes = classToRes(rdfRepo, actionClass);
		dss.storeNCacheTypeType(actionRes.itemRes, EJCore.implementedBy, actionClassRes);
		dss.storeNCacheTypeType(actionRes.itemRes, RJCore.refType, actionClassRes);

		if (actionFormClass != null && !actionFormClass.equals("")) {
			Resource actionFormClassRes = classToRes(rdfRepo, actionFormClass);
			dss.storeNCacheTypeType(actionRes.itemRes, EJCore.actionData, actionFormClassRes);
			dss.storeNCacheTypeType(actionRes.itemRes, RJCore.refType, actionFormClassRes);
		}
		
		for (Resource forward : actionRes.forwards) {
			dss.storeNCacheTypeType(actionRes.itemRes, EJCore.tilesCall, forward);
		}
	}
	
	 private ItemFldrFwdsRes getPathRes(String path, List<String> forwardPaths) {
		path =  EJCore.WEBROOT + path;
		String actionId = EJCore.getId(path.replaceAll("/", "."));
		Resource actionRes  = RSECore.idToResource(rdfRepo, EJCore.jdtExtWkspcNS, actionId);
		rdfRepo.addStatement(actionRes, rdfRepo.rdfType, EJCore.webPathType);
		rdfRepo.addStatement(actionRes, RSECore.name, "/" + actionId.substring(actionId.indexOf("$")+1));
		rdfRepo.addStatement(actionRes, RJCore.access, RJCore.publicAccess);
		Resource packageRes = AsmPackageSupport.getPackage(rdfRepo, AsmPackageSupport.getPckgNameFromClassID(actionId), projectRes);
		rdfRepo.addStatement(packageRes, RSECore.contains, actionRes);
		rdfRepo.addStatement(packageRes, EJCore.webFolder, RSECore.trueStatement);

//		rdfRepo.commitTransaction();
		builder.runProcessors(actionRes, false);
//		rdfRepo.startTransaction();
		String forwardId = "";
		List<Resource> forwards = new ArrayList<Resource>();
		for (String forwardPath : forwardPaths) {
			URI type = EJCore.webPathType;
			String forwardToActionSlash = "";
			if (forwardPath.contains("%")) {
				// .do
				if (forwardPath.contains(".do?") || forwardPath.endsWith(".do")) {
					
					forwardPath = forwardPath.substring(0, forwardPath.indexOf(".do"));
					forwardPath = forwardPath.replace("%", "");
					forwardPath = EJCore.WEBROOT + forwardPath;
					if (forwardPath.contains(".."))
						forwardPath= forwardPath.replace("..", "");
					forwardId = EJCore.webFileToID(forwardPath);
					forwardToActionSlash = "/";
				} else {
					// JSP
					forwardPath = EJCore.WEBROOT + forwardPath;
					forwardPath = forwardPath.replace("%", "");
					forwardId = EJCore.webFileToID(forwardPath);
				}
			} else {
				//Tiles
				type = EJCore.tilesDefType;
				forwardPath = EJCore.WEBROOT + ".tiles" + forwardPath;
				forwardPath = forwardPath.replaceAll("/", ".");
				forwardId = EJCore.getId(forwardPath);
			}
			Resource fwdPackageRes = AsmPackageSupport.getPackage(rdfRepo, AsmPackageSupport.getPckgNameFromClassID(forwardId), projectRes);
			Resource forwardRes  = RSECore.idToResource(rdfRepo, EJCore.jdtExtWkspcNS, forwardId);
			rdfRepo.addStatement(forwardRes, rdfRepo.rdfType, type);
			rdfRepo.addStatement(forwardRes, RSECore.name, forwardToActionSlash+ forwardId.substring(forwardId.indexOf("$")+1));
			rdfRepo.addStatement(forwardRes, RJCore.access, RJCore.publicAccess);
			rdfRepo.addStatement(actionRes, EJCore.tilesCall, forwardRes);
			rdfRepo.addStatement(fwdPackageRes, RSECore.contains, forwardRes);
			forwards.add(forwardRes);
		}
		return new ItemFldrFwdsRes(actionRes, packageRes, forwards);
	}
	    
	////////////////////
	// Helper Methods //
	////////////////////

    private static Resource classToRes(ReloRdfRepository rdfRepo, String fqn){
    	if (fqn.contains(".")) {
    		String beg = fqn.substring(0, fqn.lastIndexOf("."));
    		String end = fqn.substring(fqn.lastIndexOf(".")+1);
    		fqn = beg + "$" + end;
    	}
		
		return RSECore.idToResource(rdfRepo, RJCore.jdtWkspcNS, fqn);
    }
	public void autoBuildJar(String string) throws ZipException, IOException {
	}
}
