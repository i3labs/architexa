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
 * Created on May 26, 2005
 *
 */
package com.architexa.store;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.RepositoryConfig;
import org.openrdf.sesame.config.SailConfig;
import org.openrdf.sesame.query.serql.SerqlEngine;
import org.openrdf.sesame.repository.local.LocalRepository;
import org.openrdf.sesame.sail.RdfRepository;
import org.openrdf.sesame.sail.SailChangedListener;
import org.openrdf.sesame.sail.SailUpdateException;
import org.openrdf.sesame.sail.StatementIterator;
import org.openrdf.sesame.sailimpl.nativerdf.NativeRdfRepository;
import org.openrdf.sesame.sailimpl.nativerdf.NativeRdfRepositoryConfig;
import org.openrdf.vocabulary.RDF;
import org.openrdf.vocabulary.RDFS;

/**
 * @author vineet
 *
 * Basically a utility class
 */
public class ReloRdfRepository extends RSERepo {
    static final Logger logger = ReloStorePlugin.getLogger(ReloRdfRepository.class);

    protected RdfRepository sailRepo;
    
    public static IPluggableNameGuesser nameGuesser = null;
    
    public static final URI createReloURI(String str) {
    	return StoreUtil.createMemURI(atxaRdfNamespace + str);
    }

    public URI rdfType, rdfSubject, rdfObject, rdfList, rdfFirst, rdfRest, rdfNil;

    // true for debugging
	private boolean showAllErrors = false;

	private boolean tooManyErrors;

    // not used as often so create in memory
    public static final URI rdfsLabel = StoreUtil.createMemURI(RDFS.LABEL); 
    
    // mem repo
    public ReloRdfRepository() {
    	super();
    	this.sailRepo = new org.openrdf.sesame.sailimpl.memory.RdfRepository();
    	initRepo();
    }
    public ReloRdfRepository(ReloRdfRepository rrr) {
    	super(rrr.getPath());
    	this.sailRepo = rrr.sailRepo;
    	initRepo();
    }
    
    private void initRepo() {
        rdfType = createURI(RDF.TYPE);
        rdfSubject = createURI(RDF.SUBJECT);
        rdfObject = createURI(RDF.OBJECT);
        rdfList = createURI(RDF.LIST);
        rdfFirst = createURI(RDF.FIRST);
        rdfRest = createURI(RDF.REST);
        rdfNil = createURI(RDF.NIL);
        tooManyErrors = false;
    }
    
    private static final String defaultRepositoryId = "ReloStore";
    public static final String defaultDBName = "rdfdb";
    
    protected LocalRepository localRepository = null;

    // file repo - null for path if to use the workspace path
	public ReloRdfRepository(IPath _path) {
		super(_path);

        // we should support multiple repositories, and shut them down together
        //logger.info("Returning Native Repository as DefaultRepository");
		this.localRepository = setupFileSystemRepository(getLocation(), defaultRepositoryId, defaultDBName);
        this.sailRepo = (RdfRepository) localRepository.getSail();
    	initRepo();
	}
    private static LocalRepository setupFileSystemRepository(IPath workspacePath, String repositoryId, String dbName)  {
        RepositoryConfig config = new RepositoryConfig(repositoryId);

        SailConfig nativeRepoConfig = new NativeRdfRepositoryConfig();
        nativeRepoConfig.setParameter(NativeRdfRepository.DATA_DIR_KEY, workspacePath.append(dbName).toString());
        nativeRepoConfig.setParameter(NativeRdfRepository.TRIPLES_INDEXES_KEY, "spo, ops");
		config.addSail(nativeRepoConfig);

		config.setWorldReadable(true);
		config.setWorldWriteable(true);

		try {
			return Sesame.getService().createRepository(config);
		} catch (ConfigurationException e) {
			logger.error("Unexpeceted error", e);
			return null;
		}
    }

