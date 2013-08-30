/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.query.InternalQuery;
import org.neodatis.odb.core.server.ReturnValue;
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.odb.core.server.message.StoreObjectMessageResponse;

/**
 * @author olivier
 * 
 */
public class StoreObjectMessageResponseSerializer extends SerializerAdapter {

	public StoreObjectMessageResponseSerializer(NeoDatisConfig config) {
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		StoreObjectMessageResponse message = new StoreObjectMessageResponse();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);

		ObjectOid oid = bytes.readObjectoid(readSize.get(), readSize, "oid");

		int nbServerOids = bytes.readInt(readSize.get(), readSize, "nboids");
		ObjectOid[] serverIds = new ObjectOid[nbServerOids];
		for (int i = 0; i < nbServerOids; i++) {
			serverIds[i] = bytes.readObjectoid(readSize.get(), readSize, "oid");
		}

		int nbReturnValues = bytes.readInt(readSize.get(), readSize, "nbReturnValues");

		List<ReturnValue> rvs = new ArrayList<ReturnValue>();

		for (int i = 0; i < nbReturnValues; i++) {
			int size = bytes.readInt(readSize.get(), readSize, "size rv");

			byte[] bb = bytes.getBytes().extract(readSize.get(), size);
			readSize.add(bb.length);
			ByteArrayInputStream bais = new ByteArrayInputStream(bb);
			ObjectInputStream ois = new ObjectInputStream(bais);
			ReturnValue rv = (ReturnValue) ois.readObject();
			rvs.add(rv);
		}
		message.setReturnValues(rvs);
		message.setServerIds(serverIds);
		message.setOid(oid);
		return message;
	}

	public Bytes toBytes(Message message) {
		try {
			StoreObjectMessageResponse m = (StoreObjectMessageResponse) message;

			BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(), getConfig());
			int position = HeaderSerializer.toBytes(bytes, message);

			position += bytes.writeObjectOid(m.getOid(), position, "oid");

			// write Server Oids
			int nbServerIds = m.getServerIds().length;
			position += bytes.writeInt(nbServerIds, position, "nboids");
			for (int i = 0; i < nbServerIds; i++) {
				position += bytes.writeObjectOid(m.getServerIds()[i], position, "oid");
			}

			int nbReturnValues = 0;

			if (m.getReturnValues() != null) {
				nbReturnValues = m.getReturnValues().size();
			}

			position += bytes.writeInt(nbReturnValues, position, "nbReturnValues");

			if (nbReturnValues != 0) {
				for (ReturnValue rv : m.getReturnValues()) {
					// we use serialization
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(baos);
					oos.writeObject(rv);
					byte[] bb = baos.toByteArray();
					position += bytes.writeInt(bb.length, position, "size rv");
					position += bytes.getBytes().append(bb);
				}
			}

			return bytes.getBytes();
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "Serializing StoreObjectResponse message");
		}

	}

}
