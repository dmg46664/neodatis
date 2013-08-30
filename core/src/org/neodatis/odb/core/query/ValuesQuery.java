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

import java.util.HashSet;

import org.neodatis.odb.Values;
import org.neodatis.odb.core.query.values.CustomQueryFieldAction;
import org.neodatis.odb.core.query.values.ICustomQueryFieldAction;
import org.neodatis.odb.core.query.values.ValuesCriteriaQuery;
import org.neodatis.tool.wrappers.list.IOdbList;

public interface ValuesQuery extends InternalQuery {
	ValuesQuery count(String alias);
	ValuesQuery sum(String fieldName);
	ValuesQuery sum(String fieldName, String alias);
	ValuesQuery avg(String fieldName, String alias);
	ValuesQuery avg(String fieldName);
	ValuesQuery max(String fieldName, String alias);
	ValuesQuery max(String fieldName);
	ValuesQuery min(String fieldName, String alias);
	ValuesQuery min(String fieldName);
	ValuesQuery field(String fieldName);
	ValuesQuery field(String fieldName, String alias);
	ValuesQuery sublist(String attributeName, String alias, int fromIndex, int size, boolean throwException);
	ValuesQuery sublist(String attributeName, int fromIndex, int size, boolean throwException);
	ValuesQuery sublist(String attributeName, String alias, int fromIndex, int toIndex);
	ValuesQuery sublist(String attributeName, int fromIndex, int toIndex);
	ValuesQuery size(String attributeName);
	ValuesQuery size(String attributeName, String alias);
	ValuesQuery groupBy(String fieldList);
	String[] getGroupByFieldList();
	boolean hasGroupBy();
	
	HashSet<String> getAllInvolvedFields();

	/** A collection of IQueryFieldAction*/
	IOdbList<IQueryFieldAction> getObjectActions();
		
	/** To indicate if a query will return one row (for example, sum, average, max and min, or will return more than one row*/
	boolean isMultiRow();
	/**
	 * @return
	 */
	boolean returnInstance();
	/** To indicate if query execution must build instances or return object representation, Default value is true(return instance)*/
	void setReturnInstance(boolean returnInstance);
	/**
	 * @param q
	 * @return
	 */
	Values values();

	public ValuesQuery custom(String attributeName, ICustomQueryFieldAction action);

	public ValuesQuery custom(String attributeName, String alias, ICustomQueryFieldAction action);

	
}
