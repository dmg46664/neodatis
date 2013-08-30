/*
 NeoDatis ODB : Native Object Database (odb.info@neodatis.org)
 Copyright (C) 2007 NeoDatis Inc. http://www.neodatis.org

 "This file is part of the NeoDatis ODB open source object database".

 NeoDatis ODB is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 NeoDatis ODB is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.neodatis.odb.core.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neodatis.btree.IBTree;
import org.neodatis.btree.IBTreeMultipleValuesPerKey;
import org.neodatis.btree.IBTreeSingleValuePerKey;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.Objects;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.btree.LazyODBBTreePersister;
import org.neodatis.odb.core.layers.layer2.meta.AttributeValuesMap;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfoIndex;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator;
import org.neodatis.odb.core.query.criteria.NnoiMatchResult;
import org.neodatis.odb.core.query.criteria.OidMatchResult;
import org.neodatis.odb.core.query.nq.NQMatchResult;
import org.neodatis.odb.core.query.values.ValuesMatchResult;
import org.neodatis.odb.core.session.Session;
import org.neodatis.odb.core.session.SessionEngine;
import org.neodatis.odb.tool.MemoryMonitor;
import org.neodatis.tool.DLogger;

/**
 * 
 * <p>
 * Generic query executor. This class does all the job of iterating in the
 * object list and call particular query matching to check if the object must be
 * included in the query result.
 * </p>
 * 
 * <p>
 * If the query has index, An execution plan is calculated to optimize the
 * execution. The query execution plan is calculated by subclasses (using
 * abstract method getExecutionPlan).
 * 
 * </P>
 * 
 */
public abstract class GenericQueryExecutor<T> implements IMultiClassQueryExecutor {

	public static final String LOG_ID = "GenericQueryExecutor";

	/** The session engine */
	protected SessionEngine engine;

	/** The query being executed */
	protected InternalQuery query;

	/** The class of the object being fetched */
	protected ClassInfo classInfo;

	/** The current database session */
	protected Session session;

	/** A boolean to indicate if query must be ordered */
	private boolean queryHasOrderBy;

	/** The key for ordering */
	private Comparable orderByKey;

	protected IQueryExecutorCallback callback;

	/**
	 * Used for multi class executor to indicate not to execute start and end
	 * method of query result action
	 */
	protected boolean executeStartAndEndOfQueryAction;

	public GenericQueryExecutor(InternalQuery query, SessionEngine engine) {
		this.query = query;
		this.engine = engine;
		this.session = engine.getSession();
		this.callback = session.getConfig().getQueryExecutorCallback();
		this.executeStartAndEndOfQueryAction = true;
	}

	public abstract IQueryExecutionPlan getExecutionPlan();

	public abstract void prepareQuery();

	public abstract Comparable computeIndexKey(ClassInfo ci, ClassInfoIndex index);

