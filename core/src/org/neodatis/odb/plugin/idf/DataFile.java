package org.neodatis.odb.plugin.idf;

import java.io.IOException;

import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.buffer.MultiBufferedFileIO;

/** A class to write data in a file. it uses a MultiBuffer to enable transparent caching 
 * 
 * @author olivier
 *
 */
public class DataFile {
	protected MultiBufferedFileIO io;
	
	public DataFile(String name, String fileName, int nbBuffers, int bufferSize, boolean debug){
		io = new MultiBufferedFileIO(nbBuffers, name, fileName, true, bufferSize, debug);
	}
	
	public void write(long position, Bytes bytes){
		io.goToPosition(position);
		byte[] b = bytes.getByteArray(); 
		io.writeBytes(b);
		//io.internalWrite(b,b.length);
	}
	public Bytes read(long position, int size){
		io.goToPosition(position);
		byte[] bb = new byte[size];
		//io.internalRead(bb, size);
		int iPosition = (int) position;
		int iSize = (int) size;
		//@TODO check if we can actually convert to int
		io.readBytes(bb,iPosition , iSize);
		return BytesFactory.getBytes(bb);
	}

	public void flush() throws IOException{
		io.flushIO();
	}
	public void close() {
		io.close();
	}

	public long length() {
		return io.getLength();
	}

	public void rollback() {
		// TODO Auto-generated method stub
		
	}

	public void commit() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Writes at the end of the file and return the position where bytes have been written
	 * @param bytes
	 * @return
	 */
	public long write(Bytes bytes) {
		long positionToWrite = length();
		System.out.println("real size is "+ positionToWrite);
		
		write(positionToWrite, bytes);
		long l = positionToWrite;
		System.out.println("\tafter size is "+ length());
		return l;
	}
	
}
