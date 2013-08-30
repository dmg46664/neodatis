package org.neodatis.odb.core.session;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer2.meta.ObjectInfoHeader;

public class Cache2Impl implements Cache {

	/**
	 * The cache for NeoDatis OID. This cache supports a weak reference and it
	 * is sync
	 */
	private Map<Integer, ObjectOid> objects;
	protected Map<ObjectOid, Object> oids;

	public Cache2Impl() {
		init();
	}

	/**
	 * 
	 */
	private void init() {
		objects = Collections.synchronizedMap(new WeakHashMap<Integer, ObjectOid>());
		oids = Collections.synchronizedMap(new WeakHashMap<ObjectOid, Object>());
	}

	public boolean existObject(Object object) {
		Integer key = new Integer(System.identityHashCode(object));
		return objects.containsKey(key);
	}

	public ObjectInfoHeader getObjectInfoHeaderFromOid(OID oid, boolean throwExceptionIfNotFound) {
		return null;
	}

	public void addObject(ObjectOid oid, Object o) {
		if (o == null) {
			return;
		}
		Integer key = new Integer(System.identityHashCode(o));
		objects.put(key, oid);
		oids.put(oid, new WeakReference<Object>(o));

	}

	public Object getObjectWithOid(ObjectOid oid) {
		WeakReference<Object> wr = (WeakReference<Object>) oids.get(oid);
		if (wr == null) {
			return null;
		}
		return wr.get();
	}

	public ObjectOid getOid(Object object, boolean throwExceptionIfNotFound) {
		Integer key = new Integer(System.identityHashCode(object));
		return objects.get(key);
	}

	public int getSize() {
		return objects.size();
	}

	public void clear() {
		objects.clear();
		oids.clear();
	}

	public void remove(Object o, ObjectOid oid) {
		Integer key = new Integer(System.identityHashCode(o));
		objects.remove(key);
		oids.remove(oid);
	}

	public void remove(ObjectOid oid) {
		WeakReference<Object> ref = (WeakReference<Object>) oids.remove(oid);
		if (ref != null) {
			Integer key = new Integer(System.identityHashCode(ref.get()));
			objects.remove(key);
		}
	}

}
