package org.neodatis.odb.core.session;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map;

import org.neodatis.odb.ClassOid;
import org.neodatis.odb.OID;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.Objects;
import org.neodatis.odb.Query;
import org.neodatis.odb.Values;
import org.neodatis.odb.core.layers.layer1.ObjectIntrospector;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilderContext;
import org.neodatis.odb.core.layers.layer2.meta.AttributeValuesMap;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfoList;
import org.neodatis.odb.core.layers.layer2.meta.MetaModel;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.layers.layer2.meta.ObjectInfoHeader;
import org.neodatis.odb.core.layers.layer3.OidAndBytes;
import org.neodatis.odb.core.layers.layer4.ClassOidIterator;
import org.neodatis.odb.core.layers.layer4.ObjectOidIterator;
import org.neodatis.odb.core.layers.layer4.StorageEngine;
import org.neodatis.odb.core.query.InternalQuery;
import org.neodatis.odb.core.query.ValuesQuery;
import org.neodatis.odb.core.query.criteria.CriteriaQuery;
import org.neodatis.odb.core.query.criteria.Criterion;
import org.neodatis.odb.core.query.values.ValuesCriteriaQuery;
import org.neodatis.odb.core.refactor.CheckMetaModelResult;
import org.neodatis.odb.core.refactor.RefactorManager;
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.odb.core.trigger.DeleteTrigger;
import org.neodatis.odb.core.trigger.InsertTrigger;
import org.neodatis.odb.core.trigger.OIDTrigger;
import org.neodatis.odb.core.trigger.SelectTrigger;
import org.neodatis.odb.core.trigger.TriggerManager;
import org.neodatis.odb.core.trigger.UpdateTrigger;
import org.neodatis.tool.wrappers.list.IOdbList;

public interface SessionEngine {
	
	// storing from layer 1 to layer 4
	NonNativeObjectInfo layer1ToLayer2(Object object);
	IOdbList<OidAndBytes> layer2ToLayer3(NonNativeObjectInfo nnoi);
	OID layer3ToLayer4(IOdbList<OidAndBytes> oidsAndBytes);
	
	// retrieving from layer 4 to layer 1
	OidAndBytes layer4ToLayer3(OID oid);
	/**
	 * 
	 * @param oidAndBytes
	 * @param full if false, only load the object header
	 * @param depth The recursion depth conversion must go to
	 * @return
	 */
	NonNativeObjectInfo layer3ToLayer2(OidAndBytes oidAndBytes, boolean full, int depth);
	Object layer2ToLayer1(NonNativeObjectInfo nnoi, InstanceBuilderContext context);
	
	ObjectOid store(Object object);
	/**force the oid of an object
	 * @param oid
	 * @param object
	 */
	ObjectOid store(ObjectOid oid, Object object);

	Object getObjectFromOid(ObjectOid oid,boolean throwExceptionIfNotFound, InstanceBuilderContext context);
	NonNativeObjectInfo getMetaObjectFromOid(ObjectOid oid, boolean throwExceptionIfNotFound, InstanceBuilderContext context);
	ObjectInfoHeader getMetaHeaderFromOid(ObjectOid oid, boolean throwExceptionIfNotFound, boolean useCache); 
	
	Session getSession();
	ObjectOidIterator getObjectOidIterator(ClassOid classOid, ObjectOidIterator.Way way);
	ClassOidIterator getClassOidIterator();
	/**
	 * @param oid
	 * @param involvedFields
	 * @param orderByFieldNames
	 * @param optimizeObjectCompararison
	 * @return
	 */
	AttributeValuesMap getFieldValuesFromOid(OID oid, HashSet<String> fields, boolean optimizeObjectCompararison, int depth);
	/**
	 * @param o
	 * @return
	 */
	ObjectOid delete(Object o, boolean cascade);
	/**
	 * @param object
	 * @param b
	 * @return
	 */
	ObjectOid getObjectOid(Object object, boolean b);
	/**
	 * @param clazz
	 * @param criterion
	 * @return
	 */
	CriteriaQuery criteriaQuery(Class clazz, Criterion criterion);
	CriteriaQuery criteriaQuery(String className, Criterion criterion);
	CriteriaQuery criteriaQuery(String className);
	/**
	 * @param clazz
	 * @return
	 */
	CriteriaQuery criteriaQuery(Class clazz);
	/**
	 * @param clazz
	 * @param inMemory
	 * @param i
	 * @param j
	 * @return
	 */
	<T>Objects<T> execute(InternalQuery query);
	/**
	 * @param oid
	 */
	void deleteObjectWithOid(ObjectOid oid, boolean cascade);
	/**
	 * @param query
	 * @return
	 */
	Values getValues(ValuesQuery query);
	/**
	 * @param newFileName
	 */
	void defragmentTo(String newFileName);
	/**
	 * @param name
	 * @param trigger
	 */
	void addUpdateTriggerFor(String name, UpdateTrigger trigger);
	/**
	 * @param name
	 * @param trigger
	 */
	void addInsertTriggerFor(String name, InsertTrigger trigger);

