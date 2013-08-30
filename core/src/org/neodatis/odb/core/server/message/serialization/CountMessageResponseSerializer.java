/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import java.math.BigInteger;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.server.message.CountMessageResponse;
import org.neodatis.odb.core.server.message.Message;

/**
 * @author olivier
 *
 */
public class CountMessageResponseSerializer extends SerializerAdapter{
	
	
	public CountMessageResponseSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		CountMessageResponse message = new CountMessageResponse();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		BigInteger bi = bytes.readBigInteger(readSize.get(), readSize,"nb");
		message.setNbObjects(bi);
		return message;
	}

	public Bytes toBytes(Message message) {
		CountMessageResponse m = (CountMessageResponse) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		position+= bytes.writeBigInteger(m.getNbObjects(), position, "nb");
		return bytes.getBytes();
	}

}
