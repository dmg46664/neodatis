/**
 * 
 */
package org.neodatis.fs;

import java.io.File;

/**
 * @author olivier
 *
 */
public class NdfsConfig {
	
	protected static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";
	protected static final int DEFAULT_BLOCK_SIZE = 1024;
	protected static final String DEFAULT_TRANSACTION_DIRECTORY = ".transactions";

	
	private String baseDirectory;
	private String transactionDirectory;
	private int blockSize;
	protected long timeBetweenEachFlush;
	protected boolean debug;
	protected String characterEncoding;
	
	


	public NdfsConfig(String baseDirectory, int blockSize) {
		super();
		File f = new File(baseDirectory);
		this.baseDirectory = f.getAbsolutePath();
		if(!f.exists()){
			if(f.getParentFile()!=null){
				f.getParentFile().mkdirs();
			}else{
				f.mkdirs();
			}
		}
		
		this.blockSize = blockSize;
		
		
		timeBetweenEachFlush = 100;
		debug = false;
		characterEncoding = DEFAULT_CHARACTER_ENCODING;
		transactionDirectory = DEFAULT_TRANSACTION_DIRECTORY;
	}

	public NdfsConfig(String baseDirectory) {
		this(baseDirectory, DEFAULT_BLOCK_SIZE);
	}

	
	
	
	public boolean debug(){
		return debug;
	}
	public NdfsConfig setDebug(boolean newDebug){
		debug = newDebug;
		return this;
	}


	public long getTimeBetweenEachFlush() {
		return timeBetweenEachFlush;
	}


	public NdfsConfig setTimeBetweenEachFlush(long timeBetweenEachFlush) {
		timeBetweenEachFlush = timeBetweenEachFlush;
		return this; 
	}
	/**
	 * @return
	 */
	public String getCharacterEncoding() {
		return  characterEncoding;
	}




	public String getBaseDirectory() {
		return baseDirectory;
	}




	public void setBaseDirectory(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}




	public int getBlockSize() {
		return blockSize;
	}




	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}




	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	public NdfsConfig setTransactionDirectory(String transactionDirectory) {
		this.transactionDirectory = transactionDirectory;
		return this;
	}
	
	public String getTransactionFileName(long transactionId){
		return String.format("%d.transaction",transactionId);
	}
	public String getTransactionDirectory(boolean full){
		if(full){
			return new StringBuffer(baseDirectory).append("/").append(transactionDirectory) .toString();
		}
		return transactionDirectory;
	}

}
