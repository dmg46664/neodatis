/**
 * 
 */
package org.neodatis.fs.transaction;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.neodatis.fs.NDFS;
import org.neodatis.fs.NdfsConfig;
import org.neodatis.fs.NdfsFile;
import org.neodatis.fs.io.AsyncBlockWriter;
import org.neodatis.fs.io.BlockReader;
import org.neodatis.fs.io.NdfsFileImpl;
import org.neodatis.fs.io.SplittedNDFSFileImpl;
import org.neodatis.fs.memory.GMA;
import org.neodatis.fs.memory.TMA;
import org.neodatis.odb.NeoDatis;
import org.neodatis.odb.core.layers.layer3.DataConverter;
import org.neodatis.odb.core.layers.layer3.DataConverterImpl;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.BytesFactory;
import org.neodatis.odb.core.layers.layer3.ReadSize;
import org.neodatis.tool.DLogger;

/**
 * @author olivier
 * 
 */
public class NdfsTransactionImpl implements NdfsTransaction {
	protected long id;
	protected TMA tma;
	protected GMA gma;
	protected NdfsFile transactionFile;
	protected AsyncBlockWriter asyncBlockWriter;
	protected NDFS ndfs;
	protected Map<String, NdfsFile> openFilesNames;
	protected boolean debug;
	protected DataConverter converter;

	public NdfsTransactionImpl(NDFS ndfs, long id) {
		this.debug = ndfs.getConfig().debug();
		this.ndfs = ndfs;
		this.id = id;
		this.gma = ndfs.getGMA();
		this.tma = new TMA(this);
		this.openFilesNames = new HashMap<String, NdfsFile>();
		this.transactionFile = new NdfsFileImpl(this, 0, ndfs.getConfig().getTransactionDirectory(false), ndfs.getConfig().getTransactionFileName(id));
		this.asyncBlockWriter = new AsyncBlockWriter(this);
		this.converter = new DataConverterImpl(debug,ndfs.getConfig().getCharacterEncoding(), NeoDatis.getConfig());

		if (debug) {
			DLogger.debug("Transaction:Starting transaction with id" + id + " (file=" + transactionFile.getFullName());
		}

		initTransactionFlags();
	}

	private void initTransactionFlags() {
		Bytes bytes = BytesFactory.getBytes();
		converter.byteToByteArray(STATUS_UNCOMMITTED, bytes, 0, "status");
		converter.longToByteArray(System.currentTimeMillis(), bytes, 1, "timestamp");
		converter.longToByteArray(id, bytes, 9, "id");
		transactionFile.writeDirect(bytes);
	}

	public void markTransactionFileAsCommited() {
		Bytes bytes = BytesFactory.getBytes();
		converter.byteToByteArray(STATUS_COMMITTED, bytes, 0, "status");
		converter.longToByteArray(System.currentTimeMillis(), bytes, 1, "timestamp");
		transactionFile.writeDirect(bytes);
		if (debug) {
			DLogger.debug("Transaction:file marked as committed");
		}
	}

	public void markTransactionFileAsRollbacked() {
		Bytes bytes = BytesFactory.getBytes();
		converter.byteToByteArray(STATUS_ROLLBACKED, bytes, 0, "status");
		converter.longToByteArray(System.currentTimeMillis(), bytes, 1, "timestamp");
		transactionFile.writeDirect(bytes);
		if (debug) {
			DLogger.debug("Transaction:file marked as rollbacked");
		}
	}

	public void markTransactionFileAsApplied() {
		Bytes bytes = BytesFactory.getBytes();
		converter.byteToByteArray(STATUS_APPLIED, bytes, 0, "status");
		converter.longToByteArray(System.currentTimeMillis(), bytes, 1, "timestamp");
		transactionFile.writeDirect(bytes);
		if (debug) {
			DLogger.debug("Transaction:file marked as applied");
		}
	}

