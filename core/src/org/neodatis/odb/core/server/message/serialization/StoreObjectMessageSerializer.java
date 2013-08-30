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
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.odb.core.server.message.StoreObjectMessage;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;

/**
 * @author olivier
 *
 */
public class StoreObjectMessageSerializer extends SerializerAdapter{

	
	public StoreObjectMessageSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		StoreObjectMessage message = new StoreObjectMessage();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		
		int nbOids = bytes.readInt(readSize.get(), readSize, "nboids");
		ObjectOid[] clientIds = new ObjectOid[nbOids];
		for(int i=0;i<nbOids;i++){
			clientIds[i] = bytes.readObjectoid(readSize.get(),readSize,"oid");
			boolean isNew = bytes.readBoolean(readSize.get(), readSize, "isnew");
			clientIds[i].setIsNew(isNew);			
		}
			
		int nbNnois = bytes.readInt(readSize.get(), readSize, "nbnnois");
		IOdbList<OidAndBytes> oabs = new OdbArrayList<OidAndBytes>();
		for(int i=0;i<nbNnois;i++){
			//ObjectOid oid = bytes.readObjectoid(readSize.get(),readSize,"oid");
			int size = bytes.readInt(readSize.get(), readSize, "size");
			Bytes bb = bytes.readBytes(readSize.get(), size, readSize, "bytes");
			OidAndBytes oab = new OidAndBytes(clientIds[i], bb);
			oabs.add(oab);
		}
		message.setOabs(oabs);
		message.setClientIds( clientIds );
		return message;
	}

	public Bytes toBytes(Message message) {
		StoreObjectMessage m = (StoreObjectMessage) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		
		
		// write Client Oids
		int nbIds = m.getClientIds().length;
		position += bytes.writeInt(nbIds, position,"nboids");
		for(int i=0;i<nbIds;i++){
			position += bytes.writeObjectOid(m.getClientIds()[i],position,"oid");
			position += bytes.writeBoolean(m.getClientIds()[i].isNew(),position,"isnew");
		}
		
		IOdbList<OidAndBytes> bytesNnoi = m.getOabs();
		
		position += bytes.writeInt(bytesNnoi.size(), position,"nbnnois");
		for(OidAndBytes oab:bytesNnoi){
			//position += bytes.writeObjectOid((ObjectOid) oab.oid,position,"oid");
			position += bytes.writeInt(oab.bytes.getRealSize(),position,"size");
			position += bytes.writeBytes(oab.bytes,position,"bytes");
		}
		
		return bytes.getBytes();
	}

}
