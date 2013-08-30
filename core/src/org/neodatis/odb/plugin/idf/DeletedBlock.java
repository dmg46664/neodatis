package org.neodatis.odb.plugin.idf;

import java.io.Serializable;

import org.neodatis.odb.core.layers.layer3.DataConverterUtil;

/**
 * A simple class to keep track of a position in a  file
 * @author olivier smadja (osmadja@gmail.com)
 *
 */
public class DeletedBlock implements Serializable{
	/** The size of the deleted block*/
	protected int size;

	/** the id of the file where to find the block*/
	protected int fileId;
	/** The position (in the file) of the block*/
	protected long position;
	
	/**
	 * 
	 * @param fileId
	 * @param position
	 */
	public DeletedBlock(int fileId, long position, int size) {
		super();
		this.fileId = fileId;
		this.position = position;
		this.size = size;
	}
	public int getFileId() {
		return fileId;
	}
	public void setFileId(int fileId) {
		this.fileId = fileId;
	}
	public long getPosition() {
		return position;
	}
	public void setPosition(long position) {
		this.position = position;
	}
	
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public byte[] toBytes() {
		byte[] bytes = new byte[2*DataConverterUtil.INT_SIZE+DataConverterUtil.LONG_SIZE];
		DataConverterUtil.intToByteArray(fileId, bytes, 0, "fileid", false);
		DataConverterUtil.longToByteArray(position, bytes, DataConverterUtil.INT_SIZE, "position", false);
		DataConverterUtil.intToByteArray(size, bytes, DataConverterUtil.INT_SIZE+DataConverterUtil.LONG_SIZE, "size", false);
		return bytes;
	}
	public static DeletedBlock fromBytes(byte[] bytes){
		int fileId = DataConverterUtil.byteArrayToInt(bytes, 0, "fileid", false);
		long position = DataConverterUtil.byteArrayToLong(bytes, DataConverterUtil.INT_SIZE, "position", false);
		int size = DataConverterUtil.byteArrayToInt(bytes, DataConverterUtil.INT_SIZE+DataConverterUtil.LONG_SIZE, "size", false);
		return new DeletedBlock(fileId, position, size);
	}
	@Override
	public String toString() {
		return fileId+":"+position+":"+size;
	}
}
