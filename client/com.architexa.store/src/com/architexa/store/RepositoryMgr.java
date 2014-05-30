package com.architexa.store;


/**
 * @author Vineet
 */

public class RepositoryMgr extends ReloRdfRepository {
	
	// typically the memRepo - stores whatever is in the file that was opened
	private ReloRdfRepository fileRepo;

	// a pointer to the base class - created for easier 
	private ReloRdfRepository storeRepo;
	
	public RepositoryMgr(ReloRdfRepository repository) {
		super(repository);
		storeRepo = repository;
	}

	public RepositoryMgr() {
		this(StoreUtil.getDefaultStoreRepository());
	}
	
	
	/**
	 * Instead of calling this method users should just create a new Repository Manager. We do this because  
	 */
	public void setStoreRepo(ReloRdfRepository storeRepo) {
		setRepo(storeRepo);
		this.storeRepo = storeRepo;
	}
	
	public ReloRdfRepository getStoreRepo() {
		return storeRepo;
	}
	
    // Cache repo allows for the diagram to be drawn the way it was saved
    
    public void setFileRepo(ReloRdfRepository cacheRepo) {
		this.fileRepo = cacheRepo;
	}
	
	public ReloRdfRepository getFileRepo() {
		return fileRepo;
	}	
	
//	@Override
//	public StatementIterator getStatements(Resource subj, URI pred, Value obj) {
//		//// WAS: would check both store and file repo
//		//ReloRdfRepository memRep = StoreUtil.getMemRepository();
//		//StatementIterator it1 = storeRepo.getStatements(subj, pred, obj);
//		//while (it1.hasNext()) {
//		//	Statement stmt = it1.next();
//		//	memRep.startTransaction();
//		//	memRep.addStatement(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
//		//	memRep.commitTransaction();
//		//}
//		//it1.close();
//		//if (fileRepo==null) return memRep.getStatements(subj, pred, obj);
//		//
//		//StatementIterator it2 = fileRepo.getStatements(subj, pred, obj);
//		//while (it2.hasNext()) {
//		//	Statement stmt = it2.next();
//		//	memRep.startTransaction();
//		//	memRep.addStatement(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
//		//	memRep.commitTransaction();
//		//}
//		//it2.close();
//		//return memRep.getStatements(subj, pred, obj);
//		return storeRepo.getStatements(subj, pred, obj);
//	}
	
//	@Override
//	public Statement getStatement(Resource subj, URI pred, Value obj) {
//		//// WAS: would check both store and file repo
//		//if (fileRepo != null) {
//		//	Statement stmt = fileRepo.getStatement(subj, pred, obj);
//		//	if (stmt != ReloRdfRepository.nullStatement)
//		//		return stmt;
//		//}
//		return storeRepo.getStatement(subj, pred, obj);
//	}
	
//	@Override
//	public boolean hasStatement(Resource subj, URI pred, Value obj) {
//		//// WAS: would check both store and file repo
//		//if(storeRepo.hasStatement(subj, pred, obj)) return true;
//		//if(fileRepo!=null && fileRepo.hasStatement(subj, pred, obj)) return true;
//		//return false;
//		return storeRepo.hasStatement(subj, pred, obj);
//	}
	
}
