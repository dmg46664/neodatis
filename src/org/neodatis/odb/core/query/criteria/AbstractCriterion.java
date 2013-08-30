
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

import org.neodatis.odb.core.layers.layer2.meta.AbstractObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.AttributeValuesMap;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.query.InternalQuery;

/**
 * An adapter for Criterion.
 * 
 * @author olivier s
 *
 */
public abstract class AbstractCriterion implements Criterion {

	/** The query containing the criterion*/
	private InternalQuery query;

	/** The name of the attribute involved by this criterion*/
	protected String attributeName;


	public boolean canUseIndex() {
		return false;
	}

	public AbstractCriterion(String fieldName) {
		this.attributeName = fieldName;
	}

	public boolean match(AbstractObjectInfo aoi) {
		NonNativeObjectInfo nnoi = (NonNativeObjectInfo) aoi;
		Object aoiValue = nnoi.getValueOf(attributeName);
		return match(aoiValue);
	}

	public boolean match(AttributeValuesMap attributeValues) {
		return match(attributeValues.getAttributeValue(attributeName));
	}

	abstract public boolean match(Object valueToMatch);

	public IExpression and(Criterion criterion) {
		return new And().add(this).add(criterion);
	}

	public IExpression or(Criterion criterion) {
		return new Or().add(this).add(criterion);
	}

	public IExpression not() {
		return new Not(this);
	}

	/** Gets the whole query 
	 * @return The owner query*/
	public InternalQuery getQuery() {
		return query;
	}

	public void setQuery(InternalQuery query) {
		this.query = query;
	}

	
	/**
	 * 
	 * @return The attribute involved in the criterion
	 */
	public String getAttributeName(){
		return attributeName;
	}

	/** An abstract criterion only restrict one field => it returns a list of one field!
	 * @return The list of involved field of the criteria
	 * 
	 */
	public HashSet<String> getAllInvolvedFields() {
		HashSet<String> l = new HashSet<String>();
		l.add(attributeName);
		return l;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

}
