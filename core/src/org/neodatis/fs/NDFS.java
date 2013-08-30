/**
 * 
 */
package org.neodatis.fs;

import java.util.Collection;

import org.neodatis.fs.io.SyncBlockWriter;
import org.neodatis.fs.memory.GMA;
import org.neodatis.fs.transaction.NdfsTransaction;

/**
 * @author olivier
 *
 */
public interface NDFS {
	
	public static final byte STATUS_OPEN = 1;
	public static final byte STATUS_CLOSED = 2;
	
	public NdfsTransaction startTransaction();

	/**
	 * @return
	 */
	public String getName();

	public void close();
	/**
	 * @return
	 */
	public boolean isClosed();

	/**
	 * @return
	 */
	public boolean canFlushCommittedTransaction();
	
	public NdfsConfig getConfig();
	
	public SyncBlockWriter getSyncWriter(NdfsFile file);

	/**
	 * @return
	 */
	public GMA getGMA();

	/**
	 * @param ndfsTransactionImpl
	 */
	public void markAsCommited(NdfsTransaction transaction);

	/**
	 * @param transaction
	 */
	public void release(NdfsTransaction transaction);
	
	public long getCreationTimeStamp();

	/** Return the name of the file from its id
	 * @param fileId
	 * @return
	 */
	public String getFileNameFromFileId(long fileId);

	/**
	 * Gets the id of a file. If file does not exist, create a new id for the file and 
	 * save it
	 * @param fileName
	 * @return
	 */
	public long getFileIdFromFullFileName(String fileName);

	/** Return a file that won't use transactions
	 * 
	 * @param fullName
	 * @return
	 */
	public NdfsFile getFile(String directory, String fileName);

	/** return the list of file names of the file system
	 * 
	 * @return
	 */
	public Collection<String> getFileNames();
}
