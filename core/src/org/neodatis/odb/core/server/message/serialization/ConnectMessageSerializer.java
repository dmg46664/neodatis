/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.server.message.ConnectMessage;
import org.neodatis.odb.core.server.message.Message;

/**
 * @author olivier
 *
 */
public class ConnectMessageSerializer extends SerializerAdapter{
	public ConnectMessageSerializer(NeoDatisConfig config) {
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		ConnectMessage message = new ConnectMessage();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		
		message.setIp(bytes.readString(false, readSize.get(), readSize, "baseid"));
		message.setUser(bytes.readString(false, readSize.get(), readSize, "baseid"));
		message.setPassword(bytes.readString(false, readSize.get(), readSize, "baseid"));
		message.setTransactional(bytes.readBoolean(readSize.get(), readSize, "transactional"));
		message.setUserInfo(bytes.readString(false, readSize.get(), readSize, "user info"));
		return message;
	}

	public Bytes toBytes(Message message) {
		ConnectMessage m = (ConnectMessage) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(), getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		position += bytes.writeString(m.getIp(), false, position,"ip");
		position += bytes.writeString(m.getUser()==null?"":m.getUser(), false, position,"user");
		position += bytes.writeString(m.getPassword()==null?"":m.getPassword(), false, position,"password");
		position += bytes.writeBoolean(m.isTransactional(), position,"transactional");
		position += bytes.writeString(m.getUserInfo()==null?"empty":m.getUserInfo(), false, position,"user info");
		return bytes.getBytes();
	}

}