	/**
	 * Check if the object with oid matches the query, returns true if it matches, false if not
	 * 
	 * This method must compute the orderBy key if it
	 * exists!
	 * 
	 * @param oid The object OID
	 * @param inMemory To indicate if object must be actually loaded to memory
	 * @return the result. Null if object does not match
	 */
	public abstract MatchResult matchObjectWithOid(ObjectOid oid, boolean inMemory);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.query.execution.IQueryExecutor#execute(boolean,
	 * int, int, boolean,
	 * org.neodatis.odb.core.query.execution.IMatchingObjectAction)
	 */
	public <T>Objects<T> execute(IMatchingObjectAction queryResultAction) {

		if (session.isClosed()) {
			throw new NeoDatisRuntimeException(NeoDatisError.ODB_IS_CLOSED.addParameter(session.getBaseIdentification()));
		}

		if (session.isRollbacked()) {
			throw new NeoDatisRuntimeException(NeoDatisError.ODB_HAS_BEEN_ROLLBACKED);
		}

		// When used as MultiClass Executor, classInfo is already set
		if (classInfo == null) {
			// Class to execute query on
			String fullClassName = QueryManager.getFullClassName(query);

			// If the query class does not exist in meta model, return an empty
			// collection
			if (!session.getMetaModel().existClass(fullClassName)) {
				queryResultAction.start();
				queryResultAction.end();
				query.setExecutionPlan(new EmptyExecutionPlan());
				return queryResultAction.getObjects();
			}

			classInfo = session.getMetaModel().getClassInfo(fullClassName, true);
		}

		// Get the query execution plan
		IQueryExecutionPlan plan = getExecutionPlan();

		plan.start();
		try {
			if (plan.useIndex() && session.getConfig().useIndex()) {
				return executeUsingIndex(plan.getIndex(), queryResultAction);
			}
			// When query must be applied to a single object
			if (query.isForSingleOid()) {
				return executeForOneOid( queryResultAction);
			}
			return executeFullScan(queryResultAction);
		} finally {
			plan.end();
		}
	}

	/**
	 * Query execution full scan
	 * 
	 * <pre>
	 * 
	 * startIndex &amp; endIndex
	 * A B C D E F G H I J K L
	 * 
	 * 
	 * [1,3] : nb &gt;=1 &amp;&amp; nb&lt;3
	 * 
	 * 1) 
	 * analyze A
	 * nb = 0
	 * nb E [1,3] ? no
	 * r=[]
	 * 2) 
	 * analyze B
	 * nb = 1
	 * nb E [1,3] ? yes
	 * r=[B]
	 * 3) analyze C
	 * nb = 2
	 * nb E [1,3] ? yes
	 * r=[B,C]
	 * 4) analyze C
	 * nb = 3
	 * nb E [1,3] ? no and 3&gt; upperBound([1,3]) =&gt; exit
	 * 
	 * </pre>
	 * 
	 * @param inMemory
	 * @param startIndex
	 * @param endIndex
	 * @param returnObjects
	 * @return
	 * @throws Exception
	 */
	private <T>Objects<T> executeFullScan( IMatchingObjectAction queryResultAction) {

		boolean objectInRange = false;

		if (session.isClosed()) {
			throw new NeoDatisRuntimeException(NeoDatisError.ODB_IS_CLOSED.addParameter(session.getBaseIdentification()));
		}

		if (executeStartAndEndOfQueryAction()) {
			queryResultAction.start();
		}

		int startIndex = query.getQueryParameters().getStartIndex();
		int endIndex = query.getQueryParameters().getEndIndex();
		boolean inMemory = query.getQueryParameters().isInMemory();
		ObjectOid oid = null;

		ObjectOidIterator oidIterator = engine.getObjectOidIterator(classInfo.getOid(), ObjectOidIterator.Way.INCREASING);

		prepareQuery();

		if (query != null) {
			queryHasOrderBy = query.hasOrderBy();
		}
		boolean monitorMemory = session.getConfig().monitoringMemory();

		// used when startIndex and endIndex are not negative
		int nbObjectsInResult = 0;

		//for (int i = 0; i < nbObjects; i++) {
		int i=0;
		MatchResult matchResult = null;
		
		while(oidIterator.hasNext()){
			if (monitorMemory && i % 10000 == 0) {
				MemoryMonitor.displayCurrentMemory("" + (i + 1), true);
			}
			// Reset the order by key
			orderByKey = null;

			oid = oidIterator.next();

			// This is an error
			if (oid == null) {
				continue;
			}

			// If there is an endIndex condition
			if (endIndex != -1 && nbObjectsInResult >= endIndex) {
				break;
			}

			// If there is a startIndex condition
			if (startIndex != -1 && nbObjectsInResult < startIndex) {
				objectInRange = false;
			} else {
				objectInRange = true;
			}

			// There is no query
			if (!inMemory && query == null) {

				nbObjectsInResult++;

				if (objectInRange) {
					orderByKey = null;
					if(query.hasOrderBy()){
						throw new NeoDatisRuntimeException(NeoDatisError.NOT_YET_SUPPORTED);
						//orderByKey = buildOrderByKey((Object)null);
					}
					// here we must check if oid exist
					boolean exist = engine.existOid(oid);
					if(exist){
						queryResultAction.add(new OidMatchResult(oid), orderByKey);
					}					
				}
			} else {

				matchResult = matchObjectWithOid(oid, inMemory);

				if (matchResult!=null && matchResult.match) {
					nbObjectsInResult++;
					if (objectInRange) {
						if (queryHasOrderBy) {
							orderByKey = buildOrderByKey(matchResult);
						}
						queryResultAction.add(matchResult, orderByKey);
						if (callback != null) {
							callback.readingObject(i, -1);
						}
					}
				}
			}
			i++;
		}
		if (executeStartAndEndOfQueryAction()) {
			queryResultAction.end();
		}
		return queryResultAction.getObjects();
	}

	/**
	 * Execute query using index
	 * 
	 * @param index
	 * @param inMemory
	 * @param startIndex
	 * @param endIndex
	 * @param returnObjects
	 * @return
	 * @throws Exception
	 */
	private <T>Objects<T> executeUsingIndex(ClassInfoIndex index, IMatchingObjectAction queryResultAction) {

		// Index that have not been used yet do not have persister!
		if (index.getBTree().getPersister() == null) {
			index.getBTree().setPersister(new LazyODBBTreePersister(engine));
		}

		long btreeSize = index.getBTree().getSize();
		
		if (executeStartAndEndOfQueryAction()) {
			queryResultAction.start();
		}

		prepareQuery();

		if (query != null) {
			queryHasOrderBy = query.hasOrderBy();
		}

		IBTree tree = index.getBTree();

		boolean isUnique = index.isUnique();

		// Iterator iterator = new BTreeIterator(tree,
		// OrderByConstants.ORDER_BY_ASC);
		Comparable key = computeIndexKey(classInfo, index);

		List<ObjectOid> list = null;
		// If index is unique, get the object
		if (isUnique) {
			IBTreeSingleValuePerKey treeSingle = (IBTreeSingleValuePerKey) tree;
			ObjectOid o = (ObjectOid) treeSingle.search(key);
			if (o != null) {
				list = new ArrayList<ObjectOid>();
				list.add(o);
			}
		} else {
			IBTreeMultipleValuesPerKey treeMultiple = (IBTreeMultipleValuesPerKey) tree;
			list = treeMultiple.search(key);
		}
		if (list != null) {
			MatchResult result = null;
			Iterator<ObjectOid> iterator = list.iterator();
			while (iterator.hasNext()) {
				ObjectOid oid = iterator.next();
				orderByKey = null;

				result = matchObjectWithOid(oid,query.getQueryParameters().isInMemory());

				if (result.match) {
					queryResultAction.add(result, orderByKey);
				}
			}
			queryResultAction.end();
			return queryResultAction.getObjects();
		}
		if (executeStartAndEndOfQueryAction()) {
			queryResultAction.end();
		}
		return queryResultAction.getObjects();
	}

	/**
	 * Execute query using index
	 * 
	 * @param inMemory
	 * @param returnObjects
	 * @return
	 * @throws Exception
	 */
	private <T>Objects<T> executeForOneOid(IMatchingObjectAction queryResultAction) {

		if (session.getConfig().isDebugEnabled(LOG_ID)) {
			DLogger.debug("loading Object with oid " + query.getOidOfObjectToQuery() + " - class " + classInfo.getFullClassName());
		}

		if (executeStartAndEndOfQueryAction()) {
			queryResultAction.start();
		}

		prepareQuery();

		ObjectOid oid = query.getOidOfObjectToQuery();
		MatchResult result = matchObjectWithOid(oid,query.getQueryParameters().isInMemory());
		queryResultAction.add(result, orderByKey);
		queryResultAction.end();
		return queryResultAction.getObjects();

	}

	/**
	 * TODO very bad. Should remove the instanceof
	 * 
	 * @param object
	 * @return
	 */
	public Comparable buildOrderByKey(MatchResult matchResult) {
		if (matchResult instanceof ValuesMatchResult) {
			ValuesMatchResult r = (ValuesMatchResult) matchResult;
			return buildOrderByKey(r.valuesMap);
		}else if(matchResult instanceof NnoiMatchResult){
			NnoiMatchResult r = (NnoiMatchResult) matchResult;
			return buildOrderByKey(r.nnoi);
		}else if(matchResult instanceof NQMatchResult){
			NQMatchResult r = (NQMatchResult) matchResult;
			return buildOrderByKey(r.nnoi);
		}
		throw new NeoDatisRuntimeException(NeoDatisError.NOT_YET_SUPPORTED);
		
	}

	public Comparable buildOrderByKey(NonNativeObjectInfo nnoi) {
		// TODO cache the attributes ids to compute them only once
		return IndexTool.buildIndexKey("OrderBy", nnoi, QueryManager.getOrderByAttributeIds(classInfo, query));
	}

	public Comparable buildOrderByKey(AttributeValuesMap values) {
		return IndexTool.buildIndexKey("OrderBy", values, query.getOrderByFieldNames());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.neodatis.odb.core.query.execution.IQueryExecutor#
	 * executeStartAndEndOfQueryAction()
	 */
	public boolean executeStartAndEndOfQueryAction() {
		return executeStartAndEndOfQueryAction;
	}

	public void setExecuteStartAndEndOfQueryAction(boolean yes) {
		this.executeStartAndEndOfQueryAction = yes;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.query.execution.IQueryExecutor#getQuery()
	 */
	public InternalQuery getQuery() {
		return query;
	}

	public void setClassInfo(ClassInfo classInfo) {
		this.classInfo = classInfo;
	}
	public SessionEngine getSessionEngine(){
		return engine;
	}

}
