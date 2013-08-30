/**
 * 
 */
package org.neodatis.fs.io;

import java.io.File;

import org.neodatis.fs.Block;
import org.neodatis.fs.NDFS;
import org.neodatis.fs.NdfsFile;
import org.neodatis.fs.memory.FileAndBlockKey;
import org.neodatis.fs.transaction.NdfsTransaction;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.tool.DLogger;

/**
 * @author olivier
 * 
 */
public class NdfsFileImpl implements NdfsFile {
	protected long id;
	protected String directory;
	protected String name;
	protected String fullName;
	protected NDFS ndfs;
	protected NdfsTransaction transaction;
	protected BlockReader blockReader;
	protected long blockSize;
	protected boolean debug;


	public NdfsFileImpl(NDFS ndfs, long id, String fullName) {
		File file = new File( new File(fullName).getAbsolutePath());
		String directory = null;
		String name = file.getName();
		init(ndfs,id,directory,name);
	}
	/**
	 * @param ndfs
	 * @param id
	 * @param directory
	 * @param name
	 */
	private void init(NDFS ndfs, long id, String directory, String name) {
		this.debug = ndfs.getConfig().debug();
		this.ndfs = ndfs;
		this.id = id;
		this.transaction = null;
		this.directory = directory;
		this.name = name;
		if (directory == null) {
			this.fullName = new File(new StringBuffer(ndfs.getConfig().getBaseDirectory()).append("/").append(name)
					.toString()).getAbsolutePath();
		} else {
			this.fullName = new File(new StringBuffer(ndfs.getConfig().getBaseDirectory()).append("/").append(
					directory).append("/").append(name).toString()).getAbsolutePath();
		}
		this.blockReader = new BlockReader(this);
		this.blockSize = ndfs.getConfig().getBlockSize();
	}
	public NdfsFileImpl(NDFS ndfs, long id, String directory, String name) {
		super();
		init(ndfs, id, directory, name);
	}

	public NdfsFileImpl(NdfsTransaction transaction, long id, String directory, String name) {
		this(transaction.getNdfs(),id,directory,name);
		this.transaction = transaction;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return
	 */
	public String getFullName() {
		return fullName;
	}

	public void createDirectory() {
		new File(this.directory).mkdirs();
	}

	public Bytes read(long position, long length) {
		long blockId = position / blockSize;
		long offsetPosition = blockId * blockSize;
		int delta = (int) (position - offsetPosition);
		FileAndBlockKey key = new FileAndBlockKey(this, blockId);
		Bytes bytes = null;
		boolean ok = false;
		long currentPosition = offsetPosition;
		while (!ok) {
			Block block = readOneBlock(currentPosition);

			if (bytes == null) {
				bytes = BytesFactory.getBytes(block.bytes);
			} else {
				bytes.append(block.bytes);
			}

			if (bytes.getRealSize() - delta >= length) {
				ok = true;
			}
			currentPosition += blockSize;
		}
		return bytes;
	}

	protected Block readOneBlock(long position) {
		long blockId = position / blockSize;
		long offsetPosition = blockId * blockSize;
		FileAndBlockKey key = new FileAndBlockKey(this, blockId);

		byte[] bytes = tryToGetFromMemory(key);

		if (bytes == null) {
			bytes = blockReader.readBlock(blockId).bytes;
			if (debug) {
				DLogger.debug("NdfsFile:Reading block id " + blockId + " with size = " + bytes.length);// +
																										// " - "
																										// +
																										// BytesFactory.getBytes(bytes).toString());
			}
			if(transaction!=null){
				transaction.getTma().getByteArrayByBlockId().put(key, bytes);
			}
		}
		return new Block(this, blockId, bytes);
	}

	public byte[] tryToGetFromMemory(FileAndBlockKey key) {
		if(transaction==null){
			return null;
		}
		// Try to get from transaction memory Area (TMA)
		byte[] bytes = transaction.getTma().getByteArrayByBlockId().get(key);
		if (bytes != null) {
			return bytes;
		}
		// Then Try to get from global memory Area (GMA)
		bytes = transaction.getGma().getByteArrayByBlockId().get(key);

		return bytes;
	}

	public void write(Bytes bytes) {
		if(transaction!=null){
			transaction.writeTo(this, bytes);
		}else{
			writeDirect(bytes);
		}
	}

	public void writeDirect(Bytes bytes) {
		ndfs.getSyncWriter(this).writeTo(bytes);
	}

	public long getSize() {
		return new File(getFullName()).length();
	}

	public String toString() {
		return fullName + " - fid=" + id;
	}

	public NDFS getNdfs() {
		return ndfs;
	}

	public long append(Bytes bytes) {
		long position = getSize();
		bytes.setOffset(position);
		write(bytes);
		return position;
	}

	public long appendDirect(Bytes bytes) {
		long position = getSize();
		bytes.setOffset(position);
		writeDirect(bytes);
		return position;
	}

	public Bytes readAll() {
		return read(0, getSize());
	}
	public void append(String string) {
		
	}
	public void close() {
		
		
	}
	public long getLength() {
		return new File(fullName).length();
	}
}
	