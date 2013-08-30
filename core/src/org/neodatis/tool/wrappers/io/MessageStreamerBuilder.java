package org.neodatis.tool.wrappers.io;

import java.lang.reflect.Constructor;
import java.net.Socket;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.server.MessageStreamer;

/**
 * @sharpen.ignore
 * @author olivier
 *
 */
public class MessageStreamerBuilder {
	
	/** (non-Javadoc)
	 * @see org.neodatis.odb.core.ICoreProvider#getMessageStreamer(java.net.Socket)
	 * 
	 */
	public static MessageStreamer getMessageStreamer(Socket socket, NeoDatisConfig config) {
		Class clazz = null;
		try{
			Class messageStreamerClass = config.getMessageStreamerClass();
			Constructor c = messageStreamerClass.getDeclaredConstructor(Socket.class, NeoDatisConfig.class);
			
			MessageStreamer messageStreamer = (MessageStreamer) c.newInstance(socket,config);
			return messageStreamer;
		}catch (Exception e) {
			String streamerClassName = "<null>";
			if(clazz!=null){
				streamerClassName = clazz.getName();
			}
			throw new NeoDatisRuntimeException(NeoDatisError.ERROR_WHILE_CREATING_MESSAGE_STREAMER.addParameter(streamerClassName),e);
		}
	}
	/**
	 * 
	 */
	public static MessageStreamer getMessageStreamer(String host, int port, String name, NeoDatisConfig config) {
		Class clazz = null;
		try{
			Constructor c = config.getMessageStreamerClass().getDeclaredConstructor();
			
			MessageStreamer messageStreamer = (MessageStreamer) c.newInstance();
			messageStreamer.init(host, port, name, config);
			
			return messageStreamer;
		}catch (Exception e) {
			String streamerClassName = "<null>";
			if(clazz!=null){
				streamerClassName = clazz.getName();
			}
			throw new NeoDatisRuntimeException(NeoDatisError.ERROR_WHILE_CREATING_MESSAGE_STREAMER.addParameter(name),e);
		}
	}
	

}
