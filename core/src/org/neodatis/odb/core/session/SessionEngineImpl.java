package org.neodatis.odb.core.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;

import org.neodatis.btree.IBTree;
import org.neodatis.btree.IBTreePersister;
import org.neodatis.odb.ClassOid;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.Objects;
import org.neodatis.odb.Query;
import org.neodatis.odb.Values;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.btree.LazyODBBTreePersister;
import org.neodatis.odb.core.btree.ODBBTreeMultiple;
import org.neodatis.odb.core.btree.ODBBTreeSingle;
import org.neodatis.odb.core.context.ObjectReconnector;
import org.neodatis.odb.core.index.IndexManager;
import org.neodatis.odb.core.layers.layer1.ClassIntrospector;
import org.neodatis.odb.core.layers.layer1.DefaultInstrospectionCallbackForStore;
import org.neodatis.odb.core.layers.layer1.IntrospectionCallback;
import org.neodatis.odb.core.layers.layer1.ObjectIntrospector;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilder;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilderContext;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilderImpl;
import org.neodatis.odb.core.layers.layer2.meta.AttributeValuesMap;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfoIndex;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfoList;
import org.neodatis.odb.core.layers.layer2.meta.MetaModel;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeDeletedObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.ODBType;
import org.neodatis.odb.core.layers.layer2.meta.ObjectInfoHeader;
import org.neodatis.odb.core.layers.layer2.meta.ObjectRepresentationImpl;
import org.neodatis.odb.core.layers.layer3.Layer3Reader;
import org.neodatis.odb.core.layers.layer3.Layer3ReaderImpl;
import org.neodatis.odb.core.layers.layer3.Layer3Writer;
import org.neodatis.odb.core.layers.layer3.Layer3WriterImpl;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.core.layers.layer4.ClassOidIterator;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator;
import org.neodatis.odb.core.layers.layer4.OidGenerator;
import org.neodatis.odb.core.layers.layer4.StorageEngine;
import org.neodatis.odb.core.oid.OIDTypes;
import org.neodatis.odb.core.query.IMatchingObjectAction;
import org.neodatis.odb.core.query.InternalQuery;
import org.neodatis.odb.core.query.QueryManager;
import org.neodatis.odb.core.query.ValuesQuery;
import org.neodatis.odb.core.query.criteria.CollectionQueryResultAction;
import org.neodatis.odb.core.query.criteria.CriteriaQuery;
import org.neodatis.odb.core.query.criteria.CriteriaQueryImpl;
import org.neodatis.odb.core.query.criteria.Criterion;
import org.neodatis.odb.core.query.criteria.W;
import org.neodatis.odb.core.query.values.GroupByValuesQueryResultAction;
import org.neodatis.odb.core.query.values.ValuesCriteriaQuery;
import org.neodatis.odb.core.query.values.ValuesQueryResultAction;
import org.neodatis.odb.core.refactor.CheckMetaModelResult;
import org.neodatis.odb.core.refactor.MetaModelEvolutionManagerImpl;
import org.neodatis.odb.core.refactor.RefactorManager;
import org.neodatis.odb.core.refactor.RefactorManagerImpl;
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.odb.core.trigger.DeleteTrigger;
import org.neodatis.odb.core.trigger.InsertTrigger;
import org.neodatis.odb.core.trigger.OIDTrigger;
import org.neodatis.odb.core.trigger.SelectTrigger;
import org.neodatis.odb.core.trigger.TriggerManager;
import org.neodatis.odb.core.trigger.TriggerManagerImpl;
import org.neodatis.odb.core.trigger.UpdateTrigger;
import org.neodatis.odb.tool.MemoryMonitor;
import org.neodatis.tool.DLogger;
import org.neodatis.tool.wrappers.list.IOdbList;
import org.neodatis.tool.wrappers.list.OdbArrayList;

public class SessionEngineImpl extends Observable implements SessionEngine {
	protected Session session;
	protected Cache cache;
	protected ObjectIntrospector objectIntrospector;
	protected IntrospectionCallback callback;
	protected InstanceBuilder instanceBuilder;
	protected Layer3Writer layer3Writer;
	protected Layer3Reader layer3Reader;
	protected StorageEngine storageEngine;
	protected boolean debug = true;
	protected RefactorManager refactorManager;
	protected TriggerManager triggerManager;
	protected IndexManager indexManager;
	protected ObjectReconnector objectReconnector;
	protected boolean isNewDatabase;

