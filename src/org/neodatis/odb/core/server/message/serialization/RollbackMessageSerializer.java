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
import org.neodatis.odb.core.server.message.RollbackMessage;

/**
 * @author olivier
 *
 */
public class RollbackMessageSerializer extends SerializerAdapter{

	
	public RollbackMessageSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		RollbackMessage message = new RollbackMessage();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		
		return message;
	}

	public Bytes toBytes(Message message) {
		RollbackMessage m = (RollbackMessage) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		
		return bytes.getBytes();
	}

}
