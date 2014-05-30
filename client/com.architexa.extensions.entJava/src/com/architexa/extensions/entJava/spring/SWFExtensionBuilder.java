package com.architexa.extensions.entJava.spring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.PluggableExtensionBuilderSupport;
import com.architexa.diagrams.jdt.builder.asm.AsmPackageSupport;
import com.architexa.diagrams.services.PluggableNavAids;
import com.architexa.extensions.entJava.BaseSpringExtensionBuilder;
import com.architexa.extensions.entJava.EJCore;

public class SWFExtensionBuilder extends BaseSpringExtensionBuilder {
//	 public static final Logger logger = Activator.getLogger(Struts1ExtensionBuilder.class);

	 @Override
		public void earlyStartup() {
			try {
				super.earlyStartup();
				earlyStartInternal();
			} catch (Throwable t) {
				logger.error("Unexpected Error", t);
			}
		}
	String Webflow_Config = "//http://www.springframework.org/schema/webflow-config";
	
	
	private void earlyStartInternal() {
		PluggableExtensionBuilderSupport.registerExtensionBuilder("xml", this);
		PluggableNavAids.registerNavAidsSource(PluggableNavAids.getFWDSpec(EJCore.transitionCall));
		PluggableNavAids.registerNavAidsSource(PluggableNavAids.getREVSpec(EJCore.transitionCall));
	}
	
	@Override
	protected void processSWFConfig(String moduleName, String configPath) throws SAXException, IOException, ParserConfigurationException, CoreException, XPathExpressionException {
		
			IResource configRes = wkspcRoot.findMember(warRootPath + configPath);
			if (configRes == null || !configRes.exists() || !(configRes instanceof IFile)) {
				configRes = SpringExtensionBuilder.findResourceInWksp(configPath, wkspcRoot);
			}
			if (configRes == null || !configRes.exists() || !(configRes instanceof IFile)) return; // we are likely not processing the main 'web.xml' file
			
			Document configXMLDoc = EJCore.getXMLDoc(((IFile)configRes).getContents());

			//look for imports
			NodeList imports =  EJCore.xPathQueryEval("//import", configXMLDoc);
			List<String> importList = new ArrayList<String>();
			for (int i = 0; i < imports.getLength(); i++) {
				Node impt = imports.item(i);
				NamedNodeMap map = impt.getAttributes();
				if (map.getNamedItem("resource") != null)
					importList.add(map.getNamedItem("resource").getTextContent());
					
			}
			
			if (importList.isEmpty()) {
				processConfigXml(configXMLDoc);
				return;
			}
			
			for (String xml: importList) {
				configRes = wkspcRoot.findMember(warRootPath + configPath.substring(0, configPath.lastIndexOf("/") + 1) + xml);
				if (configRes == null || !configRes.exists()) return; // we are likely not processing the main 'web.xml' file
				
				Document configXMLDocImp = EJCore.getXMLDoc(((IFile)configRes).getContents());
				processConfigXml(configXMLDocImp);
			}
		}
		
		private void populateTransitionMap(NodeList nodeList, Node urlPath) {
			for (int j = 0; j < nodeList.getLength(); j++) {
				Node child = nodeList.item(j);
				NamedNodeMap attrib = child.getAttributes();
				if (attrib == null) continue;
				Node exp = attrib.getNamedItem("expression");
				if (exp != null) 
					actionIdToClassId.put(urlPath.getTextContent(), exp.getTextContent());
				Node transition = attrib.getNamedItem("to");
				if (transition == null) continue;
				String tranText = transition.getTextContent();
				if (tranText.equals(urlPath.getTextContent()))
					continue;
				
				if (urlToTransitionPathMap.containsKey(urlPath.getTextContent())) {
					urlToTransitionPathMap.get(urlPath.getTextContent()).add(tranText);
				} else {
					Set<String> set = new HashSet<String>();
					set.add(tranText);
					urlToTransitionPathMap.put(urlPath.getTextContent(), set);
				}
			}
		}
		
