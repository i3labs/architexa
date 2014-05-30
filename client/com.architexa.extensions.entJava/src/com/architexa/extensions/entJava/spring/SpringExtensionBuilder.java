package com.architexa.extensions.entJava.spring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.sesame.sail.StatementIterator;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.builder.PluggableExtensionBuilderSupport;
import com.architexa.extensions.entJava.Activator;
import com.architexa.extensions.entJava.BaseSpringExtensionBuilder;
import com.architexa.extensions.entJava.EJCore;

public class SpringExtensionBuilder extends BaseSpringExtensionBuilder {
    public static final Logger logger = Activator.getLogger(SpringExtensionBuilder.class);

	private HashMap<String, String> propIDToRefBeanMap = new HashMap<String, String>();
	
	private HashMap<String, String> refBeanToFactoryMethodMap = new HashMap<String, String>();
	private HashMap<String, List<String>> beanToConstructorArgs = new HashMap<String, List<String>>();
	
	@Override
	protected void clearCachedMaps() {
		propIDToRefBeanMap.clear();
		refBeanToFactoryMethodMap.clear();
		beanToConstructorArgs.clear();
	}
	
	@Override
	public void earlyStartup() {
		try {
			super.earlyStartup();
			PluggableExtensionBuilderSupport.registerExtensionBuilder("xml", this);
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	@Override
	protected void processSpringConfig(String strutsModuleName, String springConfigPath) throws SAXException, IOException, ParserConfigurationException, CoreException, XPathExpressionException {
		String[] springConfigPathLines = removeLineFeedsAndTabs(springConfigPath);
		for (String configPath : springConfigPathLines) {
			IResource configRes = wkspcRoot.findMember(warRootPath + configPath);
			if (configRes == null || !configRes.exists() || !(configRes instanceof IFile)) {
				configRes = findResourceInWksp(configPath, wkspcRoot);
			}
			
			if (configRes == null || !configRes.exists() || !(configRes instanceof IFile)) {
				continue; // we are likely not processing the main 'web.xml' file
			}
			Document configXMLDoc = EJCore.getXMLDoc(((IFile)configRes).getContents());

			// look for: struts-config/form-beans/form-bean ... and add all name->type in a map
			NodeList formBeans =  EJCore.xPathQueryEval("//bean", configXMLDoc);
			for (int i=0; i<formBeans.getLength(); i++) {
				Node formBeanEntry = formBeans.item(i);
				NamedNodeMap attribs = formBeanEntry.getAttributes();
				Node id = attribs.getNamedItem("id");
				Node name = attribs.getNamedItem("name");
				Node classNode = attribs.getNamedItem("class");
				// if the bean has a class with either a name or id use it as the
				// key to reference the bean class/ TODO: add support for separate
				// bean types with appropriate refs similar to struts 
				if (classNode != null) {
					if (id != null) 
						beanIdToClassMap.put(id.getTextContent(), classNode.getTextContent());
					if (name!=null)
						beanIdToClassMap.put(name.getTextContent(), classNode.getTextContent());
				} else
					logger.error("Bad Spring XML Format");
			}
		}
	}


	public static IResource findResourceInWksp(String configPath, IWorkspaceRoot wkspcRoot) {
		  IProject[] projects = wkspcRoot.getProjects();
	        for (int i=0;i<projects.length; i++) {
	            IJavaProject project = null;
	            try {
	                project = JavaCore.create(projects[i]);
	            } catch (IllegalArgumentException iae) {}
	            if (project == null || !project.exists()) continue;
	            try {
		            IPackageFragmentRoot[] pfrs = project.getAllPackageFragmentRoots();
		            for (int j=0; j<pfrs.length; j++) {
						IResource matchingRes = searchChildren(configPath, pfrs[j]);
						if (matchingRes!=null)
							return matchingRes;
		            }
	            } catch (JavaModelException e) {
					e.printStackTrace();
				}
	        }
	        return null;
	}
	private static IResource searchChildren(String configPath, IPackageFragmentRoot iPackageFragmentRoot) throws JavaModelException {
		if (iPackageFragmentRoot.equals(configPath)) 
			return  iPackageFragmentRoot.getResource();
		else {
			for (Object child : iPackageFragmentRoot.getNonJavaResources()) {
				if (child.toString().contains(configPath) && child instanceof IResource) 
					return  (IResource) child;
//					System.err.println(child.toString());
			}
		}
		return null;
	}
	
	
	@Override
	protected void processSpringConfigProperties(String springModuleName, String springConfigPath) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, CoreException {
		String[] springConfigPathLines = removeLineFeedsAndTabs(springConfigPath);
		for (String configPath : springConfigPathLines) {
			IResource configRes = wkspcRoot.findMember(springModuleName + configPath);
			
			if (configRes == null || !configRes.exists() || !(configRes instanceof IFile)) {
				configRes = findResourceInWksp(configPath, wkspcRoot);
			}
			if (configRes == null || !configRes.exists() || !(configRes instanceof IFile)) continue; // we are likely not processing the main 'web.xml' file
			
			
			Document configXMLDoc = EJCore.getXMLDoc(((IFile)configRes).getContents());
			// add properties to thier beans
			NodeList formBeans =  EJCore.xPathQueryEval("//bean", configXMLDoc);
			for (int i=0; i<formBeans.getLength(); i++) {
				Node formBeanEntry = formBeans.item(i);
				NodeList childNodes = formBeanEntry.getChildNodes();
				NamedNodeMap beanAttribs = formBeanEntry.getAttributes();
				Node id = beanAttribs.getNamedItem("id");
				Node factory = beanAttribs.getNamedItem("factory-method");
				String factoryName = null;
				if (factory != null) {
					factoryName = factory.getTextContent();
				}
				for (int j=0; j<childNodes.getLength(); j++) {
					Node property = childNodes.item(j);
					boolean isConstructorArgProperty = property.toString().contains("constructor-arg");
					boolean isProperty = property.toString().contains("property");
					NamedNodeMap attribs = property.getAttributes();
					if (attribs==null) continue;
					Node name = attribs.getNamedItem("name");
					Node ref = attribs.getNamedItem("ref");
//					if (name != null && id != null)
//						propNameToBeanMap.put(name.getTextContent(), id.getTextContent());
					if (id != null && ref != null) {
						String beanClassId = beanIdToClassMap.get(id.getTextContent());
						String factoryOrConstructorMethod = "";
						if (isConstructorArgProperty) { 
							if(factoryName == null) {
								String constructorName = beanClassId.substring(beanClassId.lastIndexOf(".")+1);
								factoryOrConstructorMethod = beanClassId + "::" + constructorName ;
							} else {
								factoryOrConstructorMethod = beanClassId+"::"+factoryName;
							}
							List<String> constructorArgs = beanToConstructorArgs.get(id.getTextContent());
							
							if (constructorArgs == null)
								constructorArgs = new ArrayList<String>();
							if (!constructorArgs.contains(ref.getTextContent()))
									constructorArgs.add(ref.getTextContent());								
							beanToConstructorArgs.put(id.getTextContent(), constructorArgs);
							if (!factoryOrConstructorMethod.equals(""))
								refBeanToFactoryMethodMap.put(id.getTextContent() + "><" + ref.getTextContent(), factoryOrConstructorMethod);
						} else if (isProperty) { //assume properties are fields in the class
							propIDToRefBeanMap.put(id.getTextContent() +"::" + name.getTextContent(), ref.getTextContent());
						}
						
					}
//					else
//						logger.warn("Bad Spring XML Format");
				}
			}
			
			// populate bean properties to their refs 
//			NodeList properties =  EJCore.xPathQueryEval("//property", configXMLDoc);
//			for (int i=0; i<properties.getLength(); i++) {
//				Node formBeanEntry = properties.item(i);
//				NamedNodeMap attribs = formBeanEntry.getAttributes();
//				Node name = attribs.getNamedItem("name");
//				Node ref = attribs.getNamedItem("ref");
//				if (name != null && ref != null)
//					beanPropToRefClassMap.put(name.getTextContent(), ref.getTextContent());
//			}

			// old way of storing constructor args as properties
//			NodeList constructorArgs =  EJCore.xPathQueryEval("//constructor-arg", configXMLDoc);
//			for (int i=0; i<constructorArgs.getLength(); i++) {
//				Node formBeanEntry = constructorArgs.item(i);
//				NamedNodeMap attribs = formBeanEntry.getAttributes();
//				Node ref = attribs.getNamedItem("ref");
//				if (ref != null) {
//					beanPropToRefClassMap.put(ref.getTextContent(), ref.getTextContent());
//				}
//			}
			
			// add to repo
			processBeans();
		}
		
	}
	
	

	private void processBeans() {
		// for each bean find class, add bean property statement
		// for each bean find fields(ids) and add beanFieldProp
		// for each field add ref to class
		Set<String> beans = beanIdToClassMap.keySet();
    	try {
    		rdfRepo.startTransaction();
    		for (int j=0; j<beans.size(); j++) {
	    		String bean = (String) beans.toArray()[j]; 
	    		String beanClass = beanIdToClassMap.get(bean);
	    		/*String beanClassId =*/ EJCore.getId(beanClass);
	    		Resource beanClassRes = classToRes(rdfRepo, beanClass);
	    		rdfRepo.addStatement(beanClassRes, EJCore.springBeanClassProp, RSECore.trueStatement);
	    		
//	    		for (String propName : propNameToBeanMap.keySet()) {
//		    		
//		    		String propertyParentBean = propNameToBeanMap.get(propName);
//		    		Resource beanFieldRes  = RSECore.idToResource(rdfRepo, RJCore.jdtWkspcNS, propertyParentBean+"."+ propName);
//		    		rdfRepo.addStatement(beanFieldRes, EJCore.springBeanFieldProp, RSECore.trueStatement);
//		    		
//		    		String propRef = beanPropToRefClassMap.get(propName);
//		    		if (propRef!=null) {
//		    			String beanRefClass = beanIdToClassMap.get(propRef);
//		    			if (beanRefClass!=null) {
//		    				Resource beanRefClassRes = classToRes(rdfRepo, beanRefClass);
//		    				rdfRepo.addStatement(beanFieldRes, EJCore.springBeanPropertyRef, beanRefClassRes);
//		    			}
//		    		}
//	    		}
	    		
	    		if (beanToConstructorArgs.keySet().contains(bean)) {
	    			List<String> constructorBeans = beanToConstructorArgs.get(bean);
	    			for (String paramBean : constructorBeans) {
	    				String paramBeanClass = beanIdToClassMap.get(paramBean);
	    				if (paramBeanClass == null) continue;
	    	    		Resource paramBeanClassRes = classToRes(rdfRepo, paramBeanClass);
	    	    		
	    	    		// Add factory method constructor arg references
	    	    		if (refBeanToFactoryMethodMap.keySet().contains(bean + "><" + paramBean)) {
	    	    			String classId = refBeanToFactoryMethodMap.get(bean + "><" + paramBean).split("::")[0];
	    		    		Resource factoryBeanClassRes = classToRes(rdfRepo, classId);
	    	    			String factoryMethodId = refBeanToFactoryMethodMap.get(bean + "><" + paramBean).split("::")[1];
	    	    			StatementIterator children = rdfRepo.getStatements(factoryBeanClassRes, RSECore.contains, null);

	    	    			while (children.hasNext()) {
	    	    				Statement child = children.next();
	    	    				if (child.getObject().toString().contains(factoryMethodId+"(")) {
	    	    					Resource factoryMethodRes  = (Resource) child.getObject();
	    	    					
	    	    					rdfRepo.addStatement(paramBeanClassRes, EJCore.springBeanPropertyRef, factoryMethodRes);
	    	    					break;
	    	    				}
	    	    			}
	    	    		}
	    			}
	    		}
			}
    		
    		// Process Properties
    		for (String propId : propIDToRefBeanMap.keySet()) {
    			if (!propId.contains("::")) continue;
    			
    			String beanRef = propIDToRefBeanMap.get(propId);
    			String classBean = propId.split("::")[0];
    			String classId = beanIdToClassMap.get(classBean);
    			String propertyName = propId.split("::")[1];
	    		Resource beanClassRes = classToRes(rdfRepo, classId);
	    		Resource beanRefClassRes = classToRes(rdfRepo, beanIdToClassMap.get(beanRef));
	    		if (beanClassRes==null || beanRefClassRes==null) continue;
	    		
	    		Resource propertyRes = fieldToRes(rdfRepo, classId+"."+propertyName);
	    		StatementIterator children = rdfRepo.getStatements(beanClassRes, RSECore.contains, null);
	    		
	    		rdfRepo.addStatement(propertyRes, EJCore.springBeanFieldProp, RSECore.trueStatement);
    			while (children.hasNext()) {
    				Statement child = children.next();
    				if (child.getObject().toString().equals(propertyRes.toString())) {
    					rdfRepo.addStatement(beanRefClassRes, EJCore.springBeanPropertyRef, propertyRes);
    					break;
    				}
    			}
    		}

    		
    	} catch (Throwable t) {
			logger.error("Unexpected Exception", t);
    	} finally {
			rdfRepo.commitTransaction();
    	}
	}
	    
}
