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
package org.neodatis.odb.core.query.nq;

import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilderContext;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfoIndex;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.query.GenericQueryExecutor;
import org.neodatis.odb.core.query.IQueryExecutionPlan;
import org.neodatis.odb.core.query.InternalQuery;
import org.neodatis.odb.core.query.MatchResult;
import org.neodatis.odb.core.query.QueryManager;
import org.neodatis.odb.core.session.SessionEngine;

public class NativeQueryExecutor extends GenericQueryExecutor {

    
    public NativeQueryExecutor(InternalQuery query, SessionEngine engine) {
        super(query, engine);
    }

    public IQueryExecutionPlan getExecutionPlan() {
        IQueryExecutionPlan plan = new NativeQueryExecutionPlan(classInfo,query);
        return plan;
    }

    public void prepareQuery() {
    }
    
    /**
     * Check if the object at position currentPosition matches the query, returns true
     * 
     * This method must compute the next object position and the orderBy key if it exists!
     */
    public MatchResult matchObjectWithOid(ObjectOid oid, boolean inMemory)  {
    	InstanceBuilderContext ibc = new InstanceBuilderContext(query.getQueryParameters().getLoadDepth());
        NonNativeObjectInfo nnoi = engine.getMetaObjectFromOid(oid,false,ibc);
        boolean objectMatches = false;
        if (!nnoi.isDeletedObject()) {
            Object o = engine.layer2ToLayer1(nnoi,ibc);
            objectMatches = query == null || QueryManager.match(query, o);
            if(objectMatches){
            	return new NQMatchResult(nnoi,o);
            }
        }
        
        return null;
    }

    public Comparable computeIndexKey(ClassInfo ci, ClassInfoIndex index) {
        return null;
    }

/*
    public Comparable buildOrderByKey() {
        return IndexTool.buildIndexKey("OrderBy",currentNnoi, QueryManager.getOrderByAttributeIds(classInfo, query));
    }
    */
}
