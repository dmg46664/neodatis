/**
 * 
 */
package org.neodatis.odb.core.layers.layer4.plugin.jdbm;

import java.io.IOException;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.core.layers.layer4.ClassOidIterator;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator;
import org.neodatis.odb.core.layers.layer4.StorageEngineAdapter;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator.Way;
import org.neodatis.tool.DLogger;

/**
 * @author olivier
 * 
 */
public class NeoDatisJdbmPlugin extends StorageEngineAdapter {
	protected PHashMapBTree pHashMap;
	protected NeoDatisConfig neoDatisConfig;
	protected boolean debug;

	public static int nbPut = 0;
	public static int nbRead = 0;
	public static int nbExist = 0;

	/**
	 * @param useCacheForOid
	 */
	public NeoDatisJdbmPlugin() {
		super();
	}

	public OidAndBytes read(OID oid, boolean useCache) {
		nbRead++;
		try {
			// byte[] data = (byte[]) pHashMap.get(oid.oidToString());
			Bytes data = (Bytes) pHashMap.get(oid);
			if (data == null) {
				if (debug) {
					DLogger.info("Reading OID " + oid.oidToString() + " = null");
				}
				return null;
			}
			OidAndBytes oab = new OidAndBytes(oid, data);
			if (debug) {
				DLogger.info("Reading OID " + oid.oidToString() + " | bytes = " + oab.bytes);
			}
			return oab;
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "Error while reading data for oid " + oid.oidToString());
		}
	}

	public void write(OidAndBytes oidAndBytes) {
		nbPut++;
		try {
			if (debug) {
				DLogger.info("writing " + oidAndBytes.bytes.getByteArray().length);
			}

			// pHashMap.put(oidAndBytes.oid.oidToString(), oidAndBytes.bytes);
			pHashMap.put(oidAndBytes.oid, oidAndBytes.bytes);
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "Error while writing data for oid " + oidAndBytes.oid.oidToString());
		}
	}

	public void close() {
		try {
			pHashMap.close();
		} catch (IOException e) {
			throw new NeoDatisRuntimeException(e, "Error while closing persistent Map");
		}

	}

	public void commit() {
		try {
			pHashMap.commit();
		} catch (IOException e) {
			throw new NeoDatisRuntimeException(e, "Error while create executing commit");
		}
	}

	public void deleteObjectWithOid(OID oid) {
		try {
			pHashMap.remove(oid);
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "Error while delete data for oid " + oid.oidToString());
		}

	}

	public boolean existOid(OID oid) {
		nbExist++;
		try {
			return pHashMap.containsKey(oid);
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e, "Error while checking if key exist" + oid.oidToString());
		}
	}

	public String getEngineDirectoryForBaseName(String theBaseName) {
		String baseDirectory = neoDatisConfig.getBaseDirectory(); 
		if ( baseDirectory!= null && !baseDirectory.equals(".") && baseDirectory.length()>0) {
			return neoDatisConfig.getBaseDirectory() + "/";
		}
		return "";
	}

	public String getStorageEngineName() {
		return "jdbm";
	}

	public void open(String baseName, NeoDatisConfig config) {
		try {
			neoDatisConfig = config;
			debug = config.debugStorageEngine();
			String fullName = getEngineDirectoryForBaseName(baseName) + baseName;
			pHashMap = new PHashMapBTree(fullName, config.isTransactional(), config.useStorageEngineCache());
		} catch (IOException e) {
			throw new NeoDatisRuntimeException(e, "Error while create persistent hashmap");
		}
	}

	public void open(String host, int port, String baseName, NeoDatisConfig config) {
		throw new NeoDatisRuntimeException(NeoDatisError.NOT_YET_IMPLEMENTED);
	}

	public void rollback() {
		try {
			pHashMap.rollback();
		} catch (IOException e) {
			throw new NeoDatisRuntimeException(e, "Error while create executing rollback");
		}
	}

	public boolean useDirectory() {
		return false;
	}

	public static String stats() {
		return new StringBuilder().append("NbPut=").append(nbPut).append(" / nbRead=").append(nbRead).append(" / nbExist=").append(nbExist).toString();
	}

	@Override
	public ClassOidIterator getClassOidIterator() {
		return new JdbmClassOidIterator(pHashMap.getClassOidBTree(), getOidGenerator());
	}

	@Override
	public ObjectOidIterator getObjectOidIterator(ClassOid classOid, Way way) {
		return new JdbmObjectOidIterator(pHashMap.getBtreeForName(classOid.oidToString()), way, getOidGenerator());
	}

}
