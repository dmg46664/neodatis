/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.server.message.GetObjectFromIdMessageResponse;
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;

/**
 * @author olivier
 *
 */
public class GetObjectFromOidMessageResponseSerializer extends SerializerAdapter{
	
	
	public GetObjectFromOidMessageResponseSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		GetObjectFromIdMessageResponse message = new GetObjectFromIdMessageResponse();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		
		boolean isNull = bytes.readBoolean(readSize.get(), readSize, "isnull");
		
		if(isNull){
			return message;
		}
		
		int nbNnois = bytes.readInt(readSize.get(), readSize, "nbnnois");
		IOdbList<OidAndBytes> oabs = new OdbArrayList<OidAndBytes>();
		for(int i=0;i<nbNnois;i++){
			ObjectOid oid = bytes.readObjectoid(readSize.get(),readSize,"oid");
			int size = bytes.readInt(readSize.get(), readSize, "size");
			Bytes bb = bytes.readBytes(readSize.get(), size, readSize, "bytes");
			OidAndBytes oab = new OidAndBytes(oid, bb);
			oabs.add(oab);
		}
		
		message.setOabs(oabs);
		return message;
	}

	public Bytes toBytes(Message message) {
		GetObjectFromIdMessageResponse m = (GetObjectFromIdMessageResponse) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(), getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);

		position += bytes.writeBoolean(m.getOabs()==null,position,"isnull");
		
		if(m.getOabs()==null){
			return bytes.getBytes();
		}
		
		position += bytes.writeInt(m.getOabs().size(), position,"nbnnois");
		for(OidAndBytes oab:m.getOabs()){
			position += bytes.writeObjectOid((ObjectOid) oab.oid,position,"oid");
			position += bytes.writeInt(oab.bytes.getRealSize(),position,"size");
			position += bytes.writeBytes(oab.bytes,position,"bytes");
		}
		
		return bytes.getBytes();
	}

}