		private void populateTransitionMapWithDecision(NodeList nodeList, Node urlPath) {
			for (int j = 0; j < nodeList.getLength(); j++) {
				Node child = nodeList.item(j);
				NamedNodeMap attrib = child.getAttributes();
				if (attrib == null) continue;
				Node test = attrib.getNamedItem("test");
				if (test == null) continue;
//				if (exp != null) 
//					actionIdToClassId.put(urlPath.getTextContent(), exp.getTextContent());
				Node then = attrib.getNamedItem("then");
				if (then == null) continue;
				String thenText = then.getTextContent();
				
				Node elseNode = attrib.getNamedItem("else");
				if (elseNode == null) continue;
				String elseText = elseNode.getTextContent();
				
				if (!thenText.equals(urlPath.getTextContent())) {
					if (urlToTransitionPathMap.containsKey(urlPath.getTextContent())) {
						urlToTransitionPathMap.get(urlPath.getTextContent()).add(thenText);
					} else {
						Set<String> set = new HashSet<String>();
						set.add(thenText);
						urlToTransitionPathMap.put(urlPath.getTextContent(), set);
					}
				}
				
				if (!elseText.equals(urlPath.getTextContent())) {
					if (urlToTransitionPathMap.containsKey(urlPath.getTextContent())) {
						urlToTransitionPathMap.get(urlPath.getTextContent()).add(elseText);
					} else {
						Set<String> set = new HashSet<String>();
						set.add(elseText);
						urlToTransitionPathMap.put(urlPath.getTextContent(), set);
					}
				}
			}
		}
		
		List<String> webViewPaths = new ArrayList<String>();
		List<String> webActionStates = new ArrayList<String>();
		List<String> webDecisionStates = new ArrayList<String>();
		List<String> webSubFlowStates = new ArrayList<String>();
		List<String> webEndStates = new ArrayList<String>();
		
		Map<String, String> urlToJspPathMap = new LinkedHashMap<String, String>();
		Map<String, Set<String>> urlToTransitionPathMap = new LinkedHashMap<String, Set<String>>();
		Map<String, String> actionIdToClassId = new LinkedHashMap<String, String>();
		private void processXML(String xmlPath) throws SAXException, IOException, ParserConfigurationException, CoreException, XPathExpressionException {
			IResource configRes = wkspcRoot.findMember(warRootPath + xmlPath);
			if (configRes == null || !configRes.exists()) return; // we are likely not processing the main 'web.xml' file
			
			Document configXMLDoc = EJCore.getXMLDoc(((IFile)configRes).getContents());
			NodeList viewStates = EJCore.xPathQueryEval("//view-state", configXMLDoc);
			NodeList actionStates = EJCore.xPathQueryEval("//action-state", configXMLDoc);
			NodeList subFlowStates = EJCore.xPathQueryEval("//subflow-state", configXMLDoc);
			NodeList decisionStates = EJCore.xPathQueryEval("//decision-state", configXMLDoc);
			NodeList endStates = EJCore.xPathQueryEval("//end-state", configXMLDoc);
			// Decision States
			processStates(decisionStates, EJCore.DECISION_STATE);
			//ActionStates
			processStates(actionStates, EJCore.ACTION_STATE);
			//View States
			for (int i = 0; i < viewStates.getLength(); i++) {
				Node view = viewStates.item(i);
				NamedNodeMap attribs = view.getAttributes();
				if (attribs == null) continue;
				Node urlPath = attribs.getNamedItem("id");
				Node jspPath = attribs.getNamedItem("view");
				if (urlPath != null && jspPath != null) {
					urlToJspPathMap.put(urlPath.getTextContent(), jspPath.getTextContent());
					webViewPaths.add(urlPath.getTextContent());
					webViewPaths.add(jspPath.getTextContent());
				}
				populateTransitionMap(view.getChildNodes(), urlPath);
			}
			//subFlowStates
			processStates(subFlowStates, EJCore.SUBFLOW_STATE);
			//End States
			processStates(endStates, EJCore.END_STATE);
			
		}
		
		private void processStates(NodeList states, int type) {
			for (int i = 0; i < states.getLength(); i++) {
				Node action = states.item(i);
				NamedNodeMap attribs = action.getAttributes();
				if (attribs == null) continue;
				Node urlPath = attribs.getNamedItem("id");
				if (urlPath == null) continue;
				
				if (EJCore.DECISION_STATE == type)
					populateTransitionMapWithDecision(action.getChildNodes(), urlPath);
				else	
					populateTransitionMap(action.getChildNodes(), urlPath);

				if (EJCore.END_STATE == type) {
					Node jspPath = attribs.getNamedItem("view");
					if (jspPath != null)
						urlToJspPathMap.put(urlPath.getTextContent(), urlPath.getTextContent());
				}
				else	
					urlToJspPathMap.put(urlPath.getTextContent(), urlPath.getTextContent());
				
				if (EJCore.ACTION_STATE == type)
					webActionStates.add(urlPath.getTextContent());
				if (EJCore.DECISION_STATE == type)
					webDecisionStates.add(urlPath.getTextContent());
				if (EJCore.SUBFLOW_STATE == type)
					webSubFlowStates.add(urlPath.getTextContent());
				if (EJCore.END_STATE == type)
					webEndStates.add(urlPath.getTextContent());
				
			}
		}
		
