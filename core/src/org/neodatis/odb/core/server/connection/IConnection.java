/**
 * 
 */
package org.neodatis.odb.core.server.connection;

import org.neodatis.odb.OID;
import org.neodatis.odb.core.session.SessionEngine;

/**
 * @author olivier
 *
 */
public interface IConnection {

	String getId();

	SessionEngine getSessionEngine();

	void close() throws Exception;

	void commit() throws Exception;

	void unlockObjectWithOid(OID oid) throws Exception;

	void rollback() throws Exception;

	boolean lockObjectWithOid(OID oid) throws InterruptedException;
	boolean lockClass(String fullClassName) throws InterruptedException;

	void setCurrentAction(int action);

	void endCurrentAction();

	String getDescription();

}