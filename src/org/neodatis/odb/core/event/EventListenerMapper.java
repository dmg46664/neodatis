/**
 * 
 */
package org.neodatis.odb.core.event;

import java.util.HashMap;
import java.util.Map;

import org.neodatis.odb.NeoDatisEventType;

/**
 * @author olivier
 *
 */
public class EventListenerMapper {
	public Map<NeoDatisEventType, Class> listenersByType;
	
	public EventListenerMapper(){
		this.listenersByType = new HashMap<NeoDatisEventType, Class>();
		
		//listenersByType.put(NeoDatisEventType.META_MODEL_HAS_CHANGED, )
	}

}
