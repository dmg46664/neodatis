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

import org.neodatis.odb.NeoDatisConfig;
import org.neodatis.odb.NeoDatisRuntimeException;
import org.neodatis.odb.OID;
import org.neodatis.odb.core.NeoDatisError;
import org.neodatis.odb.core.layers.layer4.BaseIdentification;
import org.neodatis.odb.core.server.ServerSession;
import org.neodatis.odb.core.session.Session;
import org.neodatis.odb.core.trigger.TriggerManagerImpl;
import org.neodatis.odb.core.trigger.Triggers;
import org.neodatis.odb.core.trigger.TriggersImpl;
import org.neodatis.tool.wrappers.map.OdbHashMap;

/**
 * To manage all sessions of a database
 * 
 * @author olivier
 * 
 */
public class SessionManager {
	public static final String LOG_ID = "SessionManager";
	protected BaseIdentification baseIdentification;
	private Map<String, Session> sessions;
	
	private Triggers triggers;

	public SessionManager(BaseIdentification baseIdentification) {
		this.baseIdentification = baseIdentification;
		sessions = new OdbHashMap<String, Session>();
		triggers = new TriggersImpl();
	}

	public ServerSession newSession(String ip, long dateTime, int sequence, boolean transactional, NeoDatisConfig neoDatisConfig) {
		String sessionId = SessionIdGenerator.newId(ip, dateTime, sequence);
		BaseIdentification bi = baseIdentification.copy();
		bi.setConfig(neoDatisConfig);
		bi.getConfig().setTransactional(transactional);
		ServerSession session = new ServerSession(bi, sessionId);
		session.getEngine().setTriggerManager(new TriggerManagerImpl(session, triggers));
		sessions.put(sessionId, session);
		return session;
	}

	public Session getSession(String sessionId) {
		Session session = sessions.get(sessionId);

		if (session == null) {
			throw new NeoDatisRuntimeException(NeoDatisError.CLIENT_SERVER_UNKNOWN_SESSION.addParameter(sessionId).addParameter(
					baseIdentification.getBaseId()));
		}

		return session;
	}

	public void removeSession(Session session) {
		sessions.remove(session.getId());
	}

	public int getNbSessions() {
		return sessions.size();
	}

	public String getSessionDescriptions() {
		Iterator iterator = sessions.values().iterator();
		IConnection connection = null;
		StringBuffer buffer = new StringBuffer();
		while (iterator.hasNext()) {
			connection = (IConnection) iterator.next();
			buffer.append("\n\t+ ").append(connection.getDescription()).append("\n");
		}
		return buffer.toString();
	}

	public synchronized void lockOidForSession(OID oid, Session session, long timeout) throws InterruptedException {
		session.lockOidForSession(oid,timeout);
	}

	public synchronized void lockClassForSession(String fullClassName, Session session, long timeout) throws InterruptedException {
		session.lockClassForSession(fullClassName, timeout);
	}

	public synchronized void unlockOidForSession(OID oid, Session session) throws InterruptedException {
		session.unlockOidForSession(oid);
	}

	public synchronized void unlockClass(String fullClassName, Session session) throws InterruptedException {
		session.unlockClass(fullClassName);
	}
	public Map<String,Session> getSessions(){
		return sessions;
	}

	public Triggers getTriggers() {
		return triggers;
	}

	/*
	 * public synchronized boolean oidIsLockedFor(OID oid, IConnection
	 * connection){ IConnection c = (IConnection) lockedOids.get(oid);
	 * if(c==null){ return false; } // If oid is locked for by the passed
	 * connection, no problem, it is not considered as being locked
	 * if(c!=null&&c.equals(connection)){ return false; } return true; }
	 */
	
	
}