		private void processConfigXml(Document configXMLDoc) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, CoreException {
			// look for id to class mapping
			NodeList formBeans =  EJCore.xPathQueryEval("//bean", configXMLDoc);
			for (int i=0; i<formBeans.getLength(); i++) {
				Node formBeanEntry = formBeans.item(i);
				NamedNodeMap attribs = formBeanEntry.getAttributes();
				Node id = attribs.getNamedItem("id");
				Node classStr = attribs.getNamedItem("class");
				if (id == null || classStr == null) continue;
				beanIdToClassMap.put(id.getTextContent(), classStr.getTextContent());
			}
			
			// Get Individual Flow xmls
		    List<String> flowXMLList = new ArrayList<String>();
		    NodeList registry =  EJCore.xPathQueryEval("//*[name()='webflow:flow-registry']", configXMLDoc);
		    for (int i = 0; i < registry.getLength(); i++) {
				Node reg = registry.item(i);
				NodeList nList = reg.getChildNodes();
				for (int j = 0; j < nList.getLength(); j++) {
					Node loc = nList.item(j);
					NamedNodeMap att = loc.getAttributes();
					if (att == null) continue;
					Node path = att.getNamedItem("path");
					if (path == null) continue;
					flowXMLList.add(path.getTextContent());
				}
			}
			
		    // Process individual xmls from the list
		    for (String xmlPath : flowXMLList) {
		    	processXML(xmlPath);
		    }
		    
		    processBeans();
		    processMaps();
		}
		
		private void processBeans() {
			Set<String> beans = beanIdToClassMap.keySet();
	    	try {
	    		rdfRepo.startTransaction();
	    		for (int j=0; j<beans.size(); j++) {
		    		String bean = (String) beans.toArray()[j]; 
		    		String beanClass = beanIdToClassMap.get(bean);
		    		Resource beanClassRes = classToRes(rdfRepo, beanClass);
		    		rdfRepo.addStatement(beanClassRes, EJCore.springBeanClassProp, RSECore.trueStatement);
				}
	    	} catch (Throwable t) {
				logger.error("Unexpected Exception", t);
	    	} finally {
				rdfRepo.commitTransaction();
	    	}
		}
		
		Set<String> forwardPaths= new HashSet<String>();
		Set<String> intermediatePaths= new HashSet<String>();
		private void processMaps() {
			rdfRepo.startTransaction();
			for (String url : urlToJspPathMap.keySet()) {
				populateForwards(url);
				Resource actionRes = getPathRes(url, forwardPaths, intermediatePaths);
				
				if (actionIdToClassId.containsKey(url)) {
					Resource actionClassRes = classToRes(rdfRepo, beanIdToClassMap.get(actionIdToClassId.get(url)));
					if (actionClassRes != null) 
						rdfRepo.addStatement(actionRes, EJCore.implementedBy, actionClassRes);
				}
				forwardPaths.clear();
				intermediatePaths.clear();
			}
			
			rdfRepo.commitTransaction();
		}
		
		private void populateForwards(String url){
			if (!urlToJspPathMap.containsKey(url) || urlToJspPathMap.get(url).length() == 0)
				return;
			
			//Break loops
			if (forwardPaths.contains(urlToJspPathMap.get(url)))
				return;
			
			if (!urlToJspPathMap.get(url).equals(url))
				forwardPaths.add(urlToJspPathMap.get(url));
			if (urlToTransitionPathMap.containsKey(url)) {
				Set<String> trans = urlToTransitionPathMap.get(url);
				for (String tran : trans) {
					if (url.equals(tran)) continue;
					intermediatePaths.add(tran);
				}
			}
		}

