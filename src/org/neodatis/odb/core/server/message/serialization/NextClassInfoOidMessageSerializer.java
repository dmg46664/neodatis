/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.odb.core.server.message.NextClassInfoOidMessage;

/**
 * @author olivier
 *
 */
public class NextClassInfoOidMessageSerializer extends SerializerAdapter{

	
	public NextClassInfoOidMessageSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		NextClassInfoOidMessage message = new NextClassInfoOidMessage();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		
		return message;
	}

	public Bytes toBytes(Message message) {
		NextClassInfoOidMessage m = (NextClassInfoOidMessage) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		
		return bytes.getBytes();
	}

}
