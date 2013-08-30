/**
 * 
 */
package org.neodatis.odb.core.layers.layer4.plugin.memory;

import java.util.HashMap;
import java.util.Map;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.core.layers.layer4.ClassOidIterator;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator;
import org.neodatis.odb.core.layers.layer4.StorageEngineAdapter;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator.Way;
import org.neodatis.odb.core.oid.StringOid;
import org.neodatis.odb.core.oid.StringOidImpl;
import org.neodatis.tool.DLogger;

/**
 * A NeoDatis storage engine backed by a memory HashMap
 * 
 * @author olivier
 * 
 */
public class InMemoryStorageEngineFast extends StorageEngineAdapter {

	public StringOid CLASS_OID =  new StringOidImpl("__class-oid__");
	public StringOid STRING_OID = new StringOidImpl("__string-oid__");

	/**
	 * A boolean value to indicate whether to log debug message
	 * 
	 */
	protected boolean debug = false;

	/**
	 * A hashmap to store the storage engine data the key is the store name
	 */
	protected Map<OID, Map<OID, OidAndBytes>> stores;

	public InMemoryStorageEngineFast() {
		super();
	}

	public OidAndBytes read(OID oid, boolean useCache) {
		if (debug) {
			DLogger.info("Reading OID " + oid.oidToString());
		}

		if (oid == null) {
			return null;
		}

		if (oid instanceof ObjectOid) {
			ObjectOid ooid = (ObjectOid) oid;
			ClassOid coid = ooid.getClassOid();
			Map<OID, OidAndBytes> store = stores.get(coid);
			if(store==null){
				return null;
			}

			return store.get(oid);
		}
		if (oid instanceof ClassOid) {
			ClassOid coid = (ClassOid) oid;
			Map<OID, OidAndBytes> store = stores.get(CLASS_OID);
			if(store==null){
				return null;
			}

			return store.get(oid);
		}

		if (oid instanceof StringOid) {
			Map<OID, OidAndBytes> store = stores.get(STRING_OID);
			if(store==null){
				return null;
			}

			return store.get(oid);
		}
		throw new RuntimeException("get:Unmanaged OID of type " + oid.getClass().getName());
	}

	public void write(OidAndBytes oidAndBytes) {
		if (debug) {
			DLogger.info("Writing OID " + oidAndBytes.oid.oidToString() + " | bytes = " + oidAndBytes.bytes);
		}
		OID oid = oidAndBytes.oid;

		if (oid instanceof ObjectOid) {
			ObjectOid ooid = (ObjectOid) oid;
			ClassOid coid = ooid.getClassOid();
			Map<OID, OidAndBytes> store = stores.get(coid);
			
			if(store==null){
				store = new HashMap<OID, OidAndBytes>();
				stores.put(coid, store);
			}
			store.put(oid, oidAndBytes);
			
			return;
		}
		if (oid instanceof ClassOid) {
			ClassOid coid = (ClassOid) oid;
			Map<OID, OidAndBytes> store = stores.get(CLASS_OID);
			store.put(oid, oidAndBytes);
			return;
		}

		if (oid instanceof StringOid) {
			Map<OID, OidAndBytes> store = stores.get(STRING_OID);
			store.put(oid, oidAndBytes);
			return;
		}
		throw new RuntimeException("get:Unmanaged OID of type " + oid.getClass().getName());

	}

	public void close() {
		// nothing to do
	}

	public void commit() {
		// nothing to do
	}

	public void open(String baseName, NeoDatisConfig config) {
		this.stores = new HashMap<OID, Map<OID, OidAndBytes>>();
		
		this.stores.put(CLASS_OID, new HashMap<OID, OidAndBytes>());
		this.stores.put(STRING_OID, new HashMap<OID, OidAndBytes>());
	}

	public void open(String host, int port, String baseName, NeoDatisConfig config) {
		throw new RuntimeException("Client server mode is not supported by InMemory Mode");
	}

	public void rollback() {
		throw new RuntimeException("rollback not supported by InMemory Mode");
	}

	public void deleteObjectWithOid(OID oid) {

		if (oid instanceof ObjectOid) {
			ObjectOid ooid = (ObjectOid) oid;
			ClassOid coid = ooid.getClassOid();
			Map<OID, OidAndBytes> store = stores.get(coid);
			if(store==null){
				return ;
			}

			store.remove(oid);
			return;
		}
		if (oid instanceof ClassOid) {
			ClassOid coid = (ClassOid) oid;
			Map<OID, OidAndBytes> store = stores.get(CLASS_OID);
			if(store==null){
				return ;
			}

			store.remove(oid);
			return;
		}

		if (oid instanceof StringOid) {
			Map<OID, OidAndBytes> store = stores.get(STRING_OID);
			if(store==null){
				return ;
			}

			store.remove(oid);
			return;
		}
		throw new RuntimeException("get:Unmanaged OID of type " + oid.getClass().getName());
	}

	public boolean existOid(OID oid) {
		if (oid == null) {
			return false;
		}

		if (oid instanceof ObjectOid) {
			ObjectOid ooid = (ObjectOid) oid;
			ClassOid coid = ooid.getClassOid();
			Map<OID, OidAndBytes> store = stores.get(coid);
			if(store==null){
				return false;
			}
			return store.containsKey(oid);
		}
		if (oid instanceof ClassOid) {
			ClassOid coid = (ClassOid) oid;
			Map<OID, OidAndBytes> store = stores.get(CLASS_OID);
			if(store==null){
				return false;
			}
			return store.containsKey(oid);
		}

		if (oid instanceof StringOidImpl) {
			Map<OID, OidAndBytes> store = stores.get(STRING_OID);
			if(store==null){
				return false;
			}
			return store.containsKey(oid);
		}
		throw new RuntimeException("get:Unmanaged OID of type " + oid.getClass().getName());
	}

	public String getEngineDirectoryForBaseName(String theBaseName) {
		return null;
	}

	public String getStorageEngineName() {
		return "in memory";
	}

	public boolean useDirectory() {
		return false;
	}

	@Override
	public ClassOidIterator getClassOidIterator() {
		return new InMemoryClassOidIterator(stores.get(CLASS_OID));
	}

	@Override
	public ObjectOidIterator getObjectOidIterator(ClassOid classOid, Way way) {
		Map<OID, OidAndBytes> store = stores.get(classOid);
		return new InMemoryObjectOidIterator(store);
	}
}
