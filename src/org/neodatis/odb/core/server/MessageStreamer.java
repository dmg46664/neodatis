/**
 * 
 */
package org.neodatis.odb.core.server;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.core.server.message.Message;



/**
 * @author olivier
 *
 */
public interface MessageStreamer {

	
	void init(String host, int port, String name, NeoDatisConfig config);
	void close();

	void write(Message message) throws Exception;

	Message read() throws Exception ;

	/**
	 * @param msg
	 * @return
	 */
	Message sendAndReceive(Message msg) throws Exception;

}