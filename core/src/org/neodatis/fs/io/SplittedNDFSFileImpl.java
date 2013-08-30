/**
 * 
 */
package org.neodatis.fs.io;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neodatis.btree.exception.BTreeException;
import org.neodatis.fs.Block;
import org.neodatis.fs.NDFS;
import org.neodatis.fs.NdfsFile;
import org.neodatis.fs.memory.FileAndBlockKey;
import org.neodatis.fs.transaction.NdfsTransaction;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.tool.DLogger;

/**
 * @author olivier
 *
 */
public class SplittedNDFSFileImpl implements NdfsFile{
	protected long id;
	protected String directory;
	protected String name;
	protected String fullName;
	protected NDFS ndfs;
	protected NdfsTransaction transaction;
	protected long blockSize;
	protected long oneFileSize;
	protected boolean debug;
	protected Map<Long, NdfsFile> filesById;
	protected Map<Long, BlockReader> blockReadersbyId;
	
	/** The max number of block a file can contain*/
	protected int maxNbBlockPerFile;


	public SplittedNDFSFileImpl(NDFS ndfs, long id, String fullName, int maxNbBlockPerFile) {
		File file = new File( new File(fullName).getAbsolutePath());
		String directory = null;
		String name = file.getName();
		this.maxNbBlockPerFile = maxNbBlockPerFile;
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
		this.filesById = new HashMap<Long, NdfsFile>();
		this.blockReadersbyId = new HashMap<Long, BlockReader>();
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
		this.blockSize = ndfs.getConfig().getBlockSize();
		this.oneFileSize = maxNbBlockPerFile * blockSize;
	}
	public SplittedNDFSFileImpl(NDFS ndfs, long id, String directory, String name, int maxNbBlockPerFile) {
		super();
		this.maxNbBlockPerFile = maxNbBlockPerFile;
		init(ndfs, id, directory, name);
	}

