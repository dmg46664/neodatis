/**
 * 
 */
package org.neodatis.odb.core.layers.layer1;

import org.neodatis.odb.ObjectOid;


/**
 * @author olivier
 * 
 */
public class DefaultInstrumentationCallback implements IntrospectionCallback {

	public DefaultInstrumentationCallback() {
		super();
	}

	public boolean objectFound(Object object, ObjectOid oid) {
		return true;
	}

}
