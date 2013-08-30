package org.neodatis.odb.plugin.idf;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.plugin.idf.jdbm.JDBMIndexer;
import org.neodatis.tool.DLogger;
import org.neodatis.tool.DisplayUtility;

/**
 * An IOMnager is a class that has access to all files managed by a specific
 * data type. For example, a class is managed by at least 3 files : 1) An
 * Indexer, that keeps track of all object ids with thir file location 2) The
 * datafile that actually keeps the data 3) A deleted entry index to keep track
 * of deleted blocks in the data file
 * 
 * @author olivier
 * 
 */
public class IOManager {
	protected String key;
	protected Indexer indexer;
	protected DataFile dataFile;
	protected boolean debug;
	protected NeoDatisConfig config; 
	
	public IOManager(String key, NeoDatisConfig config) {
		super();
		this.key = key;
		this.config = config;
		this.debug = config.debugLayers();
		init();
	}

	private void init() {
		String baseDirectory = config.getBaseDirectory() +"/"+ config.getBaseName() + "/" + key;
		String indexerFileName = baseDirectory + "/index.neodatis";
		String dataFileName = baseDirectory + "/data.neodatis";
		String deletedFileName = baseDirectory + "/deleted.neodatis";
		
		new File(baseDirectory).mkdirs();
		
		indexer = createIndexer();
		dataFile = new DataFile(key, dataFileName, 4, 4096, debug);
		
		if (debug) {
			DLogger.info("Creating IOManager for " + key + " in directory : " + baseDirectory);
			DLogger.info("\tIndexer : "+ indexerFileName);
			DLogger.info("\tDatafile : "+ dataFileName);
			DLogger.info("\tDeleted blocks : "+ deletedFileName);
		}
		
		try {

			indexer.init(key, indexerFileName);
			indexer.setDebug(debug);

		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e);
		}
		
	}

	private Indexer createIndexer() {
		return new JDBMIndexer();
	}

	public String getKey() {
		return key;
	}

	public Indexer getIndexer() {
		return indexer;
	}

	public DataFile getDataFile() {
		return dataFile;
	}

	public void close() throws IOException {
		indexer.close();
		dataFile.close();
	}

	public void commit() {
		indexer.commit();
		dataFile.commit();
	}

	public void delete(OID oid) {
		ObjectLocation ol = indexer.delete(oid);
		storeDeletedBlock(ol);

	}

	private void storeDeletedBlock(ObjectLocation ol) {
		// TODO Auto-generated method stub
		
	}

	public void rollback() {
		indexer.rollback();
		dataFile.rollback();
	}

	public void write(OidAndBytes oidAndBytes) throws CorruptIndexException, IOException {
		long position = dataFile.write(oidAndBytes.bytes);
		if (debug) {
			DLogger.info("writing " + oidAndBytes.bytes.getByteArray().length + " bytes  at " + position + " : " + DisplayUtility.byteArrayToString(oidAndBytes.bytes.getByteArray()));
		}

		ObjectLocation ol = new ObjectLocation(1, position, oidAndBytes.bytes.getRealSize());

		OID oid = oidAndBytes.oid;

		indexer.put(oid, ol);
	}

	public OidAndBytes read(OID oid, boolean useCache) {
		try {
			ObjectLocation ol = indexer.get(oid);
			if (debug) {
				DLogger.info("reading data of oid " + oid + " at " + ol.getPosition() + " , size=" + ol.getSize());
			}
			Bytes bytes = dataFile.read(ol.getPosition(), ol.getSize());
			
			if(debug){
				DLogger.info("\tbytes read for oid "+oid.oidToString() +" : "+ DisplayUtility.byteArrayToString(bytes.getByteArray()));
			}
				
			return new OidAndBytes(oid, bytes);
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e);
		}
	}

}
