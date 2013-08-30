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
import org.neodatis.odb.core.server.message.DeleteObjectMessageResponse;
import org.neodatis.odb.core.server.message.Message;

/**
 * @author olivier
 *
 */
public class DeleteObjectFromOidMessageResponseSerializer extends SerializerAdapter{
	
	
	public DeleteObjectFromOidMessageResponseSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		DeleteObjectMessageResponse message = new DeleteObjectMessageResponse();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		ObjectOid oid = bytes.readObjectoid(readSize.get(), readSize,"oid");
		message.setOid(oid);
		return message;
	}

	public Bytes toBytes(Message message) {
		DeleteObjectMessageResponse m = (DeleteObjectMessageResponse) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		position+= bytes.writeObjectOid(m.getOid(), position, "oid");
		return bytes.getBytes();
	}

}
