/**
 * 
 */
package org.neodatis.odb.core.context;

import org.neodatis.odb.NeoDatisContext;
import org.neodatis.odb.NeoDatisObject;

/**
 * @author olivier
 *
 */
public class NeoDatisObjectAdapter implements NeoDatisObject {

	protected transient NeoDatisContext neoDatisContext;
	
	public NeoDatisContext getNeoDatisContext() {
		return neoDatisContext;
	}

	public void setNeoDatisContext(NeoDatisContext context) {
		this.neoDatisContext = context;
	}

}
