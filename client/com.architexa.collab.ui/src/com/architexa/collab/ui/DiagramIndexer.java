package com.architexa.collab.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.core.runtime.IPath;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.Parser;
import org.openrdf.rio.StatementHandler;
import org.openrdf.rio.StatementHandlerException;

import com.architexa.diagrams.RSECore;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class DiagramIndexer {

	static final Logger logger = Activator.getLogger(DiagramIndexer.class);

	public static String diagramId = "diagramId";
	public static String diagramName = "diagramName";
	public static String diagramGroupId = "groupId";
	public static String diagramGroupName = "groupName";
	public static String diagramCreatedAt = "createdAt";
	public static String diagramCreatorId = "creatorId";
	public static String diagramType = "type";
	public static String diagramDescription = "description";
	public static String diagramTags = "tags";
	
	public static String modelField = "model";

	private static String indexName = "LuceneCollabIndex";
	private static IndexWriter indexWriter;

	static {
		try {
			File indexFile = getIndexFile();
			handleLock(indexFile);
			boolean create = !indexFile.exists();
			createIndexWriter(create);
		} catch (Throwable t) {
			logger.error("Unexpected exception initializing diagram indexer.", t);
		}
	}

	public static void createNewIndex() {
		createIndexWriter(true);
	}

	private static void createIndexWriter(boolean create) {
		File indexFile = getIndexFile();
		handleLock(indexFile);
		try {
			indexWriter = new IndexWriter(indexFile, new StandardAnalyzer(), create);
		} catch (IOException e) {
			logger.error("Directory "+indexFile+" cannot be read/written to or does not exist.", e);
		}
	}

	public static File getIndexFile() {
		try {
			IPath indexDirectory = Activator.getDefault().getStateLocation().addTrailingSeparator().append(indexName);
			return indexDirectory.toFile();
		} catch (NullPointerException npe) {
			System.err.println("Unexpected NPE");
		}
		return null;
	}

	private static void checkIndex() {
		File indexFile = getIndexFile();
		if(!indexFile.exists())
			createNewIndex();
	}
	
	private static void handleLock(File indexFile) {
		String indexLocation = indexFile.getPath();
		try {
			if (indexFile.exists() && IndexReader.isLocked(indexLocation)) {
				// Forcefully release lock
				IndexReader.unlock(FSDirectory.getDirectory(indexFile, false));
			}
		}
		catch (IOException e) {
			logger.error("Unexpected exception while testing for lock " +
					"or releasing lock on index " + indexLocation, e);
		}
	}
	
	private static boolean diagramAlreadyIndexed(JSONObject diagram) {
		String idOfDiagramToBeIndexed = diagram.getString("key");
		try {
			QueryParser parser = new QueryParser(DiagramIndexer.diagramId, new StandardAnalyzer());
			Query query = parser.parse(idOfDiagramToBeIndexed);

			IndexSearcher indexSearcher = new IndexSearcher(FSDirectory.getDirectory(getIndexFile(), false));
			Hits hits = indexSearcher.search(query);
			indexSearcher.close();

			if(hits.length()>0) return true; // found a diagram with given diagram's id already in index
		} catch (IOException e) {
			logger.error("Unexpected exception while determining whether " +
					"diagram "+idOfDiagramToBeIndexed+" already indexed.", e);
		} catch (ParseException e) {
			logger.error("Unexpected exception while determining whether " +
					"diagram "+idOfDiagramToBeIndexed+" already indexed.", e);
		}
		return false;
	}

	private static ReloRdfRepository getParsedDiagramBody(String diagramBody, final List<Resource> models) {

		BufferedReader in = new BufferedReader(new StringReader(diagramBody));
		final ReloRdfRepository bodyRepo = StoreUtil.getMemRepository();
		Parser parser = StoreUtil.getRDFParser(bodyRepo);
		parser.setStatementHandler(new StatementHandler() {
			public void handleStatement(Resource subj, URI pred, Value obj) throws StatementHandlerException {
				bodyRepo.addStatement(subj, pred, obj);
				if(RSECore.model.equals(pred) && 
						(obj instanceof Resource) && !models.contains(obj)) {
					models.add((Resource)obj);
				}
			}});

		bodyRepo.startTransaction();
		try {
			parser.parse(in, ReloRdfRepository.atxaRdfNamespace);
		} catch (Exception e) {
			logger.error("Exception while parsing.", e);
		}
		bodyRepo.commitTransaction();

		return bodyRepo;
	}

	private static Field createField(String name, String value) {
		return new Field(name, value, Field.Store.YES, Field.Index.TOKENIZED);
	}

	public static void removeDocumentsOfDiagramsInGroup(String groupId) {
		try {
			removeDocument(DiagramIndexer.diagramGroupId, groupId);
		} catch(IOException e) {
			logger.error("Exception while removing documents corresponding to diagrams in group " + groupId, e);
		} catch (ParseException e) {
			logger.error("Exception while searching for documents in group " + groupId, e);
		}
	}

	public static void removeDiagramDocument(String idOfDiagram) {
		try {
			removeDocument(DiagramIndexer.diagramId, idOfDiagram);
		} catch(IOException e) {
			logger.error("Exception while removing document corresponding to diagram " + idOfDiagram, e);
		} catch (ParseException e) {
			logger.error("Exception while searching for document " + idOfDiagram, e);
		}
	}
	
	public static void removeGroupDocument(String idOfGroup) {
		try {
			checkIndex();
			File indexFile = getIndexFile();
			handleLock(indexFile);

			IndexSearcher indexSearcher = new IndexSearcher(FSDirectory.getDirectory(indexFile, false));
			IndexReader indexReader = indexSearcher.getIndexReader();

			Query query = new WildcardQuery(new Term(idOfGroup, "*"));
			Hits hits = indexSearcher.search(query);
			for(int i=0; i<hits.length(); i++) {
				int luceneDocId = hits.id(i);
				indexReader.deleteDocument(luceneDocId);
			}

			indexReader.close();
			indexWriter.optimize();
			indexWriter.close();
		} catch(IOException e) {
			logger.error("Exception while removing document corresponding to diagram " + idOfGroup, e);
		} 
	}

	// Deletes all documents containing a field with the given name and value
	private static void removeDocument(String fieldName, String fieldValue) throws IOException, ParseException {
		try {
			checkIndex();
			File indexFile = getIndexFile();
			handleLock(indexFile);
	
			IndexSearcher indexSearcher = new IndexSearcher(FSDirectory.getDirectory(indexFile, false));
			IndexReader indexReader = indexSearcher.getIndexReader();
	
			QueryParser parser = new QueryParser(fieldName, new StandardAnalyzer());
			Query query = parser.parse(fieldValue);
	
			Hits hits = indexSearcher.search(query);
			for(int i=0; i<hits.length(); i++) {
				Document doc = hits.doc(i);
				if(!fieldValue.equals(doc.get(fieldName))) continue;
	
				int luceneDocId = hits.id(i);
				indexReader.deleteDocument(luceneDocId);
			}
			
			indexReader.close();
			indexWriter.optimize();
			indexWriter.close();
		} catch (FileNotFoundException e) {
			logger.info("Lucene could not find an index file so we could not delete. Try deleting and recreating the lucene index", e);
		}
	}

	/**
	 * Searches the index for all diagrams belonging to the group that has
	 * the given groupId. Returns a list of the "key" ids of those diagrams
	 * @return a list of the IDs of the diagrams whose group id is groupId.
	 */
	public static List<String> getIdsOfAllIndexedDiagramsInGroup(String groupId) {
		List<String> diagramIds = new ArrayList<String>();

		try {
			checkIndex();
			File indexFile = getIndexFile();
			handleLock(indexFile);

			IndexSearcher indexSearcher = new IndexSearcher(FSDirectory.getDirectory(indexFile, false));

			QueryParser parser = new QueryParser(DiagramIndexer.diagramGroupId, new StandardAnalyzer());
			Query query = parser.parse(groupId);

			Hits hits = indexSearcher.search(query);
			Iterator<?> hitsIterator = hits.iterator();
			while(hitsIterator.hasNext()) {
				Hit hit = (Hit) hitsIterator.next();
				try {
					String indexedDiagramId = hit.get(DiagramIndexer.diagramId);
					if(indexedDiagramId==null) continue;
					if(!diagramIds.contains(indexedDiagramId))
						diagramIds.add(indexedDiagramId);
				} catch (IOException e) {
					logger.error("Unexpected exception while querying groups and their hosts.", e);
				}
			}
			indexSearcher.close();

		} catch(Exception e) {
			logger.error("Unexpected exception while getting all " +
					"indexed diagrams in group " + groupId, e);
		}
		return diagramIds;
	}

	public static String getIndexedGroupMD5(String groupId, String groupMD5 ) throws IOException, ParseException {
		checkIndex();
		File indexFile = getIndexFile();
		handleLock(indexFile);

		IndexSearcher indexSearcher = new IndexSearcher(FSDirectory.getDirectory(indexFile, false));
		IndexReader indexReader = indexSearcher.getIndexReader();

		Query query = new WildcardQuery(new Term(groupId, "*"));
		Hits hits = indexSearcher.search(query);
		for(int i=0; i<hits.length(); i++) {
			Document doc = hits.doc(i);
			if(!groupMD5.equals(doc.get(groupId))) continue;

			return doc.get(groupId);
		}
		indexReader.close();
		return null;
	}

	public static void indexGroup(String groupId, String groupMD5) {
		if(indexWriter==null) return;

		Field groupIdField = new Field(groupId, groupMD5, Field.Store.YES, Field.Index.TOKENIZED);

		Document groupDoc = new Document();
		groupDoc.add(groupIdField);

		try {
			indexWriter.addDocument(groupDoc);
			indexWriter.optimize();
			indexWriter.close();
		} catch (IOException e) {
			logger.error("Exception adding document.", e);
		}
	}

}
