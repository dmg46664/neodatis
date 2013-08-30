/**
 * 
 */
package org.neodatis.fs.io;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neodatis.Id;
import org.neodatis.fs.NDFS;
import org.neodatis.fs.NdfsConfig;
import org.neodatis.fs.NdfsException;
import org.neodatis.fs.NdfsFile;
import org.neodatis.fs.memory.GMA;
import org.neodatis.fs.transaction.NdfsTransaction;
import org.neodatis.fs.transaction.NdfsTransactionImpl;
import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer3.DataConverterImpl;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.BytesHelper;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.tool.DLogger;

/** The default NDFS implementation
 * 
 * <pre>
 * It uses .main.ndfs to keep header informations
 * It uses .files.ndfs to keep track of all file system files
 * It uses .ids.ndfs to keep track of next id for each object types
 * 
 * 
 * .main.ndfs
 * close status
 * </pre>
 * @author olivier
 *
 */
public class NDFSImpl implements NDFS {
	
	protected static final String MAIN = "main.ndfs";
	protected static final String FILES = "files.ndfs";
	protected static final String IDS = "ids.ndfs";
	
	
	protected NdfsConfig ndfsConfig;
	protected NeoDatisConfig neoDatisConfig;
	protected GMA gma;
	protected int nextTransactionId;
	protected boolean isClosed;
	protected SyncWriters syncWriters;
	protected IOFlusher flusher;
	protected Thread flusherThread;
	protected boolean debug;
	protected DataConverter converter;
	/** in nano second*/
	protected long creationTimeStamp;
	/** a map with all the ids of existing file in the NDFS, for each id, we keep the position of the name*/
	protected Map<Long,Long> fileNamesPositionById;
	protected Map<Long,String> fileNamesById;
	public Map<String,Long> fileIdsByName;
	
	/** Contain some basic info about the filesystem like status and creation date*/
	protected NdfsFile mainFile;
	/** Contain all the file ids of the file system : file id and position where to find the name*/
	protected NdfsFile fileIds;
	/** Contain all the file names of the file system*/
	protected NdfsFile fileNames;
	
	protected String characterEncoding;
	
	public NDFSImpl(NdfsConfig config) {
		this.ndfsConfig = config;
		try{
			this.neoDatisConfig = NeoDatis.getConfig().setDatabaseCharacterEncoding(config.getCharacterEncoding());
		}catch (Exception e) {
			throw new NdfsException(e);
		}
		debug = config.debug();
		if(debug){
			DLogger.debug("Starting NDFS with base directory=" + config.getBaseDirectory());
		}
		this.characterEncoding = config.getCharacterEncoding();
		this.gma = new GMA();
		this.nextTransactionId = 1;
		this.isClosed = false;
		this.syncWriters = new SyncWriters(this);
		this.converter = ConverterBuilder.buildByteArrayConverter(config);
		this.flusher = new IOFlusher(this);
		fileIdsByName = new HashMap<String, Long>();
		fileNamesPositionById = new HashMap<Long, Long>();
		fileNamesById = new HashMap<Long, String>();
		
		loadDefinitions();
		this.flusherThread = new Thread(flusher);
		flusherThread.start();		
	}

	
	/**
	 * 
	 */
	private void loadDefinitions() {
		// first check if file system already exist
		String fullDirectoryName = new File(ndfsConfig.getBaseDirectory()).getAbsolutePath();
		File file = new File(fullDirectoryName);
		boolean exist = file.exists();
		
		if(debug){
			DLogger.debug("Checking if FS base directory exist : " + fullDirectoryName + " : exist?" + exist);
		}
		
		if(exist){

			mainFile = new NdfsFileImpl(this, 1, MAIN);
			fileNames = new NdfsFileImpl(this,2,FILES);
			fileIds = new NdfsFileImpl(this,3,IDS);

			loadFileDefinition();
		}else{
			file.getParentFile().mkdirs();
			createMainFiles();
		}
	}

	/**
	 * 
	 */
	private void createMainFiles() {
		creationTimeStamp = System.nanoTime();
		mainFile = new NdfsFileImpl(this, 1, MAIN);
		fileNames = new NdfsFileImpl(this,2,FILES);
		fileIds = new NdfsFileImpl(this,3,IDS);
		
		if(debug){
			DLogger.debug("NDFS:Creating main ndfs file " + mainFile.getFullName() + " - creation timestamp="+ creationTimeStamp);
		}
		markAsOpen();
	}