    private static Map<IPath, ReloRdfRepository> activeRepo = new HashMap<IPath, ReloRdfRepository>();

    public synchronized static ReloRdfRepository getRepo(IPath _path) {
		if (_path == null) _path = Path.EMPTY;
		if (!activeRepo.containsKey(_path)) {
			activeRepo.put(_path, new ReloRdfRepository(_path));
		}
		return activeRepo.get(_path);
	}
    public static ReloRdfRepository returnDefaultRepo() {
		return activeRepo.get(Path.EMPTY);
    }
    public static void clearDefaultRepo() {
		activeRepo.remove(Path.EMPTY);
    }
	public static void shutdownRepositories() {
		for (ReloRdfRepository repo : activeRepo.values()) {
			repo.shutdown();
		}
		activeRepo.clear();
	}
    
    @Override
    public void setRepo(RSERepo rrr) {
    	if (!(rrr instanceof ReloRdfRepository)) throw new IllegalArgumentException();
   		this.sailRepo = ((ReloRdfRepository)rrr).sailRepo;
    	super.setRepo(rrr);
    }
    
    public void startTransaction() {
    	if (sailRepo.transactionStarted()) {
    		logger.error("Transcation started twice", new Exception());
    		sailRepo.commitTransaction();
    	}
    	// XXX sometimes this method is called when the transaction has already been started - TO FIX
        sailRepo.startTransaction();
    }
    public void commitTransaction() {
        sailRepo.commitTransaction();
    }
    public SerqlEngine getSerqlEngine() {
        return new SerqlEngine(sailRepo);
    }

    public void registerListener(SailChangedListener listener) {
    	sailRepo.addListener(listener);
	}
    public void removeListener(SailChangedListener listener) {
    	sailRepo.removeListener(listener);
    }
    
    public final static String atxaRdfNamespace = "http://www.architexa.com/rdf/";
    
    public URI getDefaultURI(String str) {
        return sailRepo.getValueFactory().createURI(atxaRdfNamespace + str);
    }
    public URI getDefaultURI(String namespaceExt, String str) {
        return getURI(atxaRdfNamespace + namespaceExt, str);
    }
    public URI getURI(String namespace, String str) {
        return createURI(namespace, str);
    }
    public Literal getLiteral(String str) {
        return sailRepo.getValueFactory().createLiteral(str);
    }
    public BNode createBNode() {
        return sailRepo.getValueFactory().createBNode();
    }
    public BNode createBNode(String nodeId) {
        return sailRepo.getValueFactory().createBNode(nodeId);
    }
    public URI createURI(String namespace, String str) {
        return sailRepo.getValueFactory().createURI(namespace, str);
    }
    public URI createURI(String str) {
        return sailRepo.getValueFactory().createURI(str);
    }
    public Statement createStmt(final Resource subj, final URI pred, final Value obj) {
        return new Statement() {
            private static final long serialVersionUID = 1L;
			public Resource getSubject() {
                return subj;
            }
			public URI getPredicate() {
                return pred;
            }
			public Value getObject() {
                return obj;
            }
			public int compareTo(Object o) {
                return 0;
            }};
    }

