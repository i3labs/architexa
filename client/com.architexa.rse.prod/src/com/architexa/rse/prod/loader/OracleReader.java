package com.architexa.rse.prod.loader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.architexa.rse.prod.loader.ToolRunner.ShellStream;

import oracle.jdbc.pool.OracleDataSource;

/**
 * USER_* views only contain information that USER owns.
 * ALL_* views contain information that USER has been GRANTed access
 * DBA_* views contains about ALL objects in the database
 */
public class OracleReader {

    private ShellStream writer;
    
    private Connection conn;

	Set<String> filteredTypes = new HashSet<String>();
	{
		// we likely will never need to support these
		filteredTypes.add("PACKAGE");
		filteredTypes.add("JAVA CLASS");
		filteredTypes.add("JAVA RESOURCE");
		filteredTypes.add("SYNONYM");
		filteredTypes.add("INDEX");
		filteredTypes.add("INDEXTYPE");
		filteredTypes.add("SEQUENCE");
		filteredTypes.add("XML SCHEMA");
		filteredTypes.add("OPERATOR");
		filteredTypes.add("CONSUMER GROUP");
		filteredTypes.add("PROGRAM");
		filteredTypes.add("WINDOW");
		filteredTypes.add("JOB CLASS");
		filteredTypes.add("DESTINATION");
		filteredTypes.add("SCHEDULE");
		filteredTypes.add("SCHEDULER GROUP");
		filteredTypes.add("EVALUATION CONTEXT");
		filteredTypes.add("EDITION");
	
		// not supported for now
		filteredTypes.add("VIEW");
		filteredTypes.add("FUNCTION");
		filteredTypes.add("TRIGGER");
		filteredTypes.add("TYPE");
	}

    private static class DBEntity {
    	String fldr;
    	String name;
    	String type;
    	public DBEntity(String _fldr, String _name, String _type) {
    		fldr = _fldr;
   			name = _name;
    		type = _type;
    	}
    	public void dump(ShellStream writer) {
    		String lName = name;
    		if (lName.endsWith("$")) 
    			lName = lName.substring(0, lName.length()-1);
    		writer.writeLine("E," + fldr + "$" + lName + ",\"" + type + "\"");
    	}
    }
    
    private interface DBEntityContentsProcessor {
    	public void process(DBEntity dbEntity) throws Throwable;
    }

    private Map<String, DBEntityContentsProcessor> typeToProcessor = new HashMap<String, DBEntityContentsProcessor>();

    {
    	typeToProcessor.put("TABLE", new DBEntityContentsProcessor() {
			public void process(DBEntity dbEntity) throws Throwable {
				List<DBEntity> e = getTableColumns(dbEntity);
				writer.writeLine("C," + e.size());
				for (DBEntity params : e) {
					params.dump(writer);
				}
				getTableConstraints(dbEntity);
			}
		});
    	typeToProcessor.put("PROCEDURE", new DBEntityContentsProcessor() {
			public void process(DBEntity dbEntity) throws Throwable {
				getDependencies(dbEntity);
			}
		});
    }
	
	public OracleReader(ShellStream w) {
		this.writer = w;
	}

    public void initDB(String jdbcUrl, String userid, String password) throws SQLException{
		OracleDataSource ds;
		ds = new OracleDataSource();
		ds.setURL(jdbcUrl);
		conn = ds.getConnection(userid, password);
    }

	protected void cleanup() throws Throwable {
		if (conn != null) {
			try {
				conn.close();
				conn = null;
			} catch (SQLException ignore) {
			}
		}
		if (writer != null) {
			writer.close();
			writer = null;
		}
	}
    
    public void readFromDB() throws Throwable {
		List<DBEntity> e = getEntities();
		//List<DBEntity> e = getDummyEntities();

		writer.writeLine("C," + e.size());

	    for (DBEntity dbEntity : e) {
			dbEntity.dump(writer);
			boolean success = process(dbEntity);
			if (!success) return;
		}
	}

	private boolean process(DBEntity dbEntity) throws Throwable {
		if (!typeToProcessor.containsKey(dbEntity.type)) {
			System.err.println("Don't know how to handle: " + dbEntity.type);
			return false;
		}
		typeToProcessor.get(dbEntity.type).process(dbEntity);
		return true;
	}

