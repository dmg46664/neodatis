/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.server.message.AddIndexMessage;
import org.neodatis.odb.core.server.message.Message;

/**
 * @author olivier
 *
 */
public class AddIndexMessageSerializer extends SerializerAdapter{

	
	public AddIndexMessageSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		AddIndexMessage message = new AddIndexMessage();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		
		
		String className = bytes.readString(true, readSize.get(), readSize, "classname");
		String indexName = bytes.readString(true, readSize.get(), readSize, "indexname");
		boolean acceptMultiple = bytes.readBoolean(readSize.get(), readSize, "ismultiple");
		boolean verbose = bytes.readBoolean(readSize.get(), readSize, "verbose");
		int nbFields = bytes.readInt(readSize.get(), readSize, "nbfields");
		String [] fields = new String[nbFields];
		for(int i=0;i<nbFields;i++){
			fields[i] = bytes.readString(false, readSize.get(), readSize, "fieldname");
		}
		message.setClassName(className);
		message.setIndexName(indexName);
		message.setAcceptMultipleValue(acceptMultiple);
		message.setVerbose(verbose);
		message.setFieldNames(fields);
		return message;
	}

	public Bytes toBytes(Message message) {
		AddIndexMessage m = (AddIndexMessage) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		
		position += bytes.writeString(m.getClassName(), true, position,"classname");
		position += bytes.writeString(m.getIndexName(), true, position,"indexname");
		position += bytes.writeBoolean(m.acceptMultipleValuesForSameKey(), position,"ismultiple");
		position += bytes.writeBoolean(m.isVerbose(), position,"verbose");
		position += bytes.writeInt(m.getIndexFieldNames().length,position,"nbfields");
		
		for(int i=0;i<m.getIndexFieldNames().length;i++){
			position += bytes.writeString(m.getIndexFieldNames()[i], false, position,"fieldname");
		}
		
		return bytes.getBytes();
	}

}