    public boolean hasStatement(Resource subj, URI pred, Value obj) {
        return sailRepo.hasStatement(subj, pred, obj);
    }
    public final boolean hasStatement(Resource subj, URI pred, boolean obj) {
    	return hasStatement(subj, pred, getLiteral(Boolean.toString(obj)));
    }
    public final boolean delayTagged_hasStatement(Resource subj, URI pred, Value obj) {
        long start = System.currentTimeMillis();
        boolean retVal = sailRepo.hasStatement(subj, pred, obj);
        long delay = System.currentTimeMillis() - start;
        logger.info("hasDelay: " + delay + ";" + subj + ";" + pred + ";" + obj);
        return retVal;
    }
    public final boolean contains(Resource subj, URI pred, Value obj) {
        return hasStatement(subj, pred, obj);
    }
    public final boolean contains(Resource subj, URI pred, String obj) {
        return hasStatement(subj, pred, getLiteral(obj));
    }
    public final boolean contains(Resource subj, URI pred, boolean obj) {
        return hasStatement(subj, pred, getLiteral(Boolean.toString(obj)));
    }
    public final boolean contains(Resource subj, URI pred, int obj) {
        return hasStatement(subj, pred, getLiteral(Integer.toString(obj)));
    }
    
    
    public void addStatement(Resource subj, URI pred, Value obj) {
		//if (subj.toString().indexOf("rdf/jdt-wkspc#simplePckg$Main$Child") != -1) {
		//	System.err.println(">> Adding: " + subj + " -->" + pred + "--> " + obj);
		//}
        try {
            sailRepo.addStatement(subj, pred, obj);
        } catch (SailUpdateException e) {
            logger.error("Unexpected exception", e);
        }
    }
	//private String endObj(Object inObj) {
	//	if (inObj == null)
	//		return endStr("(null)");
	//	return endStr(inObj.toString());
	//}
	//private String endStr(String inStr) {
	//	int len = 30;
	//	String spaces = "                              "; // needs to be longer than len
	//	int start = inStr.length() - len;
	//	if (start < 0)
	//		return spaces.substring(0, len - inStr.length()) + inStr;
	//	return inStr.substring(start);
	//}
    
    //A reverse to addStatements with Statement subj parameter
    public void removeStatements(Statement subj, URI pred, Value obj){
    	try {
            sailRepo.removeStatements(statementToRes(subj), pred, obj);
        } catch (SailUpdateException e) {
            logger.error("Unexpected exception", e);
        }
    }
    
    public void removeStatements(Resource subj, URI pred, Value obj) {
		//if (subj.toString().indexOf("rdf/jdt-wkspc#simplePckg$Main$Child") != -1) {
		//	// children are being deleted.... when they where updated first
		//	System.err.println(">> Removing: " + subj + " -->" + pred + "--> " + obj);
		//}
        try {
            sailRepo.removeStatements(subj, pred, obj);
        } catch (SailUpdateException e) {
            logger.error("Unexpected exception", e);
        }
    }
    public void addStatement(Resource subj, URI pred, String obj) {
        addStatement(subj, pred, getLiteral(obj));
    }
    public void addStatement(Resource subj, URI pred, boolean obj) {
        addStatement(subj, pred, getLiteral(Boolean.toString(obj)));
    }

    public StatementIterator getStatements(Resource subj, URI pred, Value obj) {
        return sailRepo.getStatements(subj, pred, obj);
    }
    public StatementIterator delayTagged_getStatements(Resource subj, URI pred, Value obj) {
        return new WrappedStatementIterator(subj, pred, obj);
    }
    
    public void dumpStatements(Resource subj, URI pred, Value obj) {
        StatementIterator si = sailRepo.getStatements(subj, pred, obj);
		logger.info("Statement dump begin");
		while (si.hasNext()) {
			Statement s = si.next();
			logger.info(s.getSubject() + " --[[" + s.getPredicate() + "]]--> " + s.getObject());
		}
		logger.info("Statement dump end");
		si.close();
    }
    public void dumpStatementsWithType(Resource subj) {
        StatementIterator si = sailRepo.getStatements(subj, null, null);
		logger.info("Statement dump begin");
		while (si.hasNext()) {
			Statement s = si.next();
			String logStmt = s.getSubject() + " --[[" + s.getPredicate() + "]]--> " + s.getObject();
			if (s.getObject() instanceof Resource) {
				logStmt += " {";
		        StatementIterator si2 = sailRepo.getStatements((Resource)s.getObject(), rdfType, null);
		        while (si2.hasNext()) {
		        	logStmt += si2.next().getObject() + ", ";
		        }
				logStmt += "}";
			}
			logger.info(logStmt);
		}
		logger.info("Statement dump end");
		si.close();
    }
    
