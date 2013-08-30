package org.neodatis.odb.plugin.idf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.core.layers.layer3.Bytes;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.core.layers.layer4.ClassOidIterator;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator;
import org.neodatis.odb.core.layers.layer4.StorageEngineAdapter;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator.Way;
import org.neodatis.odb.plugin.idf.jdbm.JDBMIndexer;
import org.neodatis.tool.DLogger;

public class IDFPlugin extends StorageEngineAdapter {
	protected static final String CLASS_OID = "class-oid";

	protected Map<String, IOManager> ioManagers;
	protected boolean debug = true;
	protected NeoDatisConfig neoDatisConfig;
	protected String baseName;

	public void open(String baseName, NeoDatisConfig config) {
		this.neoDatisConfig = config;
		this.baseName = baseName;
		this.neoDatisConfig.setBaseName(baseName);
		

		initClassOidIOManager();

	}

	private void initClassOidIOManager() {
		ioManagers = new HashMap<String, IOManager>();
		// class oid indexer
		IOManager ioManager = createIOManagerFor(CLASS_OID);
		
		System.out.println("debug:IDF Plugin - " + config.getBaseDirectory() + "/" + baseName);

		/**
		 * load current indexers
		 * 
		 */
		ClassOidIterator iterator = ioManager.getIndexer().getClassOidIterator();
		if (iterator != null) {
			while (iterator.hasNext()) {
				ClassOid coid = iterator.next();
				String soid = coid.oidToString();
				ioManager = createIOManagerFor(soid);
			}
		}
	}

	@Override
	public ClassOidIterator getClassOidIterator() {
		Indexer indexer = ioManagers.get(CLASS_OID).getIndexer();
		if (indexer == null) {
			return null;
		}
		return indexer.getClassOidIterator();
	}

	@Override
	public ObjectOidIterator getObjectOidIterator(ClassOid classOid, Way way) {

		Indexer indexer = ioManagers.get(classOid.oidToString()).getIndexer();
		if (indexer == null) {
			return null;
		}
		return indexer.getObjectOidIterator();
	}

	@Override
	public OidAndBytes read(OID oid, boolean useCache) {

		IOManager ioManager = getIOManagerFor(oid);

		return ioManager.read(oid, useCache);
	}

	@Override
	public void write(OidAndBytes oidAndBytes) {
		try {

			IOManager ioManager = getIOManagerFor(oidAndBytes.oid);

			ioManager.write(oidAndBytes);

		} catch (Exception e) {
			// (Exception e) {
			rollback();
			throw new NeoDatisRuntimeException(e, "Error while writing data for oid " + oidAndBytes.oid.oidToString());

		}

	}

	protected synchronized IOManager createIOManagerFor(String oid) {
		IOManager ioManager = ioManagers.get(oid);

		if (ioManager == null) {
			ioManager = new IOManager(oid, neoDatisConfig);
			ioManagers.put(oid, ioManager);
		}
		return ioManager;
	}

	/**
	 * get (and create if it does not exist) an indexer for a specific indexer
	 * 
	 * @param oid
	 * @return
	 */
	protected synchronized IOManager getIOManagerFor(OID oid) {

		if (oid instanceof ObjectOid) {
			ObjectOid ooid = (ObjectOid) oid;
			// get indexer
			String classOid = ooid.getClassOid().oidToString();
			IOManager ioManager = ioManagers.get(classOid);
			if (ioManager == null) {
				ioManager = createIOManagerFor(classOid);
			}
			return ioManager;
		}
		if (oid instanceof ClassOid) {
			// get indexer
			IOManager ioManager = ioManagers.get(CLASS_OID);
			if (ioManager == null) {
				ioManager = createIOManagerFor(CLASS_OID);
			}
			return ioManager;
		}

		return null;
	}

	public void close() {
		try {
			Iterator<IOManager> iterator = ioManagers.values().iterator();
			while (iterator.hasNext()) {
				iterator.next().close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void commit() {
		Iterator<IOManager> iterator = ioManagers.values().iterator();
		while (iterator.hasNext()) {
			iterator.next().commit();
		}
	}

	public void deleteObjectWithOid(OID oid) {
		IOManager ioManager = getIOManagerFor(oid);
		ioManager.delete(oid);

	}

	public boolean existOid(OID oid) {

		IOManager ioManager = getIOManagerFor(oid);
		return ioManager.getIndexer().existKey(oid);
	}

	public String getEngineDirectoryForBaseName(String theBaseName) {
		if (neoDatisConfig.getBaseDirectory() != null) {
			return neoDatisConfig.getBaseDirectory() + "/";
		}
		return "";
	}

	public String getStorageEngineName() {
		return "IDFPlugin";
	}

	public void open(String host, int port, String baseName, NeoDatisConfig config) {
		// TODO Auto-generated method stub

	}

	public void rollback() {
		Iterator<IOManager> iterator = ioManagers.values().iterator();
		while (iterator.hasNext()) {
			iterator.next().rollback();
		}

	}

	public boolean useDirectory() {
		return true;
	}

}