	private List<DBEntity> getEntities() throws Throwable {
		ResultSet rs;
		Statement stmt = conn.createStatement();
		String sqlStmt = "select * "
				+ "from all_objects c "
//				+ "where owner like '%' "
				+ "where owner = 'HR' "
				;
		//System.err.println(sqlStmt);
		
		List<DBEntity> retVal = new ArrayList<DBEntity>(100);

		rs = stmt.executeQuery(sqlStmt);
	    while (rs.next()) {
	    	if (filteredTypes.contains(rs.getString(6))) continue;
	    	retVal.add(new DBEntity(rs.getString(1), rs.getString(2), rs.getString(6)));
	    }

	    return retVal;
	}

	//private List<DBEntity> getDummyEntities() throws Throwable {
	//	List<DBEntity> retVal = new ArrayList<DBEntity>(100);
	//	retVal.add(new DBEntity("HR", "EMPLOYEES", "TABLE"));
	//	retVal.add(new DBEntity("HR", "ADD_JOB_HISTORY", "PROCEDURE"));
	//    return retVal;
	//}
	
	
	private void getTableConstraints(DBEntity dbe) throws Throwable {
		ResultSet rs;
		Statement stmt = conn.createStatement();

		String sqlStmt = "select c.owner, c.table_name, s.column_name, d.owner, d.table_name, d.column_name "
				+ "from all_constraints c "
				
				// look up primary key constraint on the same table
				// second join gives us column ids
				// + "inner join  all_constraints d on d.owner=c.r_owner and d.constraint_name=c.r_constraint_name "
				+ "inner join  all_cons_columns d on d.owner=c.r_owner and d.constraint_name=c.r_constraint_name "
				+ "inner join  all_cons_columns s on s.owner=c.owner and s.constraint_name=c.constraint_name "
				
				+ "where c.owner = '" + dbe.fldr + "' "
				+ "and c.table_name  = '" + dbe.name + "' "
				+ "and c.constraint_type = 'R' "
				//+ "order by c.owner, c.table_name "
				;
		//System.err.println(sqlStmt);
		rs = stmt.executeQuery(sqlStmt);

		while (rs.next()) {
	    	writer.writeLine(
	    			"R," +
	    			rs.getString(1) + "$" + rs.getString(2) + "." + rs.getString(3) +
	    			",\"Typed\"," + 
	    			rs.getString(4) + "$" + rs.getString(5) + "." + rs.getString(6)
	    	);
	    	
	    }
		
	}

	private List<DBEntity> getTableColumns(DBEntity dbEntity) throws Throwable {
		// we are assuming catalog = null
		ResultSet rslt = null;
		String schemaPattern = dbEntity.fldr;
		String tableNamePattern = dbEntity.name;

		List<DBEntity> retVal = new ArrayList<DBEntity>(10);
		
		try {
			rslt = conn.getMetaData().getColumns(null, schemaPattern, tableNamePattern, "%");
			while (rslt.next()) {
				retVal.add(new DBEntity(dbEntity.fldr, dbEntity.name + "." + rslt.getString(4), rslt.getString(6)));
			}
		} finally {
			if (rslt != null)
				try {
					rslt.close();
				} catch (SQLException ignore) {}
		}
		return retVal;
	}

	/**
	 * we don't have table-table relations (i.e. constraints)
	 */
	private void getDependencies(DBEntity dbEntity) throws Throwable {
		ResultSet rs;
		Statement stmt = conn.createStatement();
		
		String sqlStmt = "select * "
						+ "from   all_dependencies "
						+ "where  owner = '" + dbEntity.fldr + "' "
						+ "and name = '" + dbEntity.name + "' "
						;
		//System.err.println(sqlStmt);
		rs = stmt.executeQuery(sqlStmt);
		
	    while (rs.next()) {
	    	if (filteredTypes.contains(rs.getString(3)) || filteredTypes.contains(rs.getString(6))) continue;
	    	if (rs.getString(3).equals("PROCEDURE") && rs.getString(6).equals("PROCEDURE")) {
	    		// TODO: fix below - seems like for some reason 'Call' in this case is not being shown by Strata
	    		//writer.writeLine("R," + rs.getString(1) + '$' + rs.getString(2) + ",\"Call\"," + rs.getString(4) + '$' + rs.getString(5));
	    		writer.writeLine("R," + rs.getString(1) + '$' + rs.getString(2) + ",\"Set\"," + rs.getString(4) + '$' + rs.getString(5));
	    	}
	    	else
	    		writer.writeLine("R," + rs.getString(1) + '$' + rs.getString(2) + ",\"Set\"," + rs.getString(4) + '$' + rs.getString(5));
	    }
	}

	

}
