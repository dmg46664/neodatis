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
package org.neodatis.odb.core.trigger;

import java.util.Iterator;
import java.util.List;

import org.neodatis.odb.ObjectOid;
import org.neodatis.odb.ObjectRepresentation;
import org.neodatis.odb.core.IError;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer2.meta.ObjectReference;
import org.neodatis.odb.core.layers.layer2.meta.ObjectRepresentationImpl;
import org.neodatis.odb.core.layers.layer2.meta.NonNativeObjectInfo;
import org.neodatis.odb.core.session.ExecutionType;
import org.neodatis.odb.core.session.Session;
import org.neodatis.odb.main.ODBAdapter;
import org.neodatis.odb.main.ODBForTrigger;
import org.neodatis.tool.DLogger;
import org.neodatis.tool.wrappers.OdbString;
import org.neodatis.tool.wrappers.list.IOdbList;

public class TriggerManagerImpl implements TriggerManager {

	Session session;

	protected Triggers triggers;
	
	protected boolean triggersAreActive;

	public TriggerManagerImpl(Session session) {
		super();
		this.session = session;
		this.triggers = new TriggersImpl();
		this.triggersAreActive = true;
	}

	public TriggerManagerImpl(Session session, Triggers triggers) {
		super();
		this.session = session;
		this.triggers = triggers;
		this.triggersAreActive = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.trigger.ITriggerManager#manageInsertTriggerBefore
	 * (java.lang.Object)
	 */
	public boolean manageInsertTriggerBefore(String className, Object object) {

		if(!triggersAreActive){
			return false;
		}
		
		if (!callTriggerOnClass(className)) {
			return false;
		}

		if (hasInsertTriggersFor(className)) {
			InsertTrigger trigger = null;
			Iterator iterator = triggers.getListOfInsertTriggersFor(className).iterator();
			while (iterator.hasNext()) {
				trigger = (InsertTrigger) iterator.next();
				if (trigger.getOdb() == null) {
					trigger.setOdb(new ODBForTrigger(session));
				}else{
					ODBAdapter oa = (ODBAdapter) trigger.getOdb();
					if(oa.getSession()!=session){
						trigger.setOdb(new ODBForTrigger(session));
					}
				}
				try {
					if (!isNull(object)) {
						trigger.beforeInsert(transform(object, trigger));
					}
				} catch (Exception e) {
					IError warning = NeoDatisError.BEFORE_INSERT_TRIGGER_HAS_THROWN_EXCEPTION.addParameter(trigger.getClass().getName()).addParameter(
							OdbString.exceptionToString(e, false));
					if (session.getConfig().displayWarnings()) {
						DLogger.info(warning);
					}
				}
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.trigger.ITriggerManager#manageInsertTriggerAfter
	 * (java.lang.Object, org.neodatis.odb.core.ObjectOid, long)
	 */
	public void manageInsertTriggerAfter(String className, Object object, ObjectOid oid) {
		if(!triggersAreActive){
			return;
		}

		if (!callTriggerOnClass(className)) {
			return;
		}

		if (hasInsertTriggersFor(className)) {
			InsertTrigger trigger = null;
			IOdbList<InsertTrigger> triggers = getListOfInsertTriggersFor(className); 
			Iterator iterator = triggers.iterator();

			
			if(!triggers.isEmpty()){
				// if object is only a reference, actually retreive the real meta representation
				if(object instanceof ObjectReference){
					object = session.getEngine().getMetaObjectFromOid(oid, true, null);
				}
			}
			while (iterator.hasNext()) {
				trigger = (InsertTrigger) iterator.next();
				if (trigger.getOdb() == null) {
					trigger.setOdb(new ODBForTrigger(session));
				}else{
					ODBAdapter oa = (ODBAdapter) trigger.getOdb();
					if(oa.getSession()!=session){
						trigger.setOdb(new ODBForTrigger(session));
					}
				}
				try {
					trigger.afterInsert(transform(object, trigger), oid);
				} catch (Exception e) {
					IError warning = NeoDatisError.AFTER_INSERT_TRIGGER_HAS_THROWN_EXCEPTION.addParameter(trigger.getClass().getName()).addParameter(
							OdbString.exceptionToString(e, false));
					if (session.getConfig().displayWarnings()) {
						DLogger.info(warning);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.trigger.ITriggerManager#manageUpdateTriggerBefore
	 * (java.lang.Object, java.lang.Object, org.neodatis.odb.core.ObjectOid)
	 */
	public boolean manageUpdateTriggerBefore(String className, NonNativeObjectInfo oldNnoi, Object newObject, ObjectOid oid) {
		if(!triggersAreActive){
			return false;
		}

		if (!callTriggerOnClass(className)) {
			return false;
		}

		if (hasUpdateTriggersFor(className)) {
			UpdateTrigger trigger = null;
			Iterator iterator = getListOfUpdateTriggersFor(className).iterator();
			while (iterator.hasNext()) {
				trigger = (UpdateTrigger) iterator.next();
				if (trigger.getOdb() == null) {
					trigger.setOdb(new ODBForTrigger(session));
				}else{
					ODBAdapter oa = (ODBAdapter) trigger.getOdb();
					if(oa.getSession()!=session){
						trigger.setOdb(new ODBForTrigger(session));
					}
				}
				try {
					trigger.beforeUpdate(new ObjectRepresentationImpl(oldNnoi, session.getEngine().getObjectIntrospector()), transform(newObject, trigger
							), oid);
				} catch (Exception e) {
					IError warning = NeoDatisError.BEFORE_UPDATE_TRIGGER_HAS_THROWN_EXCEPTION.addParameter(trigger.getClass().getName()).addParameter(
							OdbString.exceptionToString(e, false));
					if (session.getConfig().displayWarnings()) {
						DLogger.info(warning);
					}
				}
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.trigger.ITriggerManager#manageUpdateTriggerAfter
	 * (java.lang.Object, java.lang.Object, org.neodatis.odb.core.ObjectOid)
	 */
	public void manageUpdateTriggerAfter(String className, NonNativeObjectInfo oldNnoi, Object newObject, ObjectOid oid) {
		if(!triggersAreActive){
			return ;
		}

		if (!callTriggerOnClass(className)) {
			return;
		}

		if (hasUpdateTriggersFor(className)) {
			UpdateTrigger trigger = null;
			Iterator iterator = getListOfUpdateTriggersFor(className).iterator();
			while (iterator.hasNext()) {
				trigger = (UpdateTrigger) iterator.next();
				if (trigger.getOdb() == null) {
					trigger.setOdb(new ODBForTrigger(session));
				}else{
					ODBAdapter oa = (ODBAdapter) trigger.getOdb();
					if(oa.getSession()!=session){
						trigger.setOdb(new ODBForTrigger(session));
					}
				}
				try {
					trigger.afterUpdate(new ObjectRepresentationImpl(oldNnoi, session.getEngine().getObjectIntrospector()), transform(newObject, trigger
							), oid);
				} catch (Exception e) {
					IError warning = NeoDatisError.AFTER_UPDATE_TRIGGER_HAS_THROWN_EXCEPTION.addParameter(trigger.getClass().getName()).addParameter(
							OdbString.exceptionToString(e, false));
					if (session.getConfig().displayWarnings()) {
						DLogger.info(warning);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.trigger.ITriggerManager#manageDeleteTriggerBefore
	 * (java.lang.Object, org.neodatis.odb.core.ObjectOid)
	 */
	public boolean manageDeleteTriggerBefore(String className, Object object, ObjectOid oid) {
		
		if(!triggersAreActive){
			return false;
		}

		if (!callTriggerOnClass(className)) {
			return false;
		}
		if (hasDeleteTriggersFor(className)) {
			DeleteTrigger trigger = null;
			Iterator iterator = getListOfDeleteTriggersFor(className).iterator();
			while (iterator.hasNext()) {
				trigger = (DeleteTrigger) iterator.next();
				if (trigger.getOdb() == null) {
					trigger.setOdb(new ODBForTrigger(session));
				}else{
					ODBAdapter oa = (ODBAdapter) trigger.getOdb();
					if(oa.getSession()!=session){
						trigger.setOdb(new ODBForTrigger(session));
					}
				}
				try {
					trigger.beforeDelete(transform(object, trigger), oid);
				} catch (Exception e) {
					IError warning = NeoDatisError.BEFORE_DELETE_TRIGGER_HAS_THROWN_EXCEPTION.addParameter(trigger.getClass().getName()).addParameter(
							OdbString.exceptionToString(e, true));
					if (session.getConfig().displayWarnings()) {
						DLogger.info(warning);
					}
				}
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.trigger.ITriggerManager#manageDeleteTriggerAfter
	 * (java.lang.Object, org.neodatis.odb.core.ObjectOid)
	 */
	public void manageDeleteTriggerAfter(String className, Object object, ObjectOid oid) {
		
		if(!triggersAreActive){
			return;
		}

		if (!callTriggerOnClass(className)) {
			return;
		}

		if (hasDeleteTriggersFor(className)) {
			DeleteTrigger trigger = null;
			Iterator iterator = getListOfDeleteTriggersFor(className).iterator();
			while (iterator.hasNext()) {
				trigger = (DeleteTrigger) iterator.next();
				if (trigger.getOdb() == null) {
					trigger.setOdb(new ODBForTrigger(session));
				}else{
					ODBAdapter oa = (ODBAdapter) trigger.getOdb();
					if(oa.getSession()!=session){
						trigger.setOdb(new ODBForTrigger(session));
					}
				}
				try {
					trigger.afterDelete(transform(object, trigger), oid);
				} catch (Exception e) {
					IError warning = NeoDatisError.AFTER_DELETE_TRIGGER_HAS_THROWN_EXCEPTION.addParameter(trigger.getClass().getName()).addParameter(
							OdbString.exceptionToString(e, false));
					if (session.getConfig().displayWarnings()) {
						DLogger.info(warning);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.neodatis.odb.core.trigger.ITriggerManager#manageSelectTriggerAfter
	 * (java.lang.Object, org.neodatis.odb.core.ObjectOid)
	 */
	public void manageSelectTriggerAfter(String className, Object object, ObjectOid oid) {
		
		if(!triggersAreActive){
			return;
		}

		if (!callTriggerOnClass(className)) {
			return;
		}

		if (hasSelectTriggersFor(className)) {
			SelectTrigger trigger = null;
			Iterator iterator = getListOfSelectTriggersFor(className).iterator();
			while (iterator.hasNext()) {
				trigger = (SelectTrigger) iterator.next();
				if (trigger.getOdb() == null) {
					trigger.setOdb(new ODBForTrigger(session));
				}else{
					ODBAdapter oa = (ODBAdapter) trigger.getOdb();
					if(oa.getSession()!=session){
						trigger.setOdb(new ODBForTrigger(session));
					}
				}
				if (!isNull(object)) {
					trigger.afterSelect(transform(object, trigger), oid);
				}
			}
		}
	}

	public boolean manageOidTrigger(String className, Object o, ObjectOid oid) {
		
		if(!triggersAreActive){
			return false;
		}

		if (!callTriggerOnClass(className)) {
			return false;
		}

		if (hasOidTriggersFor(className)) {
			OIDTrigger trigger = null;
			IOdbList<OIDTrigger> triggers = getListOfOidTriggersFor(className);
			Iterator<OIDTrigger> iterator = triggers.iterator();
			while (iterator.hasNext()) {
				trigger = iterator.next();
				if (trigger.getOdb() == null) {
					trigger.setOdb(new ODBForTrigger(session));
				}else{
					ODBAdapter oa = (ODBAdapter) trigger.getOdb();
					if(oa.getSession()!=session){
						trigger.setOdb(new ODBForTrigger(session));
					}
				}
				try {
					if (!isNull(o)) {
						//if(triggerIsCompatibleWithSession(trigger)){
							Object oo = transform(o,trigger);
							trigger.setOid(oo, oid);
						//}
					}
				} catch (Exception e) {
					IError warning = NeoDatisError.BEFORE_INSERT_TRIGGER_HAS_THROWN_EXCEPTION.addParameter(trigger.getClass().getName()).addParameter(
							OdbString.exceptionToString(e, false));
					if (session.getConfig().displayWarnings()) {
						DLogger.info(warning);
					}
				}
			}
		}
		return true;
	}

	private boolean triggerIsCompatibleWithSession(Trigger trigger) {
		boolean isClient = ExecutionType.isClient(trigger.getExecutionType()) && ExecutionType.isClient(session.getExecutionType());
		if(isClient){
			return true;
		}

		boolean isServer = ExecutionType.isServer(trigger.getExecutionType()) && ExecutionType.isServer(session.getExecutionType());
		if(isServer){
			return true;
		}
		return false;
	}

	protected boolean isNull(Object object) {
		return object == null;
	}

	/**
	 * For the default object trigger, no transformation is needed
	 */
	public Object transform(Object object, Trigger trigger) {
		if (ExecutionType.isClient(trigger.getExecutionType())) {
			return object;
		}
		// server mode
		if (object instanceof ObjectRepresentationImpl) {
			ObjectRepresentationImpl or = (ObjectRepresentationImpl) object;
			or.addObserver(session);
			return or;
		}
		if (object instanceof NonNativeObjectInfo) {
			ObjectRepresentationImpl or = new ObjectRepresentationImpl((NonNativeObjectInfo) object, session.getEngine().getObjectIntrospector());
			or.addObserver(session);
			return or;
		}
		return object;

	}

	// Triggers impl by delegation
	public void addDeleteTriggerFor(String className, DeleteTrigger trigger) {
		triggers.addDeleteTriggerFor(className, trigger);
	}

	public void addInsertTriggerFor(String className, InsertTrigger trigger) {
		triggers.addInsertTriggerFor(className, trigger);
	}

	public void addSelectTriggerFor(String className, SelectTrigger trigger) {
		triggers.addSelectTriggerFor(className, trigger);
	}

	public void addUpdateTriggerFor(String className, UpdateTrigger trigger) {
		triggers.addUpdateTriggerFor(className, trigger);
	}

	public IOdbList<Trigger> getListOfDeleteTriggersFor(String className) {
		return triggers.getListOfDeleteTriggersFor(className);
	}

	public IOdbList<InsertTrigger> getListOfInsertTriggersFor(String className) {
		return triggers.getListOfInsertTriggersFor(className);
	}

	public IOdbList<OIDTrigger> getListOfOidTriggersFor(String className) {
		return triggers.getListOfOidTriggersFor(className);
	}

	public IOdbList<Trigger> getListOfSelectTriggersFor(String className) {
		return triggers.getListOfSelectTriggersFor(className);
	}

	public IOdbList<Trigger> getListOfUpdateTriggersFor(String className) {
		return triggers.getListOfUpdateTriggersFor(className);
	}

	public boolean hasDeleteTriggersFor(String classsName) {
		return triggers.hasDeleteTriggersFor(classsName);
	}

	public boolean hasInsertTriggersFor(String className) {
		return triggers.hasInsertTriggersFor(className);
	}

	public boolean hasOidTriggersFor(String classsName) {
		return triggers.hasOidTriggersFor(classsName);
	}

	public boolean hasSelectTriggersFor(String className) {
		return triggers.hasSelectTriggersFor(className);
	}

	public boolean hasUpdateTriggersFor(String className) {
		return triggers.hasUpdateTriggersFor(className);
	}

	public void addOidTriggerFor(String className, OIDTrigger trigger) {
		triggers.addOidTriggerFor(className, trigger);
	}
	public void removeOidTrigger(String className, OIDTrigger trigger) {
		triggers.removeOidTrigger(className, trigger);
	}

	public void addClassesNotToCallTriggersOn(List<Class> classes) {
		triggers.addClassesNotToCallTriggersOn(classes);
	}

	public boolean callTriggerOnClass(String className) {
		return triggers.callTriggerOnClass(className);
	}

	public void resetClassesNotToCallTriggersOn() {
		triggers.resetClassesNotToCallTriggersOn();
	}

	public synchronized void disableTriggers() {
		this.triggersAreActive = false;
	}
	public synchronized void enableTriggers() {
		this.triggersAreActive = true;
	}

}
