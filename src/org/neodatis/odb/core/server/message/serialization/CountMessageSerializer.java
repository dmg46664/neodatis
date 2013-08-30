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
import org.neodatis.odb.core.query.criteria.CriteriaQuery;
import org.neodatis.odb.core.server.message.CountMessage;
import org.neodatis.odb.core.server.message.Message;

/**
 * @author olivier
 *
 */
public class CountMessageSerializer extends SerializerAdapter{

	
	public CountMessageSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		try{
			CountMessage message = new CountMessage();
			ReadSize readSize = new ReadSize();
			HeaderSerializer.fill(message, bytes, readSize);
			
			
			int size = bytes.readInt(readSize.get(), readSize, "size");
			
			byte[] bb = bytes.getBytes().extract(readSize.get(),size);
			ByteArrayInputStream bais = new ByteArrayInputStream(bb);
			ObjectInputStream ois = new ObjectInputStream(bais);
			CriteriaQuery q = (CriteriaQuery) ois.readObject();
			message.setCriteriaQuery(q);
			
			return message;
			
		}catch (Exception e) {
			throw new NeoDatisRuntimeException(e,"DeSerializing Count message");
		}
	}

	public Bytes toBytes(Message message) {
		try{
			CountMessage m = (CountMessage) message;

			BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(), getConfig());
			int position = HeaderSerializer.toBytes(bytes, message);
			
			// we use serialization
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(m.getCriteriaQuery());
			byte[] bb = baos.toByteArray();
			position += bytes.writeInt(bb.length, position, "size");
			bytes.getBytes().append(bb);
			
			return bytes.getBytes();
		}catch (Exception e) {
			throw new NeoDatisRuntimeException(e,"Serializing Count message");
		}
	}

}
