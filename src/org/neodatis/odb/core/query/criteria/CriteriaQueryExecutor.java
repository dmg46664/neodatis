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
package org.neodatis.odb.core.query.criteria;

import java.util.HashSet;

import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilderContext;
import org.neodatis.odb.core.layers.layer2.meta.AttributeValuesMap;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfoIndex;
import org.neodatis.odb.core.layers.layer2.meta.ObjectInfoHeader;
import org.neodatis.odb.core.query.GenericQueryExecutor;
import org.neodatis.odb.core.query.IQueryExecutionPlan;
import org.neodatis.odb.core.query.IndexTool;
import org.neodatis.odb.core.query.InternalQuery;
import org.neodatis.odb.core.query.MatchResult;
import org.neodatis.odb.core.query.SimpleCompareKey;
import org.neodatis.odb.core.session.SessionEngine;

public class CriteriaQueryExecutor extends GenericQueryExecutor {
	private HashSet<String> fields;

	private CriteriaQueryImpl criteriaQuery;
	protected InstanceBuilderContext ibc;

	public CriteriaQueryExecutor(InternalQuery query, SessionEngine engine) {
		super(query, engine);
		criteriaQuery = (CriteriaQueryImpl) query;
		this.ibc = new InstanceBuilderContext(query.getQueryParameters().getLoadDepth());
	}

	public IQueryExecutionPlan getExecutionPlan() {
		IQueryExecutionPlan plan = new CriteriaQueryExecutionPlan(classInfo, (CriteriaQueryImpl) query);
		return plan;
	}

	public void prepareQuery() {
		criteriaQuery = (CriteriaQueryImpl) query;
		criteriaQuery.setSessionEngine(engine);
		fields = criteriaQuery.getAllInvolvedFields();
		if(criteriaQuery.hasOrderBy()){
			String[] orderByFields = criteriaQuery.getOrderByFieldNames();
			for(int i=0;i<orderByFields.length;i++){
				fields.add(orderByFields[i]);
			}
		}
	}

	public MatchResult matchObjectWithOid(ObjectOid oid, boolean inMemory) {
		//ITmpCache tmpCache = session.getTmpCache();
		SessionEngine engine = getSessionEngine();
		ObjectInfoHeader oih = null;
		try {
			if (!criteriaQuery.hasCriteria()) {
				if (inMemory){
					return new NnoiMatchResult(engine.getMetaObjectFromOid(oid,false,ibc));
				}
				// we must check if object exist
				if(engine.existOid(oid)){
					return new OidMatchResult(oid);
				}
				return null;
			}

			boolean optimizeObjectCompararison = criteriaQuery.optimizeObjectComparison();
			// Gets a map with the values with the fields involved in the query
			AttributeValuesMap attributeValues = engine.getFieldValuesFromOid(oid,fields,optimizeObjectCompararison, query.getQueryParameters().getLoadDepth());
			// Then apply the query on the field values
			boolean objectMatches = CriteriaQueryManager.match(criteriaQuery, attributeValues);

			if (objectMatches) {
				if (inMemory){
					return new NnoiMatchResult(engine.getMetaObjectFromOid(oid,false,ibc));
				}
				return new OidMatchResult(oid);
			}
			return null;
		} finally {
			//tmpCache.clearObjectInfos();
		}
	}

	public Comparable computeIndexKey(ClassInfo ci, ClassInfoIndex index) {
		CriteriaQuery q = (CriteriaQuery) query;
		AttributeValuesMap values = q.getCriteria().getValues();
		// if values.hasOid() is true, this means that we are working of the full object,
		// the index key is then the oid and not the object itself
		if(values.hasOid()){
			return new SimpleCompareKey(values.getOid());
		}
		
		return IndexTool.computeKey(classInfo, index, (CriteriaQuery) query);
	}
}
