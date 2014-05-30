package com.architexa.extensions.entJava;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.asm.AsmPackageSupport;
import com.architexa.store.ReloRdfRepository;

public class TilesExtensionBuilder {
	static final Logger logger = Activator.getLogger(TilesExtensionBuilder.class);

	private static final String TILES_PATH = "org.apache.struts.tiles.TilesPlugin";


	public static IFile getTilesConfigFile(Document configXMLDoc, IWorkspaceRoot wkspcRoot, String warRootPath) {
		NodeList plugins = null;
		try {
			plugins = EJCore.xPathQueryEval("//plug-in", configXMLDoc);
		} catch (XPathExpressionException e) {
			logger.error("Unexpected Exception", e);
		}
		String tilesPath = getTilesPath(plugins);
		if (tilesPath == null) return null;
		return (IFile) wkspcRoot.findMember(warRootPath + tilesPath);
	}

	
	static void getTilesDefinitionsAndTargets(IFile tilesConfigRes, String strutsModuleName, ReloRdfRepository rdfRepo, Resource projectRes) throws SAXException, IOException, ParserConfigurationException, CoreException, XPathExpressionException {
		Document tilesConfigXMLDoc = EJCore.getXMLDoc(tilesConfigRes.getContents());
		NodeList definitions =  EJCore.xPathQueryEval("//definition", tilesConfigXMLDoc);
		
		for (int i=0; i<definitions.getLength(); i++) {
			NamedNodeMap defAttribs = definitions.item(i).getAttributes();
			String defName= strutsModuleName + "/" + EJCore.getNodeText(defAttribs, "name");
			
			String defExtends = EJCore.getNodeText(defAttribs, "extends");
			String defPath = EJCore.getNodeText(defAttribs, "path");
			
			if (!defExtends.equals("")) {
				// add connection  defName -> defExtends
				defExtends = strutsModuleName + "/" + defExtends;
				
				addTilesStatement(defExtends, defName, rdfRepo, projectRes);
			} 
			else if (!defPath.equals("")){
				// add connection  defName -> defPath
				addTilesStatement(defPath, defName, rdfRepo, projectRes);
			} 
			Element putNodes = (Element)definitions.item(i);
			NodeList putTags=  putNodes.getElementsByTagName("put");
			for (int k=0; k<putTags.getLength(); k++) {
				NamedNodeMap putAttribs = putTags.item(k).getAttributes();
				String value = EJCore.getNodeText(putAttribs, "value");
				if (value.lastIndexOf(".") == value.length()-4 && value.lastIndexOf(".")!=-1) {
					// add connection  defName -> value
					addTilesStatement(value, defName, rdfRepo, projectRes);
				}
			}
		}
	}
	
	private static void addTilesStatement(String target, String defName, ReloRdfRepository rdfRepo, Resource projectRes) {
		String defID= defName.replace(".", "");
		if (defID.equals("")) return;
		defID = EJCore.getId(defID.replaceAll("/", "."));
		defID = EJCore.WEBROOT + ".tiles" + defID;
		Resource defRes  = RSECore.idToResource(rdfRepo, EJCore.jdtExtWkspcNS, defID);

		// target can be tilesDef or a file
		URI type = EJCore.tilesDefType;
		URI conType = EJCore.tilesReference;
		if (target.lastIndexOf(".") == target.length()-4 && target.lastIndexOf(".")!=-1) {
			type = EJCore.webPathType;
			target = EJCore.WEBROOT + target;
			target = EJCore.webFileToID(target);
		} else {
			target = EJCore.WEBROOT + target;
			conType = EJCore.tilesExtend;
			target = target.replace(".", "");
			target = target.replace("/", ".");
			target = EJCore.getId(target);
		}
		Resource packageRes = AsmPackageSupport.getPackage(rdfRepo, AsmPackageSupport.getPckgNameFromClassID(target), projectRes);
		Resource targetRes  = RSECore.idToResource(rdfRepo, EJCore.jdtExtWkspcNS, target);
		String targetName = "/" + target.substring(target.indexOf("$")+1) ;
		rdfRepo.addStatement(targetRes, rdfRepo.rdfType, type);
		rdfRepo.addStatement(targetRes, RSECore.name, targetName);
		rdfRepo.addStatement(targetRes, RJCore.access, RJCore.publicAccess);

		rdfRepo.addStatement(defRes, conType, targetRes );
		rdfRepo.addStatement(defRes, rdfRepo.rdfType, EJCore.tilesDefType);
		rdfRepo.addStatement(defRes, RSECore.name, defID.substring(defID.indexOf("$")+1));
		rdfRepo.addStatement(defRes, RJCore.access, RJCore.publicAccess);
		rdfRepo.addStatement(packageRes, RSECore.contains, defRes);
		
		rdfRepo.addStatement(packageRes, RSECore.contains, targetRes);
		rdfRepo.addStatement(packageRes, EJCore.webFolder, RSECore.trueStatement);
	}
	
	
	 private static String getTilesPath(NodeList plugins) {
			for (int i=0; i<plugins.getLength(); i++) {
				NamedNodeMap pluginAttribs = plugins.item(i).getAttributes();
				
				if (!EJCore.getNodeText(pluginAttribs, "className").equals(TILES_PATH)) continue;
				Element tilesNode = (Element)plugins.item(i);
				NodeList tilesProps =  tilesNode.getElementsByTagName("set-property");
				for (int j=0; j<tilesProps.getLength(); j++) {
					NamedNodeMap tilesAttribs = tilesProps.item(j).getAttributes();
					String property = EJCore.getNodeText(tilesAttribs, "property");
					if (!property.equals("definitions-config")) continue;
					
					return EJCore.getNodeText(tilesAttribs, "value");
				}
			}	
			return null;
		}
}
