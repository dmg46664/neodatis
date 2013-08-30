/**
 * 
 */
package org.neodatis.fs.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.neodatis.fs.NdfsException;
import org.neodatis.fs.NdfsFile;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.tool.DLogger;

/**
 * @author olivier
 * A class that write data synchronously
 *
 */
public class SyncBlockWriter {
	protected RandomAccessFile raf;
	protected NdfsFile file;
	
	public SyncBlockWriter(NdfsFile file) {
		this.file = file;
		try {
			File f = new File(file.getFullName());
			if(!f.exists()){
				f.getParentFile().mkdirs();
			}
			this.raf = new RandomAccessFile(file.getFullName(),"rw");
		} catch (FileNotFoundException e) {
			throw new NdfsException(e);
		}
	}

	/**
	 * 
	 * @param b The block to be written
	 * @throws IOException 
	 */
	public void writeTo(Bytes b) {
		try{
			byte[] bytes = b.getByteArray();
			// get the position of the first block
			long offset = b.getOffset();
			raf.seek(offset);
			raf.write(bytes);
			
			if(file.getNdfs().getConfig().debug()){
				DLogger.debug(String.format("\t\tSyncBlockWriter:Writing %d bytes at %d in file %s : %s",bytes.length, offset, file.getName(),b.toString()));
			}
		}catch (Exception e) {
			throw new NdfsException(e);
		}
	}
	

	public void close() throws IOException{
		raf.close();
	}
	
}
