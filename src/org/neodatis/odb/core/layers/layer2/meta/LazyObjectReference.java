/**
 * 
 */
package org.neodatis.odb.core.layers.layer2.meta;

import org.neodatis.odb.ObjectOid;

/**
 * @author olivier
 *
 */
public class LazyObjectReference extends ObjectReference {

	public LazyObjectReference(ObjectOid id, ClassInfo ci) {
		super(id);
		setClassInfo(ci);
	}

}
