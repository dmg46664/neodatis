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
package org.neodatis.odb.core.query.values;

import java.util.HashSet;

import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer2.meta.AttributeValuesMap;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfoIndex;
import org.neodatis.odb.core.query.GenericQueryExecutor;
import org.neodatis.odb.core.query.IQueryExecutionPlan;
import org.neodatis.odb.core.query.IndexTool;
import org.neodatis.odb.core.query.InternalQuery;
import org.neodatis.odb.core.query.MatchResult;
import org.neodatis.odb.core.query.criteria.CriteriaQuery;
import org.neodatis.odb.core.query.criteria.CriteriaQueryExecutionPlan;
import org.neodatis.odb.core.query.criteria.CriteriaQueryImpl;
import org.neodatis.odb.core.query.criteria.CriteriaQueryManager;
import org.neodatis.odb.core.session.SessionEngine;

public class ValuesCriteriaQueryExecutor extends GenericQueryExecutor {
	private HashSet<String> involvedFields;
	private CriteriaQueryImpl criteriaQuery;

	public ValuesCriteriaQueryExecutor(InternalQuery query, SessionEngine engine) {
		super(query, engine);
		criteriaQuery = (CriteriaQueryImpl) query;
	}

	public IQueryExecutionPlan getExecutionPlan() {
		IQueryExecutionPlan plan = new CriteriaQueryExecutionPlan(classInfo, (CriteriaQueryImpl) criteriaQuery);
		return plan;
	}

	public void prepareQuery() {
		criteriaQuery = (CriteriaQueryImpl) criteriaQuery;
		//CriteriaQueryImpl.setStorageEngine(storageEngine);
		involvedFields = criteriaQuery.getAllInvolvedFields();
		
		if(criteriaQuery.hasOrderBy()){
			String[] orderByFields = criteriaQuery.getOrderByFieldNames();
			for(int i=0;i<orderByFields.length;i++){
				involvedFields.add(orderByFields[i]);
			}
		}
	}

	public MatchResult matchObjectWithOid(ObjectOid oid, boolean inMemory) {
		boolean optimizeObjectCompararison = criteriaQuery.optimizeObjectComparison();
		
		// Gets a map with the values with the fields involved in the query
		AttributeValuesMap values = engine.getFieldValuesFromOid(oid, involvedFields,optimizeObjectCompararison,criteriaQuery.getQueryParameters().getLoadDepth());
		// if values is empty, nothing exist for the oid (probably the object has been deleted
		if(values==null){
			return new ValuesMatchResult(false);
		}
		
		boolean objectMatches = true;

		if (!criteriaQuery.isForSingleOid()) {
			// Then apply the query on the field values
			objectMatches = CriteriaQueryManager.match(criteriaQuery, values);
		}

		if(objectMatches){
			return new ValuesMatchResult(values);
		}
		return null;
	}

	public Comparable computeIndexKey(ClassInfo ci, ClassInfoIndex index) {
		return IndexTool.computeKey(classInfo, index, (CriteriaQuery) criteriaQuery);
	}
}