    private class WrappedStatementIterator implements StatementIterator {
        private final StatementIterator intIt;
        long delay = 0;
        private final Resource subj;
        private final URI pred;
        private final Value obj;
        WrappedStatementIterator(Resource subj, URI pred, Value obj) {
            this.subj = subj;
            this.pred = pred;
            this.obj = obj;
            long start = System.currentTimeMillis();
            this.intIt = sailRepo.getStatements(subj, pred, obj);
            delay += System.currentTimeMillis() - start;
        }
		public void close() {
            intIt.close();
            logger.info("getDelay: " + delay + ";" + subj + ";" + pred + ";" + obj);
        }
		public boolean hasNext() {
            return intIt.hasNext();
        }
		public Statement next() {
            long start = System.currentTimeMillis();
            Statement retVal = intIt.next();
            delay += System.currentTimeMillis() - start;
            return retVal;
        }
    };

    /**
     * @param input
     * @param s
     * @param p
     * @param o
     */
    public void getResourcesFor(Collection<Resource> input, Resource s, URI p, Value o) {
        StatementIterator si = getStatements(s, p, o);
        while (si.hasNext()) {
            Statement stmt = si.next();
            if (s == null)
                input.add(stmt.getSubject());
            else if (p == null)
                input.add(stmt.getPredicate());
            else if (o == null)
                input.add((Resource) stmt.getObject());
        }
        si.close();
    }

    
    /**
     * @param elementRes
     * @param name
     * @return
     */
    public String getRequiredLiteral(Resource subj, URI pred) {
        return getRequiredProperty(subj, pred).getObject().toString();
    }
    public Statement getRequiredProperty(Resource subj, URI pred) {
        Statement retVal = getStatement(subj, pred, null);
        if (retVal == nullStatement && !tooManyErrors) {
            logger.error(getNullError(subj, pred), new Exception());
            tooManyErrors=!showAllErrors;
        }
        return retVal;
    }
    public Statement getWarnedRequiredProperty(Resource subj, URI pred) {
        Statement retVal = getStatement(subj, pred, null);
        if (retVal == nullStatement) {
            logger.warn(getNullError(subj, pred), new Exception());
        }
        return retVal;
    }
    private StringBuffer getNullError(Resource subj, URI pred) {
        StringBuffer sb = new StringBuffer();
        sb.append("Did not find statement: ").append(subj).append(" // ").append(pred).append("\n");

        sb.append("Statements found: ").append(subj).append(" ...\n");
        StatementIterator stmts = this.getStatements(subj, null, null);
        while (stmts.hasNext()) {
        	Statement s = stmts.next();
            sb.append("\t").append(s.getPredicate());
            sb.append(" ").append(s.getObject()).append("\n");
        }
        stmts.close();
		return sb;
	}
	public Value getRequiredObj(Resource subj, URI pred) {
    	return getRequiredProperty(subj, pred).getObject();
    }
    /**
     * @param elementRes
     * @param type
     * @return
     */
    @Deprecated
    public Statement getRequiredProperty(Resource subj, String pred) {
        return getRequiredProperty(subj, sailRepo.getValueFactory().createURI(pred));
    }

    public Statement getStatement(Statement subj, URI pred, Value obj) {
        return getStatement(statementToRes(subj), pred, obj);
    }
    public void addStatement(Statement subj, URI pred, Value obj) {
        addStatement(statementToRes(subj), pred, obj);
    }
    public void addStatement(Statement subj, URI pred, String obj) {
        addStatement(statementToRes(subj), pred, getLiteral(obj));
    }
    
    // implementation from haystack
    private static String byteChars = "0123456789abcdef";

