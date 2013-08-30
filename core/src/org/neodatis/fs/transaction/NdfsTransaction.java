/**
 * 
 */
package org.neodatis.fs.transaction;

import org.neodatis.fs.NDFS;
import org.neodatis.fs.NdfsFile;
import org.neodatis.fs.memory.GMA;
import org.neodatis.fs.memory.TMA;
import org.neodatis.odb.core.layers.layer3.Bytes;

/**
 * @author olivier
 *
 */
public interface NdfsTransaction {
	public static final byte STATUS_UNCOMMITTED = 0;
	public static final byte STATUS_COMMITTED = 1;
	public static final byte STATUS_ROLLBACKED = 2;
	public static final byte STATUS_APPLIED = 3;
	
	
	
	public long getId();
	public TMA getTma();
	public GMA getGma();
	public NdfsFile getTransactionFile();
	public void commit();
	public void rollback();
	public NdfsFile getFile(String directory, String fileName);
	public int getNbOpenFiles();
	/**
	 * @return
	 */
	public NDFS getNdfs();
	/**
	 * @param bytes
	 */
	public void writeTo(NdfsFile file, Bytes bytes);
	public void markTransactionFileAsCommited();
	public void markTransactionFileAsRollbacked();
	public void markTransactionFileAsApplied();
	/**
	 * @param directory
	 * @param fileName
	 * @param i
	 * @return
	 */
	public NdfsFile getSplitFile(String directory, String fileName, int maxNbBlocksPerFile);
	
}
