/**
 * 
 */
package org.neodatis.odb;

import java.io.Serializable;

/**An interface to contain some info about an object : example, its oid
 * @author olivier
 *
 */
public interface NeoDatisContext extends Serializable{
	void setOid(ObjectOid oid);
	ObjectOid getOid();
	boolean hasChanged();
	void markAsChanged();
	
}
