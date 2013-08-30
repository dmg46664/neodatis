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
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.odb.core.server.message.StoreClassInfoMessage;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;

/**
 * @author olivier
 *
 */
public class StoreClassInfoMessageSerializer extends SerializerAdapter{

	
	public StoreClassInfoMessageSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		StoreClassInfoMessage message = new StoreClassInfoMessage();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		
		int nbNnois = bytes.readInt(readSize.get(), readSize, "nbnnois");
		IOdbList<OidAndBytes> oabs = new OdbArrayList<OidAndBytes>();
		for(int i=0;i<nbNnois;i++){
			ClassOid oid = bytes.readClassOid(readSize.get(),readSize,"oid");
			int size = bytes.readInt(readSize.get(), readSize, "size");
			Bytes bb = bytes.readBytes(readSize.get(), size, readSize, "bytes");
			OidAndBytes oab = new OidAndBytes(oid, bb);
			oabs.add(oab);
		}
		message.setOabs(oabs);
		return message;
	}

	public Bytes toBytes(Message message) {
		StoreClassInfoMessage m = (StoreClassInfoMessage) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		
		
		IOdbList<OidAndBytes> bytesNnoi = m.getOabs();
		
		position += bytes.writeInt(bytesNnoi.size(), position,"nbnnois");
		for(OidAndBytes oab:bytesNnoi){
			position += bytes.writeClassOid( (ClassOid) oab.oid,position,"oid");
			position += bytes.writeInt(oab.bytes.getRealSize(),position,"size");
			position += bytes.writeBytes(oab.bytes,position,"bytes");
		}
		
		return bytes.getBytes();
	}

}
