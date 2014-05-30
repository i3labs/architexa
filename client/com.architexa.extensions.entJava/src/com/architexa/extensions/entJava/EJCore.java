package com.architexa.extensions.entJava;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.eclipse.ui.IStartup;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.relo.jdt.parts.OverridesRelationPart;
import com.architexa.diagrams.relo.jdt.parts.PackageEditPart;
import com.architexa.diagrams.relo.jdt.parts.SimpleCallRelationPart;
import com.architexa.diagrams.services.PluggableNameGuesser;
import com.architexa.diagrams.services.PluggableNameGuesser.NameGuesser;
import com.architexa.diagrams.services.PluggableTypeGuesser;
import com.architexa.diagrams.services.PluggableTypeGuesser.TypeGuesser;
import com.architexa.diagrams.services.PluggableTypes;
import com.architexa.diagrams.services.PluggableTypes.PluggableFilterInfo;
import com.architexa.diagrams.services.PluggableTypes.PluggableTypeInfo;
import com.architexa.extensions.entJava.spring.SpringBeanClassEditPart;
import com.architexa.extensions.entJava.spring.SpringBeanFieldEditPart;
import com.architexa.store.ReloRdfRepository;

public class EJCore implements IStartup {
	static final Logger logger = Activator.getLogger(EJCore.class);

    private static final URI createRseUri(String str) {
    	return RSECore.createRseUri(str);
    }

    // web path root
    public static String WEBROOT = "web-root";

	public static String jdtExtWkspcNS = "jdtext-wkspc#";

    // object types
	public static final URI webActionStateType = createRseUri("jdt-web#webActionPath");
	public static final URI webViewStateType = createRseUri("jdt-web#webViewStatePath");
	public static final URI webDecisionStateType = createRseUri("jdt-web#webDecisionPath");
	public static final URI webEndStateType = createRseUri("jdt-web#webEndStatePath");
	public static final URI webSubFlowStateType = createRseUri("jdt-web#webSubFlowStatePath");
	
    public static final URI webPathType = createRseUri("jdt-web#webPath");
    public static final URI tilesDefType = createRseUri("jdt-web#tilesDef");;

    public static final URI springBeanClassProp  = createRseUri("jdt-spring#beanClassProp ");
    public static final URI springBeanFieldProp  = createRseUri("jdt-spring#beanFieldProp ");
    
    // relations
	public static final URI implementedBy = createRseUri("jdt-web#implementedBy");
	public static final URI tilesCall = createRseUri("jdt-web#tilesCall");
	public static final URI transitionCall = createRseUri("jdt-web#transitionCall");
	public static final URI tilesExtend = createRseUri("jdt-web#tilesExtend");
	public static final URI tilesReference = createRseUri("jdt-web#tilesReference");
	public static final URI actionData = createRseUri("jdt-web#actionData");

	
	public static final URI springBeanPropertyRef = createRseUri("jdt-spring#beanPropertyRef");
	public static final URI webFolder = createRseUri("jdt-web#webFolder");
	
    // support for a containment cache
    public static final URI containmentBasedImplementedBy = createRseUri("jdt#containmentBasedImplementedBy");
    public static final URI containmentBasedActionData = createRseUri("jdt#containmentBasedActionData");
    public static final URI containmentBasedTilesCall = createRseUri("jdt#containmentBasedTilesCall");
    public static final URI containmentBasedTilesExtend = createRseUri("jdt#containmentBasedTilesExtend");


