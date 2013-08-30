
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

import org.neodatis.OrderByConstants;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.Query;
import org.neodatis.odb.core.session.SessionEngine;

public interface InternalQuery extends Query{
	
	
	
	/** Returns the field names of the order by
	 * @return The array of  fields of the order by*/
	public String[] getOrderByFieldNames();
	/**  
	 * @return the type of the order by - ORDER_BY_NONE,ORDER_BY_DESC,ORDER_BY_ASC*/
	public OrderByConstants getOrderByType();
	
	public SessionEngine getSessionEngine();
	public void setSessionEngine(SessionEngine engine);
	
	public void setExecutionPlan(IQueryExecutionPlan plan);
	
    /** To indicate if a query must be executed on a single object with the specific OID. Used for ValuesQeuries
	 * 
	 * @return
	 */
	boolean isForSingleOid();
	
	/** used with isForSingleOid == true, to indicate we are working on a single object with a specific oid
	 * 
	 * @return
	 */
	 ObjectOid getOidOfObjectToQuery();
	public boolean optimizeObjectComparison();
	
}
