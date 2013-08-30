/**
 * 
 */
package org.neodatis.odb.core.oid;

import java.util.UUID;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer4.OidGenerator;
import org.neodatis.odb.core.layers.layer4.StorageEngine;
import org.neodatis.odb.core.oid.uuid.ClassOidImpl;
import org.neodatis.odb.core.oid.uuid.ObjectOidImpl;

/**
 * @author olivier
 * 
 */
public abstract class OidGeneratorAdapter implements OidGenerator {
	private StorageEngine engine;
	protected boolean useCache;

	public void init(StorageEngine engine, boolean useCache) {
		this.engine = engine;
		this.useCache = useCache;	
	}

	public StorageEngine getLayer4() {
		return engine;
	}
	protected long readLong(OID oid, boolean useCache ){
		return engine.readLong(oid, useCache);
	}
	protected void writeLong(OID oid, long l){
		engine.writeLong(oid, l);
	}

	

}
