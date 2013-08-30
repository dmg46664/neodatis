/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.server.message.AddIndexMessageResponse;
import org.neodatis.odb.core.server.message.Message;

/**
 * @author olivier
 *
 */
public class AddIndexMessageResponseSerializer extends SerializerAdapter{

	
	public AddIndexMessageResponseSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		AddIndexMessageResponse message = new AddIndexMessageResponse();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		return message;
		
	}
	public Bytes toBytes(Message message) {
		AddIndexMessageResponse m = (AddIndexMessageResponse) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		
		return bytes.getBytes();
	}

}
