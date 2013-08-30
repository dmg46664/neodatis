/**
 * 
 */
package org.neodatis.odb.core.event;

/**
 * @author olivier
 *
 */
public interface NeoDatisEventListener {

	EventResult onEvent(NeoDatisEvent event);
}
