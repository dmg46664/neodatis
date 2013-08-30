/**
 * 
 */
package org.neodatis.odb.core.server.message.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.odb.core.server.message.SendFileMessage;
import org.neodatis.odb.core.server.message.Message;

/**
 * @author olivier
 * 
 */
public class SendFileMessageSerializer extends SerializerAdapter{

	
	public SendFileMessageSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		SendFileMessage message = new SendFileMessage();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		
		String localFileName = bytes.readString(false, readSize.get(), readSize, "local");
		String remoteFileName = bytes.readString(false, readSize.get(), readSize, "remote");
		boolean isInbox = bytes.readBoolean(readSize.get(), readSize, "is inbox");
		
		long fileSize = bytes.readLong(readSize.get(), readSize, "size");
		
		Bytes b = bytes.readBytes(readSize.get(),(int) fileSize,readSize,"content");
		
		String fullFileName = getConfig().getInboxDirectory()+"/"+ remoteFileName;
		
		File inboxDirectory = new File(getConfig().getInboxDirectory());
		
		// check if inbox directory exists
		if(!inboxDirectory.exists()){
			inboxDirectory.mkdirs();
		}
		
		FileOutputStream fos = new FileOutputStream(fullFileName);
		
		fos.write(b.getByteArray());
		
		fos.close();
		
		File f = new File(fullFileName);
		
		boolean fileExist = f.exists();
		long size = f.length();
		
		message.setLocalFileName(localFileName);
		message.setRemoteFileName(remoteFileName);
		message.setPutFileInServerInbox(isInbox);
		
		return message;
	}

	public Bytes toBytes(Message message) {
		SendFileMessage m = (SendFileMessage) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		
		// Check if file exists
		File f = new File(m.getLocalFileName());
		
		if(!f.exists()){
			throw new NeoDatisRuntimeException("File "+ m.getLocalFileName()+" does not exist");
		}

		position += bytes.writeString(m.getLocalFileName(),false, position, "local file name");
		
		boolean hasRemoteName = m.getRemoteFileName()!=null;
		
		if(hasRemoteName){
			position += bytes.writeString(m.getRemoteFileName(),false, position, "remote file name");
		}else{
			position += bytes.writeString("",false, position, "remote file name");
		}
		position += bytes.writeBoolean(m.isPutFileInServerInbox(), position, "put inbox");
		
		position += bytes.writeLong(f.length(), position, "size");

		try{
			FileInputStream fis = new FileInputStream(f);
			
			byte[] b = new byte[1024];
			int size = fis.read(b);
			while(size!=-1){
				position += bytes.appendByteArray(b,size);
				size = fis.read(b);
			}
			
			return bytes.getBytes();
		}catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "Building File Message for file " + f.getAbsolutePath());
		}
	}
}
