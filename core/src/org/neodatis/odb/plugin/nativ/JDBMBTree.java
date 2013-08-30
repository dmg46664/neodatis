package org.neodatis.odb.plugin.nativ;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.RecordManagerOptions;
import jdbm.btree.BTree;
import jdbm.helper.FastIterator;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer4.kv.KVStore;
import org.neodatis.odb.core.oid.StringOid;
import org.neodatis.odb.core.oid.StringOidImpl;
import org.neodatis.tool.DLogger;

/**
 * 
 */

/**
 * @author olivier
 * 
 */
public class JDBMBTree implements KVStore {
	protected String CLASS_OID = "class-oid";
	protected String STRING_OID = "string-oid";
	protected RecordManager recordManager;
	protected boolean debug = false;

	/** Map that contains one btree per class OID */
	protected Map<String, BTree> btrees;

	public JDBMBTree(String fileName, boolean withTransaction, boolean withCache) throws IOException {
		RecordManagerOptions.FILE_EXTENSION = "";
		// creates a JDBM property object and set the right properties...
		Properties props = new Properties();

		if (!withTransaction) {
			props.setProperty(RecordManagerOptions.DISABLE_TRANSACTIONS, "TRUE");
			props.setProperty(RecordManagerOptions.DISABLE_TRANSACTIONS_PERFORMSYNCONCLOSE, "TRUE");
		}
		if (!withCache) {
			props.setProperty(RecordManagerOptions.NO_CACHE, "TRUE");
			props.setProperty(RecordManagerOptions.CACHE_TYPE, RecordManagerOptions.NO_CACHE);
		} else {
			props.setProperty(RecordManagerOptions.NO_CACHE, "FALSE");
		}

		// Check if directory exists
		File f = new File(fileName);

		if (!f.exists() && f.getParentFile() != null) {
			f.getParentFile().mkdirs();
			f.createNewFile();
		}

		if (f.exists() && f.isDirectory()) {
			throw new NeoDatisRuntimeException(fileName + " is a directory, it must be a file");
		}
		// Creates the jdbm record manager
		recordManager = RecordManagerFactory.createRecordManager(fileName, props);

		/*
		 * long recid = recordManager.getNamedObject("objects"); if (recid != 0)
		 * { btree = BTree.load(recordManager, recid); } else { btree =
		 * BTree.createInstance(recordManager, new OidComparator(), new
		 * KeySerializer(), new BytesSerializer());
		 * recordManager.setNamedObject("objects", btree.getRecid()); }
		 */
		btrees = new HashMap<String, BTree>();
	}

	public void put(OID key, Object value) {
		if (key instanceof ObjectOid) {
			ObjectOid oid = (ObjectOid) key;
			BTree btree = getBtreeForName(oid.getClassOid().oidToString());
			if (debug) {
				DLogger.info("inserting object data of oid " + key.oidToString() + " in store " + oid.getClassOid().oidToString());
			}
			try {
				btree.insert((Serializable) oid, (Serializable) value, true);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return;
		}
		if (key instanceof StringOid) {
			StringOid oid = (StringOid) key;
			BTree btree = getBtreeForName(STRING_OID);
			if (debug) {
				DLogger.info("inserting object data of oid " + key.oidToString() + " in store " + STRING_OID);
			}
			try {
				btree.insert((Serializable) oid, (Serializable) value, true);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return;
		}
		if (key instanceof ClassOid) {
			BTree btree = getBtreeForName(CLASS_OID);
			if (debug) {
				DLogger.info("inserting class info data of oid " + key.oidToString() + " in store " + CLASS_OID);
			}

			try {
				btree.insert((Serializable) key, (Serializable) value, true);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return;
		}
		throw new RuntimeException("put:Unmanaged OID of type " + key.getClass().getName());
	}

	public Object get(OID key) {
		if (key instanceof ObjectOid) {
			ObjectOid oid = (ObjectOid) key;
			BTree btree = getBtreeForName(oid.getClassOid().oidToString());
			if (debug) {
				DLogger.info("reading object data of oid " + key.oidToString() + " in store " + oid.getClassOid().oidToString());
			}

			try {
				return btree.find((Serializable) oid);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		if (key instanceof StringOid) {
			StringOid oid = (StringOid) key;
			BTree btree = getBtreeForName(STRING_OID);
			if (debug) {
				DLogger.info("reading object data of oid " + key.oidToString() + " in store " + STRING_OID);
			}

			try {
				return btree.find((Serializable) oid);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		if (key instanceof ClassOid) {
			BTree btree = getBtreeForName(CLASS_OID);
			if (debug) {
				DLogger.info("reading class info data of oid " + key.oidToString() + " in store " + CLASS_OID);
			}

			try {
				return btree.find((Serializable) key);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		throw new RuntimeException("get:Unmanaged OID of type " + key.getClass().getName());
	}

	public Object remove(OID key) {
		if (key instanceof ObjectOid) {
			ObjectOid oid = (ObjectOid) key;
			BTree btree = getBtreeForName(oid.getClassOid().oidToString());
			try {
				return btree.remove((Serializable) oid);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		if (key instanceof ClassOid) {
			BTree btree = getBtreeForName(CLASS_OID);
			try {
				return btree.remove((Serializable) key.oidToString());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		if (key instanceof StringOid) {
			StringOid oid = (StringOid) key;
			BTree btree = getBtreeForName(STRING_OID);

			try {
				return btree.remove((Serializable) oid);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	public boolean containsKey(OID key){
		return get(key) != null;
	}

	/**
	 * Retrieve the btree to contain objects of type soid
	 * 
	 * @param classOid
	 * @return
	 * @throws IOException
	 */
	public BTree getBtreeForName(String soid) {
		try {
			BTree btree = btrees.get(soid);

			if (btree != null) {
				return btree;
			}
			// check if btree already exists
			long recid = recordManager.getNamedObject(soid);
			if (recid != 0) {
				btree = BTree.load(recordManager, recid);
				btrees.put(soid, btree);
			} else {
				// we need to create the btree
				btree = creatreBtree();
				recordManager.setNamedObject(soid, btree.getRecid());
				btrees.put(soid, btree);
			}
			return btree;
		} catch (Exception e) {
			throw new NeoDatisRuntimeException(e);
		}
	}

	protected BTree creatreBtree() throws IOException {
		return BTree.createInstance(recordManager, new OidComparator(), new KeySerializer(), new BytesSerializer());
	}

	/**
	 * @throws IOException
	 * 
	 */
	public void commit() throws IOException {
		recordManager.commit();
	}

	public void rollback() throws IOException {
		recordManager.rollback();
	}

	public FastIterator keys() throws IOException {
		return null;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public void close() throws IOException {
		recordManager.close();
	}

	public BTree getClassOidBTree() {
		return getBtreeForName(CLASS_OID);
	}
}
