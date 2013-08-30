
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
import java.util.Iterator;

import org.neodatis.odb.core.layers.layer2.meta.AttributeValuesMap;
import org.neodatis.odb.core.query.InternalQuery;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;

public abstract class ComposedExpression extends AbstractExpression {
	protected IOdbList<Criterion> criteria;
	
	public ComposedExpression(){
		criteria = new OdbArrayList<Criterion>(5);
	}
	public ComposedExpression add(Criterion criterion){
		criteria.add(criterion);
		return this;
	}
	public HashSet<String> getAllInvolvedFields() {
		Iterator iterator = criteria.iterator();;
		Criterion criterion = null;
		HashSet<String> fields = new HashSet<String>();
		while(iterator.hasNext()){
			criterion = (Criterion) iterator.next();
			fields.addAll(criterion.getAllInvolvedFields());
		}
		return fields;
	}
	public boolean isEmpty(){
		return criteria.isEmpty();
	}
	public AttributeValuesMap getValues() {
		AttributeValuesMap map = new AttributeValuesMap();
		Iterator iterator = criteria.iterator();;
		Criterion criterion = null;
		while(iterator.hasNext()){
			criterion = (Criterion) iterator.next();
			map.putAll(criterion.getValues());
		}
		return map;
	}
	public int getNbCriteria(){
		return criteria.size();
	}
	public Criterion getCriterion(int index){
		return criteria.get(index);
	}
	public void ready() {
		Iterator iterator = criteria.iterator();;
		Criterion criterion = null;
		while(iterator.hasNext()){
			criterion = (Criterion) iterator.next();
			criterion.setQuery(getQuery());
			criterion.ready();
		}
	}
	public void setQuery(InternalQuery query) {
		super.setQuery(query);
		Iterator iterator = criteria.iterator();;
		Criterion criterion = null;
		while(iterator.hasNext()){
			criterion = (Criterion) iterator.next();
			criterion.setQuery(getQuery());
		}
	}
	
}
