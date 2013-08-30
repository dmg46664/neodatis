/**
 * 
 */
package org.neodatis.odb.core.layers.layer4;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;

/**
 * @author olivier
 *
 */
public interface StorageEngine {
	/**
	 * Opens the storage engine for database 'baseName' and the configuration. In Local mode (no network IO)
	 * @param baseName
	 * @param config
	 */
	void open(String baseName, NeoDatisConfig config);
	
	/**
	 * Opens the storage engine for database 'baseName' and the configuration. In Client Server mode
	 * @param host
	 * @param port
	 * @param baseName
	 * @param config
	 */
	public void open(String host, int port, String baseName, NeoDatisConfig config);
	/**
	 * Commits changes to the storage engine
	 */
	void commit();
	/**
	 * Roolback changes to the storage engine
	 */
	void rollback();
	/**
	 * Closes the storage engine
	 */
	void close();
	/**
	 * Reads data of the object with the given oid
	 * @param oid
	 * @param useCache
	 * @return
	 */
	OidAndBytes read(OID oid, boolean useCache);
	/**
	 * Check if an object with the given oid exists in the base
	 * @param oid
	 * @return
	 */
	boolean existOid(OID oid);
	/**
	 * Reads a long value from storage engine
	 * @param oid
	 * @param useCache
	 * @return
	 */
	long readLong(OID oid, boolean useCache);
	/**
	 * Writes data of an object
	 * @param oidAndBytes
	 */
	void write(OidAndBytes oidAndBytes);
	/**
	 * Writes a long to the storage engine
	 * @param oid
	 * @param l
	 */
	void writeLong(OID oid, long l);
	/**
	 * Returns an oidGenerator, responsible for creating new OIDs
	 * @return
	 */
	OidGenerator getOidGenerator();
	/**Returns an OidIterator: responsible for iterating over the object oids 
	 * 
	 * @param classOid
	 * @param way
	 * @return
	 */
	ObjectOidIterator getObjectOidIterator(ClassOid classOid, ObjectOidIterator.Way way);
	/**
	 * Returns an OidIterator: responsible for iterating over the class oids 
	 * @return
	 */
	ClassOidIterator getClassOidIterator();
	void init(NeoDatisConfig config);
	/**Delete data of the object with the given oid
	 * @param oid The oid of the object/class/data that must be deleted 
	 */
	void deleteObjectWithOid(OID oid);
	/** The way the storage engine have to inform NeoDatis in which directory files are being stored
	 * NeoDatis will create a file "neodatis.root" to store the storage engine plugin name
	 * @param directory If null, NeoDatis won't do anything
	 */
	String getEngineDirectoryForBaseName(String theBaseName);
	
	
	/** Returns the name of the storage engine
	 * 
	 * @return
	 */
	String getStorageEngineName();
	
	/**
	 * To specify if the plugin use a directory to store its files
	 * @return
	 */
	boolean useDirectory();
}
