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
package org.neodatis.odb;

import java.math.BigInteger;
import java.util.Collection;

import org.neodatis.odb.core.event.NeoDatisEventListener;
import org.neodatis.odb.core.query.ValuesQuery;
import org.neodatis.odb.core.query.criteria.CriteriaQuery;
import org.neodatis.odb.core.query.criteria.Criterion;
import org.neodatis.odb.core.query.nq.NativeQuery;
import org.neodatis.odb.core.query.values.ValuesCriteriaQuery;
import org.neodatis.odb.core.trigger.DeleteTrigger;
import org.neodatis.odb.core.trigger.InsertTrigger;
import org.neodatis.odb.core.trigger.OIDTrigger;
import org.neodatis.odb.core.trigger.SelectTrigger;
import org.neodatis.odb.core.trigger.UpdateTrigger;

/**
 * The main ODB public interface: It is what the user sees.
 * 
 * @author osmadja
 * 
 */
public interface ODB {

	/**
	 * Commit all the change of the database @
	 * */
	void commit();

	/**
	 * Undo all uncommitted changes
	 * */
	void rollback();

	/**
	 * Closes the database. Automatically commit uncommitted changes
	 * */
	void close();

	/**
	 * Store a plain java Object in the ODB Database
	 * 
	 * @param object
	 *            A plain Java Object
	 */
	ObjectOid store(Object object);

	/**
	 * Get all objects of a specific type
	 * 
	 * @param clazz
	 *            The type of the objects
	 * @return The list of objects
	 * @deprecated Use odb.query(Class clazz).objects(); instead
	 */
	<T> Objects<T> getObjects(Class clazz);

	
	
	/**
	 * Delete an object from database
	 * 
	 * @param object
	 */
	ObjectOid delete(Object object);

	/**
	 * Delete a collection of object
	 * 
	 * @param objects The objects to be deleted
	 */
	void deleteAll(Collection objects);

	
	/**
	 * Delete an object from the database with the id
	 * 
	 * @param oid
	 *            The object id to be deleted
	 */
	void deleteObjectWithId(ObjectOid oid);

	/**
	 * Search for objects that matches the query.
	 * 
	 * @param query
	 * @return The list of objects
	 * @deprecated
	 */
	<T> Objects<T> getObjects(Query query);

	/**
	 * Search for objects that matches the native query.
	 * 
	 * @param query
	 * @param inMemory
	 * @return The list of objects
	 * @deprecated
	 * 
	 */
	<T> Objects<T> getObjects(Query query, boolean inMemory);

	/**
	 * Return a list of objects that matches the query
	 * 
	 * @param query
	 * @param inMemory
	 *            if true, preload all objects,if false,load on demand
	 * @param startIndex
	 *            The index of the first object
	 * @param endIndex
	 *            The index of the last object that must be returned
	 * @return A List of objects, if start index and end index are -1, they are
	 *         ignored. If not, the length of the sublist is endIndex -
	 *         startIndex
	 * @deprecated
	 */
	<T> Objects<T> getObjects(Query query, boolean inMemory, int startIndex, int endIndex);

	/**
	 * Returns the number of objects that satisfy the query
	 * 
	 * @param query
	 * @return The number of objects that satisfy the query
	 * 
	 */
	BigInteger count(CriteriaQuery query);

	/**
	 * Get the id of an ODB-aware object
	 * 
	 * @param object
	 * @return The ODB internal object id
	 */
	ObjectOid getObjectId(Object object);

	/**
	 * Get the object with a specific id *
	 * 
	 * @param id
	 * @return The object with the specific id @
	 */

	Object getObjectFromId(ObjectOid id);

	/**
	 * Defragment ODB Database
	 * 
	 * @param newFileName
	 * 
	 */
	void defragmentTo(String newFileName);

	/**
	 * Get an abstract representation of a class
	 * 
	 * @param clazz
	 * @return a public meta-representation of a class
	 * 
	 */
	ClassRepresentation getClassRepresentation(Class clazz);

	/**
	 * Get an abstract representation of a class
	 * 
	 * @param fullClassName
	 * @return a public meta-representation of a class
	 * 
	 */
	ClassRepresentation getClassRepresentation(String fullClassName);

	/**
	 * Used to add an update trigger callback for the specific class
	 * 
	 * @param trigger
	 */
	void addUpdateTrigger(Class clazz, UpdateTrigger trigger);

	/**
	 * Used to add an insert trigger callback for the specific class
	 * 
	 * @param trigger
	 */
	void addInsertTrigger(Class clazz, InsertTrigger trigger);

	
	void addOidTrigger(Class clazz, OIDTrigger trigger);
	void removeOidTrigger(Class clazz, OIDTrigger trigger);
	
	/**
	 * USed to add a delete trigger callback for the specific class
	 * 
	 * @param trigger
	 */
	void addDeleteTrigger(Class clazz, DeleteTrigger trigger);

	/**
	 * Used to add a select trigger callback for the specific class
	 * 
	 * @param trigger
	 */
	void addSelectTrigger(Class clazz, SelectTrigger trigger);

	/** Get the extension of ODB to get access to advanced functions */
	ODBExt ext();

	/**@deprecated Reconnection is now automatic 
	 * 
	 * Used to reconnect an object to the current session */
	void reconnect(Object object);
	
	/** Ask NeoDatis to load data data of the object using the depth*/
	void refresh(Object o, int depth);

	/**
	 * Used to disconnect the object from the current session. The object is
	 * removed from the cache
	 */
	void disconnect(Object object);

	/**
	 * @return
	 */
	boolean isClosed();
	
	/**
	 * Return the name of the database
	 * @return the file name in local mode and the base id (alias) in client server mode.
	 */
	String getName();
	
	/** Used to let the user register listener for specific events
	 * 
	 * @param neoDatisEventType The type of the event to associate the listener to
	 * @param eventListener The listener
	 */
	void registerEventListenerFor(NeoDatisEventType neoDatisEventType, NeoDatisEventListener eventListener);
	
	/** Returns a query
	 * 
	 */
	Query query(Class clazz, Criterion criteria);
	Query query(Class clazz);
	Query query(String className, Criterion criteria);
	Query query(String className);
	Query query(NativeQuery q);
	Query query(Query q);

	

	/**
	 * @param class1
	 * @return
	 */
	ValuesQuery queryValues(Class clazz, Criterion criteria);
	ValuesQuery queryValues(String className, Criterion criteria);
	ValuesQuery queryValues(String className);
	ValuesQuery queryValues(Class clazz);

	/** Create a values query on the object with the specific oid
	 * 
	 * @param clazz
	 * @param oid
	 * @return
	 */
	ValuesCriteriaQuery queryValues(Class clazz, ObjectOid oid);

	/** Retrieve the neoDatisConfig instance
	 * 
	 * @return
	 */
	NeoDatisConfig getConfig();

	
}