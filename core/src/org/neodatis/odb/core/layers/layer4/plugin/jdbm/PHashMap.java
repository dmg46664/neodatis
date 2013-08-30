package org.neodatis.odb.core.layers.layer4.plugin.jdbm;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.RecordManagerOptions;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import org.neodatis.odb.NeoDatisRuntimeException;

/**
 * 
 */

/**
 * @author olivier
 * 
 */
public class PHashMap {
	protected RecordManager recordManager;
	protected HTree hashtable;

	public PHashMap(String fileName) throws IOException {
		RecordManagerOptions.FILE_EXTENSION = "";
		// create or open fruits record manager
		Properties props = new Properties();

		props.setProperty(RecordManagerOptions.DISABLE_TRANSACTIONS, "TRUE");
		props.setProperty(RecordManagerOptions.DISABLE_TRANSACTIONS_PERFORMSYNCONCLOSE, "TRUE");
		props.setProperty(RecordManagerOptions.NO_CACHE, "TRUE");

		File f = new File(fileName);

		if (!f.exists()) {
			f.createNewFile();
		}

		if (f.exists() && f.isDirectory()) {
			throw new NeoDatisRuntimeException(fileName + " is a directory, it must be a file");
		}
		recordManager = RecordManagerFactory.createRecordManager(fileName, props);

		long recid = recordManager.getNamedObject("objects");
		if (recid != 0) {
			hashtable = HTree.load(recordManager, recid);
		} else {
			hashtable = HTree.createInstance(recordManager);
			recordManager.setNamedObject("objects", hashtable.getRecid());
		}
	}

	public void put(Object key, Object value) throws IOException {
		hashtable.put(key, value);
	}

	public Object get(Object key) throws IOException {
		return hashtable.get(key);
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
		return hashtable.keys();
	}

	public void remove(Object key) throws IOException {
		hashtable.remove(key);
	}

	public boolean containsKey(Object key) throws IOException {
		return hashtable.get(key) != null;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public void close() throws IOException {
		recordManager.close();
	}

}
