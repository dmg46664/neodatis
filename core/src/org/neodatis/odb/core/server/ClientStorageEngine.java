/**
 * 
 */
package org.neodatis.odb.core.server;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.core.layers.layer4.ClassOidIterator;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator;
import org.neodatis.odb.core.layers.layer4.OidGenerator;
import org.neodatis.odb.core.layers.layer4.StorageEngine;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator.Way;
import org.neodatis.odb.core.server.oid.ClientOidGenerator;
import org.neodatis.odb.core.server.oid.ClientOidGeneratorImpl;
import org.neodatis.odb.core.session.Session;

/**A layer implementation for client side engine. The layer4 is not used on the client side. All operations that need the layer4 are executed on the server
 * Except getNextClassOid()
 * @author olivier
 * 
 */
public class ClientStorageEngine implements StorageEngine {

	protected ClientOidGenerator oidGenerator;
	protected MessageStreamer messageStreamer;
	protected Session session;
	
	public ClientStorageEngine(Session session, MessageStreamer messageStreamer){
		oidGenerator = new ClientOidGeneratorImpl();
		this.messageStreamer = messageStreamer;
		this.session = session;
		oidGenerator.set(session.getConfig().getCoreProvider().getOidGenerator(),  session, messageStreamer);
	}
	
	public void close() {
		// Nothing to do on client side
	}

	public void commit() {
		// nothing to do
	}

	public void deleteObjectWithOid(OID oid) {
		throw new RuntimeException("Not implemented");
	}

	public boolean existOid(OID oid) {
		throw new RuntimeException("Not implemented");
	}

	
	public ClassOidIterator getClassOidIterator() {
		throw new RuntimeException("Not implemented");
	}

	
	public String getEngineDirectoryForBaseName(String theBaseName) {
		throw new RuntimeException("Not implemented");
	}

	public ObjectOidIterator getObjectOidIterator(ClassOid classOid, Way way) {
		throw new RuntimeException("Not implemented");
	}

	public OidGenerator getOidGenerator() {
		return oidGenerator;
	}

	public void open(String baseName, NeoDatisConfig config) {
		throw new RuntimeException("Not implemented");
	}

	public void open(String host, int port, String baseName, NeoDatisConfig config) {
		throw new RuntimeException("Not implemented");
	}

	public OidAndBytes read(OID oid, boolean useCache) {
		throw new RuntimeException("Not implemented");
	}

	public long readLong(OID oid, boolean useCache) {
		throw new RuntimeException("Not implemented");
	}

	public void rollback() {
		throw new RuntimeException("Not implemented");
	}

	public void write(OidAndBytes oidAndBytes) {
		throw new RuntimeException("Not implemented");
	}

	public void writeLong(OID oid, long l) {
		throw new RuntimeException("Not implemented");
	}

	public String getStorageEngineName() {
		return "client layer4";
	}

	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.layers.layer4.StorageEngine#init(org.neodatis.odb.NeoDatisConfig)
	 */
	public void init(NeoDatisConfig config) {
		// TODO Auto-generated method stub
		
	}

	public boolean useDirectory() {
		return false;
	}

}
