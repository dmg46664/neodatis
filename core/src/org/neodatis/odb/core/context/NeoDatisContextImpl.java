/**
 * 
 */
package org.neodatis.odb.core.context;

import org.neodatis.odb.NeoDatisContext;
import org.neodatis.odb.ObjectOid;

/**
 * @author olivier
 *
 */
public class NeoDatisContextImpl implements NeoDatisContext {
	protected ObjectOid oid;
	protected boolean hasChanged;

	public NeoDatisContextImpl(){
	}
	public NeoDatisContextImpl(ObjectOid oid){
		this.oid = oid;
	}
	public ObjectOid getOid() {
		return oid;
	}

	public void setOid(ObjectOid oid) {
		this.oid = oid;
	}
	public boolean hasChanged() {
		return hasChanged;
	}
	public void markAsChanged() {
		hasChanged = true;
	}

}
