/**
 * 
 */
package org.neodatis.odb.core.server;

import java.io.Serializable;

import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;

/**
 * @author olivier
 *
 */
public interface ReturnValue extends Serializable {
	/** This is called by the server side module to inform the oid of a specific NonNativeObject
	 * <pre>
	 * Return value are used by insert triggers to inform the client side when values are changed by the server. As the trigger is called before storing the object,
	 * the non native object info, that represent the object to be stored doesn't have OID yet. When the NNOI is stored, the server sets its oid. And call  this method 
	 * to inform the ReturnValue oids of the objects that are being stored. So the return value can manage its way to retrieve the right oid! 
	 * 
	 * </pre>
	 * @param nnoi
	 * @param oid
	 */
	public void setObjectOid(NonNativeObjectInfo nnoi, ObjectOid oid);

	/**
	 * @return The OID of the object being changed. Depending of the return type, it can be null
	 */
	public ObjectOid getObjectOid();

}
