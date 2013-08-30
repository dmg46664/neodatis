package org.neodatis.odb.core.session;

import java.util.Observable;

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

/** A wrapper to change the execution type of any session
 * 
 * @author olivier
 *
 */
public class SessionWrapper implements Session {
	protected Session session;
	protected int executionType;

	public SessionWrapper(Session session, int executionType) {
		this.executionType = executionType;
		this.session = session;
	}

	/** Returns the own execution type instead of the session executionType
	 * 
	 */
	public int getExecutionType() {
		return executionType;
	}

	
	public ClassInfoList addClasses(ClassInfoList ciList) {
		return session.addClasses(ciList);
	}

	public void addCommitListener(CommitListener commitListener) {
		session.addCommitListener(commitListener);
	}

	public void addObjectToCache(OID oidCrossSession, Object o, ObjectInfoHeader oih) {
		session.addObjectToCache(oidCrossSession, o, oih);
	}

	public void clearCache() {
		session.clearCache();
	}

	public void close() {
		session.close();
	}

	public void commit() {
		session.commit();
	}

	public BaseIdentification getBaseIdentification() {
		return session.getBaseIdentification();
	}

	public Cache getCache() {
		return session.getCache();
	}

	public ClassInfo getClassInfo(String fullClassName) {
		return session.getClassInfo(fullClassName);
	}

	public NeoDatisConfig getConfig() {
		return session.getConfig();
	}

	public TransactionId getCurrentTransactionId() {
		return session.getCurrentTransactionId();
	}

	public DatabaseId getDatabaseId() {
		return session.getDatabaseId();
	}

	public SessionEngine getEngine() {
		return session.getEngine();
	}

	public EventManager getEventManager() {
		return session.getEventManager();
	}


	public String getId() {
		return session.getId();
	}

	public MetaModel getMetaModel() {
		return session.getMetaModel();
	}

	public ObjectInfoHeader getObjectInfoHeaderFromOid(OID oidCrossSession, boolean b) {
		return session.getObjectInfoHeaderFromOid(oidCrossSession, b);
	}

	public OidGenerator getOidGenerator() {
		return session.getOidGenerator();
	}

	public boolean isClosed() {
		return session.isClosed();
	}

	public boolean isLocal() {
		return session.isLocal();
	}

	public boolean isRollbacked() {
		return session.isRollbacked();
	}

	public void lockClassForSession(String fullClassName, long timeout) throws InterruptedException {
		session.lockClassForSession(fullClassName, timeout);
	}

	public void lockOidForSession(OID oid, long timeout) throws InterruptedException {
		session.lockOidForSession(oid, timeout);
	}

	public void registerEventListenerFor(NeoDatisEventType neoDatisEventType, NeoDatisEventListener eventListener) {
		session.registerEventListenerFor(neoDatisEventType, eventListener);
	}

	public void rollback() {
		session.rollback();
	}

	public void setId(String sessionId) {
		session.setId(sessionId);
	}

	public void setMetaModel(MetaModel metaModel) {
		session.setMetaModel(metaModel);
	}

	public boolean transactionIsPending() {
		return session.transactionIsPending();
	}

	public void unlockClass(String fullClassName) throws InterruptedException {
		session.unlockClass(fullClassName);
	}

	public void unlockObjectsAndClasses() {
		session.unlockObjectsAndClasses();
	}

	public void unlockOidForSession(OID oid) throws InterruptedException {
		session.unlockOidForSession(oid);
	}

	public void update(Observable o, Object arg) {
		session.update(o, arg);
	}

	public void updateMetaModel() {
		session.updateMetaModel();
	}

	public void addCloseListener(CloseListener closeListener) {
		session.addCloseListener(closeListener);
	}

	public Object getUserParameter(String name, boolean remove) {
		return session.getUserParameter(name, remove);
	}

	public void setUserParameter(String name, Object object) {
		session.setUserParameter(name, object);
		
	}

	
	
}
