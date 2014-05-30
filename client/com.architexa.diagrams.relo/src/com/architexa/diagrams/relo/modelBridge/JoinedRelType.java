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
 * Created on Mar 7, 2005
 *
 */
package com.architexa.diagrams.relo.modelBridge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.query.QueryResultsTableBuilder;
import org.openrdf.sesame.query.TableQuery;
import org.openrdf.sesame.query.serql.SerqlEngine;

import com.architexa.diagrams.model.Artifact;
import com.architexa.store.ReloRdfRepository;



/**
 * @author vineet
 *
 */
public class JoinedRelType extends RelType {
	
	public ArrayList<RelType> relations = new ArrayList<RelType>();

	public JoinedRelType(RelType first, RelType next) {
		// include all in one list - allows for optimizations for lists
		if (first instanceof JoinedRelType)
			relations.addAll( ((JoinedRelType)first).relations );
		else
			relations.add(first);
		if (next instanceof JoinedRelType)
			relations.addAll( ((JoinedRelType)next).relations );
		else
			relations.add(next);
	}
	
	protected String getSelectClause() {
		String clause = "" ;
		Iterator<RelType> relIt;
		int cnt;

		relIt = relations.iterator();
		cnt = 0;
		relIt.next();		// advance once, since there is one less result
		while (relIt.hasNext()) {
			clause += " x" + cnt + "";
			relIt.next();
			cnt++;
		}
		return clause;		
	}
	
	@Override
    protected String getWhereClause(String src, String dst) {
		String clause = "" ;
		Iterator<RelType> relIt;
		int cnt;

		relIt = relations.iterator();
		cnt = 0;
		clause += " " + relIt.next().getWhereClause(src, "x" + cnt);
		while (cnt < relations.size()-2) {
			clause += " " + relIt.next().getWhereClause("x" + cnt, "x" + (cnt+1));
			cnt++;
		}
		clause += " " + relIt.next().getWhereClause("x" + cnt, dst);
		return clause;
	}
	public String getQueryString(Artifact src, Artifact dst) {
		String queryString = "SELECT" ;
		//queryString += getSelectClause();
		queryString += " *";

		queryString += " FROM";
		
		String whereClause = getWhereClause("<" + src.elementRes + ">", "<" + dst.elementRes + ">");
		//queryString += whereClause.substring(0, whereClause.length()-1); // eat the last comma
		whereClause = whereClause.substring(0, whereClause.length()-1); // eat the last comma
		//whereClause = whereClause.substring(0, whereClause.lastIndexOf(", ")); // eat the second last comma
		//whereClause += whereClause.substring(0, whereClause.lastIndexOf(", ")); // eat the second last comma
		queryString += whereClause;
		return queryString;
	}
	public List<List<Artifact>> getPaths(ReloRdfRepository repo, Artifact src, Artifact dst) {
		//Iterator relIt;
		//int cnt;

		String queryString = getQueryString(src, dst);
		//System.err.println("Query String: " + queryString);
		
		
		List<List<Artifact>> resultSet = new ArrayList<List<Artifact>>(10);

		try {
			SerqlEngine serqlQueryEngine = repo.getSerqlEngine();
			TableQuery tableQuery = serqlQueryEngine.parseTableQuery(queryString);
			QueryResultsTableBuilder builder = new QueryResultsTableBuilder();
			serqlQueryEngine.evaluateSelectQuery(tableQuery, builder);
			QueryResultsTable resultsTable = builder.getQueryResultsTable();

			// retreive the results
			int rowCount = resultsTable.getRowCount();
			int columnCount = resultsTable.getColumnCount();
			for (int row = 0; row < rowCount; row++) {
				List<Artifact> result = new ArrayList<Artifact>(10);
			    for (int column = 0; column < columnCount; column++) {
					result.add(new Artifact((Resource) resultsTable.getValue(row, column)));
			    }
				resultSet.add(result);
			}

			//System.err.println("Returning resultSet size: " + rowCount);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		return resultSet;
	}
	
}
