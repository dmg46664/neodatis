/**
 * 
 */
package org.neodatis.odb.core.server;

import org.neodatis.odb.core.layers.layer4.StorageEngine;
import org.neodatis.odb.core.oid.uuid.UniqueOidGeneratorImpl;
import org.neodatis.tool.mutex.Mutex;
import org.neodatis.tool.mutex.MutexFactory;

/**
 * @author olivier
 * 
 */
public class ServerOidGeneratorImpl extends UniqueOidGeneratorImpl {

	protected Mutex mutex;

	/**
	 * @param layer4
	 * @param useCache
	 */
	public ServerOidGeneratorImpl(String basename, StorageEngine layer4, boolean useCache) {
		init(layer4, useCache);
		this.mutex = MutexFactory.get(basename);
	}

	
}