    public static int DECISION_STATE = 0;
    public static int ACTION_STATE = 1;
    public static int VIEW_STATE = 2;
    public static int SUBFLOW_STATE = 3;
    public static int END_STATE = 4;
     
	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	private void earlyStartupInternal() {
		PluggableTypes
    	.registerType(new PluggableFilterInfo(RJCore.packageType, "Web Path", ArtifactFragment.class, PackageEditPart.class, EJCore.webFolder, RSECore.trueStatement, new WebPathEditPart()))
    	.isGraphNode = true;
    
		PluggableTypes
        	.registerType(new PluggableTypeInfo(EJCore.webPathType, "Web Path", ArtifactFragment.class, WebPathEditPart.class, new WebPathEditPart()))
        	.isGraphNode = true;
        
		PluggableTypes
    	.registerType(new PluggableTypeInfo(EJCore.webDecisionStateType, "Decision State", ArtifactFragment.class, WebDecisionStateEditPart.class, new WebDecisionStateEditPart()))
    	.isGraphNode = true;
		
		PluggableTypes
    	.registerType(new PluggableTypeInfo(EJCore.webActionStateType, "Action State", ArtifactFragment.class, WebActionStateEditPart.class, new WebActionStateEditPart()))
    	.isGraphNode = true;
		
		PluggableTypes
    	.registerType(new PluggableTypeInfo(EJCore.webViewStateType, "View State", ArtifactFragment.class, WebVewStateEditPart.class, new WebVewStateEditPart()))
    	.isGraphNode = true;
		
		PluggableTypes
    	.registerType(new PluggableTypeInfo(EJCore.webSubFlowStateType, "SubFlow State", ArtifactFragment.class, WebSubFlowEditPart.class, new WebSubFlowEditPart()))
    	.isGraphNode = true;
		
		PluggableTypes
    	.registerType(new PluggableTypeInfo(EJCore.webEndStateType, "End State", ArtifactFragment.class, WebEndStateEditPart.class, new WebEndStateEditPart()))
    	.isGraphNode = true;
		
        PluggableTypes
    		.registerType(new PluggableTypeInfo(EJCore.tilesDefType, "Tiles Definition", ArtifactFragment.class, TilesDefEditPart.class, new TilesDefEditPart()))
    		.isGraphNode = true;
        PluggableTypes
    		.registerType(new PluggableFilterInfo(RJCore.classType, "Bean Class", ArtifactFragment.class, SpringBeanClassEditPart.class, EJCore.springBeanClassProp, RSECore.trueStatement, new SpringBeanClassEditPart()))
    		.isGraphNode = true;
        PluggableTypes
        	.registerType(new PluggableFilterInfo(RJCore.fieldType, "Bean Property Field", ArtifactFragment.class, SpringBeanFieldEditPart.class, EJCore.springBeanFieldProp, RSECore.trueStatement, new SpringBeanFieldEditPart()));
        
        PluggableTypes.registerType(new PluggableTypeInfo(implementedBy, "implemented by", ArtifactRel.class, SimpleCallRelationPart.class));
        PluggableTypes.registerType(new PluggableTypeInfo(actionData, "action data", ArtifactRel.class, SimpleCallRelationPart.class));
        PluggableTypes.registerType(new PluggableTypeInfo(tilesCall, "struts forward", ArtifactRel.class, SimpleCallRelationPart.class));
        PluggableTypes.registerType(new PluggableTypeInfo(transitionCall, "transition", ArtifactRel.class, SimpleCallRelationPart.class));
        PluggableTypes.registerType(new PluggableTypeInfo(tilesExtend, "extends", ArtifactRel.class, OverridesRelationPart.class));
        PluggableTypes.registerType(new PluggableTypeInfo(tilesReference, "tiles reference", ArtifactRel.class, SimpleCallRelationPart.class));
        PluggableTypes.registerType(new PluggableTypeInfo(springBeanPropertyRef, "spring bean reference", ArtifactRel.class, SimpleCallRelationPart.class));
        
        PluggableTypeGuesser.registerTypeGuesser(new TypeGuesser() {
			public Resource getType(Resource elementRes, ReloRdfRepository repo) {
				Statement stmt = repo.getStatement(elementRes, repo.rdfType, null);
				if (stmt!=ReloRdfRepository.nullStatement)
					return (Resource) stmt.getObject();
				return null;
			}
		});
		ReloRdfRepository.nameGuesser = new PluggableNameGuesser();
		PluggableNameGuesser.registerNameGuesser(new NameGuesser() {
			public String getName(Resource res, ReloRdfRepository repo) {
				Statement stmt = repo.getStatement(res, RSECore.name, null);
				if (stmt!=ReloRdfRepository.nullStatement)
					return stmt.getObject().toString(); 
				return null;
			}
		});
        
	}
	static String getNodeText(NamedNodeMap pluginAttribs, String string) {
		Node className = pluginAttribs.getNamedItem(string);
		if ( className == null) return "";
		return className.getTextContent();
		
	}
	public static String webFileToID(String webFilePath) {
		webFilePath = webFilePath.replace(".", "^");
		webFilePath = webFilePath.replace("/", ".");
		webFilePath = getId(webFilePath);	
		return webFilePath.replace("^", ".");
	}
	
	public static String getId(String fqn) {
		if (!fqn.contains(".")) return fqn;
		String beg = fqn.substring(0, fqn.lastIndexOf("."));
		String end = fqn.substring(fqn.lastIndexOf(".")+1);
		return beg + "$" + end;
	}
	public static Document getXMLDoc(InputStream xmlInpStream) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		db.setEntityResolver(new CachedEntityResolver());
		return db.parse(xmlInpStream);
	}
	public static NodeList xPathQueryEval(String xpathQuery, Document xmlDoc) throws XPathExpressionException {
		return (NodeList) XPathFactory.newInstance().newXPath().evaluate(xpathQuery, xmlDoc, XPathConstants.NODESET);
	}
	
	
	
}
