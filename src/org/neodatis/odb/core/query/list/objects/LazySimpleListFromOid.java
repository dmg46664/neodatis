package org.neodatis.odb.core.query.list.objects;

import java.util.Collection;
import java.util.Iterator;

import org.neodatis.OrderByConstants;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.Objects;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilderContext;
import org.neodatis.odb.core.session.SessionEngine;
import org.neodatis.tool.wrappers.list.OdbArrayList;

/**
 * A simple list to hold query result. It is used when no index and no order by is
 * used and inMemory = false
 * 
 * This collection does not store the objects, it only holds the OIDs of the objects. When user ask an object
 * the object is lazy loaded by the getObjectFromId method
 * 
 * @author osmadja
 * 
 */
public class LazySimpleListFromOid <T> extends OdbArrayList<T> implements Objects<T> {

	/** a cursor when getting objects*/
	private int currentPosition;
	
	/** The odb engine to lazily get objects*/
	private SessionEngine engine;
	
	/** indicate if objects must be returned as instance (true) or as non native objects (false)*/
	private boolean returnInstance;
	
	protected InstanceBuilderContext ibc;

	public LazySimpleListFromOid(int size, SessionEngine engine, boolean returnObjects, int depth) {
		super(size);
		this.engine = engine;
		this.returnInstance = returnObjects;
		this.ibc = new InstanceBuilderContext(depth);
	}

	public LazySimpleListFromOid(SessionEngine engine, boolean returnObjects, Collection<T> oids, int depth) {
		super(oids);
		this.engine = engine;
		this.returnInstance = returnObjects;
		this.ibc = new InstanceBuilderContext(depth);
	}

	public boolean addWithKey(Comparable key, T object) {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_IMPLEMENTED);
	}

	public boolean addWithKey(int key, T object) {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_IMPLEMENTED);
	}

	public T first() {
		try {
			return get(0);
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(NeoDatisError.ERROR_WHILE_GETTING_OBJECT_FROM_LIST_AT_INDEX.addParameter(0), e);
		}
	}

	public T get(int index) {
		ObjectOid oid = (ObjectOid) super.get(index);
		try {
			if(returnInstance){
				return (T) engine.getObjectFromOid(oid,true,ibc);
			}
			return (T) engine.getMetaObjectFromOid(oid,true,ibc);
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(NeoDatisError.ERROR_WHILE_GETTING_OBJECT_FROM_LIST_AT_INDEX.addParameter(index),e);
		}
	}

	public boolean hasNext() {
		return currentPosition < size();
	}

	public Iterator<T> iterator(OrderByConstants orderByType) {
		throw new NeoDatisRuntimeException(NeoDatisError.OPERATION_NOT_IMPLEMENTED);
	}

	public T next() {
		try {
			return get(currentPosition++);
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(NeoDatisError.ERROR_WHILE_GETTING_OBJECT_FROM_LIST_AT_INDEX.addParameter(0), e);
		}
	}

	public void reset() {
		currentPosition = 0;
	}

}
