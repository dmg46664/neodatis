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
import org.neodatis.odb.core.server.message.GetFileMessageResponse;
import org.neodatis.odb.core.server.message.SendFileMessage;
import org.neodatis.odb.core.server.message.Message;

/**
 * @author olivier
 * 
 */
public class GetFileMessageResponseSerializer extends SerializerAdapter{

	
	public GetFileMessageResponseSerializer(NeoDatisConfig config){
		super(config);
	}

	public Message fromBytes(BytesHelper bytes) throws Exception {
		GetFileMessageResponse message = new GetFileMessageResponse();
		ReadSize readSize = new ReadSize();
		HeaderSerializer.fill(message, bytes, readSize);
		
		boolean serverInbox = bytes.readBoolean(readSize.get(), readSize, "s inbox");
		String remoteFileName = bytes.readString(false, readSize.get(), readSize, "remote file name");
		boolean clientInbox = bytes.readBoolean(readSize.get(), readSize, "c inbox");
		String localFileName = bytes.readString(false, readSize.get(), readSize, "local file name");
		boolean exist = bytes.readBoolean(readSize.get(), readSize, "exist");
		long size = bytes.readLong(readSize.get(), readSize, "size");


		Bytes b = bytes.readBytes(readSize.get(),(int) size,readSize,"content");
		
		String fullFileName = localFileName;
		
		if(clientInbox){
			fullFileName = getConfig().getInboxDirectory()+"/"+fullFileName;
			
			File f = new File(fullFileName);
			
			// check if inbox directory exists
			if(f.getParentFile()!=null &&  !f.exists()){
				f.getParentFile().mkdirs();
			}
		}else{
			File f = new File(fullFileName);
			if(f.getParentFile()!=null && !f.getParentFile().exists()){
				f.getParentFile().mkdirs();
			}
		}
		
		//System.out.println("debug:Trying to write file at "+ fullFileName);
		FileOutputStream fos = new FileOutputStream(fullFileName);
		
		fos.write(b.getByteArray());
		
		fos.close();
		
		message.setLocalFileName(localFileName);
		message.setRemoteFileName(remoteFileName);
		message.setGetFileInServerInbox(serverInbox);
		message.setPutFileInClientInbox(clientInbox);
		message.setFileExist(exist);
		message.setFileSize(size);
		
		return message;
	}

	public Bytes toBytes(Message message) {
		GetFileMessageResponse m = (GetFileMessageResponse) message;

		BytesHelper bytes = new BytesHelper(BytesFactory.getBytes(),getConfig());
		int position = HeaderSerializer.toBytes(bytes, message);
		
		String fileName = m.getRemoteFileName();
		
		if(m.isGetFileInServerInbox()){
			fileName = getConfig().getInboxDirectory()+"/"+fileName;
		}
		// Check if file exists
		File f = new File(fileName);
		
		boolean exist = f.exists();

		position += bytes.writeBoolean(m.isGetFileInServerInbox(),position, "s inbox");
		position += bytes.writeString(m.getRemoteFileName(),false, position, "remote file name");
		position += bytes.writeBoolean(m.isPutFileInClientInbox(),position, "c inbox");
		position += bytes.writeString(m.getLocalFileName(),false, position, "local file name");
		position += bytes.writeBoolean(f.exists(), position, "exist");
		position += bytes.writeLong(f.length(), position, "size");
		
		if(exist){
			try{
				FileInputStream fis = new FileInputStream(f);
				
				byte[] b = new byte[1024];
				int size = fis.read(b);
				while(size!=-1){
					position += bytes.appendByteArray(b,size);
					size = fis.read(b);
				}
				
		
			}catch (Exception e) {
				throw new NeoDatisRuntimeException(e, "Building File Message for file " + f.getAbsolutePath());
			}
		}
		return bytes.getBytes();
	}
}
