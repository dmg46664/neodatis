/**
 * 
 */
package org.neodatis.fs.memory;

import org.neodatis.fs.NdfsFile;

/**
 * @author olivier
 *
 */
public class FileAndBlockKey {
	protected NdfsFile file;
	protected long blockId;
	protected int hashcode;
	
	public FileAndBlockKey(NdfsFile file, long blockId){
		this.file = file;
		this.blockId = blockId;
		this.hashcode = toString().hashCode();
	}
	
	public boolean equals(Object o){
		if(o==null|| !(o instanceof FileAndBlockKey)){
			return false;
		}
		FileAndBlockKey fileAndBlockKey = (FileAndBlockKey) o;
		return file.getId() == fileAndBlockKey.file.getId() && blockId == fileAndBlockKey.blockId;
	}

	public NdfsFile getFile() {
		return file;
	}

	public long getBlockId() {
		return blockId;
	}
	public String toString() {
		return "Block id="+blockId + " - file="+file.toString();
	}
	public int hashCode() {
		return hashcode;
	}
}
