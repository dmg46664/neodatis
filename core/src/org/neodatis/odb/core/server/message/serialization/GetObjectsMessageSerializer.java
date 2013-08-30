/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.query.InternalQuery;
import org.neodatis.odb.core.server.message.GetObjectsMessage;
import org.neodatis.odb.core.server.message.Message;

/**
 * @author olivier
 *
 */
public class GetObjectsMessageSerializer extends SerializerAdapter{

	
	public GetObjectsMessageSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		try{
			GetObjectsMessage message = new GetObjectsMessage();
			ReadSize readSize = new ReadSize();
			HeaderSerializer.fill(message, bytes, readSize);
			
			
			int startIndex = bytes.readInt(readSize.get(), readSize, "start");
			int endIndex = bytes.readInt(readSize.get(), readSize, "end");
			boolean  inMemory = bytes.readBoolean(readSize.get(), readSize, "inmemory");
			int size = bytes.readInt(readSize.get(), readSize, "size");
			
			byte[] bb = bytes.getBytes().extract(readSize.get(),size);
			ByteArrayInputStream bais = new ByteArrayInputStream(bb);
			ObjectInputStream ois = new ObjectInputStream(bais);
			InternalQuery q = (InternalQuery) ois.readObject();
			message.setQuery(q);
			
			
			message.setStartIndex(startIndex);
			message.setEndIndex(endIndex);
			message.setInMemory(inMemory);
			return message;
			
		}catch (Exception e) {
			throw new NeoDatisRuntimeException(e,"DeSerializing GetObjects message");
		}
	}

	public Bytes toBytes(Message message) {
		try{
			GetObjectsMessage m = (GetObjectsMessage) message;

			BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
			int position = HeaderSerializer.toBytes(bytes, message);
			
			position += bytes.writeInt(m.getStartIndex(), position, "start");
			position += bytes.writeInt(m.getEndIndex(), position, "end");
			position += bytes.writeBoolean(m.isInMemory(), position, "inmemory");
			
			
			
			// we use serialization
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(m.getQuery());
			byte[] bb = baos.toByteArray();
			position += bytes.writeInt(bb.length, position, "size");
			bytes.getBytes().append(bb);
			
			return bytes.getBytes();
		}catch (Exception e) {
			throw new NeoDatisRuntimeException(e,"Serializing GetObjects message");
		}
	}

}
