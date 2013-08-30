package org.neodatis.odb.core.session;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer2.meta.ObjectInfoHeader;

public class CacheImpl implements Cache{
	
	/**
	 * The cache for NeoDatis OID. This cache supports a weak reference and it is
	 * sync
	 */
	private Map<Object, ObjectOid> objects;
	protected Map<ObjectOid,WeakReference<Object>> oids;
	
	public CacheImpl(){
		init();
	}

	/**
	 * 
	 */
	private void init() {
		objects = Collections.synchronizedMap(new WeakHashMap<Object, ObjectOid>());		
		oids = Collections.synchronizedMap(new WeakHashMap<ObjectOid,WeakReference<Object>>());
	}

	public boolean existObject(Object object) {
		return objects.containsKey(object);
	}

	public ObjectInfoHeader getObjectInfoHeaderFromOid(OID oid, boolean throwExceptionIfNotFound) {
		return null;
	}



	public void addObject(ObjectOid oid, Object o) {
		if (o == null) {
			return;
		}
		try {
			objects.put(o, oid);
			oids.put(oid, new WeakReference<Object>(o));
				
		} catch (NullPointerException e) {
			///DLogger.error(OdbString.exceptionToString(e, true));
			// FIXME URL in HashMap What should we do?
			// In some case, the object can throw exception when added to the
			// cache
			// because Map.put, end up calling the equals method that can throw
			// exception
			// This is the case of URL that has a transient attribute handler
			// that is used in the URL.equals method
		}		
	}

	public Object getObjectWithOid(ObjectOid oid) {
		WeakReference<Object> wr = (WeakReference<Object>) oids.get(oid); 
		if(wr==null){
			return null;
		}
		return wr.get();
	}
	public ObjectOid getOid(Object object, boolean throwExceptionIfNotFound) {
		ObjectOid oid = objects.get(object);
		if(oid==null){
			if(throwExceptionIfNotFound){
				throw new NeoDatisRuntimeException(NeoDatisError.OBJECT_DOES_NOT_EXIST_IN_CACHE);
			}
			return null;
		}
		return oid;
	}
	
	public int getSize(){
		return objects.size();
	}

	public void clear() {
		objects.clear();
		oids.clear();
	}

	public void remove(Object o, ObjectOid oid) {
		objects.remove(o);
		oids.remove(oid);
	}
	public void remove(ObjectOid oid) {
		WeakReference<Object> ref = oids.remove(oid);
		if(ref!=null){
			Object o = ref.get();
			objects.remove(o);
		}
	}

}
