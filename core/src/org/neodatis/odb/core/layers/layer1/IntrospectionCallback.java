/**
 * 
 */
package org.neodatis.odb.core.layers.layer1;

import org.neodatis.odb.ObjectOid;

/**
 * @author olivier
 * A simple callback used by the introspection API to inform when object are found
 *
 */
public interface IntrospectionCallback {
	/** Called when the introspector find a non native object.
	 * 
	 * @param object
	 * @param objectOid 
	 * @return true to continue going recursively, false do not go deeper
	 */
	boolean objectFound(Object object, ObjectOid objectOid);
}
