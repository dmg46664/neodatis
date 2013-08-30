/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.odb.core.server.message.NextClassInfoOidResponseMessage;

/**
 * @author olivier
 *
 */
public class NextClassInfoOidResponseMessageSerializer extends SerializerAdapter{

	
	public NextClassInfoOidResponseMessageSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		NextClassInfoOidResponseMessage message = new NextClassInfoOidResponseMessage();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		
		ClassOid coid = bytes.readClassOid(readSize.get());
		message.setCoid(coid);
		
		return message;
	}

	public Bytes toBytes(Message message) {
		NextClassInfoOidResponseMessage m = (NextClassInfoOidResponseMessage) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		position += bytes.writeClassOid(m.getCoid(), position, "coid");
		
		return bytes.getBytes();
	}

}
