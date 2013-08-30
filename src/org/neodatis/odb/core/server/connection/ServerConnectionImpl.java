
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
package org.neodatis.odb.core.server.connection;

import java.util.Iterator;
import java.util.Map;

import org.neodatis.odb.OID;
import org.neodatis.odb.core.session.SessionEngine;
import org.neodatis.tool.DLogger;
import org.neodatis.tool.wrappers.OdbTime;
import org.neodatis.tool.wrappers.map.OdbHashMap;

public class ServerConnectionImpl implements IConnection {
	
	
	public static final String LOG_ID = "Connection";
	
	private String id;
	private SessionManager connectionManager;
	private SessionEngine sessionEngine;
	private String baseIdentifier;
	/** To keep locked id for this session : key = oid, value=timestamp (Long)*/
	private Map<OID,Long> oidsLockedForUpdate;
	private Map<String,Long> lockedClasses;
	
	/** Current action being executed*/
	private String currentAction;
	private long currentActionStart;
	
	private long lastActionDuration;
	private String lastAction;
	
	private int [] actions;
	
	public ServerConnectionImpl(SessionManager connectionManager, String connectionId, SessionEngine storageEngine) {
		this.connectionManager = connectionManager;
		this.id = connectionId;
		this.sessionEngine = storageEngine;
		this.baseIdentifier = storageEngine.getSession().getBaseIdentification().getBaseId();
		this.oidsLockedForUpdate = new OdbHashMap<OID, Long>();
		this.lockedClasses = new OdbHashMap<String, Long>();
		
		actions = new int[ConnectionAction.getNumberOfActions()];
	}


	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.server.connection.IConnection#getId()
	 */
	public String getId() {
		return id;
	}


	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.server.connection.IConnection#getStorageEngine()
	 */
	public SessionEngine getSessionEngine() {
		return sessionEngine;
	}


	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.server.connection.IConnection#close()
	 */
	public void close() throws Exception {
		commit();
	}
	
	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.server.connection.IConnection#commit()
	 */
	public void commit() throws Exception {
		sessionEngine.commit();
		releaseOidLocks();
		releaseClassLocks();
	}
	
	protected void releaseOidLocks() throws InterruptedException{
		OID oid = null;
		// release update mutexes
		Iterator iterator = oidsLockedForUpdate.keySet().iterator();
		while(iterator.hasNext()){
			oid = (OID) iterator.next();
			connectionManager.unlockOidForSession(oid, null);
			if(sessionEngine.getSession().getConfig().isDebugEnabled(LOG_ID)){
				DLogger.debug("Release object lock for "+oid);
			}
		}
		oidsLockedForUpdate.clear();
	}
	
	protected void releaseClassLocks() throws InterruptedException{
		Iterator<String> iterator = lockedClasses.keySet().iterator();
		while(iterator.hasNext()){
			String fullClassName = iterator.next();
			connectionManager.unlockClass(fullClassName, null);
			if(sessionEngine.getSession().getConfig().isDebugEnabled(LOG_ID)){
				DLogger.debug("Release class lock for "+fullClassName);
			}
		}
		lockedClasses.clear();
		
	}

	
	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.server.connection.IConnection#unlockObjectWithOid(org.neodatis.odb.OID)
	 */
	public synchronized void unlockObjectWithOid(OID oid) throws Exception {
		connectionManager.unlockOidForSession(oid, null);
		oidsLockedForUpdate.remove(oid);		
	}
	public synchronized void unlockClass(String fullClassName) throws Exception {
		connectionManager.unlockClass(fullClassName, null);
		lockedClasses.remove(fullClassName);		
	}

	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.server.connection.IConnection#rollback()
	 */
	public void rollback() throws Exception {
		sessionEngine.rollback();
		releaseOidLocks();
		releaseClassLocks();
	}


	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.server.connection.IConnection#lockObjectWithOid(org.neodatis.odb.OID)
	 */
	public synchronized boolean lockObjectWithOid(OID oid) throws InterruptedException {
		connectionManager.lockOidForSession(oid, null,100);
		oidsLockedForUpdate.put(oid, new Long(OdbTime.getCurrentTimeInMs()));
		return true;
	}
	
	public boolean lockClass(String fullClassName) throws InterruptedException {
		connectionManager.lockClassForSession(fullClassName, null,1000);
		lockedClasses.put(fullClassName, new Long(OdbTime.getCurrentTimeInMs()));
		return true;
	}

	
	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.server.connection.IConnection#setCurrentAction(int)
	 */
	public void setCurrentAction(int action){
		currentActionStart = OdbTime.getCurrentTimeInMs();
		currentAction = ConnectionAction.getActionLabel(action);
		actions[action]=actions[action]+1;
	}
	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.server.connection.IConnection#endCurrentAction()
	 */
	public void endCurrentAction(){
		lastAction = currentAction;
		lastActionDuration = (OdbTime.getCurrentTimeInMs()-currentActionStart);
		currentAction = ConnectionAction.ACTION_NO_ACTION_LABEL;
	}

	/* (non-Javadoc)
	 * @see org.neodatis.odb.core.server.connection.IConnection#getDescription()
	 */
	public String getDescription() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("cid="+id).append("\n\t\t+ Current action : ");
		buffer.append(currentAction).append("(").append(OdbTime.getCurrentTimeInMs()-currentActionStart).append("ms) | last action : ");
		buffer.append(lastAction).append("(").append(lastActionDuration).append("ms)");
		buffer.append("\n\t\t+ Actions : ");
		for(int i=0;i<actions.length;i++){
			buffer.append(ConnectionAction.getActionLabel(i)).append("=").append(actions[i]).append(" | ");
		}
		buffer.append("\n\t\t+ Blocked Oid (").append(oidsLockedForUpdate.size()).append(") : ");
		buffer.append(oidsLockedForUpdate.keySet());
		return buffer.toString();
	}
	
	public boolean equals(Object obj) {
		if(obj==null || !(obj instanceof ServerConnectionImpl)){
			return false;
		}
		ServerConnectionImpl c = (ServerConnectionImpl) obj;
		
		return id.equals(c.id);
	}


}