    private Resource statementToRes(Statement stmt) {
        try {
            String subj = null;
            if (stmt.getSubject() instanceof URI)
                subj = ((URI)stmt.getSubject()).getURI();
            else
                subj = stmt.getSubject().toString();
            String pred = stmt.getPredicate().getURI();
            String objc = stmt.getObject().toString();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.write(subj.length());
            dos.write(pred.length());
            dos.write(objc.length());
            dos.writeBoolean(stmt.getObject() instanceof Literal);
            dos.writeChars(subj);
            dos.writeChars(pred);
            dos.writeChars(objc);
            
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(baos.toByteArray());
            StringBuffer sb = new StringBuffer("urn:statement:md5:");
            for (int i = 0; i < bytes.length; i++) {
                int loNibble = bytes[i] & 0xf;
                int hiNibble = (bytes[i] >> 4) & 0xf;
                sb.append(byteChars.charAt(hiNibble));
                sb.append(byteChars.charAt(loNibble));
            }
            return this.createURI(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }       
    }
    
    public static final Statement nullStatement = new Statement() {
        private static final long serialVersionUID = -1068300891900789720L;
		public Resource getSubject() {
            return null;
        }
		public URI getPredicate() {
            return null;
        }
		public Value getObject() {
            return null;
        }
		public int compareTo(Object arg0) {
            return 0;
        }};
        
    public Statement getStatement(Resource subj, URI pred, Value obj) {
        StatementIterator si = getStatements(subj, pred, obj);
        if (si.hasNext()) {
            Statement retVal = si.next();
			//if (si.hasNext()) {
			//	logger.warn("Multiple statements found returning first statement");
			//	logger.warn("St1: " + retVal);
			//	logger.warn("St2: " + si.next());
			//	logger.warn(">2: " + si.hasNext());
			//}
            si.close();
            return retVal;
        }
        return nullStatement; 
    }

    class RDFListIterator implements Iterator<Value> {
        private Resource listRes;
        public RDFListIterator(Resource listRes) {
            this.listRes = listRes;
        }
		public boolean hasNext() {
            return !listRes.equals(rdfNil);
        }
		public Value next() {
            Value nextVal = getStatement(listRes, rdfFirst, null).getObject();
            listRes = (Resource) getStatement(listRes, rdfRest, null).getObject();
            return nextVal;
        }
		public void remove() {
            throw new UnsupportedOperationException();
        }
    };

    public Resource createList(Iterator<? extends Value> iterator) {
        if (iterator.hasNext()) {
            Resource listRes = createBNode();
            addTypeStatement(listRes, rdfList);
            addStatement(listRes, rdfFirst, iterator.next());
            addStatement(listRes, rdfRest, createList(iterator));
            return listRes;
        } else {
            return rdfNil;
        }
    }
	public Value createList(List<? extends Value> list) {
		return createList(list.iterator());
	}

    public Iterator<Value> getListIterator(Resource listRes) {
        return new RDFListIterator(listRes);
    }
	public void addTypeStatement(Resource subj, URI obj) {
		// we are no longer adding type statements except for interfaces
		// interfaces call regular addStatement()
		// addStatement(subj, rdfType, obj);
	}
	
	/*
     * Needed for Javadoc support
     */
	public void addProjectTypeStatement(Resource subj, URI obj) {
		 addStatement(subj, rdfType, obj);
	}
	
	public void addNameStatement(Resource subj, URI pred, String obj) {
		// we are no longer adding Name statements
		// addStatement(subj, pred, obj);
	}
	public String queryName(Resource res) {
		if (nameGuesser != null) {
			String guessedName = nameGuesser.getName(res, this);
			if (guessedName!= null)	return guessedName;
		}
		return "";
	}
	public void addInitializedStatement(Resource subj, URI pred, Object obj) {
		// we are no longer adding initialized statements except for packages and indirectPackages
		// packages use regular addStatement()
		// addStatement(subj, pred, (Value) obj);
	}
	public boolean transactionStarted() {
		return sailRepo.transactionStarted();
	}

	public void shutdown() {
    	if (localRepository != null) localRepository.shutDown();
	}

}
