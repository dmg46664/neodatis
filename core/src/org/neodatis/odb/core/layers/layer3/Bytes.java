/**
 * 
 */
package org.neodatis.odb.core.layers.layer3;

import java.io.Serializable;

/**
 * @author olivier
 *
 */
public interface Bytes extends Serializable {
	void set(int index, byte b);

	void copy(byte[] bytesToCopy, int from, int to, int length, boolean checkIndex);

	int getNbBlocks();

	int getRealSize();

	/**
	 * @param i
	 * @return
	 */
	byte get(int index);

	byte[] extract(int offset, int length);

	/**
	 * @return
	 */
	byte[] getByteArray();

	/**
	 * @param bytes
	 */
	int append(Bytes bytes);
	
	/**
	 * @param bytes
	 */
	int append(byte[] bytes);

	
	byte[] getByteArray(int index);

	/**
	 * 
	 * @param bytesWhereToWrite The byte array where the extracted bytes must be written
	 * @param offsetWhereToWrite The start where to start writing in the dest bytesWhereToWrite
	 * @param offsetWhereToRead The position from whic we must start read
	 * @param length The length to extract
	 * @return
	 */
	public byte[] extract(byte[] bytesWhereToWrite, int offsetWhereToWrite, int offsetWhereToRead, int length);
	
	long getOffset();

	/**
	 * @param position
	 */
	void setOffset(long offset);

	public boolean hasOffset();

	public void set(int index, byte[] bb);

	public byte[] get(int index, byte[] dest, int destOffset, int size);

}
