package org.neodatis.odb.core.query.values;

import java.util.Iterator;

import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.ObjectValues;
import org.neodatis.odb.Objects;
import org.neodatis.odb.Values;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilder;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilderContext;
import org.neodatis.odb.core.layers.layer2.meta.AttributeValuesMap;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.query.IMatchingObjectAction;
import org.neodatis.odb.core.query.IQueryFieldAction;
import org.neodatis.odb.core.query.MatchResult;
import org.neodatis.odb.core.query.ValuesQuery;
import org.neodatis.odb.core.query.list.values.DefaultObjectValues;
import org.neodatis.odb.core.query.list.values.InMemoryBTreeCollectionForValues;
import org.neodatis.odb.core.query.list.values.SimpleListForValues;
import org.neodatis.odb.core.session.SessionEngine;
import org.neodatis.tool.wrappers.OdbComparable;

public class ValuesQueryResultAction implements IMatchingObjectAction {
	private ValuesQuery query;
	/** A copy of the query object actions */
	private IQueryFieldAction[] queryFieldActions;

	private long nbObjects;

	private Values result;

	private boolean queryHasOrderBy;

	/** An object to build instances */
	protected InstanceBuilder instanceBuilder;

	protected ClassInfo classInfo;

	private int returnArraySize;

	private SessionEngine engine;

	public ValuesQueryResultAction(ValuesQuery query, SessionEngine storageEngine, InstanceBuilder instanceBuilder) {
		super();
		this.engine = storageEngine;
		this.query = query;
		this.queryHasOrderBy = query.hasOrderBy();
		this.instanceBuilder = instanceBuilder;
		this.returnArraySize = query.getObjectActions().size();
		Iterator iterator = query.getObjectActions().iterator();
		IQueryFieldAction qfa = null;
		queryFieldActions = new IQueryFieldAction[returnArraySize];
		int i = 0;
		while (iterator.hasNext()) {
			qfa = (IQueryFieldAction) iterator.next();
			queryFieldActions[i] = qfa.copy();
			queryFieldActions[i].setReturnInstance(query.returnInstance());
			queryFieldActions[i].setInstanceBuilder(instanceBuilder);
			i++;
		}
	}

	public void add(MatchResult matchResult, Comparable orderByKey) {
		ValuesMatchResult valuesMatchResult = (ValuesMatchResult) matchResult;
		AttributeValuesMap attributeValuesMap = valuesMatchResult.valuesMap;
		if (query.isMultiRow()) {
			ObjectValues objectValues = convertObject(attributeValuesMap);
			if (queryHasOrderBy) {
				result.addWithKey(orderByKey, objectValues);
			} else {
				result.add(objectValues);
			}
		} else {
			compute(attributeValuesMap);
		}
	}

	private void compute(AttributeValuesMap values) {
		for (int i = 0; i < returnArraySize; i++) {
			queryFieldActions[i].execute(values.getOid(), values);
		}
	}

	private ObjectValues convertObject(AttributeValuesMap values) {
		DefaultObjectValues dov = new DefaultObjectValues(returnArraySize);
		IQueryFieldAction qfa = null;
		for (int i = 0; i < returnArraySize; i++) {
			qfa = queryFieldActions[i];
			qfa.execute(values.getOid(), values);
			
			Object o = qfa.getValue();
			
			// When Values queries return objects, they actually return the oid of the object
			// So we must load it here
			if (o != null && o instanceof ObjectOid) {
				ObjectOid oid = (ObjectOid) o;
				o = engine.getObjectFromOid(oid,true,new InstanceBuilderContext(query.getQueryParameters().getLoadDepth()));
			}
			
			dov.set(i, qfa.getAlias(), o);
		}

		return dov;
	}

	public void start() {

		if (query != null && query.hasOrderBy()) {
			result = new InMemoryBTreeCollectionForValues((int) nbObjects, query.getOrderByType(),query.getSessionEngine().getSession().getConfig().getDefaultCollectionBTreeDegree());
		} else {
			result = new SimpleListForValues((int) nbObjects);
		}

		IQueryFieldAction qfa = null;
		for (int i = 0; i < returnArraySize; i++) {
			qfa = queryFieldActions[i];
			qfa.start();
		}
	}

	public void end() {
		IQueryFieldAction qfa = null;
		DefaultObjectValues dov = null;

		if (!query.isMultiRow()) {
			dov = new DefaultObjectValues(returnArraySize);
		}
		for (int i = 0; i < returnArraySize; i++) {
			qfa = queryFieldActions[i];
			qfa.end();
			if (!query.isMultiRow()) {
				Object o = qfa.getValue();
				// When Values queries return objects, they actually return the oid of the object
				// So we must load it here
				if (o != null && o instanceof OID) {
					ObjectOid oid = (ObjectOid) o;
					o = engine.getObjectFromOid(oid,true,new InstanceBuilderContext(query.getQueryParameters().getLoadDepth()));
				}

				// Sets the values now
				dov.set(i, qfa.getAlias(), o);
			}
		}
		if (!query.isMultiRow()) {
			result.add(dov);
		}
	}

	public Values getValues() {
		return result;
	}

	public <T> Objects<T> getObjects() {
		return (Objects<T>) result;
	}

	public void addNnoi(OID oid, NonNativeObjectInfo object, OdbComparable orderByKey) {
		throw new NeoDatisRuntimeException(NeoDatisError.NOT_YET_IMPLEMENTED);		
	}

	public void addObject(OID oid, Object object, OdbComparable orderByKey) {
		throw new NeoDatisRuntimeException(NeoDatisError.NOT_YET_IMPLEMENTED);		
	}

}
