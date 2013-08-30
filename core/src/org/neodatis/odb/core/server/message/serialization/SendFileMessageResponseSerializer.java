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
public class SendFileMessageResponseSerializer extends SerializerAdapter{

	public SendFileMessageResponseSerializer(NeoDatisConfig config) {
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		SendFileMessageResponse message = new SendFileMessageResponse();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);

		boolean fileExist = bytes.readBoolean(readSize.get(), readSize, "file exist");
		long fileSize = bytes.readLong(readSize.get(), readSize, "file size");

		message.setFileExist(fileExist);
		message.setFileSize(fileSize);

		return message;
	}

	public Bytes toBytes(Message message) {
		SendFileMessageResponse m = (SendFileMessageResponse) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(), getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);

		position += bytes.writeBoolean(m.fileExist(), position, "file exist");
		position += bytes.writeLong(m.getFileSize(), position, "file size");

		return bytes.getBytes();
	}
	

}
