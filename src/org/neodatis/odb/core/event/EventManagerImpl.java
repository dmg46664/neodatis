/**
 * 
 */
package org.neodatis.odb.core.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neodatis.odb.NeoDatisEventType;

/**
 * @author olivier
 *
 */
public class EventManagerImpl implements EventManager{
	protected Map<NeoDatisEventType, List<NeoDatisEventListener>> eventListeners;

	public EventManagerImpl(){
		eventListeners = new HashMap<NeoDatisEventType, List<NeoDatisEventListener>>();
	}
	
	public void addEventListener(NeoDatisEventType neoDatisEventType, NeoDatisEventListener eventListener){
		List<NeoDatisEventListener> listeners = eventListeners.get(neoDatisEventType);
		if(listeners==null){
			listeners = new ArrayList<NeoDatisEventListener>();
			eventListeners.put(neoDatisEventType, listeners);
		}
		listeners.add(eventListener);
	}
	public EventResult fireEvent(NeoDatisEvent event) {
		NeoDatisEventType type = event.getNeoDatisEventType();
		
		// check if there are some listeners for this type of event
		List<NeoDatisEventListener> l = eventListeners.get(type);
		
		if(l==null || l.isEmpty()){
			return new EventResult(true);
		}
		
		for(NeoDatisEventListener listener:l){
			listener.onEvent(event);
		}
		return new EventResult(true);
	}

}