	/**
	 * Returns the status of a transaction
	 * 
	 * @param file
	 *            The transaction file
	 * @return one of the NDFSTransaction.STATUS_*
	 */
	public static byte getStatusOfTransaction(NdfsFile file) {
		Bytes bytes = file.read(0, 1);
		return bytes.get(0);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public TMA getTma() {
		return tma;
	}

	public void setTma(TMA tma) {
		this.tma = tma;
	}

	public GMA getGma() {
		return gma;
	}

	public void setGma(GMA gma) {
		this.gma = gma;
	}

	public NdfsFile getTransactionFile() {
		return transactionFile;
	}

	public void setFile(NdfsFile file) {
		this.transactionFile = file;
	}

	public void writeTo(NdfsFile file, Bytes bytes) {
		this.asyncBlockWriter.writeTo(file, bytes);
	}

	public BlockReader getReader(NdfsFile file) {
		return new BlockReader(file);
	}

	public void commit() {
		if (debug) {
			DLogger.debug(String.format("Transaction:Committing transaction with id %d (file=%s) - nb open files=%d", id, transactionFile
					.getFullName(), getNbOpenFiles()));
		}
		ndfs.markAsCommited(this);

	}

	public void rollback() {
		if (debug) {
			DLogger.debug(String.format("Transaction:Rollbacking transaction with id %d (file=%s) - nb open files=%d", id, transactionFile
					.getFullName(), getNbOpenFiles()));
		}
		markTransactionFileAsRollbacked();
		tma.clear();
		ndfs.release(this);
	}

	/**
	 * @param directory
	 * @param fileName
	 * @return
	 */
	public synchronized NdfsFile getFile(String directory, String fileName) {
		String completeFileName = getFileName(directory, fileName);
		NdfsFile file = openFilesNames.get(completeFileName);

		if (file != null) {
			return file;
		}
		long fileId = ndfs.getFileIdFromFullFileName(completeFileName);
		file = new NdfsFileImpl(this, fileId, directory, fileName);
		openFilesNames.put(completeFileName, file);
		return file;
	}

	public synchronized NdfsFile getSplitFile(String directory, String fileName, int maxNbBlockPerFile) {
		String completeFileName = getFileName(directory, fileName);
		NdfsFile file = openFilesNames.get(completeFileName);

		if (file != null) {
			return file;
		}
		long fileId = ndfs.getFileIdFromFullFileName(completeFileName);
		file = new SplittedNDFSFileImpl(this, fileId, directory, fileName,maxNbBlockPerFile);
		openFilesNames.put(completeFileName, file);
		return file;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.fs.transaction.NdfsTransaction#getNbOpenFiles()
	 */
	public int getNbOpenFiles() {
		return openFilesNames.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.fs.transaction.NdfsTransaction#getNdfs()
	 */
	public NDFS getNdfs() {
		return ndfs;
	}

	public static NdfsTransaction loadFromFile(NdfsFile file) {
		long fileSize = new File(file.getFullName()).length();
		Bytes bytes = file.read(0, fileSize);
		DataConverter converter = new DataConverterImpl(file.getNdfs().getConfig().debug(), file.getNdfs().getConfig().getCharacterEncoding(), NeoDatis.getConfig());
		ReadSize readSize = new ReadSize();
		byte status = converter.byteArrayToByte(bytes, 0, readSize, "status");
		long creation = converter.byteArrayToLong(bytes, readSize.get(), readSize, "creation");
		long id = converter.byteArrayToLong(bytes, readSize.get(), readSize, "id");

		NdfsTransaction transaction = new NdfsTransactionImpl(file.getNdfs(), id);

		while (readSize.get() < fileSize) {
			long fileId = converter.byteArrayToLong(bytes, readSize.get(), readSize, "file id");
			long position = converter.byteArrayToLong(bytes, readSize.get(), readSize, "position");
			int length = converter.byteArrayToInt(bytes, readSize.get(), readSize, "length");
			byte[] data = bytes.extract(readSize.get(), length);
			readSize.add(length);

			String fullFileName = file.getNdfs().getFileNameFromFileId(fileId);
			NdfsFile f = null;
			transaction.getTma().add(f, bytes);
		}
		return transaction;
	}
}
