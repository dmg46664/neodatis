/**
 * 
 */
package org.neodatis.odb.core.layers.layer4;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.oid.uuid.UniqueOidGeneratorImpl;

/**
 * @author olivier
 *
 */
public abstract class StorageEngineAdapter implements StorageEngine {
	protected OidGenerator oidGenerator;
	protected DataConverter converter;
	protected NeoDatisConfig config;
	protected boolean useCacheForOid;
	
	public StorageEngineAdapter(){
	}
	
	/**
	 * @param useCacheForOid
	 * @return
	 */
	public OidGenerator buildOidGenerator() {
		OidGenerator generator =  config.getCoreProvider().getOidGenerator();
		useCacheForOid = config.oidGeneratorUseCache();
		generator.init(this, useCacheForOid);
		return generator;
	}

	public void init(NeoDatisConfig config){
		this.config = config;
		this.converter = config.getCoreProvider().getByteArrayConverter(config.debugLayers(),config.getDatabaseCharacterEncoding(),config);
		this.oidGenerator = buildOidGenerator();
	}

	public abstract OidAndBytes read(OID oid, boolean useCache);

	public abstract void write(OidAndBytes oidAndBytes);

	public abstract ObjectOidIterator getObjectOidIterator(ClassOid classOid, ObjectOidIterator.Way way);
	public abstract ClassOidIterator getClassOidIterator();
	
	public OidGenerator getOidGenerator(){
		return oidGenerator;
	}


	public long readLong(OID oid, boolean useCache) {
		OidAndBytes oidAndBytes = read(oid, useCache);
		if(oidAndBytes==null){
			//TODO: WARNING can we return MIN_VALUE???
			return Long.MIN_VALUE;
		}
		return converter.byteArrayToLong(oidAndBytes.bytes, 0, new ReadSize(), "long");
	}


	public void writeLong(OID oid, long l) {
		Bytes bytes = BytesFactory.getBytes();
		converter.longToByteArray(l,bytes,0, "long");
		OidAndBytes oidAndBytes = new OidAndBytes(oid,bytes);
		write(oidAndBytes);
	}


}
	
