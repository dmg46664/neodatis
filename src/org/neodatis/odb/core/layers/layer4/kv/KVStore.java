package org.neodatis.odb.core.layers.layer4.kv;

import org.neodatis.odb.OID;

public interface KVStore {
	
	public void put(OID oid, Object v);
	public Object get(OID oid);
	public Object remove(OID oid);
	public boolean containsKey(OID oid);
}