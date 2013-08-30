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
import org.neodatis.odb.core.server.message.RollbackMessageResponse;

/**
 * @author olivier
 *
 */
public class RollbackMessageResponseSerializer extends SerializerAdapter{

	
	public RollbackMessageResponseSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		RollbackMessageResponse message = new RollbackMessageResponse();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		
		boolean ok = bytes.readBoolean(readSize.get());
		message.setOk(ok);
		
		return message;
	}

	public Bytes toBytes(Message message) {
		RollbackMessageResponse m = (RollbackMessageResponse) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		position += bytes.writeBoolean(m.isOk(), position, "ok");
		
		return bytes.getBytes();
	}

}
