/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.server.message.GetObjectFromOidMessage;
import org.neodatis.odb.core.server.message.Message;

/**
 * @author olivier
 *
 */
public class GetObjectFromOidMessageSerializer extends SerializerAdapter{

	
	public GetObjectFromOidMessageSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		GetObjectFromOidMessage message = new GetObjectFromOidMessage();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		
		ObjectOid oid = bytes.readObjectoid(readSize.get());
		message.setObjectOid(oid);
		return message;
	}

	public Bytes toBytes(Message message) {
		GetObjectFromOidMessage m = (GetObjectFromOidMessage) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		position+= bytes.writeObjectOid(m.getOid(), position, "oid");
		
		return bytes.getBytes();
	}

}