	public SplittedNDFSFileImpl(NdfsTransaction transaction, long id, String directory, String name, int maxNbBlockPerFile) {
		this(transaction.getNdfs(),id,directory,name,maxNbBlockPerFile);
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
		SplitFiles result = getFileForPosition(new SplitFiles(), position,length);
		Bytes bytes = null;
		List<OneSplitFileResult> splits = result.getSplits();
		for(int i=0;i<splits.size();i++){
			OneSplitFileResult r = splits.get(i);
			long positionInFile = r.startPositionInFile;
			while(positionInFile<r.endPositionInFile){
				System.out.println(positionInFile+" -> " + (positionInFile+blockSize));
				Block block = readOneBlock(r.fileId, r.file, positionInFile-r.startPositionInFile);
				positionInFile+=blockSize;
				if(bytes==null){
					//bytes = BytesFactory.getBytes(block.bytes,r.blockId*blockSize,(int) (position-r.blockId*blockSize));
				}else{
					bytes.append(block.bytes);
				}
			}
		}
		return bytes;
		
		/*
		long offsetPosition = result.blockId * blockSize;
		int delta = (int) (position - offsetPosition);
		
		
		Bytes bytes = null;
		boolean ok = false;
		long currentPosition = offsetPosition;
		while (!ok) {
			
			Block block = readOneBlock(result.fileId, result.file, result.adjustedPosition);
			System.out.println(currentPosition + " - file " + result.file.getName());
			if (bytes == null) {
				bytes = BytesFactory.getBytes(block.bytes, offsetPosition, delta);
			} else {
				bytes.append(block.bytes);
			}

			if (bytes.getRealSize() - delta >= length) {
				ok = true;
			}else{
				currentPosition += blockSize;
				result = getFileForPosition(currentPosition);
				offsetPosition = result.blockId * blockSize;
				delta = 0;
			}
			
		}
		return bytes;
		*/
	}

	
	protected Block readOneBlock(long fileId, NdfsFile file,long adjustedPosition) {
		// gets the block id in the file
		long blockId = adjustedPosition / blockSize;
		FileAndBlockKey key = new FileAndBlockKey(file, blockId);

		byte[] bytes = tryToGetFromMemory(key);

		if (bytes == null) {
			bytes = getBlockReader(fileId, file).readBlock(blockId).bytes;
			if (debug) {
				DLogger.debug("NdfsFile: file " +file.getName()+" : Reading block id " + blockId + " with size = " + bytes.length);
			}
			if(transaction!=null){
				transaction.getTma().getByteArrayByBlockId().put(key, bytes);
			}
		}
		return new Block(file, blockId, bytes);
	}
	/**
	 * @param fileId
	 * @return
	 */
	private BlockReader getBlockReader(long fileId, NdfsFile file) {
		Long key = new Long(fileId);
		BlockReader reader = blockReadersbyId.get(key);
		if(reader!=null){
			return reader;
		}
		reader = new BlockReader(file);
		blockReadersbyId.put(key, reader);
		return reader;
	}
	/**
	 * @param position
	 * @return
	 */
	private SplitFiles getFileForPosition(SplitFiles splits, long position, long length) {
		long maxPosition = position + length-1;
		long blockId = position / blockSize;
		long lfileId = blockId / maxNbBlockPerFile;
		
		long fileStartPosition = lfileId*oneFileSize;
		long fileEndPosition = (lfileId+1)*oneFileSize-1;
		long adjustedPosition = position - fileStartPosition;
		try {

			Long lid = new Long(lfileId);
			NdfsFile file = getFile(lid);
			
			
			if(maxPosition<=fileEndPosition){
				OneSplitFileResult result = new OneSplitFileResult(lfileId, adjustedPosition, adjustedPosition+length, file, blockId);
				// all we need is in the file
				splits.addSplit(result);
				return splits;
			}
			OneSplitFileResult result = new OneSplitFileResult(lfileId, position, fileEndPosition, file, blockId);
			System.out.println("Split start="+ result.startPositionInFile + "  -end="+result.endPositionInFile + " - file=" + file.getName());
			// all we need is in the file
			splits.addSplit(result);
			// what we need spans in severals files
			length = length - (fileEndPosition-position+1);
			position = fileStartPosition + oneFileSize;
			
			return getFileForPosition(splits, position, length);
		} catch (Exception e) {
			throw new BTreeException("Error while getting file for id " + id, e);
		}
	}
	/**
	 * @param lid
	 * @return
	 */
	private NdfsFile getFile(Long lid) {
		NdfsFile file = filesById.get(lid);
		if (file == null) {
			String fileId = new StringBuffer(name).append(".").append(lid).toString();
			file = transaction.getFile(directory, fileId);
			filesById.put(lid, file);
			System.out.println("Creating file " + fileId);
		}
		return file;
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
			long position = bytes.getOffset();
			int lenght = bytes.getRealSize();
			SplitFiles result = getFileForPosition(new SplitFiles(), position,lenght);
			List<OneSplitFileResult> splits = result.getSplits();
			if(splits.size()==1){
				OneSplitFileResult r = result.getSplits().get(0); 
				bytes.setOffset(r.startPositionInFile);
				transaction.writeTo(r.file, bytes);
			}else{
				int localOffset = 0;
				for(int i=0;i<splits.size();i++){
					OneSplitFileResult r = result.getSplits().get(i); 
					int start = localOffset;
					int len = (int) (r.endPositionInFile-r.startPositionInFile)+1;
					byte[] b = bytes.extract(start,len);
					//TODO check delta=0?
					//Bytes bb = BytesFactory.getBytes(b,r.startPositionInFile,0);
					//stransaction.writeTo(r.file, bb);
					localOffset+=len;
				}
				
			}
		}else{
			writeDirect(bytes);
		}
	}

	public void writeDirect(Bytes bytes) {
		long position = bytes.getOffset();
		int lenght = bytes.getRealSize();
		SplitFiles result = getFileForPosition(new SplitFiles(), position,lenght);
		List<OneSplitFileResult> splits = result.getSplits();
		if(splits.size()==1){
			OneSplitFileResult r = result.getSplits().get(0); 
			bytes.setOffset(r.startPositionInFile);
			ndfs.getSyncWriter(this).writeTo(bytes);
		}else{
			for(int i=0;i<splits.size();i++){
				OneSplitFileResult r = result.getSplits().get(i); 
				byte[] b = bytes.extract((int)r.startPositionInFile, (int) (r.endPositionInFile-r.startPositionInFile));
				//TODO check delta=0?
				//Bytes bb = BytesFactory.getBytes(b,r.startPositionInFile,0);
				//ndfs.getSyncWriter(this).writeTo(bb);
			}
			
		}
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
		throw new RuntimeException("not yet suported");
		/*
		long position = getSize();
		bytes.setOffset(position);
		write(bytes);
		return position;
		*/
	}

	public long appendDirect(Bytes bytes) {
		throw new RuntimeException("not yet suported");
		/*
		long position = getSize();
		bytes.setOffset(position);
		writeDirect(bytes);
		return position;
		*/
	}

	public Bytes readAll() {
		return read(0, getSize());
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}
	public long getLength() {
		// TODO Auto-generated method stub
		return 0;
	}
}
