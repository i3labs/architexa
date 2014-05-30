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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IStartup;
import org.openrdf.model.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.services.PluggableInterfaceProvider;
import com.architexa.diagrams.chrono.util.SeqUtil;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.AtxaBuildVisitor;
import com.architexa.diagrams.jdt.builder.PluggableExtensionBuilderSupport;
import com.architexa.diagrams.jdt.builder.ResourceToProcess;
import com.architexa.diagrams.services.PluggableNavAids;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class BaseSpringExtensionBuilder implements IStartup, PluggableExtensionBuilderSupport.IAtxaExtensionBuilder {
	
	public ReloRdfRepository rdfRepo = StoreUtil.getDefaultStoreRepository();

	public IWorkspaceRoot wkspcRoot = null;
	public String warRootPath = null; // used as the basis for war file paths
	public Resource projectRes = null;
	public AtxaBuildVisitor builder = null;
	
	public Map<String, String> beanIdToClassMap = new HashMap<String, String>();
	
	public static final Logger logger = Activator.getLogger(BaseSpringExtensionBuilder.class);
	
	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	static boolean isInit = false;
	private void earlyStartupInternal() {
		addSpringPluggableTypes();
	}

	private void addSpringPluggableTypes() {
		PluggableNavAids.registerNavAidsSource(PluggableNavAids.getFWDSpec(EJCore.springBeanPropertyRef));
		PluggableNavAids.registerNavAidsSource(PluggableNavAids.getREVSpec(EJCore.springBeanPropertyRef));
		SeqUtil.pip = new PluggableInterfaceProvider() {
			@Override
			public Resource interfaceGuesser(Resource fieldRes) {
				try {
					return (Resource) rdfRepo.getStatement(fieldRes, EJCore.springBeanPropertyRef, null).getObject();
				} catch (NullPointerException e) {
					logger.error("No resouce in repository for field: " + fieldRes);
					return null;
				}
			}};
		isInit = true;
	}
	
	public void processProject(AtxaBuildVisitor builder, IProject resource) {
	}
	
	public List<Resource> processExtensionResource(AtxaBuildVisitor builder, ResourceToProcess rtp) {
		this.builder = builder;
		this.rdfRepo = builder.getRepo();
		this.projectRes = builder.getProjectResource();
		try {
			if (rtp.getName().endsWith("web.xml") && rtp.resource.exists()) {
		    	wkspcRoot  = rtp.resource.getWorkspace().getRoot();
		    	
		    	String webXMLPath = rtp.resource.getFullPath().toString();
		    	warRootPath = webXMLPath.substring(0, webXMLPath.length() - "/WEB-INF/web.xml".length());

				logger.info("Processing: " + rtp.getName());
				processWebXML((IFile) rtp.resource);
			}
		} catch (Throwable t) {
			logger.error("Unexpected Exception", t);
		}
		// doesn't need to call the runProcessors
		return null;
	}
	
	public void processWebXML(IFile webXmlRes) throws SAXException, IOException, ParserConfigurationException, CoreException, XPathExpressionException {
		if (!webXmlRes.exists()) return;
		Document webXMLDoc = EJCore.getXMLDoc(webXmlRes.getContents());
		List<String> configXmlList = getConfigXMLList(webXMLDoc);
		
		clearCachedMaps();
		for (String configPath : configXmlList)
			processSpringConfig(warRootPath, configPath);

		for (String configPath : configXmlList)
			processSpringConfigProperties(warRootPath, configPath);
		
		for (String configPath : configXmlList)
			processSWFConfig(warRootPath, configPath);
		
	}
	
	protected void clearCachedMaps() {
	}

	protected void processSWFConfig(String warRootPath2, String configPath) throws SAXException, IOException, ParserConfigurationException, CoreException, XPathExpressionException {
	}
	
	protected void processSpringConfigProperties(String warRootPath2, String configPath) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, CoreException {
	}

	protected void processSpringConfig(String warRootPath2, String configPath) throws SAXException, IOException, ParserConfigurationException, CoreException, XPathExpressionException {
	}
	
	public List<String> getConfigXMLList(Document webXMLDoc) throws XPathExpressionException {
		List<String> configXmlList = new ArrayList<String>();
		String xpathQuery = "//web-app";
		NodeList springInitParams = EJCore.xPathQueryEval(xpathQuery, webXMLDoc);
		for (int i = 0; i < springInitParams.getLength(); i++) {
			Element initParam = (Element) springInitParams.item(i);
			NodeList contextParam = initParam.getElementsByTagName("context-param");
			if (contextParam.getLength() > 0) {
				for (int j = 0; j < contextParam.getLength(); j++) {
					Element paramItem = (Element) contextParam.item(j);
					NodeList paramName = paramItem.getElementsByTagName("param-name");
					String paramText = paramName.item(0).getTextContent();
					if (paramText != null && paramText.startsWith("contextConfigLocation")) {
						NodeList paramValue = paramItem.getElementsByTagName("param-value");
						String paramValueText = paramValue.item(0).getTextContent();
						String[] paramValueTextLines = removeLineFeedsAndTabs(paramValueText);
						for (String configPath : paramValueTextLines) {
							logger.info(" Config Path: " + configPath);
							
							if (configPath.contains("classpath:"))
								configPath = configPath.replace("classpath:", "");
							configXmlList.add(configPath);
						}
					}
				}
			}
		}
		
		xpathQuery = "//servlet";
		springInitParams = EJCore.xPathQueryEval(xpathQuery, webXMLDoc);
		for (int i = 0; i < springInitParams.getLength(); i++) {
			Element initParam = (Element) springInitParams.item(i);
			NodeList paramNameNodes = initParam.getElementsByTagName("servlet-name");
			if (paramNameNodes.getLength() > 0) {
				Node paramItem = paramNameNodes.item(0);
				String paramText = paramItem.getTextContent();
				if (paramText != null) {
					String[] paramTextLines = removeLineFeedsAndTabs(paramText);	 
					for (String paramTextLine : paramTextLines) {
						configXmlList.add("/WEB-INF/" + paramTextLine + "-servlet.xml");
					}
				}
			}
		}
		return configXmlList;
	}
	
	public String[] removeLineFeedsAndTabs(String configPath) {
		 configPath = configPath.replaceAll("\r", "");
		 configPath = configPath.replaceAll("\t", "");
		 configPath = configPath.replaceAll(" ", "\n");
		 configPath.trim();
		 
		 return configPath.split("\n");
	}
	
	 public static Resource classToRes(ReloRdfRepository rdfRepo, String fqn){
	    	if (fqn == null || fqn.length() == 0)
	    		return null;
	    	String beg = fqn.substring(0, fqn.lastIndexOf("."));
			String end = fqn.substring(fqn.lastIndexOf(".")+1);
			fqn = beg + "$" + end;
			
			return RSECore.idToResource(rdfRepo, RJCore.jdtWkspcNS, fqn);
	 }
	 public static Resource fieldToRes(ReloRdfRepository rdfRepo, String fqn){
	    	if (fqn == null || fqn.length() == 0)
	    		return null;
	    	String end = fqn.substring(fqn.lastIndexOf(".")+1);
	    	String fullbeg = fqn.substring(0, fqn.lastIndexOf("."));
	    	String beg = fullbeg.substring(0, fullbeg.lastIndexOf("."));
	    	String mid = fullbeg.substring(fullbeg.lastIndexOf(".")+1);
	    	
			fqn = beg + "$" + mid +"."+end;
			
			return RSECore.idToResource(rdfRepo, RJCore.jdtWkspcNS, fqn);
	 }

	public void autoBuildJar(String string) throws ZipException, IOException {
		
	}
}
