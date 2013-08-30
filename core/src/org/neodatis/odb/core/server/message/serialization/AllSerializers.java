package org.neodatis.odb.core.server.message.serialization;

import java.util.Map;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisGlobalConfig;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.odb.core.server.message.MessageType;
import org.neodatis.tool.wrappers.map.OdbHashMap;

public class AllSerializers {
	private Map<Integer, ISerializer> serializers = null;
	private NeoDatisConfig config;

	public static synchronized AllSerializers getInstance(NeoDatisConfig config) {
		return new AllSerializers(config);
	}

	private AllSerializers(NeoDatisConfig config) {
		this.config = config;
		serializers = new OdbHashMap<Integer, ISerializer>();
		serializers.put(new Integer(MessageType.CONNECT), new ConnectMessageSerializer(config));
		serializers.put(new Integer(MessageType.CONNECT_RESPONSE), new ConnectMessageResponseSerializer(config));
		serializers.put(new Integer(MessageType.STORE_OBJECT), new StoreObjectMessageSerializer(config));
		serializers.put(new Integer(MessageType.STORE_OBJECT_RESPONSE), new StoreObjectMessageResponseSerializer(config));
		serializers.put(new Integer(MessageType.NEXT_CLASS_INFO_OID), new NextClassInfoOidMessageSerializer(config));
		serializers.put(new Integer(MessageType.NEXT_CLASS_INFO_OID_RESPONSE), new NextClassInfoOidResponseMessageSerializer(config));
		serializers.put(new Integer(MessageType.STORE_CLASS_INFO), new StoreClassInfoMessageSerializer(config));
		serializers.put(new Integer(MessageType.STORE_CLASS_INFO_RESPONSE), new StoreClassInfoMessageResponseSerializer(config));
		serializers.put(new Integer(MessageType.COMMIT), new CommitMessageSerializer(config));
		serializers.put(new Integer(MessageType.COMMIT_RESPONSE), new CommitMessageResponseSerializer(config));
		serializers.put(new Integer(MessageType.ROLLBACK), new RollbackMessageSerializer(config));
		serializers.put(new Integer(MessageType.ROLLBACK_RESPONSE), new RollbackMessageResponseSerializer(config));
		serializers.put(new Integer(MessageType.CLOSE), new CloseMessageSerializer(config));
		serializers.put(new Integer(MessageType.CLOSE_RESPONSE), new CloseMessageResponseSerializer(config));
		serializers.put(new Integer(MessageType.GET_OBJECTS), new GetObjectsMessageSerializer(config));
		serializers.put(new Integer(MessageType.GET_OBJECTS_RESPONSE), new GetObjectsMessageResponseSerializer(config));
		serializers.put(new Integer(MessageType.GET_OBJECT_FROM_ID), new GetObjectFromOidMessageSerializer(config));
		serializers.put(new Integer(MessageType.GET_OBJECT_FROM_ID_RESPONSE), new GetObjectFromOidMessageResponseSerializer(config));
		serializers.put(new Integer(MessageType.DELETE_OBJECT), new DeleteObjectWithOidMessageSerializer(config));
		serializers.put(new Integer(MessageType.DELETE_OBJECT_RESPONSE), new DeleteObjectFromOidMessageResponseSerializer(config));
		serializers.put(new Integer(MessageType.COUNT), new CountMessageSerializer(config));
		serializers.put(new Integer(MessageType.COUNT_RESPONSE), new CountMessageResponseSerializer(config));
		serializers.put(new Integer(MessageType.ADD_UNIQUE_INDEX), new AddIndexMessageSerializer(config));
		serializers.put(new Integer(MessageType.ADD_UNIQUE_INDEX_RESPONSE), new AddIndexMessageResponseSerializer(config));
		serializers.put(new Integer(MessageType.SEND_FILE), new SendFileMessageSerializer(config));
		serializers.put(new Integer(MessageType.SEND_FILE_RESPONSE), new SendFileMessageResponseSerializer(config));
		serializers.put(new Integer(MessageType.GET_FILE), new GetFileMessageSerializer(config));
		serializers.put(new Integer(MessageType.GET_FILE_RESPONSE), new GetFileMessageResponseSerializer(config));
		serializers.put(new Integer(MessageType.REMOTE_PROCESS), new RemoteProcessMessageSerializer(config));
		serializers.put(new Integer(MessageType.REMOTE_PROCESS_RESPONSE), new RemoteProcessMessageResponseSerializer(config));
	}

	public Bytes toBytes(Message message) throws Exception {

		Integer messageType = new Integer(message.getMessageType());

		ISerializer serializer = serializers.get(messageType);
		if (serializer != null) {
			return serializer.toBytes(message);
		}

		throw new RuntimeException("toBytes not implemented for message type " + messageType);
	}

	public Message fromBytes(Bytes bytes) throws Exception{
    	BytesHelper helper = new BytesHelper(bytes,config);
    	int messageType = helper.readInt(0);
    	ISerializer serializer = serializers.get(new Integer(messageType));
    	if(serializer!=null){
    		return serializer.fromBytes(helper);
    	}
    	throw new RuntimeException("fromBytes not implemented for message type " + messageType);
    }
}
