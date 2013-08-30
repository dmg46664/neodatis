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
import org.neodatis.odb.core.server.message.DeleteObjectMessage;
import org.neodatis.odb.core.server.message.Message;

/**
 * @author olivier
 *
 */
public class DeleteObjectWithOidMessageSerializer extends SerializerAdapter{

	
	public DeleteObjectWithOidMessageSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		DeleteObjectMessage message = new DeleteObjectMessage();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		
		ObjectOid oid = bytes.readObjectoid(readSize.get(), readSize,"oid");
		boolean cascade = bytes.readBoolean(readSize.get(), readSize, "cascade");
		message.setObjectOid(oid);
		message.setCascade(cascade);
		return message;
	}

	public Bytes toBytes(Message message) {
		DeleteObjectMessage m = (DeleteObjectMessage) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(), getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		position+= bytes.writeObjectOid(m.getOid(), position, "oid");
		position+= bytes.writeBoolean(m.isCascade(), position, "cascade");
		
		return bytes.getBytes();
	}

}
