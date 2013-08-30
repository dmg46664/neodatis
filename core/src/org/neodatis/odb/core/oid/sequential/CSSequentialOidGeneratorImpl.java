/**
 * 
 */
package org.neodatis.odb.core.oid.sequential;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.ObjectOid;

/**
 * Clietn Server Sequential OID generator
 * 
 * @author olivier
 * 
 */
public class CSSequentialOidGeneratorImpl extends SequentialOidGeneratorImpl {

	protected long nextOid;

	public CSSequentialOidGeneratorImpl() {
		super();
		// local oid are negative to be sure to avoid collision with real server
		// oids
		this.nextOid = -1;
	}

	public synchronized ObjectOid getNextObjectOid(ClassOid classOid) {
		return new ObjectOidImpl(classOid, nextOid--);
	}

	public void commit() {
		// nothing to do on client side
	}

}