	protected void markAsOpen(){
		Bytes bytes = BytesFactory.getBytes();
		converter.byteToByteArray(STATUS_OPEN, bytes, 0, "status");
		converter.longToByteArray(creationTimeStamp, bytes, 1, "creation");
		mainFile.writeDirect(bytes);
	}
	protected void markAsClosed(){
		Bytes bytes = BytesFactory.getBytes();
		converter.byteToByteArray(STATUS_CLOSED, bytes, 0, "status");
		converter.longToByteArray(creationTimeStamp, bytes, 1, "creation");
		mainFile.writeDirect(bytes);
	}
	/**
	 * @param fileName
	 * @return
	 */
	public NdfsFile getFile(String directory, String fileName) {
		String fullFileName = getFileName(directory, fileName);
		long id = getFileIdFromFullFileName(fullFileName);
		return new NdfsFileImpl(this,id,fullFileName);
	}

	public NdfsFile getSplitFile(String fileName, int maxNbBlockPerFile) {
		long id = getFileIdFromFullFileName(fileName);
		return new SplittedNDFSFileImpl(this,id,fileName,maxNbBlockPerFile);
	}

	/**
	 * @param directory
	 * @param fileName
	 * @return
	 */
	private String getFileName(String directory, String fileName) {
		if (directory == null || directory.equals(".")) {
			return fileName;
		}
		return new StringBuffer(directory).append("/").append(fileName).toString();
	}
	/**
	 * 
	 */
	private void loadIds() {
		File files = new File(String.format("%s/%s", ndfsConfig.getBaseDirectory() , FILES));
	}

	/**
	 * Gets the id of a file. If file does not exist, create a new id for the file and 
	 * save it
	 * @param fileName
	 * @return
	 */
	public long getFileIdFromFullFileName(String fileName){
		Long id = fileIdsByName.get(fileName);
		if(id==null){
			// file does not exist yet in the filesystem
			id = registerNewFile(fileName);
		}
		return id;
	}
	/** register a new file in the file system
	 * @param fileName
	 * @return
	 */
	public Long registerNewFile(String fileName) {
		long fileId = Id.getNew();
		
		Bytes bytes = BytesFactory.getBytes();
		converter.stringToByteArray(fileName, false, bytes, 0, "name");
		long position = fileNames.append(bytes);
		
		Bytes bytes2 = BytesFactory.getBytes();
		converter.longToByteArray(fileId, bytes2, 0, "id");
		converter.longToByteArray(position, bytes2, 8, "position");
		fileIds.append(bytes2);
	
		if(debug){
			DLogger.debug("NDFS: new file "+ fileName + " with id " + fileId);
		}
		Long lid = new Long(fileId);
		fileIdsByName.put(fileName, lid);
		fileNamesById.put(lid, fileName);
		
		return new Long(fileId);
	}


	/** Load NDFS definitions
	 * 
	 */
	private void loadFileDefinition() {
		BytesHelper bytes = new BytesHelper(mainFile.read(0,9),neoDatisConfig);
		byte status = bytes.readByte(0);
		creationTimeStamp = bytes.readLong(1);
		
		if(status==STATUS_OPEN){
			// The file system has not been cleanly closed
			recoverFromDirtyShutdown();
		}
		// Change status to OPEN
		bytes.writeByte(STATUS_OPEN, 0,"status");
		mainFile.writeDirect(bytes.getBytes());
		
		if(debug){
			DLogger.debug("NDFS:Reading main ndfs file " + mainFile.getFullName() + " - status = "+ status + " - timestamp=" + creationTimeStamp);
		}

		// Load all file ids and keep track of the position of the file name
		BytesHelper b = new BytesHelper(fileIds.readAll(),neoDatisConfig);
		BytesHelper b2 = new BytesHelper(fileNames.readAll(), neoDatisConfig);
		ReadSize readSize2 = new ReadSize();
		ReadSize readSize3 = new ReadSize(); 
		while(readSize2.get()<b.getBytes().getRealSize()){
			long id = b.readLong(readSize2.get(), readSize2,"id");
			long position = b.readLong(readSize2.get(), readSize2,"position");
			fileNamesPositionById.put(new Long(id), new Long(position));
			String name = b2.readString(false,(int) position, readSize3, "name");
			if(id==0){
				break;
			}
			if(debug){
				DLogger.debug("\tFile with id " + id+ " - position="+position + " - name=" + name);
			}
			
			Long lid = new Long(id);
			fileIdsByName.put(name, lid);
			fileNamesById.put(lid, name);
			//fileNamesPositionById.p
		}
		if(debug){
			DLogger.debug("NDFS:Loading " +fileNamesPositionById.size()+ " file ids");
		}
		
	}