	public SessionEngineImpl(Session session) {
		this.session = session;
		this.cache = session.getCache();
		// warning: layer4 must be initialized before the rest
		initLayer4();

		OidGenerator oidGenerator = storageEngine.getOidGenerator();
		ClassIntrospector classIntrospector = session.getConfig().getCoreProvider().getClassIntrospector(session, oidGenerator);

		this.objectIntrospector = session.getConfig().getCoreProvider().getLocalObjectIntrospector(session, classIntrospector, oidGenerator);
		this.triggerManager = new TriggerManagerImpl(session);
		this.callback = new DefaultInstrospectionCallbackForStore(session, this.triggerManager);
		this.instanceBuilder = new InstanceBuilderImpl(session, classIntrospector, this.triggerManager);
		this.layer3Writer = new Layer3WriterImpl(session);
		this.layer3Reader = new Layer3ReaderImpl(session, storageEngine);
		this.debug = session.getConfig().debugLayers();
		this.refactorManager = new RefactorManagerImpl(this);
		this.indexManager = new IndexManager(session);
		this.objectReconnector = new ObjectReconnector();

	}

	protected void initLayer4() {
		Class clazz = session.getConfig().getStorageEngineClass();
		String storageEngineClassName = null;
		try {
			// Get the name of the directory where the engine will save the
			// files
			String directory = getEngineDirectoryForBaseName(session.getBaseIdentification().getFullIdentification());
			// Check if neodatis,root exist there
			storageEngineClassName = getStorageEngineClassName(directory);

			if (clazz != null && storageEngineClassName != null && (!storageEngineClassName.equals(clazz.getName()))) {
				throw new NeoDatisRuntimeException(NeoDatisError.LAYER4_DIFFERENT_STORAGE_ENGINE.addParameter(storageEngineClassName).addParameter(
						clazz.getName()));
			}

			if (clazz == null && storageEngineClassName != null) {
				clazz = Class.forName(storageEngineClassName);
			}

			if (clazz == null && storageEngineClassName == null) {
				throw new NeoDatisRuntimeException(NeoDatisError.LAYER4_UNDEFINED);
			}

			this.storageEngine = (StorageEngine) clazz.newInstance();

			if (this.storageEngine.useDirectory()) {
				if (storageEngineClassName == null) {
					writeStorageEngineClassName(directory, clazz.getName());
					isNewDatabase = true;
				}else{
					isNewDatabase = false;
				}
			}else{
				// use a single file, check if the file exists
				directory = session.getConfig().getBaseDirectory();
				String name = session.getBaseIdentification().getFullIdentification();
				String fileName = new StringBuilder(directory).append("/").append(name).toString();
				File f = new File(fileName);
				if(f.exists()){
					isNewDatabase = false;
				}else{
					isNewDatabase = true;
				}
			}
			this.storageEngine.init(session.getConfig());
			this.storageEngine.open(session.getBaseIdentification().getFullIdentification(), session.getBaseIdentification().getConfig());
		} catch (ClassNotFoundException e) {
			throw new NeoDatisRuntimeException(e, "The storage engine plugin class " + storageEngineClassName
					+ " is not in the classpath. Please add the plugin and its dependencies to the Java Classpath");
		} catch (Exception e) {
			String className = "undefined";
			if (clazz != null) {
				className = clazz.getName();
			}
			throw new NeoDatisRuntimeException(e, "While creating Layer4 instance of " + className + " : " + e.getMessage());
		}
	}

	/**
	 * @param fullIdentification
	 * @return
	 */
	private String getEngineDirectoryForBaseName(String fullIdentification) {
		if (session.getConfig().getBaseDirectory() != null) {
			fullIdentification = session.getConfig().getBaseDirectory() + "/" + fullIdentification;
		}
		return fullIdentification;
	}

	/**
	 * @param directory
	 * @throws IOException
	 */
	private void writeStorageEngineClassName(String directory, String storageEngineClassName) throws IOException {
		File f = new File(directory);
		if (!f.exists()) {
			f.mkdirs();
		}
		String fileName = f.getAbsolutePath() + "/neodatis.root";
		FileOutputStream fos = new FileOutputStream(fileName);
		fos.write(storageEngineClassName.getBytes());
		fos.close();
	}

	/**
	 * @param directory
	 * @return
	 * @throws IOException
	 */
	private String getStorageEngineClassName(String directory) throws IOException {
		File f = new File(directory);
		if (!f.exists()) {
			return null;
		}
		String fileName = f.getAbsolutePath() + "/neodatis.root";
		if (!new File(fileName).exists()) {
			return null;
		}
		FileInputStream fis = new FileInputStream(fileName);
		byte[] bytes = new byte[500];
		int size = fis.read(bytes);
		String className = new String(bytes, 0, size);
		fis.close();
		return className;
	}

	public NonNativeObjectInfo layer1ToLayer2(Object object) {
		if (object == null) {
			throw new NeoDatisRuntimeException(NeoDatisError.ODB_CAN_NOT_STORE_NULL_OBJECT);
		}

		Class clazz = object.getClass();

		if (ODBType.isNative(clazz)) {
			throw new NeoDatisRuntimeException(NeoDatisError.ODB_CAN_NOT_STORE_NATIVE_OBJECT_DIRECTLY.addParameter(clazz.getName()).addParameter(
					ODBType.getFromClass(clazz).getName()).addParameter(clazz.getName()));
		}

		// Transform the object into an ObjectInfo
		NonNativeObjectInfo nnoi = (NonNativeObjectInfo) objectIntrospector.getMetaRepresentation(object, callback);

		return nnoi;
	}

