/**
 * 
 */
package org.neodatis.odb.core.layers.layer2.instance;

/**A value object to context some info to define how to build an instance
 * @author olivier
 *
 */
public class InstanceBuilderContext {
	public boolean useCache;
	public Object existingObject;
	public int depth;
	public InstanceBuilderContext(boolean useCache, Object existingObject, int depth) {
		super();
		this.useCache = useCache;
		this.existingObject = existingObject;
		this.depth = depth;
	}
	public InstanceBuilderContext() {
		super();
		this.useCache = true;
		this.existingObject = null;
		this.depth = 0;
	}
	
	public InstanceBuilderContext(int depth) {
		super();
		this.useCache = true;
		this.existingObject = null;
		this.depth = depth;
	}

}
