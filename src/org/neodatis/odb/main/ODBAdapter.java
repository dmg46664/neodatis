/*
NeoDatis ODB : Native Object Database (odb.info@neodatis.org)
Copyright (C) 2007 NeoDatis Inc. http://www.neodatis.org

"This file is part of the NeoDatis ODB open source object database".

NeoDatis ODB is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

NeoDatis ODB is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.neodatis.odb.main;

import java.math.BigInteger;
import java.util.Collection;

import org.neodatis.odb.ClassRepresentation;
import org.neodatis.odb.DatabaseStartupManager;
import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisEventType;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBExt;
import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.Objects;
import org.neodatis.odb.Query;
import org.neodatis.odb.Values;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.event.NeoDatisEventListener;
import org.neodatis.odb.core.layers.layer2.instance.InstanceBuilderContext;
import org.neodatis.odb.core.layers.layer2.meta.ClassInfo;
import org.neodatis.odb.core.query.InternalQuery;
import org.neodatis.odb.core.query.ValuesQuery;
import org.neodatis.odb.core.query.criteria.CriteriaQuery;
import org.neodatis.odb.core.query.criteria.CriteriaQueryImpl;
import org.neodatis.odb.core.query.criteria.Criterion;
import org.neodatis.odb.core.query.nq.NativeQuery;
import org.neodatis.odb.core.query.values.ValuesCriteriaQuery;
import org.neodatis.odb.core.session.Session;
import org.neodatis.odb.core.session.SessionEngine;
import org.neodatis.odb.core.trigger.DeleteTrigger;
import org.neodatis.odb.core.trigger.InsertTrigger;
import org.neodatis.odb.core.trigger.OIDTrigger;
import org.neodatis.odb.core.trigger.SelectTrigger;
import org.neodatis.odb.core.trigger.TriggerManager;
import org.neodatis.odb.core.trigger.UpdateTrigger;
import org.neodatis.tool.DLogger;

/**
 * A basic adapter for ODB interface
 * 
 * @author osmadja
 * 
 */
public abstract class ODBAdapter implements ODB {

	protected SessionEngine sessionEngine;
	protected Session session;

	private ODBExt ext;