	/**
	 * 
	 */
	protected void checkClose() {
		if (session.isClosed()) {
			throw new NeoDatisRuntimeException(NeoDatisError.ODB_IS_CLOSED.addParameter(session.getId()));
		}

		if (session.isRollbacked()) {
			throw new NeoDatisRuntimeException(NeoDatisError.ODB_HAS_BEEN_ROLLBACKED.addParameter(session.getId().toString()));
		}

	}

	public Object layer2ToLayer1(NonNativeObjectInfo nnoi, InstanceBuilderContext context) {

		if (debug) {
			DLogger.debug("<start layer2 to layer1 oid=" + nnoi.getOid().oidToString() + ">");
		}

		Object o = instanceBuilder.buildOneInstance(nnoi, context);

		if (debug) {
			DLogger.debug("<end layer2 to layer1 oid=" + nnoi.getOid().oidToString() + ">");
		}

		return o;
	}

	public IOdbList<OidAndBytes> layer2ToLayer3(NonNativeObjectInfo nnoi) {
		if (debug) {
			DLogger.debug("<start layer2 to layer3 oid=" + nnoi.getOid().oidToString() + ">");
		}
		IOdbList<OidAndBytes> oidAndBytes = layer3Writer.metaToBytes(nnoi);
		if (debug) {
			DLogger.debug("<end layer2 to layer3 oid=" + nnoi.getOid().oidToString() + ">");
		}
		return oidAndBytes;
	}

	public NonNativeObjectInfo layer3ToLayer2(OidAndBytes oidAndBytes, boolean full, int depth) {
		if (debug) {
			DLogger.debug("<start layer3 to layer2 oid=" + oidAndBytes.oid.oidToString() + ">");
		}

		NonNativeObjectInfo nnoi = layer3Reader.metaFromBytes(oidAndBytes, full, depth);

		String fullClassName = nnoi.getClassInfo().getFullClassName();
		if (triggerManager.hasSelectTriggersFor(fullClassName)) {
			triggerManager.manageSelectTriggerAfter(fullClassName, new ObjectRepresentationImpl(nnoi, objectIntrospector), nnoi.getOid());
		}
		if (debug) {
			DLogger.debug("<end layer3 to layer2 oid=" + nnoi.getOid().oidToString() + ">");
		}

		return nnoi;
	}

	public NonNativeObjectInfo layer3ToLayer2(IOdbList<OidAndBytes> oabs, boolean full, Map<OID, OID> oidsToReplace, int depth) {
		if (debug) {
			DLogger.debug("<start layer3 to layer2 oid=" + oabs.get(0).oid.oidToString() + ">");
		}

		NonNativeObjectInfo nnoi = layer3Reader.metaFromBytes(oabs, full, oidsToReplace, depth);

		if (debug) {
			DLogger.debug("<end layer3 to layer2 oid=" + nnoi.getOid().oidToString() + ">");
		}

		return nnoi;
	}

	public OID layer3ToLayer4(IOdbList<OidAndBytes> oidsAndBytes) {

		int nb = oidsAndBytes.size();
		OID mainOid = oidsAndBytes.get(0).oid;
		for (int i = 0; i < nb; i++) {
			OidAndBytes oab = oidsAndBytes.get(i);
			storageEngine.write(oab);
			if (oab.isObject()) {
				ClassInfo ci = oab.nnoi.getClassInfo();
				if (!ci.isSystemClass()) {
					if (oab.oid.isNew()) {
						indexManager.manageIndexesForInsert(oab.oid, oab.nnoi);
						insertTriggerAfter(oab.nnoi);
					} else {
						boolean hasIndex = oab.nnoi.getClassInfo().hasIndex();
						if (hasIndex) {
							// Loads the old nnoi
							NonNativeObjectInfo oldNnoi = getMetaObjectFromOid((ObjectOid) oab.oid, true, new InstanceBuilderContext(true, null, 1));

							indexManager.manageIndexesForUpdate(oab.oid, oab.nnoi, oldNnoi);
						}
						updateTriggerAfter(oab.nnoi);
					}
				}
			}
		}
		return mainOid;
	}

	protected void updateTriggerAfter(NonNativeObjectInfo nnoi) {
		if (session.isLocal()) {
			triggerManager.manageUpdateTriggerAfter(nnoi.getClassInfo().getFullClassName(), nnoi, nnoi.getObject(), nnoi.getOid());
		} else {
			triggerManager.manageUpdateTriggerAfter(nnoi.getClassInfo().getFullClassName(), nnoi, nnoi, nnoi.getOid());
		}
	}