	void addOidTriggerFor(String className, OIDTrigger trigger);
	void removeOidTrigger(String className, OIDTrigger trigger);
	/**
	 * @param name
	 * @param trigger
	 */
	void addDeleteTriggerFor(String name, DeleteTrigger trigger);
	/**
	 * @param name
	 * @param trigger
	 */
	void addSelectTriggerFor(String name, SelectTrigger trigger);
	/**
	 * @param object
	 */
	void disconnect(Object object);
	/**
	 * @param object
	 */
	void reconnect(Object object);
	/**
	 * @param fullClassName
	 * @param name
	 * @param indexFields
	 * @param verbose
	 * @param b
	 */
	void addIndexOn(String fullClassName, String name, String[] indexFields, boolean verbose, boolean b);
	/**
	 * @param fullClassName
	 * @param indexName
	 * @param verbose
	 */
	void rebuildIndex(String fullClassName, String indexName, boolean verbose);
	/**
	 * @param fullClassName
	 * @param indexName
	 * @param verbose
	 */
	void deleteIndex(String fullClassName, String indexName, boolean verbose);
	/**
	 * @return
	 */
	RefactorManager getRefactorManager();
	/**
	 * @param fullClassName
	 * @param b
	 * @return
	 */
	ClassInfoList introspectClass(String fullClassName);
	/**
	 * 
	 */
	void close();
	/**
	 * @return
	 */
	StorageEngine getStorageEngine();
	
	ObjectIntrospector getObjectIntrospector();
	/**Store a list of class infos
	 * @param ciList
	 */
	void storeClassInfos(ClassInfoList ciList);
	/** Store one class info
	 * @param ci
	 */
	void storeClassInfo(ClassInfo ci);
	/**
	 * @param metaModel 
	 * @return
	 */
	MetaModel loadMetaModel(MetaModel metaModel);
	/**
	 * @param oid
	 * @return
	 */
	boolean existOid(OID oid);
	public <T> Objects<T> getMetaObjects(InternalQuery query);
	/**
	 * @return
	 */
	String getFileFormatVersion();
	/**
	 * @return
	 */
	TriggerManager getTriggerManager();
	/**
	 * 
	 */
	void rollback();
	/**
	 * 
	 */
	void commit();
	public ObjectOid storeMeta(ObjectOid oid, NonNativeObjectInfo nnoi);
	/**
	 * @param currentCIs
	 * @return
	 */
	CheckMetaModelResult checkMetaModelCompatibility(Map<String, ClassInfo> currentCIs);
	/**
	 * @param query
	 * @return
	 */
	BigInteger count(Query query);
	public NonNativeObjectInfo layer3ToLayer2(IOdbList<OidAndBytes> oabs, boolean full, Map<OID, OID> oidsToReplace, int depth);
	public ClassInfo classInfoFromBytes(OidAndBytes oab, boolean full);
	public OidAndBytes classInfoToBytes(ClassInfo ci);
	public OidAndBytes getBytesOfObjectFromOid(ObjectOid oid, boolean throwExceptionIfNotExist);
	/**Sets a new trigger manager
	 * @param triggerManager
	 */
	void setTriggerManager(TriggerManager triggerManagerImpl);
	/** refresh an object : reload the object.
	 * @param o
	 * @param depth
	 */
	void refresh(Object o, int depth);
	/**
	 * @param clazz
	 * @param criterion
	 * @return
	 */
	ValuesQuery queryValues(Class clazz, Criterion criterion);
	/**
	 * @param className
	 * @param criterion
	 * @return
	 */
	ValuesQuery queryValues(String className, Criterion criterion);
	/**
	 * @param className
	 * @return
	 */
	ValuesQuery queryValues(String className);
	/**
	 * @param clazz
	 * @return
	 */
	ValuesQuery queryValues(Class clazz);
	
	/**
	 * Create a query values for the object for the object of the specific oid
	 * @param clazz
	 * @param oid
	 * @return
	 */
	ValuesCriteriaQuery queryValues(Class clazz, ObjectOid oid);
	
	/**Sends a message to the server
	 * @param message The message to be sent
	 * @return The response message
	 */
	Message sendMessage(Message message);
	
	/** to indicate if the database is new or not */
	boolean isNewDatabase();
	void writeDatabaseHeader(DatabaseInfo di);
	

	
}
