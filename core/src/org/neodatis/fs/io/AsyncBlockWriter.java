/**
 * 
 */
package org.neodatis.fs.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.neodatis.fs.NdfsConfig;
import org.neodatis.fs.NdfsException;
import org.neodatis.fs.NdfsFile;
import org.neodatis.fs.transaction.NdfsTransaction;
import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer3.DataConverterImpl;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.tool.DLogger;

/**
 * @author olivier
 * A class that writes data asynchronously
 *
 */
public class AsyncBlockWriter {
	protected NdfsFile transactionFile;
	protected RandomAccessFile raf;
	protected DataConverter converter;
	protected NdfsTransaction ndfsTransaction;
	
	public AsyncBlockWriter(NdfsTransaction ndfsTransaction) {
		this.transactionFile = ndfsTransaction.getTransactionFile();
		try {
			File f = new File(transactionFile.getFullName()); 
			if(!f.exists()){
				f.getParentFile().mkdirs();
			}
			
			this.raf = new RandomAccessFile(transactionFile.getFullName(),"rw");
		} catch (FileNotFoundException e) {
			throw new NdfsException(e);
		}
		this.converter = new DataConverterImpl(ndfsTransaction.getNdfs().getConfig().debug(),ndfsTransaction.getNdfs().getConfig().getCharacterEncoding(), NeoDatis.getConfig());
		this.ndfsTransaction = ndfsTransaction;
	}

	/**
	 * 
	 * @param transactionFile The transactionFile to write to
	 * @param blockArray The block to be written
	 * @throws IOException 
	 */
	public void writeTo(NdfsFile mainFile,Bytes bytes) {
		if(transactionFile.getNdfs().getConfig().debug()){
			DLogger.debug(String.format("asyncBlockWriter: Writing %d bytes to transactionFile %s at position %d",bytes.getRealSize(),transactionFile.getName(),bytes.getOffset()));
		}
		queue(mainFile, bytes);
	}

	/**
	 * @param transactionFile
	 * @param blockArray
	 * @param position The position where to write
	 * @throws IOException 
	 */
	private void queue(NdfsFile mainFile, Bytes bytes){
		int bytesSize = bytes.getRealSize();
		int fileIdSize = 8;
		int positionSize = 8;
		int nbBlockSize = 4;
		int offset = fileIdSize + nbBlockSize + positionSize;
		
		byte[] newbytes = new byte[bytesSize + offset ];
		
		Bytes headerBytes = BytesFactory.getBytes();
		// put main file (file that changes will be applied) id 
		converter.longToByteArray(mainFile.getId(), headerBytes, 0, "file id");
		// put position 
		converter.longToByteArray(bytes.getOffset(),headerBytes, 8, "position");
		// put the bytes size
		converter.intToByteArray(bytesSize, headerBytes, 16, "bytes size");
		
		// copy all to the byte array
		headerBytes.extract(newbytes, 0, 0, 20);
		bytes.extract(newbytes, offset, 0, bytesSize);
		
		try{
			raf.seek(raf.length());
			raf.write(newbytes);
		}catch (Exception e) {
			throw new NdfsException(e);
		}
		
		ndfsTransaction.getTma().add(mainFile, bytes);
	}
	
	public void close() throws IOException{
		raf.close();
	}
	
}