	protected void insertTriggerAfter(NonNativeObjectInfo nnoi) {
		if (session.isLocal()) {
			triggerManager.manageInsertTriggerAfter(nnoi.getClassInfo().getFullClassName(), nnoi.getObject(), (ObjectOid) nnoi.getOid());
		} else {
			triggerManager.manageInsertTriggerAfter(nnoi.getClassInfo().getFullClassName(), nnoi, (ObjectOid) nnoi.getOid());
		}
	}

	public OidAndBytes layer4ToLayer3(OID oid) {
		if (debug) {
			DLogger.debug("<start layer4 to layer3 oid=" + oid.oidToString() + ">");
		}

		OidAndBytes oab = storageEngine.read(oid, true);

		if (debug) {
			DLogger.debug("<end layer4 to layer3 oid=" + oid.oidToString() + ">");
		}

		return oab;
	}

	public ObjectOid store(ObjectOid oid, Object object) {
		checkClose();

		if (oid != null) {
			cache.addObject(oid, object);
		}
		NonNativeObjectInfo nnoi = layer1ToLayer2(object);

		IOdbList<OidAndBytes> oidAndBytes = layer2ToLayer3(nnoi);
		// in local mode, the mainOid is always equal to nnoi.getOid() but in cs
		// mode, as we need to replace local oids by server oids, we need to
		// retrieve the ObjectOid from the layer4 that comes from the server
		// after the
		// OID synchronization
		ObjectOid mainOid = (ObjectOid) layer3ToLayer4(oidAndBytes);

		return mainOid;
	}

	public ObjectOid storeMeta(ObjectOid oid, NonNativeObjectInfo nnoi) {
		checkClose();

		IOdbList<OidAndBytes> oidAndBytes = layer2ToLayer3(nnoi);
		layer3ToLayer4(oidAndBytes);
		return nnoi.getOid();
	}

	public ObjectOid store(Object object) {
		return store(null, object);
	}

	public ObjectOidIterator getObjectOidIterator(ClassOid classOid, ObjectOidIterator.Way way) {
		checkClose();

		int oidType = OIDTypes.TYPE_OBJECT_OID;
		return storageEngine.getObjectOidIterator(classOid, way);
	}

	public ClassOidIterator getClassOidIterator() {
		return storageEngine.getClassOidIterator();
	}

