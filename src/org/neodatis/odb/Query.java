
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
package org.neodatis.odb;

import java.io.Serializable;
import java.math.BigInteger;

import org.neodatis.odb.core.query.IQueryExecutionPlan;

public interface Query extends Serializable{
	
	/** To order by the result of a query in descendent order
	 * 
	 * @param fields A comma separated field list
	 * @return this
	 */
	public Query orderByDesc(String fields);
	/** To order by the result of a query in ascendent order
	 * 
	 * @param fields A comma separated field list
	 * @return this
	 */
	public Query orderByAsc(String fields);
	
	/** Returns true if the query has an order by clause
	 * @return true if has an order by flag*/
	public boolean hasOrderBy();
	
	
	public IQueryExecutionPlan getExecutionPlan();
	
    /** To specify that instances of subclass of the query class must not be load
     * if true, when querying objects of class Class1, only direct instances of Class1 will be loaded.
     * 
     * If false, when querying objects of class Class1, direct instances of Class1 will be loaded and all instances of subclasses of Class1.*/ 
	Query setPolymorphic(boolean yes);
	boolean isPolymorphic();
	
	/**
	 * @param b
	 */
	public Query setOptimizeObjectComparison(boolean b);
	public boolean optimizeObjectComparison();
	
	public QueryParameters getQueryParameters();
	
	public <T>Objects<T> objects();
	/**
	 * @return
	 */
	public BigInteger count();

}
