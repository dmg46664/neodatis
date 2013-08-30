/**
 * 
 */
package org.neodatis.odb.core.server.oid;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.DatabaseId;
import org.neodatis.odb.ExternalOID;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer4.OidGenerator;
import org.neodatis.odb.core.layers.layer4.StorageEngine;
import org.neodatis.odb.core.server.MessageStreamer;
import org.neodatis.odb.core.server.message.NextClassInfoOidMessage;
import org.neodatis.odb.core.server.message.NextClassInfoOidResponseMessage;
import org.neodatis.odb.core.session.Session;

/**
 * @author olivier
 * 
 */
public class ClientOidGeneratorImpl implements ClientOidGenerator {

	protected Session session;
	protected MessageStreamer messageStreamer;
	protected long nextOid;
	protected OidGenerator oidGenerator;

	/**
	 * @param storageEngine
	 */
	public ClientOidGeneratorImpl() {
		// local oid are negative to be sure to avoid collision with real server
		// oids
		this.nextOid = -1;

	}

	public void set(OidGenerator generator, Session session, MessageStreamer streamer) {
		this.oidGenerator = generator;
		this.session = session;
		this.messageStreamer = streamer;
	}

	public ClassOid buildClassOID(byte[] bb) {
		return oidGenerator.buildClassOID(bb);
	}

	public ObjectOid buildObjectOID(byte[] bb) {
		return oidGenerator.buildObjectOID(bb);
	}

	public OID buildStringOid(String s) {
		return oidGenerator.buildStringOid(s);
	}

	public ClassOid getNullClassOid() {
		return oidGenerator.getNullClassOid();
	}

	public ObjectOid getNullObjectOid() {
		return oidGenerator.getNullObjectOid();
	}

	public ExternalOID toExternalOid(ObjectOid oid, DatabaseId databaseId) {
		return oidGenerator.toExternalOid(oid, databaseId);
	}

	public void commit() {
	}

	public ClassOid createClassOid() {
		NextClassInfoOidMessage message = new NextClassInfoOidMessage(session.getBaseIdentification().getBaseId(), session.getId());
		try {
			NextClassInfoOidResponseMessage rmsg = (NextClassInfoOidResponseMessage) messageStreamer.sendAndReceive(message);
			return rmsg.getCoid();
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "getNextClassOid");
		}
	}

	public ObjectOid createObjectOid(ClassOid classOid) {
		return oidGenerator.createObjectOid(classOid);
	}

	public void init(StorageEngine engine, boolean useCache) {
	}

	public ClassOid classOidFromString(String s) {
		return oidGenerator.classOidFromString(s);
	}

	public ObjectOid objectOidFromString(String s) {
		return oidGenerator.objectOidFromString(s);
	}

	public String getSimpleName() {
		return oidGenerator.getSimpleName();
	}

}
