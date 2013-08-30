/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.server.message.GetFileMessage;
import org.neodatis.odb.core.server.message.SendFileMessage;
import org.neodatis.odb.core.server.message.SendFileMessageResponse;
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.odb.core.server.message.StoreObjectMessage;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;

/**
 * @author olivier
 *
 */
public class GetFileMessageSerializer extends SerializerAdapter{

	public GetFileMessageSerializer(NeoDatisConfig config) {
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		GetFileMessage message = new GetFileMessage();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);

		boolean serverInbox = bytes.readBoolean(readSize.get(), readSize, "s inbox");
		String remoteFileName = bytes.readString(false, readSize.get(), readSize, "remote file name");
		boolean clientInbox = bytes.readBoolean(readSize.get(), readSize, "c inbox");
		String localFileName = bytes.readString(false, readSize.get(), readSize, "local file name");

		message.setRemoteFileName(remoteFileName);
		message.setGetFileInServerInbox(serverInbox);
		message.setLocalFileName(localFileName);
		message.setPutFileInClientInbox(clientInbox);

		return message;
	}

	public Bytes toBytes(Message message) {
		GetFileMessage m = (GetFileMessage) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(), getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);

		position += bytes.writeBoolean(m.isGetFileInServerInbox(),position, "s inbox");
		position += bytes.writeString(m.getRemoteFileName(),false, position, "remote file name");
		position += bytes.writeBoolean(m.isPutFileInClientInbox(),position, "c inbox");
		position += bytes.writeString(m.getLocalFileName(),false, position, "local file name");

		return bytes.getBytes();
	}
	

}
