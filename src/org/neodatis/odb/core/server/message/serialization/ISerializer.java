package org.neodatis.odb.core.server.message.serialization;

import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.server.message.Message;

public interface ISerializer {
	public Bytes toBytes(Message message) throws Exception;

	public Message fromBytes(BytesHelper bytes) throws Exception ;

}
