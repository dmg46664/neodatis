package org.neodatis.odb.core.session;

import java.util.Observer;

import org.neodatis.odb.DatabaseId;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisEventType;
import org.neodatis.odb.OID;
import org.neodatis.odb.TransactionId;
import org.neodatis.odb.core.event.EventManager;
import org.neodatis.odb.core.event.NeoDatisEventListener;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfoList;
import org.neodatis.odb.core.layers.layer2.meta.MetaModel;
import org.neodatis.odb.core.layers.layer2.meta.ObjectInfoHeader;
import org.neodatis.odb.core.layers.layer4.BaseIdentification;
import org.neodatis.odb.core.layers.layer4.OidGenerator;
import org.neodatis.odb.core.trigger.CloseListener;
import org.neodatis.odb.core.trigger.CommitListener;


public interface Session extends Observer{

	boolean isRollbacked();
	boolean isClosed();
	String getId();
	MetaModel getMetaModel();
	Cache getCache();
	ClassInfoList addClasses(ClassInfoList ciList);
	ObjectInfoHeader getObjectInfoHeaderFromOid(OID oidCrossSession, boolean b);
	void addObjectToCache(OID oidCrossSession, Object o, ObjectInfoHeader oih);
	BaseIdentification getBaseIdentification();
	NeoDatisConfig getConfig();
	int getExecutionType();
	boolean isLocal();
	/**
	 * 
	 */
	void commit();
	/**
	 * 
	 */
	void rollback();
	/**
	 * 
	 */
	void close();
	/**
	 * @param fullClassName
	 */
	ClassInfo getClassInfo(String fullClassName);
	/**
	 * @return
	 */
	SessionEngine getEngine();
	/**
	 * @return
	 */
	DatabaseId getDatabaseId();
	/**
	 * @return
	 */
	TransactionId getCurrentTransactionId();
	/**
	 * 
	 */
	void clearCache();
	void addCommitListener(CommitListener commitListener);
	/** adds a listener that will be close on close */
	void addCloseListener(CloseListener closeListener);
	
	/**
	 * @param metaModel
	 */
	void setMetaModel(MetaModel metaModel);
	/**
	 * @return
	 */
	boolean transactionIsPending();
	/**
	 * @param sessionId
	 */
	void setId(String sessionId);
	public void lockOidForSession(OID oid, long timeout) throws InterruptedException;
	public void lockClassForSession(String fullClassName, long timeout) throws InterruptedException;
	public void unlockOidForSession(OID oid) throws InterruptedException;
	public void unlockClass(String fullClassName) throws InterruptedException;
	/** Release all objects and classes lock by this session
	 * 
	 */
	public void unlockObjectsAndClasses();
	
	/**
	 * @param neoDatisEventType
	 * @param eventListener
	 */
	void registerEventListenerFor(NeoDatisEventType neoDatisEventType, NeoDatisEventListener eventListener);
	/**
	 * 
	 */
	void updateMetaModel();
	/**
	 * @return The event manager of the session. Responsible for managing all events
	 */
	EventManager getEventManager();
	OidGenerator getOidGenerator();
	/** Sets a paraemter to the session
	 * 
	 * @param name
	 * @param object
	 */
	void setUserParameter(String name, Object object);
	/** Retrieve a parameter from session
	 * 
	 * @param name
	 * @param remove 
	 * @return null if parameter does not exist
	 */
	Object getUserParameter(String name, boolean remove);
}
