/**
 * 
 */
package org.neodatis.odb.core.btree.nativ;

/**This is the value object used as the value of btree nodes
 * @author olivier
 *
 */
public class Position {
	public static final int SIZE = 2*8+4;
	protected long fileId;
	protected long blockId;
	protected int offset;
	
	
	public Position(long fileId, long blockId, int offset) {
		super();
		this.fileId = fileId;
		this.blockId = blockId;
		this.offset = offset;
	}
	public long getFileId() {
		return fileId;
	}
	public void setFileId(long fileId) {
		this.fileId = fileId;
	}
	public long getBlockId() {
		return blockId;
	}
	public void setBlockId(long blockId) {
		this.blockId = blockId;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	

}
