/**
 * 
 */
package org.neodatis.odb.core.event;

import org.neodatis.odb.NeoDatisEventType;

/**
 * @author olivier
 *
 */
public interface EventManager {
	public EventResult fireEvent(NeoDatisEvent event);

	public void addEventListener(NeoDatisEventType neoDatisEventType, NeoDatisEventListener eventListener);

}
