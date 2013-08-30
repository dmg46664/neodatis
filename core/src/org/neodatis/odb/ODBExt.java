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

import java.util.List;

import org.neodatis.odb.core.refactor.RefactorManager;
import org.neodatis.odb.core.server.message.GetFileMessageResponse;
import org.neodatis.odb.core.server.message.Message;
import org.neodatis.odb.core.server.message.SendFileMessageResponse;
import org.neodatis.odb.core.server.message.process.RemoteProcess;
import org.neodatis.odb.core.server.message.process.RemoteProcessReturn;
import org.neodatis.odb.core.trigger.CloseListener;

/**
 * An interface to provider extended access to ODB.
 * 
 * @author osmadja
 * 
 */
public interface ODBExt {
	/**
	 * Gets the external OID of an Object. The external OID contains the ID of
	 * the database + the oid of the object. The External OID can be used to
	 * identify objects outside the ODB database as it should be unique across
	 * databases. It can be used for example to implement a replication process.
	 * 
	 * @param object
	 * @return
	 */
	ExternalObjectOid getObjectExternalOID(Object object);

	/**
	 * Get the Database ID
	 * 
	 * @return
	 */
	DatabaseId getDatabaseId();

	/**
	 * Convert an OID to External OID
	 * 
	 * @param oid
	 * @return The external OID
	 */
	ExternalOID convertToExternalOID(ObjectOid oid);

	/**
	 * Gets the current transaction Id
	 * 
	 * @return The current transaction Id
	 */

	TransactionId getCurrentTransactionId();

	/**Returns the object version of the object that has the specified OID
	 * 
	 * @param oid
	 * @param useCache if false, force a disk read. else use the version that has already been loaded in the cache
	 * @return
	 */
	long getObjectVersion(ObjectOid oid, boolean useCache);

	/**
	 * Returns the object creation date in ms since 1/1/1970
	 * 
	 * @param oid
	 * @return The creation date
	 */
	public long getObjectCreationDate(ObjectOid oid);

	/**
	 * Returns the object last update date in ms since 1/1/1970
	 * 
	 * @param oid
	 * @param useCache if false, force a disk read. else use the date that has already been loaded in the cache
	 * @return The last update date
	 */
	public long getObjectUpdateDate(ObjectOid oid, boolean useCache);
	
	public RefactorManager getRefactorManager();

	/**To force the oid of an object
	 * @param oid
	 * @param object
	 */
	ObjectOid store(ObjectOid oid, Object object);
	

	/**
	 * Sends a message to the server
	 * @param message The message to be sent
	 * @return The response message
	 */
	Message sendMessage(Message message);
	
	/**
	 * 
	 * @param localFileName The full name of the local file
	 * @param remoteFileName The name of the remote file
	 * @param putFileInServerInbox if true the file is copied on server in ${server.inbox}/remoteFileName, if false, the file is copied on server in remoteFileName (abslolute path) 
	 * @return
	 */
	SendFileMessageResponse sendFile(String localFileName, String remoteFileName, boolean putFileInServerInbox);

	/** Sends a file to the server using the local file name as the remote file name and file will be copied in server inbox
	 * 
	 * @param localFileName The full name of the local file
	 * @return
	 */
	SendFileMessageResponse sendFile(String localFileName);

	/**Execute a process on the server
	 * 
	 * @param process The process to be executed
	 * @param synchronous To specify if process must be implemented asynchronously. If true, the server only return after execution. If false, the server creates a thread to execute the process and returns immedialty
	 * @return The process execution response. If asynchronous (synchronous=false), the response is a AsyncProcessId that contains a key to be used to retrieve the return (after execution) 
	 */
	RemoteProcessReturn executeRemoteProcess(RemoteProcess process, boolean synchronous);
	
	/** Specify a list of classes on which NeoDatis must not call the triggers
	 * 
	 * @param classes
	 */
	void dontCallTriggersForClasses(List<Class> classes);

	/** to convert an oid string representation to the ObjectOid*/
	ObjectOid objectOidFromString(String id);

	/** Gets a file from the server
	 * 
	 * @param remoteFileInbox To say the file is in the server inbox directory
	 * @param remoteFileName The name of the file
	 * @param localFileInbox To tell NeoDatis to copy the file in the local inbox ditectory
	 * @param localFileName The local file name
	 * @return
	 */
	public GetFileMessageResponse getFile(boolean remoteFileInbox, String remoteFileName, boolean localFileInbox, String localFileName);
	
	/**Gets a file from the server. It will get the file from the server inbox directory and copy it to the local inbox directory
	 * 
	 * @param remoteFileName
	 * @return
	 */
	public GetFileMessageResponse getFile(String remoteFileName);

	/** disable all existing triggers
	 * 
	 */
	void disableTriggers();
	/** enable all existing triggers
	 * 
	 */
	void enableTriggers();

	/** Adds a close listener
	 * 
	 * @param l
	 */
	void addCloseListener(CloseListener l);
}