	/**
	 * @param fakeTransaction 
	 * 
	 */
	private void recoverFromDirtyShutdown() {
		DLogger.debug("NDFS:FileSystem has not been closed properly, check for pending transactions");
		NdfsFile[] transactionFiles = getTransactionFiles();
		
		for(NdfsFile file:transactionFiles){
			recoverOneTransaction(file);
		}
	}

	/**
	 * @param file
	 */
	private void recoverOneTransaction(NdfsFile file) {
		byte status = NdfsTransactionImpl.getStatusOfTransaction(file);
		
		if(status== NdfsTransaction.STATUS_COMMITTED){
			// this means that the transaction has been committed but not applied to the main files
		}else{
			DLogger.debug("Transaction file " + file.getName()+ " has status " + status+ " => delete it");
		}
	}

	/**
	 * @return
	 */
	private NdfsFile[] getTransactionFiles() {
		File transactionDirectory = new File(ndfsConfig.getTransactionDirectory(false));
		if(!transactionDirectory.exists()){
			return new NdfsFile[0];
		}
		File[] files = transactionDirectory.listFiles(new FilenameFilter() {
			
			public boolean accept(File dir, String name) {
				return name.endsWith(".transaction");
			}
		});
		NdfsFile[] tfiles = new NdfsFile[files.length];
		for(int i=0;i<files.length;i++){
			tfiles[i] = new NdfsFileImpl(this, System.nanoTime(), files[i].getParentFile().getAbsolutePath(),  files[i].getName());
		}
		return tfiles;
	}


	public NdfsTransaction startTransaction() throws NdfsException {
		long transactionId = getNextTransactionId();
		NdfsTransaction transaction = new NdfsTransactionImpl(this,transactionId);
		return transaction;
	}

	protected synchronized long getNextTransactionId(){
		try{
			return System.nanoTime();
		}finally{
		}
	}
	public String getName(){
		return ndfsConfig.getBaseDirectory();
	}
	
	public void close(){
		if(debug){
			DLogger.debug("NDFS:Closing file system " + getName());
		}

		isClosed = true;
		
		flusher.flushNow(true);
		
		flusher.close();
		
		flusher.waitForClose();
		
		markAsClosed();
		
		if(debug){
			DLogger.debug("NDFS:file system " + getName() + " closed");
		}

	}
	public boolean isClosed(){
		return isClosed;
	}


	public boolean canFlushCommittedTransaction() {
		return true;
	}


	public SyncBlockWriter getSyncWriter(NdfsFile file) {
		return syncWriters.getSyncWriter(file);
	}

	public void markAsCommited(NdfsTransaction transaction){
		gma.merge(transaction.getTma());
		flusher.markAsFlushable(transaction);
	}


	public GMA getGMA() {
		return gma;
	}


	public void release(NdfsTransaction transaction) {
		String fullName = transaction.getTransactionFile().getFullName();
		boolean deleted = new File(fullName).delete();
		if(debug){
			DLogger.debug("NDFS:transaction file " + fullName + " has been deleted : " + deleted);
		}
	}


	public long getCreationTimeStamp() {
		return creationTimeStamp;
	}


	/* (non-Javadoc)
	 * @see org.neodatis.fs.NDFS#getFileNameFromFileId(long)
	 */
	public String getFileNameFromFileId(long fileId) {
		Long id = new Long(fileId);
		String fileName = fileNamesById.get(id);
		if(fileName!=null){
			return fileName;
		}
		Long position = fileNamesPositionById.get(id);
		if(position==null){
			throw new NdfsException("File with id " + fileId + " does not exist in the file system " + ndfsConfig.getBaseDirectory());
		}
		// we must load the file name
		Bytes bytes = fileNames.read(position, 1024);
		fileName = converter.byteArrayToString(bytes, false, position.intValue(), new ReadSize(), "name");
		// put the name in the cache
		fileNamesById.put(id, fileName);
		return fileName;		
	}


	public NdfsConfig getConfig() {
		return ndfsConfig;
	}


	public Collection<String> getFileNames() {
		return fileNamesById.values();
	}
	
	
}
