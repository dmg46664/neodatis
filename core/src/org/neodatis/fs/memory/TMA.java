/**
 * 
 */
package org.neodatis.fs.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.neodatis.fs.NdfsConfig;
import org.neodatis.fs.NdfsFile;
import org.neodatis.fs.transaction.NdfsTransaction;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.tool.DLogger;

/** Transaction Memory Area. This area keeps track of all the modified block of a transaction.
 * These blocks are kept by id and by group. Before reading any block, the transaction checks if block is in the MTA.
 * @author olivier
 *
 */
public class TMA {
	protected NdfsTransaction ndfsTransaction;
	/** To keep all blocks by id*/
	protected Map<FileAndBlockKey, byte[]> byteArrayByBlockId;
	/** To keep all blocks by file*/
	protected Map<NdfsFile, Collection<Bytes>> blocksByFile;
	
	protected boolean debug;
	
	public TMA(NdfsTransaction ndfsTransaction){
		this.ndfsTransaction = ndfsTransaction;
		this.byteArrayByBlockId = new HashMap<FileAndBlockKey, byte[]>();
		this.blocksByFile = new HashMap<NdfsFile, Collection<Bytes>>();
		
		this.debug = ndfsTransaction.getNdfs().getConfig().debug();
	}
	
	public void add(NdfsFile file, Bytes bytes){
		// first add each block
		int nbBlocks = bytes.getNbBlocks();
		long blockId = bytes.getOffset() / ndfsTransaction.getNdfs().getConfig().getBlockSize();
		for(int i=0;i<nbBlocks;i++){
			byte[] block = bytes.getByteArray(i);
			FileAndBlockKey key = new FileAndBlockKey(file,blockId+i);
			byteArrayByBlockId.put(key, block);
			
			if(debug){
				DLogger.info(String.format("TMA:tid=%d : adding block id %d in transaction for file %s (fid=%d)", ndfsTransaction.getId(),blockId+i, file.getName(), file.getId()));
			}
		}
		Collection<Bytes> blocksList = blocksByFile.get(file);
		if(blocksList==null){
			blocksList = new ArrayList<Bytes>();
			blocksByFile.put(file, blocksList);
		}
		blocksList.add(bytes);
		if(debug){
			DLogger.info(String.format("TMA:tid=%d : adding bytes (size=%d) for file %s, nb [blocks]=%d ", ndfsTransaction.getId(),bytes.getRealSize(), file.getName(),blocksList.size()));
		}
	}
	public Map<FileAndBlockKey, byte[]> getByteArrayByBlockId(){
		return byteArrayByBlockId;
	}

	public Map<NdfsFile, Collection<Bytes>> getBlocksByFile(){
		return blocksByFile;
	}

	/**
	 * 
	 */
	public void clear() {
		byteArrayByBlockId.clear();
		blocksByFile.clear();
	}

}
