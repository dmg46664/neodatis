package org.neodatis.odb.core.btree.kv;

import java.util.HashMap;
import java.util.Map;

public class InMemoryKVStore {
	Map<Object, Object> store ;
	
	public InMemoryKVStore(){
		store = new HashMap<Object, Object>();
	}
	
	public void put(Object k, Object v){
		store.put(k, v);
	}
	public Object get(Object k){
		return store.get(k);
	}

	public Object remove(Object id) {
		return store.remove(id);
	}
}
