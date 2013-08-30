/**
 * 
 */
package org.neodatis.odb.core.server;


/**
 * @author olivier
 *
 */
public interface ReturnValueProcessor {
	void process(ReturnValue rv, Object object) throws Exception;
}
