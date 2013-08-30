
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

import java.math.BigInteger;

import org.neodatis.OrderByConstants;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.Objects;
import org.neodatis.odb.Query;
import org.neodatis.odb.QueryParameters;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.session.SessionEngine;
import org.neodatis.tool.wrappers.OdbString;

public abstract class AbstractQuery implements InternalQuery {

	protected String [] orderByFields;
	protected OrderByConstants orderByType;
	protected transient SessionEngine engine;
	protected IQueryExecutionPlan executionPlan;
	protected boolean polymorphic;
	/** When set to true, object comparison are done using OID, else comparison are done comparing the whole object, default is true*/
	private boolean optimizeObjectComparison;
	
	/** The OID attribute is used when the query must be restricted the object with this OID*/
	protected ObjectOid oidOfObjectToQuery;
	
	protected QueryParameters queryParameters;

	public AbstractQuery(){
		orderByType = OrderByConstants.ORDER_BY_NONE;
		polymorphic = false;
		optimizeObjectComparison = true;
		this.queryParameters = new QueryParameters();
	}
	public Query orderByDesc(String fields) {
		orderByType = OrderByConstants.ORDER_BY_DESC;
		orderByFields = OdbString.split(fields,",");
		return this;
				
	}

	public Query orderByAsc(String fields) {
		orderByType = OrderByConstants.ORDER_BY_ASC;
		orderByFields = OdbString.split(fields,",");
		return this;
	}

	public String[] getOrderByFieldNames() {
		return orderByFields;
	}

	public void setOrderByFields(String[] orderByFields) {
		this.orderByFields = orderByFields;
	}

	public OrderByConstants getOrderByType() {
		return orderByType;
	}

	public void setOrderByType(OrderByConstants orderByType) {
		this.orderByType = orderByType;
	}

	public boolean hasOrderBy(){
		return !orderByType.isOrderByNone();
	}
	public SessionEngine getSessionEngine(){
		return engine;
	}
	public void setSessionEngine(SessionEngine engine){
		this.engine = engine;
	}
	
	public IQueryExecutionPlan getExecutionPlan() {
		if(executionPlan==null){
			throw new NeoDatisRuntimeException(NeoDatisError.EXECUTION_PLAN_IS_NULL_QUERY_HAS_NOT_BEEN_EXECUTED);
		}
		return executionPlan;
	}
	public void setExecutionPlan(IQueryExecutionPlan plan) {
		executionPlan = plan; 
	}
	public boolean isPolymorphic() {
		return polymorphic;
	}
	public Query setPolymorphic(boolean yes) {
		polymorphic = yes;
		return this;
	}
	public ObjectOid getOidOfObjectToQuery() {
		return oidOfObjectToQuery;
	}
	public void setOidOfObjectToQuery(ObjectOid oidOfObjectToQuery) {
		this.oidOfObjectToQuery = oidOfObjectToQuery;
	}
	/** Returns true is query must apply on a single object OID
	 * 
	 */
	public boolean isForSingleOid() {
		return oidOfObjectToQuery!=null;
	}
	
	/**
	 * @return
	 */
	public boolean optimizeObjectComparison() {
		return optimizeObjectComparison;
	}
	public Query setOptimizeObjectComparison(boolean yesNo){
		this.optimizeObjectComparison = yesNo;
		return this;
	}
	public QueryParameters getQueryParameters() {
		return queryParameters;
	}
	
	public <T>Objects<T> objects(){
		return engine.execute(this);
	}
	
	/**
	 * @return
	 */
	public BigInteger count(){
		return engine.count(this);
	}
}
