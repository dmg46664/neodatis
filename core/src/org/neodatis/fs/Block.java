/**
 * 
 */
package org.neodatis.fs;


/**
 * @author olivier
 *
 */
public class Block {
	protected NdfsFile owner;
	public byte[] bytes;
	public long id;
	protected int blockSize;
	
	/**
	 * @param properties
	 */
	public Block(NdfsFile file, long id) {
		this.owner = file;
		this.id = id;
		this.blockSize = owner.getNdfs().getConfig().getBlockSize();
		clear();
	}
	
	/**
	 * @param ndfsFileImpl
	 * @param blockId
	 * @param bytes2
	 */
	public Block(NdfsFile file, long blockId, byte[] bytes2) {
		this.owner = file;
		this.id = blockId;
		this.bytes = bytes2;
		
	}

	public Block initWith(byte b){
		for(int i=0;i<bytes.length;i++){
			bytes[i] = b;
		}
		return this;
	}
	
	public long getPosition(){
		return (id-1)*blockSize;
	}
	
	public void clear(){
		bytes = new byte[blockSize];
	}

}