		 private Resource getPathRes(String path, Set<String> forwardPaths, Set<String> intermediatePaths2) {
			 URI resType = getType(path);
			 if (!path.startsWith("/"))
				 path = "/" + path;
			path =  EJCore.WEBROOT + path;
			String actionId = EJCore.getId(path.replaceAll("/", "."));
			Resource actionRes  = RSECore.idToResource(rdfRepo, EJCore.jdtExtWkspcNS, actionId);
//			rdfRepo.addStatement(actionRes, rdfRepo.rdfType, EJCore.webPathType);
			rdfRepo.addStatement(actionRes, rdfRepo.rdfType, resType);
			if (resType.equals(EJCore.webPathType))
				rdfRepo.addStatement(actionRes, RSECore.name, "/" + actionId.substring(actionId.indexOf("$")+1));
			else
				rdfRepo.addStatement(actionRes, RSECore.name, actionId.substring(actionId.indexOf("$")+1));
			rdfRepo.addStatement(actionRes, RJCore.access, RJCore.publicAccess);
			Resource packageRes = AsmPackageSupport.getPackage(rdfRepo, AsmPackageSupport.getPckgNameFromClassID(actionId), projectRes);
			rdfRepo.addStatement(packageRes, RSECore.contains, actionRes);
			rdfRepo.addStatement(packageRes, EJCore.webFolder, RSECore.trueStatement);

//			builder.runProcessors(actionRes, false);
			String forwardId = "";
			List<Resource> forwards = new ArrayList<Resource>();
			for (String intPath : intermediatePaths2) {
				URI type = getType(intPath);
				if (!intPath.startsWith("/"))
					intPath = "/" + intPath;
				intPath =  EJCore.WEBROOT + intPath;
				forwardId = EJCore.getId(intPath.replaceAll("/", "."));
				addForwardRes(forwardId, forwards, actionRes, type, resType);
			}
			
			for (String forwardPath : forwardPaths) {
				URI type = getType(forwardPath);
					//.do
					if (forwardPath.contains(".do?") || forwardPath.endsWith(".do")) {
						forwardPath = forwardPath.substring(forwardPath.indexOf("/"));
						forwardPath = forwardPath.substring(0, forwardPath.indexOf(".do"));
						forwardPath = forwardPath.replace("%", "");
						forwardPath = EJCore.WEBROOT + forwardPath;
						if (forwardPath.contains(".."))
							forwardPath= forwardPath.replace("..", "");
						forwardId = EJCore.webFileToID(forwardPath);
					} else {
						// JSP
						forwardPath = EJCore.WEBROOT + forwardPath;
						forwardPath = forwardPath.replace("%", "");
						forwardId = EJCore.webFileToID(forwardPath);
						forwardId += ".jsp";
					}
				addForwardRes(forwardId, forwards, actionRes, type, resType);
			}
			return actionRes;
		}
		    
		 
		private URI getType(String intPath) {
			if (webViewPaths.contains(intPath))
				return EJCore.webViewStateType;
			if (webActionStates.contains(intPath))
				return EJCore.webActionStateType;
			if (webDecisionStates.contains(intPath))
				return EJCore.webDecisionStateType;
			if (webSubFlowStates.contains(intPath))
				return EJCore.webSubFlowStateType;
			return EJCore.webEndStateType;
		}

		private void addForwardRes (String forwardId, List<Resource> forwards, Resource actionRes, URI resType, URI callerResType) {
			URI callType = EJCore.transitionCall;
			if (callerResType.equals(EJCore.webPathType) && resType.equals(EJCore.webPathType))
				callType = EJCore.tilesCall;
			Resource fwdPackageRes = AsmPackageSupport.getPackage(rdfRepo, AsmPackageSupport.getPckgNameFromClassID(forwardId), projectRes);
			Resource forwardRes  = RSECore.idToResource(rdfRepo, EJCore.jdtExtWkspcNS, forwardId);
//			rdfRepo.addStatement(forwardRes, rdfRepo.rdfType, EJCore.webPathType);
			rdfRepo.addStatement(forwardRes, rdfRepo.rdfType, resType);
			if (resType.equals(EJCore.webPathType))
				rdfRepo.addStatement(forwardRes, RSECore.name, "/" + forwardId.substring(forwardId.indexOf("$")+1));
			else
				rdfRepo.addStatement(forwardRes, RSECore.name, forwardId.substring(forwardId.indexOf("$")+1));
			rdfRepo.addStatement(forwardRes, RJCore.access, RJCore.publicAccess);
//			rdfRepo.addStatement(actionRes, EJCore.tilesCall, forwardRes);
			rdfRepo.addStatement(actionRes, callType, forwardRes);
			rdfRepo.addStatement(fwdPackageRes, RSECore.contains, forwardRes);
			forwards.add(forwardRes);
		}
}