	public ODBAdapter(Session session) {
		super();
		this.session = session;
		this.sessionEngine = session.getEngine();

		DatabaseStartupManager manager = session.getConfig()
				.getDatabaseStartupManager();
		if (manager != null) {
			manager.start(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.main.IODB#commit()
	 */
	public void commit() {
		session.commit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.main.IODB#rollback()
	 */
	public void rollback() {
		session.rollback();
	}

	/*
	 * @depracated
	 */
	public void commitAndClose() {
		session.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.main.IODB#store(java.lang.Object)
	 */
	public ObjectOid store(Object object) {
		return sessionEngine.store(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @deprecated
	 * 
	 * @see org.neodatis.odb.main.IODB#getObjects(java.lang.Class)
	 */
	public <T> Objects<T> getObjects(Class clazz) {
		return sessionEngine.execute(new CriteriaQueryImpl(clazz));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.main.IODB#getObjects(java.lang.Class, boolean)
	 */
	public <T> Objects<T> getObjects(Class clazz, boolean inMemory) {
		InternalQuery q = new CriteriaQueryImpl(clazz);
		q.getQueryParameters().setInMemory(inMemory);
		return sessionEngine.execute(q);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.main.IODB#getObjects(java.lang.Class, boolean, int,
	 * int)
	 */
	public <T> Objects<T> getObjects(Class clazz, boolean inMemory,
			int startIndex, int endIndex) {
		InternalQuery q = new CriteriaQueryImpl(clazz);
		q.getQueryParameters().setInMemory(inMemory).setStartIndex(startIndex)
				.setEndIndex(endIndex);
		return sessionEngine.execute(q);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.main.IODB#close()
	 */
	public void close() {
		commitAndClose();
	}

	public ObjectOid delete(Object object) {
		return sessionEngine.delete(object, false);
	}

	public void deleteAll(Collection objects) {
		for(Object o:objects){
			sessionEngine.delete(o, false);
		}
	}

	/**
	 * Delete an object from the database with the id
	 * 
	 * @param oid
	 *            The object id to be deleted @
	 */
	public void deleteObjectWithId(ObjectOid oid) {
		sessionEngine.deleteObjectWithOid(oid, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.main.IODB#getObjects(org.neodatis.odb.core.query.IQuery)
	 */
	public <T> Objects<T> getObjects(Query query) {
		InternalQuery iq = (InternalQuery) query;
		iq.setSessionEngine(sessionEngine);
		return sessionEngine.execute(iq);
	}

	public Values getValues(ValuesQuery query) {
		return sessionEngine.getValues(query);
	}

	public BigInteger count(CriteriaQuery query) {
		return sessionEngine.count(query);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.main.IODB#getObjects(org.neodatis.odb.core.query.IQuery,
	 * boolean)
	 */
	public <T> Objects<T> getObjects(Query query, boolean inMemory) {
		query.getQueryParameters().setInMemory(inMemory);
		return sessionEngine.execute((InternalQuery) query);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.main.IODB#getObjects(org.neodatis.odb.core.query.IQuery,
	 * boolean, int, int)
	 */
	public <T> Objects<T> getObjects(Query query, boolean inMemory,
			int startIndex, int endIndex) {
		query.getQueryParameters().setInMemory(inMemory).setStartIndex(
				startIndex).setEndIndex(endIndex);
		return sessionEngine.execute((InternalQuery) query);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.main.IODB#getSession()
	 */
	public Session getSession() {
		return session;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.main.IODB#getObjectId(java.lang.Object)
	 */
	public ObjectOid getObjectId(Object object) {
		return sessionEngine.getObjectOid(object, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.main.IODB#getObjectFromId(long)
	 */
	public Object getObjectFromId(ObjectOid oid) {
		return sessionEngine.getObjectFromOid(oid, true,
				new InstanceBuilderContext());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.neodatis.odb.main.IODB#defragmentTo(java.lang.String)
	 */
	public void defragmentTo(String newFileName) {
		sessionEngine.defragmentTo(newFileName);
	}

	public ClassRepresentation getClassRepresentation(Class clazz) {
		return getClassRepresentation(clazz.getName());
	}

	public ClassRepresentation getClassRepresentation(String fullClassName) {
		ClassInfo classInfo = session.getClassInfo(fullClassName);
		return new DefaultClassRepresentation(sessionEngine, classInfo);
	}

	/** or shutdown hook */
	public void run() {
		if (!session.isClosed()) {
			DLogger
					.debug("ODBFactory has not been closed and VM is exiting : force ODBFactory close");
			session.close();
		}
	}

	public void addUpdateTrigger(Class clazz, UpdateTrigger trigger) {
		// if (trigger instanceof ServerUpdateTrigger) {
		// throw new
		// ODBRuntimeException(NeoDatisError.CAN_NOT_ASSOCIATE_SERVER_TRIGGER_TO_LOCAL_OR_CLIENT_ODB.addParameter(trigger
		// .getClass().getName()));
		// }
		String className = TriggerManager.ALL_CLASS_TRIGGER;
		if (clazz != null) {
			className = clazz.getName();
		}

		sessionEngine.addUpdateTriggerFor(className, trigger);
	}

	public void addOidTrigger(Class clazz, OIDTrigger trigger) {
		// if (trigger instanceof ServerInsertTrigger) {
		// throw new
		// ODBRuntimeException(NeoDatisError.CAN_NOT_ASSOCIATE_SERVER_TRIGGER_TO_LOCAL_OR_CLIENT_ODB.addParameter(trigger
		// .getClass().getName()));
		// }
		String className = TriggerManager.ALL_CLASS_TRIGGER;
		if (clazz != null) {
			className = clazz.getName();
		}
		sessionEngine.addOidTriggerFor(className, trigger);
	}
	
	public void removeOidTrigger(Class clazz, OIDTrigger trigger) {
		String className = TriggerManager.ALL_CLASS_TRIGGER;
		if (clazz != null) {
			className = clazz.getName();
		}

		sessionEngine.removeOidTrigger(className,trigger);
	}

	public void addInsertTrigger(Class clazz, InsertTrigger trigger) {
		// if (trigger instanceof ServerInsertTrigger) {
		// throw new
		// ODBRuntimeException(NeoDatisError.CAN_NOT_ASSOCIATE_SERVER_TRIGGER_TO_LOCAL_OR_CLIENT_ODB.addParameter(trigger
		// .getClass().getName()));
		// }
		String className = TriggerManager.ALL_CLASS_TRIGGER;
		if (clazz != null) {
			className = clazz.getName();
		}
		sessionEngine.addInsertTriggerFor(className, trigger);
	}

	public void addDeleteTrigger(Class clazz, DeleteTrigger trigger) {
		// if (trigger instanceof ServerDeleteTrigger) {
		// throw new
		// ODBRuntimeException(NeoDatisError.CAN_NOT_ASSOCIATE_SERVER_TRIGGER_TO_LOCAL_OR_CLIENT_ODB.addParameter(trigger
		// .getClass().getName()));
		// }
		String className = TriggerManager.ALL_CLASS_TRIGGER;
		if (clazz != null) {
			className = clazz.getName();
		}

		sessionEngine.addDeleteTriggerFor(className, trigger);
	}

	public void addSelectTrigger(Class clazz, SelectTrigger trigger) {
		// if (trigger instanceof ServerSelectTrigger) {
		// throw new
		// ODBRuntimeException(NeoDatisError.CAN_NOT_ASSOCIATE_SERVER_TRIGGER_TO_LOCAL_OR_CLIENT_ODB.addParameter(trigger
		// .getClass().getName()));
		// }
		String className = TriggerManager.ALL_CLASS_TRIGGER;
		if (clazz != null) {
			className = clazz.getName();
		}

		sessionEngine.addSelectTriggerFor(className, trigger);
	}

	public ODBExt ext() {
		if (isClosed()) {
			throw new NeoDatisRuntimeException(NeoDatisError.ODB_IS_CLOSED
					.addParameter(getName()));
		}
		if (ext == null) {
			ext = new ODBExtImpl(sessionEngine);
		}
		return ext;
	}

	public void disconnect(Object object) {
		sessionEngine.disconnect(object);
	}

	public void reconnect(Object object) {
		sessionEngine.reconnect(object);

	}

	public boolean isClosed() {
		return session.isClosed();
	}

	public String getName() {
		return session.getBaseIdentification().getBaseId();
	}

	public void registerEventListenerFor(NeoDatisEventType neoDatisEventType,
			NeoDatisEventListener eventListener) {
		session.registerEventListenerFor(neoDatisEventType, eventListener);
	}

	public void refresh(Object o, int depth) {
		sessionEngine.refresh(o, depth);
	}

	public Query query(Class clazz, Criterion criterion) {
		return sessionEngine.criteriaQuery(clazz, criterion);
	}

	public Query query(String className, Criterion criterion) {
		return sessionEngine.criteriaQuery(className, criterion);
	}

	public Query query(String className) {
		return sessionEngine.criteriaQuery(className);
	}

	public Query query(Class clazz) {
		return sessionEngine.criteriaQuery(clazz);
	}

	public Query query(NativeQuery q) {
		q.setSessionEngine(sessionEngine);
		return q;
	}

	public Query query(Query q) {
		InternalQuery iq = (InternalQuery) q;
		iq.setSessionEngine(sessionEngine);
		return q;
	}

	public ValuesQuery queryValues(Class clazz, Criterion criterion) {
		return sessionEngine.queryValues(clazz, criterion);
	}

	public ValuesQuery queryValues(String className, Criterion criterion) {
		return sessionEngine.queryValues(className, criterion);
	}

	public ValuesQuery queryValues(String className) {
		return sessionEngine.queryValues(className);
	}

	public ValuesQuery queryValues(Class clazz) {
		return sessionEngine.queryValues(clazz);
	}
	public ValuesCriteriaQuery queryValues(Class clazz, ObjectOid oid) {
		return sessionEngine.queryValues(clazz, oid);
	}
	public NeoDatisConfig getConfig() {
		return session.getConfig();
	}

}