	public Session getSession() {
		return session;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.session.SessionEngine#delete(java.lang.Object)
	 */
	public ObjectOid delete(Object o, boolean cascade) {
		checkClose();

		if (o == null) {
			throw new NeoDatisRuntimeException(NeoDatisError.CAN_NOT_DELETE_NULL_OBJECT);
		}
		ObjectOid oid = cache.getOid(o, false);

		if (oid == null) {
			oid = objectReconnector.tryToGetOid(o);

			if (oid == null) {
				throw new NeoDatisRuntimeException(NeoDatisError.DELETE_OBJECT_NOT_LOADED_OR_CONNECTED);
			}
		}

		// Manage Indexes
		ClassInfo ci = session.getMetaModel().getClassInfoFromId(oid.getClassOid());
		boolean withIndex = !ci.getIndexes().isEmpty();
		if (withIndex) {
			NonNativeObjectInfo nnoi = getMetaObjectFromOid(oid, true, new InstanceBuilderContext(true, null, 1));
			indexManager.manageIndexesForDelete(oid, nnoi);
		}

		internalDeleteObjectWithOid(oid, cascade, false);

		// Triggers
		triggerManager.manageDeleteTriggerAfter(session.getMetaModel().getClassInfoFromId(oid.getClassOid()).getFullClassName(), o, oid);

		return oid;
	}

	public void deleteObjectWithOid(ObjectOid oid, boolean cascade) {
		internalDeleteObjectWithOid(oid, cascade, true);
	}

	protected void internalDeleteObjectWithOid(ObjectOid oid, boolean cascade, boolean callTriggerAndIndex) {

		if (callTriggerAndIndex) {
			ClassInfo ci = session.getMetaModel().getClassInfoFromId(oid.getClassOid());
			boolean withIndex = !ci.getIndexes().isEmpty();

			if (withIndex) {
				NonNativeObjectInfo nnoi = getMetaObjectFromOid(oid, true, new InstanceBuilderContext(true, null, 1));
				indexManager.manageIndexesForDelete(oid, nnoi);
			}
			triggerManager.manageDeleteTriggerAfter(session.getMetaModel().getClassInfoFromId(oid.getClassOid()).getFullClassName(), null, oid);
		}
		storageEngine.deleteObjectWithOid(oid);
		cache.remove(oid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.session.SessionEngine#getFieldValuesByOid(org.neodatis
	 * .odb.OID, org.neodatis.tool.wrappers.list.IOdbList, java.lang.String[],
	 * boolean)
	 */
	public AttributeValuesMap getFieldValuesFromOid(OID oid, HashSet<String> involvedFields, boolean optimizeObjectCompararison, int depth) {
		OidAndBytes oab = layer4ToLayer3(oid);
		if (oab == null) {
			return null;
		}
		AttributeValuesMap values = layer3Reader.valuesFromBytes(oab, involvedFields, depth);
		return values;
	}

	public NonNativeObjectInfo getMetaObjectFromOid(ObjectOid oid, boolean throwExceptionIfNotExist, InstanceBuilderContext context) {
		OidAndBytes oab = layer4ToLayer3(oid);
		if (oab == null) {
			if (throwExceptionIfNotExist) {
				throw new NeoDatisRuntimeException(NeoDatisError.OBJECT_WITH_OID_DOES_NOT_EXIST.addParameter(oid.oidToString()));
			}
			return new NonNativeDeletedObjectInfo(oid);
		}
		NonNativeObjectInfo nnoi = layer3ToLayer2(oab, true, context.depth);
		return nnoi;
	}

	public OidAndBytes getBytesOfObjectFromOid(ObjectOid oid, boolean throwExceptionIfNotExist) {
		OidAndBytes oab = layer4ToLayer3(oid);
		if (oab == null) {
			if (throwExceptionIfNotExist) {
				throw new NeoDatisRuntimeException(NeoDatisError.OBJECT_WITH_OID_DOES_NOT_EXIST.addParameter(oid.oidToString()));
			}
		}
		return oab;
	}

	/**
	 * TODO use the use cache boolean
	 */
	public ObjectInfoHeader getMetaHeaderFromOid(ObjectOid oid, boolean throwExceptinIfNotFound, boolean useCache) {
		OidAndBytes oab = layer4ToLayer3(oid);
		if (oab == null) {
			if (throwExceptinIfNotFound) {
				throw new NeoDatisRuntimeException(NeoDatisError.OBJECT_WITH_OID_DOES_NOT_EXIST.addParameter(oid));
			}
			return null;
		}
		NonNativeObjectInfo nnoi = layer3ToLayer2(oab, false, 1);
		return nnoi.getHeader();
	}

	public Object getObjectFromOid(ObjectOid oid, boolean throwExceptionIfNotFound, InstanceBuilderContext context) {
		checkClose();

		NonNativeObjectInfo nnoi = getMetaObjectFromOid(oid, throwExceptionIfNotFound, context);
		Object o = layer2ToLayer1(nnoi, context);
		return o;
	}

	public ObjectOid getObjectOid(Object object, boolean throwExceptionIfNotFound) {
		checkClose();

		ObjectOid oid = session.getCache().getOid(object, throwExceptionIfNotFound);
		return oid;
	}

	/**
	 * Adds an index to a class
	 * 
	 */
	public void addIndexOn(String className, String indexName, String[] indexFields, boolean verbose, boolean acceptMultipleValuesForSameKey) {
		checkClose();

		ClassInfo classInfo = session.getClassInfo(className);

		if (classInfo.hasIndex(indexName)) {
			throw new NeoDatisRuntimeException(NeoDatisError.INDEX_ALREADY_EXIST.addParameter(indexName).addParameter(className));
		}
		ClassInfoIndex cii = classInfo.addIndexOn(indexName, indexFields, acceptMultipleValuesForSameKey);
		IBTree btree = null;

		if (acceptMultipleValuesForSameKey) {
			btree = new ODBBTreeMultiple(className, session.getConfig().getDefaultIndexBTreeDegree(), new LazyODBBTreePersister(this));

		} else {
			btree = new ODBBTreeSingle(className, session.getConfig().getDefaultIndexBTreeDegree(), new LazyODBBTreePersister(this));
		}
		cii.setBTree(btree);
		store(cii);

		if (verbose) {
			DLogger.info("Creating index " + indexName + " on class " + className);
		}

		// We must load all objects and insert them in the index!
		InternalQuery q = criteriaQuery(className);
		q.getQueryParameters().setInMemory(false);
		Objects<NonNativeObjectInfo> objects = getMetaObjects(q);

		if (verbose) {
			DLogger.info(indexName + " : " + objects.size() + " object(s) loaded to update the index");
		}

		NonNativeObjectInfo nnoi = null;
		int i = 0;

		boolean monitorMemory = session.getConfig().monitoringMemory();

		while (objects.hasNext()) {
			nnoi = objects.next();
			btree.insert(cii.computeKey(nnoi), nnoi.getOid());
			if (verbose && i % 1000 == 0) {
				if (monitorMemory) {
					MemoryMonitor.displayCurrentMemory("Index " + indexName + " " + i + " objects inserted", true);
				}
			}
			i++;
		}

		if (verbose) {
			DLogger.info(indexName + " created!");
		}
	}

	public void addInsertTriggerFor(String className, InsertTrigger trigger) {
		checkClose();

		triggerManager.addInsertTriggerFor(className, trigger);
	}

	public void addSelectTriggerFor(String className, SelectTrigger trigger) {
		checkClose();

		triggerManager.addSelectTriggerFor(className, trigger);
	}

	public void addUpdateTriggerFor(String className, UpdateTrigger trigger) {
		checkClose();
		triggerManager.addUpdateTriggerFor(className, trigger);
	}

	public void addOidTriggerFor(String className, OIDTrigger trigger) {
		checkClose();
		triggerManager.addOidTriggerFor(className, trigger);
	}
	public void removeOidTrigger(String className, OIDTrigger trigger){
		checkClose();
		triggerManager.removeOidTrigger(className, trigger);
	}

	public void addDeleteTriggerFor(String className, DeleteTrigger trigger) {
		checkClose();
		triggerManager.addDeleteTriggerFor(className, trigger);
	}

	public CriteriaQueryImpl criteriaQuery(Class clazz, Criterion criterion) {
		checkClose();
		CriteriaQueryImpl q = new CriteriaQueryImpl(clazz, criterion);
		q.setSessionEngine(this);
		if (criterion != null) {
			criterion.ready();
		}
		return q;
	}

	public CriteriaQuery criteriaQuery(String className, Criterion criterion) {
		checkClose();
		CriteriaQueryImpl q = new CriteriaQueryImpl(className, criterion);
		q.setSessionEngine(this);
		if (criterion != null) {
			criterion.ready();
		}
		return q;
	}

	public CriteriaQueryImpl criteriaQuery(String className) {
		checkClose();
		CriteriaQueryImpl q = new CriteriaQueryImpl(className);
		q.setSessionEngine(this);
		return q;
	}

	public CriteriaQuery criteriaQuery(Class clazz) {
		checkClose();
		CriteriaQueryImpl q = new CriteriaQueryImpl(clazz);
		q.setSessionEngine(this);
		return q;
	}

	public void defragmentTo(String newFileName) {
		checkClose();
		throw new NeoDatisRuntimeException(NeoDatisError.NOT_YET_SUPPORTED);
	}

	public void deleteIndex(String fullClassName, String indexName, boolean verbose) {
		checkClose();
		ClassInfo classInfo = session.getMetaModel().getClassInfo(fullClassName, true);
		if (!classInfo.hasIndex(indexName)) {
			throw new NeoDatisRuntimeException(NeoDatisError.INDEX_DOES_NOT_EXIST.addParameter(indexName).addParameter(fullClassName));
		}
		ClassInfoIndex cii = classInfo.getIndexWithName(indexName);
		if (verbose) {
			DLogger.info("Deleting index " + indexName + " on class " + fullClassName);
		}
		delete(cii, true);
		classInfo.removeIndex(cii);
		storeClassInfo(classInfo);
		if (verbose) {
			DLogger.info("Index " + indexName + " deleted");
		}

	}

	public void disconnect(Object object) {
		// TODO Auto-generated method stub

	}

	public <T> Objects<T> getMetaObjects(InternalQuery query) {
		checkClose();
		boolean returnObjects = false;
		IMatchingObjectAction queryResultAction = new CollectionQueryResultAction(query, this, returnObjects);

		/*
		 * // Some type of query can be resolved without instantiating all
		 * objects, // check first // TODO builds a facade to put this IF! if
		 * (query != null && !QueryManager.needsInstanciation(query)) { return
		 * getObjectInfos(query, inMemory, startIndex, endIndex, true,
		 * queryResultAction); }
		 */
		return QueryManager.getQueryExecutor(query, this).execute(queryResultAction);
	}

	public <T> Objects<T> execute(InternalQuery query) {
		checkClose();

		if (query instanceof ValuesCriteriaQuery) {
			throw new NeoDatisRuntimeException(NeoDatisError.VALUES_QUERY_MUST_USE_GET_VALUES);
		}

		boolean returnObjects = true;
		IMatchingObjectAction queryResultAction = new CollectionQueryResultAction(query, this, returnObjects);

		/*
		 * // Some type of query can be resolved without instantiating all
		 * objects, // check first // TODO builds a facade to put this IF! if
		 * (query != null && !QueryManager.needsInstanciation(query)) { return
		 * getObjectInfos(query, inMemory, startIndex, endIndex, true,
		 * queryResultAction); }
		 */
		return QueryManager.getQueryExecutor(query, this).execute(queryResultAction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.session.SessionEngine#getValues(org.neodatis.odb
	 * .core.query.IValuesQuery, int, int)
	 */
	public Values getValues(ValuesQuery valuesQuery) {
		checkClose();
		valuesQuery.setSessionEngine(this);
		IMatchingObjectAction queryResultAction = null;

		if (valuesQuery.hasGroupBy()) {
			queryResultAction = new GroupByValuesQueryResultAction(valuesQuery, this, instanceBuilder);
		} else {
			queryResultAction = new ValuesQueryResultAction(valuesQuery, this, instanceBuilder);
		}
		Objects<?> r = QueryManager.getQueryExecutor(valuesQuery, this).execute(queryResultAction);

		return (Values) r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.session.SessionEngine#rebuildIndex(java.lang.String
	 * , java.lang.String, boolean)
	 */
	public void rebuildIndex(String fullClassName, String indexName, boolean verbose) {
		checkClose();
		if (verbose) {
			DLogger.info("Rebuilding index " + indexName + " on class " + fullClassName);
		}

		ClassInfo classInfo = session.getMetaModel().getClassInfo(fullClassName, true);
		if (!classInfo.hasIndex(indexName)) {
			throw new NeoDatisRuntimeException(NeoDatisError.INDEX_DOES_NOT_EXIST.addParameter(indexName).addParameter(fullClassName));
		}
		ClassInfoIndex cii = classInfo.getIndexWithName(indexName);
		deleteIndex(fullClassName, indexName, verbose);
		addIndexOn(fullClassName, indexName, classInfo.getAttributeNames(cii.getAttributeIds()), verbose, !cii.isUnique());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.session.SessionEngine#reconnect(java.lang.Object)
	 */
	public void reconnect(Object object) {
		throw new NeoDatisRuntimeException(NeoDatisError.NOT_YET_IMPLEMENTED.addParameter("getValues()"));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.session.SessionEngine#getRefactorManager()
	 */
	public RefactorManager getRefactorManager() {
		return refactorManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.session.SessionEngine#introspectClass(java.lang
	 * .String, boolean)
	 */
	public ClassInfoList introspectClass(String fullClassName) {
		return objectIntrospector.getClassIntrospector().introspect(fullClassName);
	}

	public void commit() {
		checkClose();

		storageEngine.getOidGenerator().commit();
		storageEngine.commit();

	}

	public void close() {
		if (session.isClosed()) {
			throw new NeoDatisRuntimeException(NeoDatisError.ODB_IS_CLOSED.addParameter(session.getId()));
		}
		storageEngine.close();
	}

	public StorageEngine getStorageEngine() {
		return storageEngine;
	}

	public ObjectIntrospector getObjectIntrospector() {
		return objectIntrospector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.session.SessionEngine#addClasses(org.neodatis.odb
	 * .core.layers.layer2.meta.ClassInfoList)
	 */
	public void storeClassInfos(ClassInfoList ciList) {
		Collection<ClassInfo> c = ciList.getClassInfos();

		for (ClassInfo ci : c) {
			storeClassInfo(ci);
		}
	}

	/**
	 * @param ci
	 */
	public void storeClassInfo(ClassInfo ci) {
		IOdbList<OidAndBytes> oidsAndBytes = layer3Writer.classInfoToBytes(ci);
		this.layer3ToLayer4(oidsAndBytes);
	}

	public ClassInfo classInfoFromBytes(OidAndBytes oab, boolean full) {
		return layer3Reader.classInfoFromBytes(oab, full);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.session.SessionEngine#loadMetaModel()
	 */
	public MetaModel loadMetaModel(MetaModel metaModel) {
		ClassOidIterator iterator = storageEngine.getClassOidIterator();

		Map<ClassOid, OidAndBytes> classInfoBytes = new HashMap<ClassOid, OidAndBytes>();

		// The meta model must be loaded in several steps
		// First load all class info partially, name and class info id

		while (iterator.hasNext()) {
			ClassOid classOid = iterator.next();
			OidAndBytes bytesClassInfo = storageEngine.read(classOid, true);
			classInfoBytes.put(classOid, bytesClassInfo);
			if (bytesClassInfo != null) {
				ClassInfo ci = layer3Reader.classInfoFromBytes(bytesClassInfo, false);
				metaModel.addClass(ci, false);
			}
		}

		Iterator<ClassOid> oabIterator = classInfoBytes.keySet().iterator();
		// Then load class info attributes. As some attribute may reference
		// Class info, we need to have all class info in the meta model before
		// loading attributes
		while (oabIterator.hasNext()) {
			ClassOid classOid = oabIterator.next();
			OidAndBytes bytesClassInfo = classInfoBytes.get(classOid);
			if (bytesClassInfo != null) {
				// fully read the class info
				ClassInfo ci = layer3Reader.classInfoFromBytes(bytesClassInfo, true);
				// update attributes of the pre-loaded class info
				metaModel.addClass(ci, true);
			}
		}

		// now loads indexes
		IOdbList<ClassInfoIndex> indexes = null;
		IBTreePersister persister = null;
		ClassInfoIndex cii = null;
		InternalQuery queryClassInfo = null;
		IBTree btree = null;

		// Read class info indexes
		Iterator<ClassInfo> ciIterator = metaModel.getAllClasses().iterator();
		while (ciIterator.hasNext()) {
			ClassInfo classInfo = ciIterator.next();
			indexes = new OdbArrayList<ClassInfoIndex>();
			queryClassInfo = criteriaQuery(ClassInfoIndex.class, W.equal("classInfoId", classInfo.getOid()));
			Objects<ClassInfoIndex> classIndexes = queryClassInfo.objects();
			indexes.addAll(classIndexes);
			// Sets the btree persister
			for (int j = 0; j < indexes.size(); j++) {
				cii = indexes.get(j);
				persister = new LazyODBBTreePersister(this);
				btree = cii.getBTree();
				btree.setPersister(persister);
				btree.getRoot().setBTree(btree);
			}
			if (debug) {
				DLogger.debug("Reading indexes for " + classInfo.getFullClassName() + " : " + indexes.size() + " indexes");
			}
			classInfo.setIndexes(indexes);
		}

		if (session.getConfig().checkMetaModelCompatibility()) {
			new MetaModelEvolutionManagerImpl(session).check(session.getConfig().checkMetaModelCompatibility(), true, session.getConfig()
					.logSchemaEvolutionAnalysis());
		}

		return metaModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.session.SessionEngine#existOid(org.neodatis.odb
	 * .OID)
	 */
	public boolean existOid(OID oid) {
		return storageEngine.existOid(oid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.session.SessionEngine#getFileFormatVersion()
	 */
	public String getFileFormatVersion() {
		return "1";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.session.SessionEngine#getTriggerManager()
	 */
	public TriggerManager getTriggerManager() {
		return triggerManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.core.session.SessionEngine#rollback()
	 */
	public void rollback() {
		checkClose();

		storageEngine.rollback();
	}

	public CheckMetaModelResult checkMetaModelCompatibility(Map<String, ClassInfo> currentCIs) {
		// TODO Auto-generated method stub
		return null;
	}

	public BigInteger count(Query query) {
		checkClose();

		if (!(query instanceof CriteriaQuery)) {
			throw new NeoDatisRuntimeException("count only works with Criteria queries for instance :-(");
		}

		ValuesQuery q = new ValuesCriteriaQuery((CriteriaQuery) query).count("count");
		q.setPolymorphic(query.isPolymorphic());
		Values values = getValues(q);
		BigInteger count = (BigInteger) values.nextValues().getByIndex(0);
		return count;
	}

	public OidAndBytes classInfoToBytes(ClassInfo ci) {
		IOdbList<OidAndBytes> oidsAndBytes = layer3Writer.classInfoToBytes(ci);
		return oidsAndBytes.get(0);
	}

	public void setTriggerManager(TriggerManager triggerManager) {
		this.triggerManager = triggerManager;
	}

	public void refresh(Object o, int depth) {
		ObjectOid ooid = session.getCache().getOid(o, true);
		InstanceBuilderContext ibc = new InstanceBuilderContext(false, o, depth);
		o = getObjectFromOid(ooid, true, ibc);
	}

	public ValuesQuery queryValues(Class clazz, Criterion criterion) {
		checkClose();
		ValuesCriteriaQuery q = new ValuesCriteriaQuery(clazz, criterion);
		q.setSessionEngine(this);
		if (criterion != null) {
			criterion.ready();
		}
		return q;
	}

	public ValuesQuery queryValues(String className, Criterion criterion) {
		checkClose();
		ValuesCriteriaQuery q = new ValuesCriteriaQuery(className, criterion);
		q.setSessionEngine(this);
		if (criterion != null) {
			criterion.ready();
		}
		return q;
	}

	public ValuesQuery queryValues(String className) {
		checkClose();
		ValuesCriteriaQuery q = new ValuesCriteriaQuery(className);
		q.setSessionEngine(this);
		return q;
	}

	public ValuesCriteriaQuery queryValues(Class clazz, ObjectOid oid) {
		checkClose();
		ValuesCriteriaQuery q = new ValuesCriteriaQuery(clazz, oid);
		q.setSessionEngine(this);
		return q;

	}

	public ValuesQuery queryValues(Class clazz) {
		checkClose();
		ValuesCriteriaQuery q = new ValuesCriteriaQuery(clazz);
		q.setSessionEngine(this);
		return q;
	}

	public Message sendMessage(Message message) {
		throw new NeoDatisRuntimeException("SendMessage is only available in Client/Server mode");
	}

	public boolean isNewDatabase() {
		return isNewDatabase;
	}

	public void writeDatabaseHeader(DatabaseInfo di) {
		layer3Writer.writeDatabaseHeader(di);
	}

}
