/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.query.GenericExecutionPlanImpl;
import org.neodatis.odb.core.query.IQueryExecutionPlan;
import org.neodatis.odb.core.server.message.GetObjectsMessageResponse;
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;

/**
 * @author olivier
 * 
 */
public class GetObjectsMessageResponseSerializer extends SerializerAdapter{

	public GetObjectsMessageResponseSerializer(NeoDatisConfig config) {
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		GetObjectsMessageResponse message = new GetObjectsMessageResponse();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);

		if (message.hasError()) {
			return message;
		}

		boolean isOnlyOid = bytes.readBoolean(readSize.get(), readSize, "onlyoids");
		int nbOidsNnois = bytes.readInt(readSize.get(), readSize, "nb");

		if (isOnlyOid) {
			Collection<ObjectOid> oids = new ArrayList<ObjectOid>();
			for (int i = 0; i < nbOidsNnois; i++) {
				oids.add(bytes.readObjectoid(readSize.get(), readSize, "oid"));
			}
			message.setObjectOids(oids);
		} else {
			Collection<IOdbList<OidAndBytes>> listOfOabs = new ArrayList<IOdbList<OidAndBytes>>();
			for (int i = 0; i < nbOidsNnois; i++) {
				int nbx = bytes.readInt(readSize.get(), readSize, "nbx");
				IOdbList<OidAndBytes> oabs = new OdbArrayList<OidAndBytes>();

				for (int j = 0; j < nbx; j++) {
					ObjectOid oid = bytes.readObjectoid(readSize.get(), readSize, "oid");
					int size = bytes.readInt(readSize.get(), readSize, "size");
					Bytes bb = bytes.readBytes(readSize.get(), size, readSize, "bytes");
					OidAndBytes oab = new OidAndBytes(oid, bb);
					oabs.add(oab);
				}
				listOfOabs.add(oabs);
			}
			message.setListOfOabs(listOfOabs);
		}

		long duration = bytes.readLong(readSize.get(), readSize, "duration");
		String details = bytes.readString(true, readSize.get(), readSize, "details");
		boolean useIndex = bytes.readBoolean(readSize.get(), readSize, "useindex");

		IQueryExecutionPlan plan = new GenericExecutionPlanImpl(useIndex, duration, details);
		message.setQueryExecutionPlan(plan);
		message.setOnlyOids(isOnlyOid);

		return message;
	}

	public Bytes toBytes(Message message) {
		GetObjectsMessageResponse m = (GetObjectsMessageResponse) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);

		if (message.hasError()) {
			return bytes.getBytes();
		}

		position += bytes.writeBoolean(m.isOnlyOids(), position, "onlyoids");

		if (m.isOnlyOids()) {
			int nb = m.getObjectOids().size();
			position += bytes.writeInt(nb, position, "nb");

			for (ObjectOid oid : m.getObjectOids()) {
				position += bytes.writeObjectOid(oid, position, "oid");
			}
		} else {
			Collection<IOdbList<OidAndBytes>> c = m.getListOfOabs();
			int nb = c.size();
			position += bytes.writeInt(nb, position, "nb");
			Iterator<IOdbList<OidAndBytes>> iterator = c.iterator();
			while (iterator.hasNext()) {
				IOdbList<OidAndBytes> oabs = iterator.next();

				position += bytes.writeInt(oabs.size(), position, "nbx");
				for (OidAndBytes oab : oabs) {
					position += bytes.writeObjectOid((ObjectOid) oab.oid, position, "oid");
					position += bytes.writeInt(oab.bytes.getRealSize(), position, "size");
					position += bytes.writeBytes(oab.bytes, position, "bytes");
				}
			}
		}
		// execution plan
		position += bytes.writeLong(m.getPlan().getDuration(), position, "duration");
		position += bytes.writeString(m.getPlan().getDetails(), true, position, "details");
		position += bytes.writeBoolean(m.getPlan().useIndex(), position, "useindex");

		return bytes.getBytes();
	}

}
