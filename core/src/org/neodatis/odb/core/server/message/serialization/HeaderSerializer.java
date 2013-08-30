/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.server.message.Message;

/**
 * @author olivier
 * 
 */
public class HeaderSerializer {
	public static int toBytes(BytesHelper bytes, Message message) {
		int position = bytes.writeInt(message.getMessageType(), 0, "msg type");
		position += bytes.writeString(message.getBaseIdentifier(), false, position, "baseid");
		position += bytes.writeString(message.getSessionId(), false, position, "sessionid");
		position += bytes.writeLong(message.getDateTime(), position, "datetime");
		position += bytes.writeString(message.getError()==null?"":message.getError(),true, position, "error");
		return position;
	}

	/**
	 * @param bytesHelper
	 * @param readSize
	 */
	public static void fill(Message message, BytesHelper bytesHelper, ReadSize readSize) {
		int messageType = bytesHelper.readInt(readSize.get(), readSize, "msg type");
		String baseIdentifier = bytesHelper.readString(false, readSize.get(), readSize, "baseid");
		String sessionId = bytesHelper.readString(false, readSize.get(), readSize, "sessionid");
		long dateTime = bytesHelper.readLong(readSize.get(), readSize, "datetime");
		String error = bytesHelper.readString(true,readSize.get(), readSize, "error");
		
		message.setMessageType(messageType);
		message.setBaseIdentifier(baseIdentifier);
		message.setSessionId(sessionId);
		message.setDateTime(dateTime);
		message.setError(error);
		
	}
}
