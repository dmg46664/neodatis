/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.server.message.ConnectMessageResponse;
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;

/**
 * @author olivier
 *
 */
public class ConnectMessageResponseSerializer extends SerializerAdapter{
	
	public ConnectMessageResponseSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		ConnectMessageResponse message = new ConnectMessageResponse();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		
		int nbClasses = bytes.readInt(readSize.get(), readSize, "nbclasses");
		IOdbList<OidAndBytes> oabs = new OdbArrayList<OidAndBytes>();
		for(int i=0;i<nbClasses;i++){
			int size = bytes.readInt(readSize.get(), readSize, "size");
			ClassOid ciOid = bytes.readClassOid(readSize.get(), readSize, "coid");
			Bytes ciBytes = bytes.readBytes(readSize.get(), size, readSize, "bytes");
			OidAndBytes oab = new OidAndBytes(ciOid, ciBytes);
			oabs.add(oab);
		}
		// reads the oid generator class name
		String oidGeneratorClassName = bytes.readString(false, readSize.get(), readSize, "oid generator");
		message.setOidGeneratorClassName(oidGeneratorClassName);
		message.setOabsOfMetaModel(oabs);
		
		return message;
	}

	public Bytes toBytes(Message message) {
		ConnectMessageResponse m = (ConnectMessageResponse) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(), getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		
		IOdbList<OidAndBytes> oabs  = m.getOabsOfMetaModel();
		position += bytes.writeInt(oabs.size(), position,"nbclasses");
		
		for(OidAndBytes oab: oabs){
			position += bytes.writeInt(oab.bytes.getRealSize() , position,"size");
			position += bytes.writeClassOid((ClassOid) oab.oid , position,"coid");
			position += bytes.writeBytes(oab.bytes , position,"bytes");
		}
		// write the oid generator class name
		position += bytes.writeString(m.getOidGeneratorClassName(), false, position,"oid generator");
		return bytes.getBytes();
	}

}
