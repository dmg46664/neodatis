/**
 * 
 */
package org.neodatis.fs;

import org.neodatis.odb.core.layers.layer3.Bytes;

/**
 * @author olivier
 * 
 */
public interface NdfsFile {
	long getId();

	String getDirectory();

	String getName();

	String getFullName();

	Bytes read(long position, long length);

	void write(Bytes bytes);

	/**
	 * @return
	 */
	long getSize();

	/**
	 * @param bytes
	 */
	void writeDirect(Bytes bytes);

	/**
	 * @return
	 */
	NDFS getNdfs();

	/**Append the bytes at the end of the file
	 * @param bytes
	 * @return The position where byte have been written
	 */
	long append(Bytes bytes);

	long appendDirect(Bytes bytes);

	/**
	 * @return
	 */
	Bytes readAll();

	void close();

	long getLength();

}
