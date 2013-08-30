/**
 * 
 */
package org.neodatis.odb.core.event;

import org.neodatis.odb.NeoDatisEventType;

/**
 * @author olivier
 *
 */
public abstract class NeoDatisEvent {
	protected NeoDatisEventType neoDatisEventType;

	public NeoDatisEvent(NeoDatisEventType neoDatisEventType) {
		super();
		this.neoDatisEventType = neoDatisEventType;
	}

	public NeoDatisEventType getNeoDatisEventType() {
		return neoDatisEventType;
	}

	
}
