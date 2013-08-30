/**
 * 
 */
package org.neodatis.odb.core.context;

import org.neodatis.odb.NeoDatisObject;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;

/** A class to manage all object reconnection work:
 * 
 * When possible, it keep some neodatis info in the object and retrieve them to try to reconnect objects
 * @author olivier
 *
 */
public class ObjectReconnector {
	public boolean tryToReconnect(Object object, NonNativeObjectInfo nnoi){
		// check if we can reconnect the object
		if (object != null && object instanceof NeoDatisObject) {
			NeoDatisObject no = (NeoDatisObject) object;
			if (no.getNeoDatisContext() != null) {
				ObjectOid ooid = no.getNeoDatisContext().getOid();
				if (ooid != null) {
					nnoi.setOid(ooid);
					// TODO:reload the header of the object?,yes for cs
					// mode, no for local mode
					return true;
				}
			}
		}
		return false;
	}
	
	public void tryToAttachNeoDatisContext(Object object, ObjectOid oid){
		// check it we can store info in the object to enable reconnection
		if(object!=null && object instanceof NeoDatisObject){
			NeoDatisObject no = (NeoDatisObject) object;
			no.setNeoDatisContext(new NeoDatisContextImpl(oid));
		}
	}

	/**
	 * @param o
	 */
	public ObjectOid tryToGetOid(Object object) {
		if(object!=null && object instanceof NeoDatisObject){
			NeoDatisObject no = (NeoDatisObject) object;
			return no.getNeoDatisContext().getOid();
		}
		return null;
		
	}
}
