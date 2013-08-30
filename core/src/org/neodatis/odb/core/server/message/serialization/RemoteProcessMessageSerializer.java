/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.query.InternalQuery;
import org.neodatis.odb.core.server.message.SendFileMessage;
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.odb.core.server.message.process.RemoteProcess;
import org.neodatis.odb.core.server.message.process.RemoteProcessMessage;

/**
 * @author olivier
 * 
 */
public class RemoteProcessMessageSerializer extends SerializerAdapter{

	public RemoteProcessMessageSerializer(NeoDatisConfig config) {
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		RemoteProcessMessage message = new RemoteProcessMessage();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);

		boolean isSync = bytes.readBoolean(readSize.get(), readSize,
				"sync");

		int size = bytes.readInt(readSize.get(), readSize, "size");
		
		byte[] bb = bytes.getBytes().extract(readSize.get(),size);
		ByteArrayInputStream bais = new ByteArrayInputStream(bb);
		ObjectInputStream ois = new ObjectInputStream(bais);
		RemoteProcess process = (RemoteProcess) ois.readObject();
		message.setProcess(process);
		message.setSynchronous(isSync);

		return message;
	}

	public Bytes toBytes(Message message) throws IOException {
		RemoteProcessMessage m = (RemoteProcessMessage) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);

		position += bytes.writeBoolean(m.isSynchronous(), position,"sync");

		
		// we use serialization
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(m.getProcess());
		byte[] bb = baos.toByteArray();
		position += bytes.writeInt(bb.length, position, "size");
		bytes.getBytes().append(bb);
		
		return bytes.getBytes();

	}
}
