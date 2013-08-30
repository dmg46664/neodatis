/**
 * 
 */
package org.neodatis.fs.io;

import org.neodatis.fs.NdfsFile;

/**
 * @author olivier
 *
 */
public class OneSplitFileResult {
	public long fileId;
	public long blockId;
	public long startPositionInFile;
	public long endPositionInFile;
	public NdfsFile file;
	
	public OneSplitFileResult(long fileId, long startPositionInFile, long endPositionInFile, NdfsFile file, long blockId) {
		super();
		this.fileId = fileId;
		this.blockId = blockId;
		this.startPositionInFile = startPositionInFile;
		this.endPositionInFile = endPositionInFile;
		this.file = file;
	}

}
